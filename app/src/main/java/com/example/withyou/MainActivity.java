package com.example.withyou;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.provider.MediaStore;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.w3c.dom.Text;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.Manifest.permission.CALL_PHONE;

public class MainActivity extends AppCompatActivity {


    private static final int REQUEST_CODE_LOCATION_PERMISSION = 1;

    LottieAnimationView hospital, police, police_call, contact, defence, knife, camera;
    TextView set_c, set_t;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String CALL = "call";
    public static final String TEXT = "text";
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String currentPhotoPath;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        set_c = findViewById(R.id.set_c);
        set_t = findViewById(R.id.set_t);
// fetching Details provided in settingsActivity by user
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        if (!sharedPreferences.getString(CALL, "").equals(""))
            set_c.setText(getString(R.string.call_no) + sharedPreferences.getString(CALL, ""));
        else
            set_c.setText(R.string.call_no_not_set);

        if (!sharedPreferences.getString(TEXT, "").equals(""))
            set_t.setText(getString(R.string.text_no) + sharedPreferences.getString(TEXT, ""));
        else
            set_t.setText(R.string.text_no_not_set);

//Shows battery percentage as a Toast as you enter this activity
        getBattery_percentage();
        hospital = findViewById(R.id.hospital);
        police = findViewById(R.id.police);
        police_call = findViewById(R.id.police_call);
        contact = findViewById(R.id.contact);
        defence = findViewById(R.id.defence);
        knife = findViewById(R.id.knife);
        camera = findViewById(R.id.camera);

        //Here we start a activity to go in our calender set event
        knife.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra("beginTime", cal.getTimeInMillis());
                intent.putExtra("allDay", true);
                intent.putExtra("rrule", "FREQ=DAILY");
                intent.putExtra("endTime", cal.getTimeInMillis() + 60 * 60 * 1000);
                intent.putExtra("title", "Take Weapon Stash");
                startActivity(intent);
            }
        });

        // this listener starts activity to a specific youtube video
        defence.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String url = "https://www.youtube.com/watch?v=T7aNSRoDCmg";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);

            }
        });
// Getting location of nearby hospitals through google navigation
        hospital.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uri = "google.navigation:" + "q=hospitals+near+me";
                startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
            }
        });
//Getting location of nearby Police stations through google navigation
        police.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uri = "google.navigation:" + "q=police+station+near+me";
                startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
            }
        });
        police_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
                String callNumber = sharedPreferences.getString(CALL, "");
                String textNumber = sharedPreferences.getString(TEXT, "");
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + callNumber));

// Checking and then Requesting calling permission from user if app doesn't have it
                if (ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(intent);
                } else {
                    requestPermissions(new String[]{CALL_PHONE}, 1);
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE) == PackageManager.PERMISSION_GRANTED)
                        startActivity(intent);
                    else
                        Toast.makeText(getBaseContext(), R.string.please_give_call_permission, Toast.LENGTH_SHORT).show();
                }

            }
        });
        contact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//Location permission check , requesting if app doesn't have it
                if (ContextCompat.checkSelfPermission(
                        getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                            REQUEST_CODE_LOCATION_PERMISSION
                    );
                    // if already have permission then getting location by calling a method
                } else {
                    getCurrentLocation();
                }


//                Intent smsIntent = new Intent(Intent.ACTION_SENDTO,Uri.parse("smsto:1234456;234567"));
//                smsIntent.putExtra("sms_body", etmessage.getText().toString());
//                startActivity(smsIntent);
            }
        });

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
// checking permissions to use camera and write storage , requesting if app doesn't have those
                if (ContextCompat.checkSelfPermission(
                        MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                        PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            101);
                }

                if (ContextCompat.checkSelfPermission(
                        MainActivity.this, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    //calling method to take a picture if have permissions
                    dispatchTakePictureIntent();
                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
                }
            }
        });

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        //        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File storageDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );


        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                galleryAddPic();
            }
        }
    }
// adding clicked pictures in gallery
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
// checking after requesting location permission if user has granted or denied it
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // if granted then only, getting location
                getCurrentLocation();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
            }
        }
    }
// gets user's location
    private void getCurrentLocation() {

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {

                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int latestLocationIndex = locationResult.getLocations().size() - 1;
                            double latitude =
                                    locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            double longitude =
                                    locationResult.getLocations().get(latestLocationIndex).getLongitude();

                            String locationStatus = latitude + "---" + longitude;
                            Toast.makeText(getApplicationContext(), "Your Lat---long : " + locationStatus, Toast.LENGTH_SHORT).show();

                            sendSMS(locationStatus);


                        }
                    }
                }, Looper.getMainLooper());



    }
// Sending sms
    public void sendSMS(String locationStatus){

        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.SEND_SMS,Manifest.permission.READ_SMS},PackageManager.PERMISSION_GRANTED);

        SharedPreferences sharedPreferences=getSharedPreferences(SHARED_PREFS,MODE_PRIVATE);
        String callNumber=sharedPreferences.getString(CALL,"");
        String textNumber=sharedPreferences.getString(TEXT,"");

        String message=locationStatus;
        SmsManager mySmsManager=SmsManager.getDefault();
// If user has provided a number then sending sms else giving toast to ask for number
        if(!textNumber.equals(""))
            mySmsManager.sendTextMessage(textNumber,null,message,null,null);
        else
            Toast.makeText(getBaseContext(), R.string.please_set,Toast.LENGTH_SHORT).show();


//        Intent smsIntent = new Intent(Intent.ACTION_SENDTO,Uri.parse("smsto:1234456;234567;9453998530"));
//        smsIntent.putExtra("sms_body", locationStatus);
//        startActivity(smsIntent);

    }
// getting user battery_percentage details using The BatteryManager class
    void getBattery_percentage()
    {
        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = getApplicationContext().registerReceiver(null, ifilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float)scale;
        float p = batteryPct * 100;

        Toast.makeText(getApplicationContext(),String.valueOf(p),Toast.LENGTH_SHORT).show();
    }
}
