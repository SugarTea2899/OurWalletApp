package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ManagementActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageButton imgbMenu;
    ImageButton imgbAdd;
    ImageButton imgbQuit;
    TextView tvWalletId, tvRemain, tvRevenues, tvExpenditures;
    boolean isHidden = true;
    ArrayList <HistoryItemModel> itemModels;
    HistoryAdapter adapter;
    SharedPreferences sharedPreferences;
    int id;
    long remain = 0, revenues = 0, expenditures = 0;
    String name;
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadHistory(id);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);
        getWidget();
        sharedPreferences =  getSharedPreferences("data", MODE_PRIVATE);
        id = sharedPreferences.getInt("id", 0);
        name = sharedPreferences.getString("name","trống");
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, new IntentFilter("LoadAgain"));
        tvWalletId.setText("ID: " + id + "");
        loadHistory(id);
        setEvent();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(broadcastReceiver);
    }

    private void hideFunctionButton(){
        imgbQuit.setVisibility(View.GONE);
        imgbAdd.setVisibility(View.GONE);
    }

    private void showFunctionButtons(){
        imgbQuit.setVisibility(View.VISIBLE);
        imgbAdd.setVisibility(View.VISIBLE);
    }

    private void showAddNewDialog(){
        final Dialog dialog = new Dialog(ManagementActivity.this);
        dialog.setContentView(R.layout.add_new_history);
        final Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.options, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        final EditText edtContent = (EditText) dialog.findViewById(R.id.edt_content);
        final EditText edtValue = (EditText) dialog.findViewById(R.id.edt_value);
        Button btnSave = (Button) dialog.findViewById(R.id.btn_save);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HistoryItemModel item = new HistoryItemModel();

                item.content = edtContent.getText().toString();
                item.value =  Integer.parseInt(edtValue.getText().toString());

                Date now = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                String nowString = df.format(now);

                item.date = nowString;

                int spinnerValue = spinner.getSelectedItemPosition();
                if (spinnerValue == 0)
                    item.isRevenue = true;
                else
                    item.isRevenue = false;

                if (item.isRevenue)
                    revenues += item.value;
                else
                    expenditures += item.value;

                remain = revenues - expenditures;
                tvRemain.setText(HistoryAdapter.formatMoney(remain));
                tvRevenues.setText(HistoryAdapter.formatMoney(revenues));
                tvExpenditures.setText(HistoryAdapter.formatMoney(expenditures));
                item.name = name;

                itemModels.add(0, item);
                HistoryAdapter adapter1 = new HistoryAdapter(itemModels, ManagementActivity.this);
                recyclerView.setAdapter(adapter1);
                postHistoryToSever(id, item.value, item.isRevenue, item.name, item.content);
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    private void postHistoryToSever(int walletId, long value, boolean isRevenue, String name, String describe){
        JSONObject jsonObject = new JSONObject();
        try{
            jsonObject.put("walletId", walletId);
            jsonObject.put("isRevenue", isRevenue);
            jsonObject.put("value", value);
            jsonObject.put("name", name);
            jsonObject.put("describe", describe);
            jsonObject.put("fcmToken", MainActivity.fcmToken);
            final OkHttpClient httpClient = new OkHttpClient();
            RequestBody body = RequestBody.create(jsonObject.toString(), MainActivity.JSON);
            final Request request = new Request.Builder()
                    .url(MainActivity.ADDRESS + "create-history")
                    .post(body)
                    .build();

            @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    try {
                        Response response = httpClient.newCall(request).execute();
                        return response.body().string();
                    } catch (IOException e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            };

            asyncTask.execute();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void loadHistory(int id){
        final OkHttpClient okHttpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(MainActivity.ADDRESS + "load-history?id=" + id)
                .build();

        @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    Response response = okHttpClient.newCall(request).execute();
                    if (response.isSuccessful())
                        return response.body().string();
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String s) {
                if (s != null){
                    itemModels = new Gson().fromJson(s, new TypeToken<ArrayList<HistoryItemModel>>(){}.getType());
                    try {
                        formatDate();
                        calculateMoney();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    adapter = new HistoryAdapter(itemModels, ManagementActivity.this);
                    recyclerView.setAdapter(adapter);
                }
            }
        };
        try {
            asyncTask.execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void calculateMoney(){
        revenues = 0;
        expenditures = 0;
        for (int i = 0; i < itemModels.size(); i++){
            if (itemModels.get(i).isRevenue)
                revenues += itemModels.get(i).value;
            else
                expenditures += itemModels.get(i).value;
        }

        remain = revenues - expenditures;
        tvRemain.setText(HistoryAdapter.formatMoney(remain));
        tvRevenues.setText("+" + HistoryAdapter.formatMoney(revenues));
        tvExpenditures.setText("-" + HistoryAdapter.formatMoney(expenditures));
    }
    private void formatDate() throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat printedFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        for (int i = 0; i < itemModels.size(); i++){
            Date date = df.parse(itemModels.get(i).date);
            itemModels.get(i).date = printedFormat.format(date);
        }
    }
    private void setEvent(){
        imgbMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isHidden){
                    isHidden = !isHidden;
                    showFunctionButtons();
                }else{
                    isHidden = !isHidden;
                    hideFunctionButton();
                }
            }
        });

        imgbAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideFunctionButton();
                isHidden = true;
                showAddNewDialog();
            }
        });
    }

    private void getWidget(){
        itemModels = new ArrayList<HistoryItemModel>();
        recyclerView = (RecyclerView) findViewById(R.id.rv_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        imgbAdd = (ImageButton) findViewById(R.id.imgb_add);
        imgbMenu = (ImageButton) findViewById(R.id.imgb_menu);
        imgbQuit = (ImageButton) findViewById(R.id.imgb_close);
        tvWalletId = (TextView) findViewById(R.id.idWallet);
        tvRemain = (TextView) findViewById(R.id.tv_remainMoney);
        tvExpenditures = (TextView) findViewById(R.id.tv_expenditures);
        tvRevenues = (TextView) findViewById(R.id.tv_revenues);
    }
}