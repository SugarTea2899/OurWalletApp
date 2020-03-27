package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    public ArrayList<MemberModel> memberModels;
    private Context context;
    private boolean checkedProgramatically = false;

    public MemberAdapter(ArrayList<MemberModel> memberModels, Context context) {
        this.memberModels = memberModels;
        this.context = context;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_item, parent, false);
        return new MemberViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        MemberModel item = memberModels.get(position);

        holder.tvMemberName.setText(item.name);
        if (item.isSuperAdmin){
            holder.tvMemberName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_key, 0, 0, 0);
        }else{
            holder.tvMemberName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_member, 0, 0, 0);
        }

        if (item.isBanned){
            checkedProgramatically = true;
            holder.sBtnBlock.setChecked(true);
        }else{
            checkedProgramatically = true;
            holder.sBtnBlock.setChecked(false);
        }

        if (item.isAdmin){
            checkedProgramatically = true;
            holder.sBtndEdit.setChecked(true);
        }else{
            checkedProgramatically = true;
            holder.sBtndEdit.setChecked(false);
        }
    }



    @Override
    public int getItemCount() {
        return memberModels.size();
    }

    public class MemberViewHolder extends RecyclerView.ViewHolder{
        TextView tvMemberName;
        SwitchCompat sBtnBlock, sBtndEdit;
        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = (TextView) itemView.findViewById(R.id.tv_memberName);
            sBtnBlock = (SwitchCompat) itemView.findViewById(R.id.sbtn_block);
            sBtndEdit = (SwitchCompat) itemView.findViewById(R.id.sbtn_editable);

            sBtnBlock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (checkedProgramatically){
                        checkedProgramatically = false;
                        return;
                    }
                    if (isChecked){
                        if (!container.isSuperAdmin){
                            sBtnBlock.setChecked(false);
                            Toast.makeText(context, "Bạn không có quyền thực hiên chức năng này.", Toast.LENGTH_SHORT).show();
                        }else{
                            banUser(ManagementFragment.id, ManagementFragment.name, tvMemberName.getText().toString(), sBtnBlock);
                        }
                    }else{
                        if (!container.isSuperAdmin){
                            sBtnBlock.setChecked(true);
                            Toast.makeText(context, "Bạn không có quyền thực hiên chức năng này.", Toast.LENGTH_SHORT).show();
                        }else{
                            unBanUser(ManagementFragment.id, tvMemberName.getText().toString(), sBtnBlock);
                        }
                    }
                }
            });

            sBtndEdit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (checkedProgramatically){
                        checkedProgramatically = false;
                        return;
                    }

                    if (isChecked){
                        if(!container.isSuperAdmin){
                            sBtndEdit.setChecked(false);
                            Toast.makeText(context, "Bạn không có quyền thực hiên chức năng này.", Toast.LENGTH_SHORT).show();
                        }else{
                            banEdit(ManagementFragment.id, tvMemberName.getText().toString(), true, sBtndEdit);
                        }
                    }else{
                        if(!container.isSuperAdmin){
                            sBtndEdit.setChecked(true);
                            Toast.makeText(context, "Bạn không có quyền thực hiên chức năng này.", Toast.LENGTH_SHORT).show();
                        }else{
                            if (ManagementFragment.name.equals(tvMemberName.getText().toString())){
                                Toast.makeText(context, "Không thể chặn bản thân.", Toast.LENGTH_SHORT).show();
                                sBtndEdit.setChecked(true);
                                return;
                            }
                            banEdit(ManagementFragment.id, tvMemberName.getText().toString(), false, sBtndEdit);
                        }
                    }
                }
            });
        }
    }

    private void banEdit(int walletId, String name, final boolean isAdmin, final SwitchCompat sbtn){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("walletId", walletId);
            jsonObject.put("name", name);
            jsonObject.put("isAdmin", isAdmin);

            RequestBody body = RequestBody.create(jsonObject.toString(), MainActivity.JSON);
            final Request request = new Request.Builder()
                    .url(MainActivity.ADDRESS + "ban-edit")
                    .post(body)
                    .build();

            final OkHttpClient okHttpClient = new OkHttpClient();
            final ProgressDialog dialog = new ProgressDialog(context);
            dialog.setTitle("Đang thực hiện");
            dialog.setMessage("Xin chờ...");

            @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    try {
                        publishProgress();
                        Response response = okHttpClient.newCall(request).execute();
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
                    if (s != null){
                        try {
                            JSONObject jsonObject1 = new JSONObject(s);
                            if (jsonObject1.getBoolean("res")){
                                Toast.makeText(context, "Thành công.", Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(context, "Thất bại.", Toast.LENGTH_SHORT).show();
                                if (isAdmin){
                                    sbtn.setChecked(false);
                                }else sbtn.setChecked(true);
                            }
                            dialog.dismiss();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else {
                        dialog.dismiss();
                        Toast.makeText(context, "Thất bại.", Toast.LENGTH_SHORT).show();
                        if (isAdmin){
                            sbtn.setChecked(false);
                        }else sbtn.setChecked(true);
                    }
                }
            };
            asyncTask.execute();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    private void banUser(int walletId, String sName, String bName, final SwitchCompat sbtn){
        if (sName.equals(bName)){
            Toast.makeText(context, "Không thể chặn bản thân.", Toast.LENGTH_SHORT).show();
            sbtn.setChecked(false);
            return;
        }
        final JSONObject jsonObject =  new JSONObject();
        try {
            jsonObject.put("walletId", walletId);
            jsonObject.put("sName", sName);
            jsonObject.put("bName", bName);

            RequestBody body = RequestBody.create(jsonObject.toString(), MainActivity.JSON);
            final Request request = new Request.Builder()
                    .url(MainActivity.ADDRESS + "ban-user")
                    .post(body)
                    .build();
            final OkHttpClient okHttpClient = new OkHttpClient();

            final ProgressDialog dialog = new ProgressDialog(context);
            dialog.setTitle("Đang thực hiện");
            dialog.setMessage("Xin chờ...");

            @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    try {
                        publishProgress();
                        Response response = okHttpClient.newCall(request).execute();
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
                        JSONObject jsonObject1 = new JSONObject(s);
                        if (jsonObject1.getBoolean("res")){
                            Toast.makeText(context, "Chặn thành công.", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(context, "Chặn thất bại.", Toast.LENGTH_SHORT).show();
                            sbtn.setChecked(false);
                        }
                        dialog.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };
            asyncTask.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    private void unBanUser(int walletId, String name, final SwitchCompat sbtn){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("walletId", walletId);
            jsonObject.put("name", name);
            RequestBody body = RequestBody.create(jsonObject.toString(), MainActivity.JSON);
            final Request request = new Request.Builder()
                    .url(MainActivity.ADDRESS + "unban-user")
                    .post(body)
                    .build();
            final OkHttpClient okHttpClient = new OkHttpClient();

            final ProgressDialog dialog = new ProgressDialog(context);
            dialog.setTitle("Đang thực hiện");
            dialog.setMessage("Xin chờ...");

            @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    try {
                        publishProgress();
                        Response response = okHttpClient.newCall(request).execute();
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
                        JSONObject jsonObject1 = new JSONObject(s);
                        if (jsonObject1.getBoolean("res")){
                            Toast.makeText(context, "Gỡ chặn thành công", Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(context, "Gỡ chặn thất bại", Toast.LENGTH_SHORT).show();
                            sbtn.setChecked(false);
                        }
                        dialog.dismiss();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            };

            asyncTask.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
