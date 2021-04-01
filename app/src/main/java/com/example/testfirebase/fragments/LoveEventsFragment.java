package com.example.testfirebase.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.testfirebase.AddEventActivity;
import com.example.testfirebase.AddMemoryActivity;
import com.example.testfirebase.R;
import com.example.testfirebase.adapters.EventAdapter;
import com.example.testfirebase.models.Event;
import com.example.testfirebase.utils.DateUtils;
import com.example.testfirebase.utils.FileUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Comparator;

import static android.app.Activity.RESULT_OK;
import static com.example.testfirebase.MainActivity.CURRENT_USER_ID;


public class LoveEventsFragment extends Fragment {

    public static int ADD_EVENT_CODE=3;

    ImageView imgAddEvent;
    RecyclerView eventRecyclerView;
    EventAdapter eventAdapter;

    ArrayList<Event> events= new ArrayList<>();
    SharedPreferences currentUserShared;
    DatabaseReference eventRef;

    public LoveEventsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUserShared= getActivity().getSharedPreferences(CURRENT_USER_ID, Context.MODE_PRIVATE);
        eventRef= FirebaseDatabase.getInstance().getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("events");
        setupEvent();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_love_events, container, false);
        setupView(view);
        return view;
    }

    private void setupView(View view) {
        imgAddEvent= view.findViewById(R.id.imgAddEvent);
        imgAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(getActivity(), AddEventActivity.class);
                startActivityForResult(intent, ADD_EVENT_CODE);
            }
        });
        eventRecyclerView= view.findViewById(R.id.eventRecycleView);
        eventRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager= new LinearLayoutManager(getActivity());
        eventRecyclerView.setLayoutManager(layoutManager);
        eventAdapter = new EventAdapter(events);
        eventRecyclerView.setAdapter(eventAdapter);
    }


    private void setupEvent() {
        String eventList= currentUserShared.getString("event","");
        if(eventList.equals("")){
            return;
        }
        for(String s: eventList.split("-")){
            String[] eventDetail= s.trim().split(",");
            String eventDate= eventDetail[0];
            String eventName= eventDetail[1];
            long time= DateUtils.getTime(eventDate);
            Event event= new Event(eventDate, eventName, time);
            events.add(event);
        }
        sortEvent(events);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode== ADD_EVENT_CODE && resultCode== RESULT_OK && data!=null){
            String eventDate= data.getStringExtra("eventDate");
            String eventName= data.getStringExtra("eventName");
            String key= FileUtils.grenateEventKey(eventDate);
            for (Event e: events){
                if(eventDate.equals(e.getDate())){
                    String newName= e.getName()+ "|"+  eventName;
                    e.setName(newName);
                    setEventToFirebase(key, newName);
                    updateEventList();
                    eventAdapter.notifyDataSetChanged();
                    return;
                }
            }
            long eventTime= DateUtils.getTime(eventDate);
            Event newEvent= new Event(eventDate, eventName, eventTime);
            events.add(newEvent);
            sortEvent(events);
            updateEventList();
            eventAdapter.notifyDataSetChanged();
            setEventToFirebase(key, eventName);
        }

    }

    private void setEventToFirebase(String eventDate, String eventName){
        eventRef.child(eventDate).setValue(eventName).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getActivity(),"Add event success",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void updateEventList(){
        String eventList="";
        for(Event e: events){
            eventList= eventList + e.getDate() + ","+ e.getName() + "-";
        }
        currentUserShared.edit().putString("event", eventList).apply();
    }

    private void sortEvent(ArrayList<Event> events){
        for(int i=0; i<events.size()-1; i++){
            for(int j=i+1; j<events.size(); j++){
                if(events.get(i).getTime()> events.get(j).getTime()){
                    Event temp= events.get(i);
                    events.set(i, events.get(j));
                    events.set(j, temp);
                }
            }
        }

    }

}