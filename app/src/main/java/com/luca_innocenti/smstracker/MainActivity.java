package com.luca_innocenti.smstracker;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationBuilderWithBuilderAccessor;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.widget.ToggleButton;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements LocationListener {

    private static final int REQUEST_SMS = 0;
    private LocationManager locationManager;
    private static long tempo = 0;
    private static long prec_tempo = 0;
    private static boolean invia = false;
    private TextView stato;
    private NumberPicker np;
    private static int squadra = 1;

    private String[] coda_sns = new String[100]; // questa e' la coda degli SMS non inviati per errore rete
    private static  int indice_coda_sms = -1;
    String numero_telefono = "3713602980";


    public static final int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        stato = (TextView) findViewById(R.id.stato);
        stato.setText("");

        np = (NumberPicker) findViewById(R.id.numberPicker2);
        np.setMinValue(1);
        np.setMaxValue(100);
        np.setValue(1);
        np.setWrapSelectorWheel(false);

        np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    squadra = i1;
            }
        });


        ToggleButton smsbutton = (ToggleButton) findViewById(R.id.toggleButton);
        // controlla se inviare o no gli sms con la variabile boolean invia
        smsbutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    invia = true;
                    np.setEnabled(false);
                    sendNotification();
                }
                else
                {
                    invia = false;
                    np.setEnabled(true);
                    notificationManager.cancel(NOTIFICATION_ID);
                }

            }
        }
        );

        prec_tempo = System.currentTimeMillis();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        }
        else{
            locationStart();
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            int hasSMSPermission = checkSelfPermission(Manifest.permission.SEND_SMS);
            if (hasSMSPermission != PackageManager.PERMISSION_GRANTED) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.SEND_SMS)) {
                    showMessageOKCancel("Richiesta di permesso di usare SMS",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[] {Manifest.permission.SEND_SMS},
                                                REQUEST_SMS);
                                    }
                                }
                            });
                    return;
                }
                requestPermissions(new String[] {Manifest.permission.SEND_SMS},
                        REQUEST_SMS);
                return;
            }

        }
        //sendSMS(numero_telefono,"Start;" + Integer.toString(squadra));
    }

    private void locationStart(){
        Log.d("debug","locationStart()");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!gpsEnabled) {
            Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);
            Log.d("debug", "gpsEnable, startActivity");
        } else {
            Log.d("debug", "gpsEnabled");
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);

            Log.d("debug", "checkSelfPermission false");
            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
    }


    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    public void elabora_coda()
    {
        for (int i=0; i<indice_coda_sms; i++)
        {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(numero_telefono, null, coda_sns[i], null, null);
            Log.d("debug",coda_sns[i]);
        }
        indice_coda_sms = -1;

    }

    public void sendSMS(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent",
                    Toast.LENGTH_LONG).show();
            // se e' riuscito ad inviare l'SMS vuol dire che c'e' campo
            // si inviano quindi anche gli altri SMS eventualmente presenti in coda
            elabora_coda();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),
                    Toast.LENGTH_LONG).show();
                    //se c'e' un errore sull'invio SMS, lo mette in coda per successivo invio
                    indice_coda_sms = indice_coda_sms + 1;
                    coda_sns[indice_coda_sms] = msg;
            ex.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("debug","checkSelfPermission true");
                locationStart();
                return;

            } else {
                Toast toast = Toast.makeText(this, "これ以上なにもできません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }


    public void sendNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_stat_name);

        // resume on notification tap
        Intent intent = new Intent(getApplicationContext(),MainActivity.class );
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);


        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
        builder.setContentTitle("Runner Tracker");
        builder.setContentText("");
        builder.setSubText("Tap per aprire Runner Tracker");

        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        switch (status) {
            case LocationProvider.AVAILABLE:
                Log.d("debug", "LocationProvider.AVAILABLE");
                break;
            case LocationProvider.OUT_OF_SERVICE:
                Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                break;
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("debug",location.getLatitude()+","+location.getLongitude()+","+location.getAltitude()+","+location.getAccuracy());
        stato.setText(location.getLatitude()+","+location.getLongitude()+","+Float.toString((tempo-prec_tempo)/1000));
        tempo = System.currentTimeMillis();
        if ((tempo - prec_tempo)/1000 > 900)
        {
            Log.d("debug","MARK");
            prec_tempo = tempo;

            Date date = Calendar.getInstance().getTime();
            DateFormat formatter = new SimpleDateFormat("kk:mm:ss");
            String orario = formatter.format(date);
            stato.setText(orario + "," + location.getLatitude() + "," + location.getLongitude() + "," + location.getAltitude());

            if (invia) {
                sendSMS(numero_telefono, Integer.toString(squadra) + "," + orario + "," + location.getLatitude() + "," + location.getLongitude() + "," + location.getAltitude() + "," + location.getAccuracy());
            }

        }
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        //locationManager.removeUpdates(LocationManager.GPS_PROVIDER);
        notificationManager.cancel(NOTIFICATION_ID);
        //sendSMS(numero_telefono,"Stop;" + Integer.toString(squadra));

    }
}

