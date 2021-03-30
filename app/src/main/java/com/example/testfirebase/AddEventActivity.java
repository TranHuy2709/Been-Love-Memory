package com.example.testfirebase;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testfirebase.utils.DateUtils;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEventActivity extends AppCompatActivity {

    TextView addDate;
    EditText addName;
    Button btnAddEvent;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        context=this;
        addDate= findViewById(R.id.addEventDate);
        addDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar todayCalendar= Calendar.getInstance();
                int currentYear= 2004;
                int currentMonth= todayCalendar.get(Calendar.MONTH);
                int currentDay= todayCalendar.get(Calendar.DATE);

                DatePickerDialog datePickerDialog= new DatePickerDialog(context,android.R.style.Theme_Holo_Dialog, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String pickedDay, pickedMonth;
                        if(dayOfMonth<10){
                            pickedDay= "0"+dayOfMonth;
                        }else {
                            pickedDay= dayOfMonth+"";
                        }
                        if(month<9){
                            pickedMonth= "0"+(month+1);
                        }else {
                            pickedMonth= (month+1)+"";
                        }
                        addDate.setText(pickedDay+ "/" + pickedMonth);
                    }},currentYear, currentMonth, currentDay);
                datePickerDialog.getDatePicker().findViewById(getResources().getIdentifier("android:id/year",null,null)).setVisibility(View.GONE);
                datePickerDialog.show();
            }
        });
        addName= findViewById(R.id.addEventName);
        btnAddEvent= findViewById(R.id.btnAddEvent);
        btnAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String date= addDate.getText().toString();
                String name= addName.getText().toString();

                if(date.isEmpty()){
                    addDate.setError("Please pick event date");
                    addDate.requestFocus();
                    return;
                }
                if(name.isEmpty()){
                    addName.setError("Please enter event name");
                    addName.requestFocus();
                    return;
                }

                String eventName= name.substring(0,1).toUpperCase()+ name.substring(1);
                Intent intent= new Intent(AddEventActivity.this, MainAppActivity.class);
                intent.putExtra("eventDate", date);
                intent.putExtra("eventName", eventName);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        });
    }



}