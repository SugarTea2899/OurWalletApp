package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.TimeZone;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class StatisticFragment extends Fragment implements StatisticAdapter.OnItemListener, HistoryAdapter.OnHistoryListener {

    EditText edtOriDate, edtDesDate;
    Button btnStaticts, btnRemoveAll;
    ArrayList<HistoryItemModel> itemModels;
    ArrayList<StatisticModel> statisticModels;
    HistoryAdapter historyAdapter;
    StatisticAdapter statisticAdapter;
    RecyclerView recyclerView;
    TextView tvRevenues, tvExpenditures;
    long revenues = 0, expenditures = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.statistic_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getWidget(view);


        setEvent();
    }

    private void getWidget(View view){
        edtDesDate = (EditText) view.findViewById(R.id.edt_desDate);
        edtOriDate = (EditText) view.findViewById(R.id.edt_oriDate);
        btnStaticts = (Button) view.findViewById(R.id.btn_statistic);
        itemModels = new ArrayList<>();
        recyclerView = (RecyclerView) view.findViewById(R.id.rv_statistic);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        tvRevenues = (TextView) view.findViewById(R.id.tv_revenuesStatistic);
        tvExpenditures = (TextView) view.findViewById(R.id.tv_expendituresStatistic);
        statisticModels = new ArrayList<>();
        btnRemoveAll = (Button) view.findViewById(R.id.btn_removeAll);
    }

    private void setEvent(){
        edtOriDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker(edtOriDate, getContext());
            }
        });

        edtDesDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker(edtDesDate, getContext());
            }
        });

        btnStaticts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (itemModels.size() == 0){
                    Toast.makeText(getContext(), "Danh sách rỗng.", Toast.LENGTH_SHORT).show();
                    return;
                }
                final Request request = new Request.Builder()
                        .url(MainActivity.ADDRESS + "pay-member-list?walletId=" + ManagementFragment.id)
                        .build();
                final OkHttpClient okHttpClient = new OkHttpClient();
                final ProgressDialog dialog = new ProgressDialog(getContext());
                dialog.setTitle("Đang thống kê");
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
                        if (s != null){
                            statisticModels.clear();
                            ArrayList<String> temp = new Gson().fromJson(s, new TypeToken<ArrayList<String>>(){}.getType());
                            for (int i = 0; i < temp.size(); i++){
                                StatisticModel item = new StatisticModel(temp.get(i), 0, 0);
                                statisticModels.add(item);
                            }
                            for (int i = 0; i < statisticModels.size(); i++){
                                for (int j = 0; j < itemModels.size(); j++){
                                    if (statisticModels.get(i).payMemberName.equals(itemModels.get(j).payMemberName)){
                                        if (itemModels.get(j).isRevenue){
                                            statisticModels.get(i).revenues += itemModels.get(j).value;
                                        }else{
                                            statisticModels.get(i).expenditures += itemModels.get(j).value;
                                        }
                                    }
                                }
                            }
                            statisticAdapter = new StatisticAdapter(getContext(), statisticModels, StatisticFragment.this);
                            recyclerView.setAdapter(statisticAdapter);
                        }else{
                            Toast.makeText(getContext(), "Lọc thất bại", Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                };

                asyncTask.execute();

            }
        });

        btnRemoveAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (container.isSuperAdmin){
                    if (itemModels.size() == 0){
                        Toast.makeText(getContext(), "Danh sách rỗng.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (edtOriDate.getText().toString().length() == 0 || edtDesDate.getText().toString().length() ==0){
                        Toast.makeText(getContext(), "Vui lòng không để ngày trống.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Xoá tất cả");
                    builder.setMessage("Dữ liệu đang hiển thị sẽ không thể phục hồi, tiếp tục xoá? ");
                    builder.setCancelable(true);
                    builder.setNegativeButton("Xoá", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            postRemoveAll(ManagementFragment.id, edtOriDate.getText().toString()
                                ,edtDesDate.getText().toString(), MainActivity.fcmToken);
                        }
                    });
                    builder.setPositiveButton("Không", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int k) {
                            dialogInterface.cancel();
                        }
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }else{
                    Toast.makeText(getContext(), "Bạn không có quyền thực hiện chức năng này.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void postRemoveAll(int walletId, String oriDate, String desDate, String fcmToken){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("walletId", walletId);
            jsonObject.put("oriDate", oriDate);
            jsonObject.put("desDate", desDate);
            jsonObject.put("fcmToken", MainActivity.fcmToken);

            RequestBody body = RequestBody.create(jsonObject.toString(), MainActivity.JSON);
            final Request request = new Request.Builder()
                    .url(MainActivity.ADDRESS + "remove-historys")
                    .post(body)
                    .build();

            final OkHttpClient okHttpClient = new OkHttpClient();
            final ProgressDialog dialog = new ProgressDialog(getContext());
            dialog.setTitle("Đang xoá");
            dialog.setMessage("Xin chờ...");
            @SuppressLint("StaticFieldLeak") AsyncTask<Void,Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
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
                    if (s != null){
                        Toast.makeText(getContext(), "Thành công.", Toast.LENGTH_SHORT).show();
                        itemModels.clear();
                        statisticModels.clear();
                        historyAdapter.notifyDataSetChanged();
                        statisticAdapter.notifyDataSetChanged();
                    }else{
                        Toast.makeText(getContext(), "Thật bại.", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }
            };
            asyncTask.execute();


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void datePicker(final EditText edt, Context context){
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int date = calendar.get(Calendar.DATE);
        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(year, month, dayOfMonth);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
                edt.setText(simpleDateFormat.format(calendar.getTime()));

                if(!edtOriDate.getText().toString().equals("") && !edtDesDate.getText().toString().equals("")){
                    loadFiledHistory(ManagementFragment.id, edtOriDate.getText().toString(), edtDesDate.getText().toString());
                }else{
                    if (edtOriDate.getText().toString().equals("") && !edtDesDate.getText().toString().equals("")){
                        Toast.makeText(getContext(), "Ngày rỗng", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, year,month,date);

        datePickerDialog.show();
    }
    private void loadFiledHistory(int walletId, String oriDate, String desDate){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("walletId", walletId);
            jsonObject.put("oriDate", oriDate);
            jsonObject.put("desDate", desDate);

            RequestBody body = RequestBody.create(jsonObject.toString(), MainActivity.JSON);
            final Request request = new Request.Builder()
                    .url(MainActivity.ADDRESS + "statistic")
                    .post(body)
                    .build();

            final OkHttpClient okHttpClient = new OkHttpClient();
            final ProgressDialog dialog = new ProgressDialog(getContext());
            dialog.setTitle("Đang lọc dữ liệu");
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
                    if (s != null){
                        itemModels = new Gson().fromJson(s, new TypeToken<ArrayList<HistoryItemModel>>(){}.getType());
                        try {
                            formatDate();
                            historyAdapter = new HistoryAdapter(itemModels, getContext(), StatisticFragment.this);
                            recyclerView.setAdapter(historyAdapter);
                            getRevenuesExpenditures();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }else{
                        Toast.makeText(getContext(), "Lọc thất bại", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }
            };
            asyncTask.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void showPersonalStaDialog(String payMemberName){
        Dialog dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.recycle_view_dialog);
        RecyclerView rv = (RecyclerView) dialog.findViewById(R.id.rv_personalSta);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        ArrayList<HistoryItemModel> historyItemModels = new ArrayList<>();

        for (int i = 0; i < itemModels.size(); i++){
            if (itemModels.get(i).payMemberName.equals(payMemberName)){
                historyItemModels.add(itemModels.get(i));
            }
        }

        HistoryAdapter historyAdapter = new HistoryAdapter(historyItemModels, getContext(), StatisticFragment.this);
        rv.setAdapter(historyAdapter);
        dialog.show();
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
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
    private void getRevenuesExpenditures(){
        revenues = 0;
        expenditures = 0;
        for (int i = 0; i < itemModels.size(); i++){
            if (itemModels.get(i).isRevenue){
                revenues += itemModels.get(i).value;
            }else expenditures += itemModels.get(i).value;
        }

        tvRevenues.setText("+" + HistoryAdapter.formatMoney(revenues));
        tvExpenditures.setText("-" + HistoryAdapter.formatMoney(expenditures));
    }

    @Override
    public void onItemClick(int position) {
        showPersonalStaDialog(statisticModels.get(position).payMemberName);
    }

    @Override
    public void onClick(int position) {

    }
}
