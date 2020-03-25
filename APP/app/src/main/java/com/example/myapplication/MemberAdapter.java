package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    public ArrayList<MemberModel> memberModels;
    private Context context;

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
            holder.sBtnBlock.setChecked(true);
        }else{
            holder.sBtnBlock.setChecked(false);
        }

        if (item.isAdmin){
            holder.sBtndEdit.setChecked(true);
        }else{
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
        }
    }
}
