package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class CreateWalletActivity extends AppCompatActivity {

    EditText edtName, edtPasword, edtRePassword;
    Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_wallet);
        getWidget();

        setEvent();
    }


    private void setEvent(){
        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMatch()){
                    JSONObject jsonObject = new JSONObject();
                    try {
                        jsonObject.put("memberName", edtName.getText().toString());
                        jsonObject.put("password", edtPasword.getText().toString());
                        jsonObject.put("fcmToken", MainActivity.fcmToken);
                        jsonObject.put("deviceId", MainActivity.DEVICE_ID);

                        final OkHttpClient httpClient = new OkHttpClient();
                        RequestBody body = RequestBody.create(jsonObject.toString(), MainActivity.JSON);
                        final Request request = new Request.Builder()
                                .url(MainActivity.ADDRESS + "create-wallet")
                                .post(body)
                                .build();

                        final ProgressDialog dialog = new ProgressDialog(CreateWalletActivity.this);
                        dialog.setTitle("Tạo ví");
                        dialog.setMessage("Xin chờ...");

                        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
                            @Override
                            protected String doInBackground(Void... voids) {
                                try{
                                    publishProgress();
                                    Response response = httpClient.newCall(request).execute();
                                    if (!response.isSuccessful())
                                        return null;
                                    return response.body().string();
                                }catch (Exception e){
                                    return null;
                                }
                            }

                            @Override
                            protected void onProgressUpdate(Void... values) {
                                dialog.show();
                            }

                            @Override
                            protected void onPostExecute(String s) {
                                dialog.dismiss();
                                if (s != null){
                                    Toast.makeText(getApplicationContext(), "Tạo ví thành công", Toast.LENGTH_SHORT).show();
                                    try {
                                        JSONObject jsonObject1 = new JSONObject(s);
                                        int walletId = jsonObject1.getInt("id");
                                        saveToSharePre(walletId, edtPasword.getText().toString(), edtName.getText().toString());
                                        Intent intent = new Intent(CreateWalletActivity.this, container.class);
                                        startActivity(intent);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }else{
                                    Toast.makeText(getApplicationContext(), "Tạo ví thất bại", Toast.LENGTH_SHORT).show();
                                }
                            }
                        };

                        asyncTask.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "Mật khẩu không trùng nhau hoặc ít hơn 6 ký tự.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveToSharePre(int id,String pass, String name){
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("id", id);
        editor.putString("pass", pass);
        editor.putString("name", name);
        editor.putBoolean("isConnected", true);
        editor.putString("fcmToken", MainActivity.fcmToken);
        editor.apply();
    }
    private boolean isMatch(){
        if (edtRePassword.getText().toString().length() >= 6)
            return edtPasword.getText().toString().equals(edtRePassword.getText().toString());
        else return false;
    }
    private void getWidget(){
        edtName = (EditText) findViewById(R.id.edt_enterName);
        edtPasword = (EditText) findViewById(R.id.edt_enterPass);
        edtRePassword = (EditText) findViewById(R.id.edt_repeatPass);
        btnCreate = (Button) findViewById(R.id.btn_create);
    }
}
