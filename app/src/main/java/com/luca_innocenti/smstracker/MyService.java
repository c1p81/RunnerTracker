package com.luca_innocenti.smstracker;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.zip.CRC32;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MyService  extends Service{
    private static final String TAG = "GPSService";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 0f;
    private String squadra;
    private  long tempo = 0;
    private  long prec_tempo = 0;
    private String numero_telefono = "3713602980";
    private String messaggio_url;


    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;




        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            tempo = System.currentTimeMillis();
            if ((tempo - prec_tempo)/1000 > 300)
            {
                Log.d("debug","MARK");
                prec_tempo = tempo;

                Date date = Calendar.getInstance().getTime();
                DateFormat formatter = new SimpleDateFormat("kk:mm:ss");
                String orario = formatter.format(date);
                String messaggio = squadra + "," + orario + "," + location.getLatitude() + "," + location.getLongitude() + "," + location.getAltitude() + "," + location.getAccuracy();
                messaggio_url = "http://c1p81.altervista.org/RunnerTracker/csv.php?squadra="+squadra+"&orario="+orario+"&lat="+location.getLatitude()+"&lon="+location.getLongitude()+"&alt="+location.getAltitude()+"&acc="+location.getAccuracy();

                CRC32 crc = new CRC32();
                crc.update(messaggio.getBytes());
                String enc = String.format("%08X", crc.getValue());
                sendSMS(numero_telefono, messaggio+","+enc);

            }


            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        if (intent.hasExtra("squadra")) {
            squadra = intent.getExtras().getString("squadra");
        }
        //sendSMS(numero_telefono,"Start");
        Log.d("Servizio",squadra);
        
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        prec_tempo = System.currentTimeMillis();


        initializeLocationManager();

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }



    public void sendSMS(final String  phoneNo, final String msg) {

        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, messaggio_url,
        new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //Log.d("Debug","Risposta: "+ response.substring(0,4));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Debug","Errore");
                //se non c'e' rete allora manda SMS
                try {
                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, msg, null, null);
                    //Toast.makeText(getApplicationContext(), "Message Sent",Toast.LENGTH_LONG).show();
                    // se e' riuscito ad inviare l'SMS vuol dire che c'e' campo
                    // si inviano quindi anche gli altri SMS eventualmente presenti in coda
                    //elabora_coda();
                } catch (Exception ex) {
                    //Toast.makeText(getApplicationContext(),ex.getMessage().toString(),Toast.LENGTH_LONG).show();
                    //se c'e' un errore sull'invio SMS, lo mette in coda per successivo invio
                    //indice_coda_sms = indice_coda_sms + 1;
                    //coda_sns[indice_coda_sms] = msg;
                    ex.printStackTrace();
                }
            }
        });
        queue.add(stringRequest);
    }


}
