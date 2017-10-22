package com.example.mohamed.networkinfo;

import android.Manifest;
import android.content.Context;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {


    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private double currentLatitude;
    private double currentLongitude;

    double downloadBandwidth = 0.0000;
    double uploadBandwidth = 0.0000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        // Setting service provider name
        String operatorName = telephonyManager.getNetworkOperatorName();
        ((TextView) findViewById(R.id.providerName)).setText(operatorName);


        //Setting cell id
//        GsmCellLocation loc = (GsmCellLocation) telephonyManager.getCellLocation();
//        GsmCellLocation loc = (GsmCellLocation) telephonyManager.getAllCellInfo();
//        String cellLoc = "#"
//                +loc.getCid()+"#"
//                +loc.getLac()+"#"
//                +loc.getPsc()+"#";
//
//        ((TextView) findViewById(R.id.cellID)).setText(String.valueOf(cellLoc));


//        Log.v("GsmCellLocation", "GSM Location:" + cellLoc);


        //Setting cell id
        String networkType = getNetworkClass(this);
        ((TextView) findViewById(R.id.networkType)).setText(networkType);

        // setting cell location

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                // The next two lines tell the new client that “this” current class will handle connection stuff
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                //fourth line adds the LocationServices API endpoint from GooglePlayServices
                .addApi(LocationServices.API)
                .build();

        // Create the LocationRequest object
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds


//        List<NeighboringCellInfo> NeighboringList = telephonyManager.getNeighboringCellInfo();

//        Log.v("test", "Neighboring List: " + NeighboringList);
//        String stringNeighboring = "Neighboring List- Lac : Cid : RSSIn";
//        for (int i = 0; i < NeighboringList.size(); i++) {
//
//            String dBm;
//            int rssi = NeighboringList.get(i).getRssi();
//            if (rssi == NeighboringCellInfo.UNKNOWN_RSSI) {
//                dBm = "Unknown RSSI";
//            } else {
//                dBm = String.valueOf(-113 + 2 * rssi) + " dBm";
//            }
//            Log.v("test", "Neighboring dBM: " + NeighboringList);
//        }

        startSignalLevelListener();
        Button button = (Button) findViewById(R.id.sendButton);
        button.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Data sent :", Toast.LENGTH_SHORT).show();

                new SendPostRequest().execute();

            }
        });

//    new MyDownloadTask().execute();

//

        List<CellInfo> cellInfoList =  telephonyManager.getAllCellInfo();
        Log.v("CELL-INFO", "cell info on start: " + cellInfoList);


        for (int i=0; i<cellInfoList.size(); i++) {


            CellInfoGsm cellinfogsm = (CellInfoGsm) cellInfoList.get(i);

            System.out.println("------------ cell id:"+cellinfogsm.getCellIdentity()+", sig strength: "+cellinfogsm.getCellSignalStrength());


            ((TextView) findViewById(R.id.cellID)).setText(String.valueOf(cellinfogsm.getCellIdentity().getCid()));
            ((TextView) findViewById(R.id.signalStrength)).setText(cellinfogsm.getCellSignalStrength().getDbm());
        }
    }


    public class SendPostRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {

            try{

                URL url = new URL("http://162.250.190.189:3000/network/sendinfo?servicemenid=1&serviceprovider=airter");

                JSONObject postDataParams = new JSONObject();
                postDataParams.put("ServiceProvider", "Android");
                postDataParams.put("Type", "4G - LTE");
                postDataParams.put("CellInfo", "966554");
                postDataParams.put("Lat", "10.21");
                postDataParams.put("Long", "10.22");
                postDataParams.put("UpSpeed", "0101");
                postDataParams.put("DownSpeed", "0101");


                Log.e("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                Log.e("params 2","code executed");
                Log.e("POSTDATA: ",getPostDataString(postDataParams));

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));


                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();
                Log.v("ResponseCode: ","code---"+conn.getResponseCode());

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new
                            InputStreamReader(
                            conn.getInputStream()));

                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return new String("false : "+responseCode);
                }
            }
            catch(Exception e){

                Log.e("Exception: ",e.getMessage());

                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopListening();
        //Disconnect from API onPause()
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            mGoogleApiClient.disconnect();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        startSignalLevelListener();
        mGoogleApiClient.connect();


    }

    @Override
    protected void onDestroy() {
        stopListening();
        super.onDestroy();
    }


    private void startSignalLevelListener() {

        try {
            TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            int events = PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                    | PhoneStateListener.LISTEN_DATA_ACTIVITY
                    | PhoneStateListener.LISTEN_CELL_LOCATION
                    | PhoneStateListener.LISTEN_CALL_STATE
                    | PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
                    | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                    | PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR
                    | PhoneStateListener.LISTEN_CELL_INFO
                    | PhoneStateListener.LISTEN_SERVICE_STATE;

            tm.listen(phoneStateListener, events);
        } catch (Exception e) {
            Log.e("ERROR :", "" + e);
        }

    }

    private void stopListening() {
        TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        tm.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
    }


    public String getNetworkClass(Context context) {
        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = mTelephonyManager.getNetworkType();

        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return "2G - GPRS";
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "2G - EDGE";
            case TelephonyManager.NETWORK_TYPE_CDMA:
                return "2G - CDMA";
            case TelephonyManager.NETWORK_TYPE_1xRTT:
                return "2G - 1xRTT";
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G - IDEN";
            case TelephonyManager.NETWORK_TYPE_UMTS:
                return "3G - UMTS";
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
                return "3G - EVDO_0";
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                return "3G - EVDO_A";
            case TelephonyManager.NETWORK_TYPE_HSDPA:
                return "3G - HSDPA";
            case TelephonyManager.NETWORK_TYPE_HSUPA:
                return "3G - HSUPA";
            case TelephonyManager.NETWORK_TYPE_HSPA:
                return "3G - HSPA";
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
                return "3G - EVDO_B";
            case TelephonyManager.NETWORK_TYPE_EHRPD:
                return "3G - EHRPD";
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G - HSPAP";
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "4G - LTE";
            case TelephonyManager.NETWORK_TYPE_TD_SCDMA:
                return "4G - SCDMA";
            default:
                return "Unknown type - '"+String.valueOf(networkType)+"'";
        }


    }

    private final PhoneStateListener phoneStateListener = new PhoneStateListener() {

//        @Override
//        public void onSignalStrengthChanged(int asu) {
//
////            ((TextView) findViewById(R.id.signalStrength)).setText(String.valueOf((2*asu)-113));
//            super.onSignalStrengthChanged(asu);
//
//        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength s) {


            String r = "#"
                    + s.getCdmaDbm() + "#"
                    + s.getCdmaEcio() + "#"
                    + s.getEvdoDbm() + "#"
                    + s.getEvdoEcio() + "#"
                    + s.getEvdoSnr() + "#"
                    + s.getGsmBitErrorRate() +"#"
                    + s.getGsmSignalStrength() +"#"
                    + s.hashCode();

//            ((TextView) findViewById(R.id.signalStrength)).setText(r);

            Log.v("Signal strength", "SS: "+r);

            super.onSignalStrengthsChanged(s);

        }

        @Override
        public void onCellInfoChanged(List<CellInfo> cellInfoList) {



            // This callback method will be called automatically by Android OS
            // Every time a cell info changed (if you are registered)
            // Here, you will receive a cellInfoList....
            // Same list that you will receive in RSSI_values() method that you created
            // You can maybe move your whole code to here....

//            Toast.makeText(MainActivity.this, "CellInfo :" + cellInfoList, Toast.LENGTH_SHORT).show();
            Log.v("CELL-INFO ", "CI: "+cellInfoList);

            super.onCellInfoChanged(cellInfoList);

        }

        @Override
        public void onCellLocationChanged(CellLocation location) {
            super.onCellLocationChanged(location);

            Log.v("CellLocation", "location :" + location);

        }

    };


    @Override
    public void onConnected(Bundle bundle) {


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if (location == null) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        } else {
            //If everything went fine lets get latitude and longitude
            currentLatitude = location.getLatitude();
            currentLongitude = location.getLongitude();

            Toast.makeText(this, currentLatitude + " Connected " + currentLongitude + "", Toast.LENGTH_LONG).show();

            ((TextView) findViewById(R.id.lat)).setText(String.valueOf(currentLatitude));
            ((TextView) findViewById(R.id.lon)).setText(String.valueOf(currentLongitude));

        }
    }


    @Override
    public void onConnectionSuspended(int i) {

        Toast.makeText(this, currentLatitude + " Suspended " + currentLongitude + "", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
            /*
             * Google Play services can resolve some errors it detects.
             * If the error has a resolution, try sending an Intent to
             * start a Google Play services activity that can resolve
             * error.
             */

        Log.v("CurrentEvent", "onConnectionFailed");
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
                    /*
                     * Thrown if Google Play services canceled the original
                     * PendingIntent
                     */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
                /*
                 * If no resolution is available, display a dialog to the
                 * user with the error.
                 */
            Log.e("Error", "Location services connection failed with code " + connectionResult.getErrorCode());
        }
    }

    /**
     * If locationChanges change lat and long
     *
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();

        ((TextView) findViewById(R.id.lat)).setText(String.valueOf(currentLatitude));
        ((TextView) findViewById(R.id.lon)).setText(String.valueOf(currentLongitude));


        Toast.makeText(this, currentLatitude + " Update " + currentLongitude + "", Toast.LENGTH_LONG).show();

    }


    class MyDownloadTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {

            InputStream stream = null;
            int bytesIn = 0;

            String downloadFileUrl = "http://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_20mb.mp4";
            long startCon = System.currentTimeMillis();
            URL url = null;
            try {
                url = new URL(downloadFileUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection con = null;
            try {
                con = (HttpURLConnection) url.openConnection();
            } catch (IOException e) {
                e.printStackTrace();
            }
            con.setUseCaches(false);
            long connectionLatency = System.currentTimeMillis() - startCon;
            try {
                stream = con.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }


            long start = System.currentTimeMillis();
            int currentByte = 0;
            long updateStart = System.currentTimeMillis();
            long updateDelta = 0;
            int bytesInThreshold = 0;

            try {
                while ((currentByte = stream.read()) != -1) {
                    bytesIn++;
                    bytesInThreshold++;

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            long downloadTime = (System.currentTimeMillis() - start);
            long bytespersecond = (bytesIn / downloadTime) * 1000;
            long kilobytess =bytespersecond/1000;
            downloadBandwidth = kilobytess;

            System.out.println("\ndownload speed " + bytespersecond);
            //System.out.println(kilobits);
            //System.out.println(megabits);


            /// UPLOAD TAKS

            try {

                // open a URL connection to the Servlet
//            String	upLoadServerUri = "http://www.androidexample.com/media/UploadToServer.php";
//            String uploadFileName = "/home/sivisha/eclipseproject/workspace1/urlconnection/SampleVideo.mp4";
//            String uploadFileName = "drawable://" + R.drawable.testimage;
//              String uploadFileName = "drawable://" + R.drawable.testimage;

                System.out.print("\nUpload task started");


                Uri fileUri = Uri.parse(String.valueOf(getAssets().open("testvideo.mp4")));
                String uploadFileName = fileUri.getPath();
                System.out.print("\nfile path : " + uploadFileName);


                HttpURLConnection conn = null;
                DataOutputStream dos = null;
                String lineEnd = "\r\n";
                String twoHyphens = "--";
                String boundary = "*****";
                int serverResponseCode = 0;
                int bytesRead;
                List<Byte> listOfBytes = new ArrayList<>();
//            FileInputStream fileInputStream = new FileInputStream(String.valueOf(("testvideo.mp4")));
                InputStream is = getAssets().open("testvideo.mp4");


                while ((bytesRead = is.read()) != -1) {
                    listOfBytes.add((byte) bytesRead);
                }
                is.close();
                System.out.println("\n\nlist of bytes..... " + listOfBytes.size());
                String upLoadServerUri = "http://www.androidexample.com/media/UploadToServer.php";

//                URL url = new URL("https://www.google.com/maps");
                URL urls = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) urls.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", uploadFileName);

                dos = new DataOutputStream(conn.getOutputStream());

                System.out.println("\n\nDOS.....");

                long starts = System.currentTimeMillis();
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + uploadFileName + "\"" + lineEnd);

                dos.writeBytes(lineEnd);
                for (Byte write : listOfBytes)
                    dos.write(write);

                System.out.println("\n\nMultipart.....");

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                System.out.println("\n\nRESP....");

                if (serverResponseCode == 200) {
                    System.out.println("upload successfull");

                }

                //close the streams //

                dos.flush();
                dos.close();
                long upload = System.currentTimeMillis() - starts;

                long bytesperseconds = (listOfBytes.size() / upload) * 1000;
                long kilobytespersecond = bytesperseconds / 1000;
                uploadBandwidth = kilobytespersecond;
                System.out.println("\nupolod speed...." + bytesperseconds);
//            } catch (MalformedURLException ex) {
//
//                ex.printStackTrace();
//
////         Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                e.printStackTrace();
//         Log.e("Upload file to server Exception", "Exception : "
//                                          + e.getMessage(), e);
            }
            return null;

        }
        @Override
        protected void onPreExecute() {
            //display progress dialog.
            System.out.println("\n\n onPreExecute");

        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
            System.out.println("\n\n onProgressUpdate");

//            ((TextView) findViewById(R.id.downSpeed)).setText(String.valueOf(downloadBandwidth));

        }

        @Override
        protected void onPostExecute(Void v) {
            //display progress dialog.
            System.out.println("\n\n onPostExecute");
            ((TextView) findViewById(R.id.downSpeed)).setText(String.valueOf(downloadBandwidth));
            ((TextView) findViewById(R.id.upSpeed)).setText(String.valueOf(uploadBandwidth));

        }
    }

}





