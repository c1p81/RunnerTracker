package com.luca_innocenti.smstracker;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Interpolator;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;
import com.wefika.horizontalpicker.HorizontalPicker;

import android.widget.ToggleButton;

import com.google.firebase.crash.FirebaseCrash;

public class MainActivity extends AppCompatActivity implements HorizontalPicker.OnItemSelected, HorizontalPicker.OnItemClicked{

    private static boolean invia = false;
    private TextView stato;
    //private NumberPicker np;
    private static int squadra = 1;
    private static final int REQUEST_SMS = 0;

    //private String[] coda_sns = new String[100]; // questa e' la coda degli SMS non inviati per errore rete
    //private static  int indice_coda_sms = -1;


    public static final int NOTIFICATION_ID = 1001;
    private NotificationManager notificationManager;
    private PendingIntent pendingIntent;
    private NotificationManager mNotificationManager;
    private Intent gpsIntent;
    private HorizontalPicker picker;

    private static final String PREFS_NAME = "Prefs";
    private int privacy;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //FirebaseCrash.report(new Exception("My first Android non-fatal error"));

        picker = (HorizontalPicker) findViewById(R.id.picker);
        picker.setOnItemClickedListener(this);
        picker.setOnItemSelectedListener(this);

        stato = (TextView) findViewById(R.id.stato);
        stato.setText("v0.4");

        SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
        squadra = settings.getInt("squadra",1);
        privacy = settings.getInt("privacy",0);

        picker.setSelectedItem(squadra-1);

        if (privacy == 0)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Questa applicazione e' impiegata per la sicurezza sul tracciato di corsa.\n\nIl titolare per il trattamento dei dati personali e' Banda dei Malandrini  ASD.\n\nI dati telefonici e di posizione saranno utilizzati solo durante le manifestazioni e saranno comunque cancellati alle ore 00 di ogni giorno.\n\n L'uso dell'applicazione prevede l'invio di un SMS ogni 15 minuto al costo previsto dall'operatore telefonico")
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        }

        //np = (NumberPicker) findViewById(R.id.numberPicker2);
        //np.setMinValue(1);
        //np.setMaxValue(100);
        //np.setValue(squadra);
        //np.setWrapSelectorWheel(false);



        /*np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                    squadra = i1;
            }
        });*/


        ToggleButton smsbutton = (ToggleButton) findViewById(R.id.toggleButton);
        // controlla se inviare o no gli sms con la variabile boolean invia
        smsbutton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    invia = true;
                    gpsIntent = new Intent(MainActivity.this, MyService.class);
                    gpsIntent.putExtra("squadra", Integer.toString(squadra));
                    startService(gpsIntent);
                    //np.setEnabled(false);
                    picker.setEnabled(false);
                    sendNotification();

                    SharedPreferences settings = getSharedPreferences(PREFS_NAME,0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putInt("squadra", squadra);
                    editor.putInt("privacy",1);
                    editor.commit();

                }
                else
                {
                    invia = false;
                    stopService(gpsIntent);
                    //np.setEnabled(true);
                    picker.setEnabled(true);
                    notificationManager.cancel(NOTIFICATION_ID);
                }

            }
        }
        );

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);
        }
        else{
            //locationStart();
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
    }



    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.support.v7.app.AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

/*    public void elabora_coda()
    {
        for (int i=0; i<indice_coda_sms; i++)
        {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(numero_telefono, null, coda_sns[i], null, null);
            Log.d("debug",coda_sns[i]);
        }
        indice_coda_sms = -1;

    }*/


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1000) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("debug","checkSelfPermission true");
                //locationStart();
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
        pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);


        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));
        builder.setContentTitle("Runner Tracker");
        builder.setContentText("");
        builder.setSubText("Tap per aprire Runner Tracker");

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }



    @Override
    public void onDestroy()
    {

        if (notificationManager == null)
            notificationManager = (NotificationManager)  getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        if (invia)
        {
            stopService(new Intent(MainActivity.this,MyService.class));
        }

        super.onDestroy();


    }

    @Override
    public void onItemClicked(int index) {
        //Toast.makeText(this, "Item selected " + Integer.toString(index), Toast.LENGTH_SHORT).show();
        //squadra = index;
    }

    @Override
    public void onItemSelected(int index) {
        squadra = index +1;
    }
}

