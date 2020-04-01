package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {
    public ArrayList<HistoryItemModel> itemModelsResult;
    private Context context;
    private OnHistoryListener mOnHistoryListner;

    public HistoryAdapter(ArrayList<HistoryItemModel> itemModelsResult, Context context, OnHistoryListener onHistoryListener) {
        this.itemModelsResult = itemModelsResult;
        this.context = context;
        this.mOnHistoryListner = onHistoryListener;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
        return new HistoryViewHolder(itemView, mOnHistoryListner);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItemModel item = itemModelsResult.get(position);

        holder.tvContent.setText(item.content);
        holder.tvDate.setText(item.date);
        holder.tvUsername.setText(item.name);
        holder.tvPayMemberName.setText(item.payMemberName);
        String temp = formatMoney(item.value);

        if (item.isRevenue)
            temp = "+" + temp;
        else
            temp = "-" + temp;
        holder.tvValue.setText(temp);
    }

    @Override
    public int getItemCount() {
        return itemModelsResult.size();
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView tvContent;
        public TextView tvDate;
        public TextView tvUsername;
        public TextView tvValue;
        public TextView tvPayMemberName;
        public OnHistoryListener onHistoryListener;

        public HistoryViewHolder(@NonNull View itemView, OnHistoryListener onHistoryListener) {
            super(itemView);

            tvContent = (TextView) itemView.findViewById(R.id.tv_content);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            tvUsername = (TextView) itemView.findViewById(R.id.tv_userName);
            tvValue = (TextView) itemView.findViewById(R.id.tv_value);
            tvPayMemberName = (TextView) itemView.findViewById(R.id.tv_payMemberName);
            this.onHistoryListener = onHistoryListener;
            itemView.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            onHistoryListener.onClick(getAdapterPosition());
        }
    }

    public static String formatMoney(long money){
        int count = 0;
        int condition = 0;
        if (money < 0)
            condition = 1;
        StringBuffer result = new StringBuffer(money + "");
        for (int i = result.length() - 1; i > condition; i--){
            count++;
            if (count == 3){
                count = 0;
                result.insert(i, '.');
            }
        }
        result.insert(result.length(), "đ");
        return result.toString();
    }

    public interface OnHistoryListener {
        void onClick(int position);
    }
}
