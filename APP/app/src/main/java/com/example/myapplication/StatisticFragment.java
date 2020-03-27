package com.example.myapplication;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class StatisticFragment extends Fragment {

    EditText edtOriDate, edtDesDate;
    Button btnFilter, btnStaticts;

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
        btnFilter = (Button) view.findViewById(R.id.btn_filter);
        btnStaticts = (Button) view.findViewById(R.id.btn_statistic);
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
            }
        }, year,month,date);

        datePickerDialog.show();
    }
}
