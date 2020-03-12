package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.RemoteInput;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

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
    public static final String ADDRESS = "https://wallet-api-quangthien.herokuapp.com/api/";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWidget();


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
                                    saveToSharePre(Integer.parseInt(edtWalletID.getText().toString()), edtUserName.getText().toString());
                                    Intent intent = new Intent(MainActivity.this, ManagementActivity.class);
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
    private void saveToSharePre(int id, String name){
        SharedPreferences sharedPreferences = getSharedPreferences("data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("id", id);
        editor.putString("name", name);
        editor.putBoolean("isConnected", true);
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
