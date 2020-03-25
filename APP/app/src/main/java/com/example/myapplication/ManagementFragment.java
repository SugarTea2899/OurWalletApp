package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Context.MODE_PRIVATE;

public class ManagementFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.management_fragment, container, false);
    }


    RecyclerView recyclerView;
    ImageButton imgbMenu;
    ImageButton imgbAdd;
    ImageButton imgbQuit;
    TextView tvWalletId, tvRemain, tvRevenues, tvExpenditures;
    boolean isHidden = true;
    ArrayList<HistoryItemModel> itemModels;
    HistoryAdapter adapter;
    SharedPreferences sharedPreferences;
    boolean doubleBackToExitPressedOnce = false;
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getWidget(view);
        sharedPreferences = this.getActivity().getSharedPreferences("data", MODE_PRIVATE);
        id = sharedPreferences.getInt("id", 0);
        name = sharedPreferences.getString("name","trống");
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver, new IntentFilter("LoadAgain"));
        tvWalletId.setText("ID: " + id + "");
        loadHistory(id);
        setEvent();
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
        final Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.add_new_history);
        final Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.options, android.R.layout.simple_spinner_dropdown_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        final EditText edtContent = (EditText) dialog.findViewById(R.id.edt_content);
        final EditText edtValue = (EditText) dialog.findViewById(R.id.edt_value);
        Button btnSave = (Button) dialog.findViewById(R.id.btn_save);

        edtValue.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String money = s.toString();
                if (!checkFormat(money)){
                    edtValue.setText(formatMoney(money));
                    edtValue.setSelection(edtValue.getText().length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HistoryItemModel item = new HistoryItemModel();

                item.content = edtContent.getText().toString();
                item.value =  reFormat(edtValue.getText().toString());

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
                tvRevenues.setText("+" + HistoryAdapter.formatMoney(revenues));
                tvExpenditures.setText("-" + HistoryAdapter.formatMoney(expenditures));
                item.name = name;

                itemModels.add(0, item);
                HistoryAdapter adapter1 = new HistoryAdapter(itemModels, getContext());
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
                    adapter = new HistoryAdapter(itemModels, getContext());
                    recyclerView.setAdapter(adapter);
                }
            }
        };
        asyncTask.execute();

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


        imgbQuit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject  jsonObject = new JSONObject();
                try{
                    jsonObject.put("id", id);
                    jsonObject.put("name", name);
                    jsonObject.put("fcmToken", sharedPreferences.getString("fcmToken", ""));

                    RequestBody body = RequestBody.create(jsonObject.toString(), MainActivity.JSON);
                    final OkHttpClient okHttpClient = new OkHttpClient();
                    final Request request = new Request.Builder()
                            .url(MainActivity.ADDRESS + "quit-wallet")
                            .post(body)
                            .build();

                    final ProgressDialog dialog = new ProgressDialog(getContext());
                    dialog.setTitle("Đang thoát");
                    dialog.setMessage("Xin chờ...");
                    @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
                        @Override
                        protected String doInBackground(Void... voids) {
                            try {
                                publishProgress();
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
                        protected void onProgressUpdate(Void... values) {
                            dialog.show();
                        }

                        @Override
                        protected void onPostExecute(String s) {
                            if (s == null)
                            {
                                Toast.makeText(getContext(), "Có lỗi xảy ra", Toast.LENGTH_SHORT).show();
                            }else{
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("isConnected", false);
                                editor.apply();
                                getActivity().finish();
                            }
                            dialog.dismiss();
                        }
                    };
                    asyncTask.execute();
                }catch (Exception e){
                    Log.d("Exception", e.getMessage());
                }
            }
        });
    }

    private boolean checkFormat(String money){
        if (money.length() == 0) return true;
        int count = 0;
        int size = 0;
        for (int i = 0; i < money.length(); i++){
            if (money.charAt(i) == '.'){
                count++;
            }else{
                size++;
            }
        }
        if (count == ((size - 1) / 3))
        {
            if (money.length() <= 3)
                return true;
            else{
                if (money.charAt(money.length() - 4) == '.')
                    return true;
            }
        }

        return false;
    }
    private String formatMoney(String money){
        money = money.replaceAll("\\.", "");
        StringBuffer res = new StringBuffer(money);
        int count = 0;
        for (int i = res.length() - 1; i > 0; i--){
            count++;
            if (count == 3){
                res.insert(i, '.');
                count = 0;
            }
        }
        return res.toString();
    }

    private int reFormat(String money){
        money = money.replaceAll("\\.", "");
        return Integer.parseInt(money);
    }

    private void getWidget(View view){
        itemModels = new ArrayList<HistoryItemModel>();
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_history);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        imgbAdd = (ImageButton) view.findViewById(R.id.imgb_add);
        imgbMenu = (ImageButton) view.findViewById(R.id.imgb_menu);
        imgbQuit = (ImageButton) view.findViewById(R.id.imgb_close);
        tvWalletId = (TextView) view.findViewById(R.id.idWallet);
        tvRemain = (TextView) view.findViewById(R.id.tv_remainMoney);
        tvExpenditures = (TextView) view.findViewById(R.id.tv_expenditures);
        tvRevenues = (TextView) view.findViewById(R.id.tv_revenues);
    }
}
