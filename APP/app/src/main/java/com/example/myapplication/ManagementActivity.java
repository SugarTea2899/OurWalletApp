package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ManagementActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ImageButton imgbMenu;
    ImageButton imgbAdd;
    ImageButton imgbQuit;
    boolean isHidden = true;
    ArrayList <HistoryItemModel> itemModels;
    HistoryAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_management);
        getWidget();
        adapter = new HistoryAdapter(itemModels, this);
        recyclerView.setAdapter(adapter);
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
                item.value = edtValue.getText().toString();

                Date now = Calendar.getInstance().getTime();
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
                String nowString = df.format(now);

                item.date = nowString;

                int spinnerValue = spinner.getSelectedItemPosition();
                if (spinnerValue == 0)
                    item.isRevenues = true;
                else
                    item.isRevenues = false;

                item.userName = "ĐỂ TRỐNG";

                itemModels.add(0, item);
                HistoryAdapter adapter1 = new HistoryAdapter(itemModels, ManagementActivity.this);
                recyclerView.setAdapter(adapter1);
                dialog.dismiss();
            }
        });

        dialog.show();
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
    }
}
