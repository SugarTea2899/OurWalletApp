package com.example.myapplication;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StatisticAdapter extends RecyclerView.Adapter<StatisticAdapter.StatisticViewHolder>  {

    private Context context;
    private ArrayList<StatisticModel> statisticModels;
    private OnItemListener mOnItemListener;

    public StatisticAdapter(Context context, ArrayList<StatisticModel> statisticModels, OnItemListener onItemListener) {
        this.context = context;
        this.statisticModels = statisticModels;
        this.mOnItemListener = onItemListener;
    }

    @NonNull
    @Override
    public StatisticViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.statistic_item, parent, false);
        return new StatisticViewHolder(itemView, mOnItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull StatisticViewHolder holder, int position) {
        StatisticModel item = statisticModels.get(position);
        holder.tvPayMemberName.setText(item.payMemberName);
        holder.tvRevenues.setText("+" + HistoryAdapter.formatMoney(item.revenues));
        holder.tvExpenditures.setText("-" + HistoryAdapter.formatMoney(item.expenditures));

        long remainMoney = item.revenues - item.expenditures;
        holder.tvRemain.setText(HistoryAdapter.formatMoney(remainMoney));
    }

    @Override
    public int getItemCount() {
        return statisticModels.size();
    }


    public class StatisticViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView tvPayMemberName, tvRevenues, tvExpenditures, tvRemain;
        OnItemListener onItemListener;
        public StatisticViewHolder(@NonNull View itemView, OnItemListener onItemListener) {
            super(itemView);
            tvPayMemberName = (TextView) itemView.findViewById(R.id.tv_payMemberNameSta);
            tvRevenues = (TextView) itemView.findViewById(R.id.tv_revenuesSta);
            tvExpenditures = (TextView) itemView.findViewById(R.id.tv_expendituresSta);
            tvRemain = (TextView) itemView.findViewById(R.id.tv_remainMoneySta);
            this.onItemListener = onItemListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onItemListener.onItemClick(getAdapterPosition());
        }
    }

    public interface OnItemListener{
        void onItemClick(int position);
    }
}
