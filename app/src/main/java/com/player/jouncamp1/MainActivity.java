package com.player.jouncamp1;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import com.player.jouncamp1.Model.ModelsManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    final int PERMISSION_READ_PHONE_STATE = 1;

    String IMEI;
    static HashMap<String, String> dataMap = new HashMap<>();


    private MediaPlayer mMediaPlayer = null;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        checkPermission();
        getDataFromIntent();

        AsyncTask asyncTask = new CheckDevice().execute();


        //        setContentView(R.layout.activity_main);
//
//      C
//        mSurfaceView = findViewById(R.id.surfaceView);
//        mSurfaceHolder = mSurfaceView.getHolder();
//        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//
//        mMediaPlayer = new MediaPlayer();
    }

    public void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            //권한없음
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
                //요청 거절 이력 있음
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.request_permission_title)).setMessage(R.string.request_permission_message).setPositiveButton(getString(R.string.allow), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_READ_PHONE_STATE);
                    }
                });
                builder.create().show();
            } else {
                //첫 요청
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_READ_PHONE_STATE);
            }
        } else {
            //권한 있음 => IMEI값 받아옴
            TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                IMEI = tm.getImei();
            } else {
                IMEI = tm.getDeviceId();
            }
            Log.i("Log.i", "IMEI: " + IMEI);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //권한 요청 결과
        switch (requestCode) {
            case PERMISSION_READ_PHONE_STATE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    //권한 거부
                    boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE);
                    if (!showRationale) {
                        //다시 묻지 말라며 거부 => 수동으로 권한 요청
                        Toast.makeText(getApplicationContext(), getString(R.string.request_permission_manually), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, PERMISSION_READ_PHONE_STATE);
                    } else {//그냥 거부 => 다시 권한 요청
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle(getString(R.string.request_permission_title)).setMessage(R.string.request_permission_message).setPositiveButton(getString(R.string.allow), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_READ_PHONE_STATE);
                            }
                        });
                        builder.create().show();
                    }

                } else {
                    //권한 획득 => IMEI값 받아옴
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                        TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            IMEI = tm.getImei();
                        } else {
                            IMEI = tm.getDeviceId();
                        }
                        Log.i("Log.i", "IMEI: " + IMEI);
                    } else {
                        Log.e("Log.e", "ERROR");
                        finish();
                    }
                }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PERMISSION_READ_PHONE_STATE) {
            //수동 권한 요청
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                //권한 획득 => IMEI값 받아옴
                TelephonyManager tm = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    IMEI = tm.getImei();
                } else {
                    IMEI = tm.getDeviceId();
                }
                Log.i("Log.i", "IMEI: " + IMEI);
            } else {
                //권한 없음 => 수동으로 권한 다시 요청 또는 앱 종료
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.request_permission_title)).setMessage(R.string.request_permission_message).setPositiveButton(getString(R.string.allow), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), getString(R.string.request_permission_manually), Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, PERMISSION_READ_PHONE_STATE);
                    }
                }).setNegativeButton(getString(R.string.deny), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.create().show();
            }
        }
    }

    public void getDataFromIntent() {
        Uri intentData = getIntent().getData();

        if (intentData != null) {
            Iterator<String> iterator = intentData.getQueryParameterNames().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                String value = intentData.getQueryParameter(key);
                if (key.equals("json_data")) {
                    //json_data에 IMEI값 삽입
                    JSONObject temp;
                    try {
                        temp = new JSONObject(value);
                        if (temp != null) {
                            temp.put("IMEI", IMEI);
                        }
                        value = temp.toString();
                    } catch (Exception e) {

                    }

                }
                dataMap.put(key, value);

                Log.i("Log.i", "KEY: " + key + " - VALUE: " + value);
            }
        }else{
            Log.i("Log.i", "Intent Data Empty!");
    }
    }

    class CheckDevice extends AsyncTask<String, Void, String> {

        URL url = null;

        public CheckDevice() {
            super();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... arg0) {

            HttpURLConnection conn = null;
            JSONObject postDataParams = null;

            try {

                url = new URL(M("authentication_url"));

                postDataParams = new JSONObject(GlobalVariables.getParam("json_data"));
                Log.d("data", postDataParams.toString());

                conn = (HttpURLConnection) url.openConnection();

                conn.setReadTimeout(READ_TIMEOUT /* milliseconds */);
                conn.setConnectTimeout(CONNECT_TIMEOUT /* milliseconds */);
                conn.setRequestMethod("POST");
                //conn.setRequestProperty("Cookie",cookie);
                conn.setDoInput(true);
                conn.setDoOutput(true);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (conn == null) return null;

            OutputStream os;
            BufferedWriter writer;
            try {
                os = conn.getOutputStream();
                writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));
                Log.d("***data***", getPostDataString(postDataParams).toString());
                writer.flush();
                writer.close();
                os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            StringBuffer sb = null;
            try {
                int responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                                    conn.getInputStream()));
                    sb = new StringBuffer("");
                    String line;
                    Log.i("#####", "" + responseCode);
                    while ((line = in.readLine()) != null) {
                        sb.append(line);
                    }
                    //Log.i("####*********####",sb.toString());
                    in.close();
                } else {
                    Log.i("response", "" + responseCode);
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getApplicationContext());

                    alertDialogBuilder.setTitle(getString(R.string.unauthenticated_uses));
                    alertDialogBuilder.setMessage(getString(R.string.request_the_developer)).setCancelable(false);
                    alertDialogBuilder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {

                            finish();
                        }
                    });
                    alertDialogBuilder.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (sb == null) {
                return null;
            }
            //Log.d("@@@@@@@@", sb.toString());
            return sb.toString();
        }


        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }


    }


    @Override
    protected void onStop() {
//        if (mMediaPlayer.isPlaying()) { todo
//            mMediaPlayer.pause();
//        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
//        if (mMediaPlayer != null) { todo
//            mMediaPlayer.release();
//            mMediaPlayer = null;
//        }
        super.onDestroy();
    }

}
