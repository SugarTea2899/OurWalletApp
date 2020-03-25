package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    EditText edtUserName;
    EditText edtWalletID;
    EditText edtWalletPass;
    Button btnConnect;
    TextView tvCreateNew;
    public static String fcmToken;
    public static final String ADDRESS = "https://wallet-api-quangthien.herokuapp.com/api/";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static String DEVICE_ID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWidget();

        DEVICE_ID = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (task.isSuccessful()){
                    fcmToken = task.getResult().getToken();
                    Log.d("XXX", fcmToken);
                }else{
                    Log.d("XXX", "false");
                }
            }
        });
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        boolean isConnected = sharedPreferences.getBoolean("isConnected", false);
        if (isConnected){
            autoConnect(sharedPreferences.getInt("id", -1), sharedPreferences.getString("pass", ""),
                    sharedPreferences.getString("name", ""), sharedPreferences.getString("fcmToken", ""));
        }
        setEvent();
    }

    private void setEvent(){
        btnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject jsonObject = new JSONObject();
                try{
                    jsonObject.put("id", Integer.parseInt(edtWalletID.getText().toString()));
                    jsonObject.put("password", edtWalletPass.getText().toString());
                    jsonObject.put("memberName", edtUserName.getText().toString());
                    jsonObject.put("fcmToken", fcmToken);
                    jsonObject.put("deviceId", DEVICE_ID);
                    jsonObject.put("isAuto", false);
                    RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
                    final Request request = new Request.Builder()
                            .url(ADDRESS + "connect-wallet")
                            .post(body)
                            .build();

                    final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
                    dialog.setTitle("Đang kết nối");
                    dialog.setMessage("Xin chờ...");

                    final OkHttpClient httpClient = new OkHttpClient();

                    @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
                        @Override
                        protected String doInBackground(Void... voids) {
                            publishProgress();
                            try {
                                Response response = httpClient.newCall(request).execute();
                                return response.body().string();
                            } catch (IOException e) {
                                e.printStackTrace();
                                return null;
                            }
                        }

                        @Override
                        protected void onProgressUpdate(Void... values) {

                            dialog.show();
                        }

                        @Override
                        protected void onPostExecute(String s) {
                            try {
                                JSONObject object =  new JSONObject(s);
                                if (object.getBoolean("res") == false){
                                    Toast.makeText(getApplicationContext(), object.getString("message"), Toast.LENGTH_SHORT).show();
                                }else{
                                    saveToSharePre(Integer.parseInt(edtWalletID.getText().toString()),edtWalletPass.getText().toString(), edtUserName.getText().toString());
                                    Intent intent = new Intent(MainActivity.this, container.class);
                                    startActivity(intent);
                                }
                                dialog.dismiss();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    };

                    asyncTask.execute();
                }catch (Exception e){
                    try {
                        throw e;
                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        tvCreateNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateWalletActivity.class);
                startActivity(intent);
            }
        });
    }

    private void autoConnect(int id, String pass, String name, String fcm){
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("id", id);
            jsonObject.put("password", pass);
            jsonObject.put("memberName", name);
            jsonObject.put("fcmToken", fcm);
            jsonObject.put("isAuto", true);
            jsonObject.put("deviceId", DEVICE_ID);
            RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
            final Request request = new Request.Builder()
                    .url(ADDRESS + "connect-wallet")
                    .post(body)
                    .build();

            final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
            dialog.setTitle("Đang kết nối");
            dialog.setMessage("Xin chờ...");

            final OkHttpClient httpClient = new OkHttpClient();

            @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    publishProgress();
                    try {
                        Response response = httpClient.newCall(request).execute();
                        return response.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }

                @Override
                protected void onProgressUpdate(Void... values) {
                    SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
                    int walletId = sharedPreferences.getInt("id", 0);
                    edtWalletID.setText(walletId + "");
                    edtWalletPass.setText(sharedPreferences.getString("pass", ""));
                    edtUserName.setText(sharedPreferences.getString("name", ""));
                    dialog.show();
                }

                @Override
                protected void onPostExecute(String s) {
                    try {

                        JSONObject object =  new JSONObject(s);
                        if (object.getBoolean("res") == false){
                            Toast.makeText(getApplicationContext(), object.getString("message"), Toast.LENGTH_SHORT).show();
                        }else{
                            Intent intent = new Intent(MainActivity.this, container.class);
                            startActivity(intent);
                        }
                        dialog.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };

            asyncTask.execute();
        }catch (Exception e){
            try {
                throw e;
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }
    }
    private void saveToSharePre(int id,String pass, String name){
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("id", id);
        editor.putString("pass", pass);
        editor.putString("name", name);
        editor.putBoolean("isConnected", true);
        editor.putString("fcmToken", fcmToken);
        editor.apply();
    }
    private void getWidget(){
        btnConnect = (Button) findViewById(R.id.btn_connect);
        tvCreateNew = (TextView) findViewById(R.id.tv_createNew);
        edtUserName = (EditText) findViewById(R.id.edt_name);
        edtWalletID = (EditText) findViewById(R.id.edt_walletId);
        edtWalletPass = (EditText) findViewById(R.id.edt_walletPass);
    }
}
