package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MemberListFragment extends Fragment {

    RecyclerView rvMemberList;
    MemberAdapter memberAdapter;
    ArrayList<MemberModel> memberModels;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.member_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getWidget(view);
        loadListMember(ManagementFragment.id);
    }
    private void loadListMember(int id){
        JSONObject jsonObject =  new JSONObject();
        try {
            jsonObject.put("walletId", id);
            RequestBody body = RequestBody.create(jsonObject.toString(), MainActivity.JSON);
            final Request request = new Request.Builder()
                    .url(MainActivity.ADDRESS + "list-member")
                    .post(body)
                    .build();
            final OkHttpClient httpClient = new OkHttpClient();

            final ProgressDialog dialog = new ProgressDialog(getContext());
            dialog.setTitle("Đang load danh sách");
            dialog.setMessage("Xin chờ...");

            @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> asyncTask = new AsyncTask<Void, Void, String>() {
                @Override
                protected String doInBackground(Void... voids) {
                    try {
                        publishProgress();
                        Response response = httpClient.newCall(request).execute();
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
                    if (s != null) {
                        memberModels = new Gson().fromJson(s, new TypeToken<ArrayList<MemberModel>>(){}.getType());
                        memberAdapter = new MemberAdapter(memberModels, getContext());
                        rvMemberList.setAdapter(memberAdapter);
                    }else{
                        Toast.makeText(getContext(), "Load danh sách thành viên thất bại", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                }
            };

            asyncTask.execute();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void getWidget(View view){
        rvMemberList = (RecyclerView) view.findViewById(R.id.rv_memberList);
        rvMemberList.setLayoutManager(new LinearLayoutManager(getContext()));
        memberModels = new ArrayList<>();
    }
}
