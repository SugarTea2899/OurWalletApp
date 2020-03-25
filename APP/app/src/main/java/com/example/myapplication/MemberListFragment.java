package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

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
        MemberModel member = new MemberModel("1", true, false, false);
        memberModels.add(member);
        member = new MemberModel("2", true, false, false);
        memberModels.add(member);
        member = new MemberModel("3", false, true, true);
        memberModels.add(member);
        member = new MemberModel("4", true, false, false);
        memberModels.add(member);
        memberAdapter = new MemberAdapter(memberModels, getContext());
        rvMemberList.setAdapter(memberAdapter);
    }

    private void getWidget(View view){
        rvMemberList = (RecyclerView) view.findViewById(R.id.rv_memberList);
        rvMemberList.setLayoutManager(new LinearLayoutManager(getContext()));
        memberModels = new ArrayList<>();
    }
}
