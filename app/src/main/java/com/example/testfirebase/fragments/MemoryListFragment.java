package com.example.testfirebase.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.testfirebase.AddMemoryActivity;
import com.example.testfirebase.R;
import com.example.testfirebase.adapters.MemoryAdapter;
import com.example.testfirebase.models.Memory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import static com.example.testfirebase.MainActivity.CURRENT_USER_ID;


public class MemoryListFragment extends Fragment {

    public static int ADD_MEMORY_CODE = 1;
    public static int UPDATE_MEMORY_CODE = 2;

    private List<Memory> memories;
    private String currentUserId;
    private int updatePosition=0;

    private ImageView imgAddMemory;
    private RecyclerView memoryRecyclerView;
    private MemoryAdapter adapter;
    private SharedPreferences currentUserShared;

    public MemoryListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUserShared = getActivity().getSharedPreferences(CURRENT_USER_ID, Context.MODE_PRIVATE);
        currentUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        setupMemory();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_memory_list, container, false);
        setupView(view);
        return view;
    }

    private void setupView(View view){
        imgAddMemory= view.findViewById(R.id.imgAddMemory);
        imgAddMemory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent= new Intent(getActivity(), AddMemoryActivity.class);
                int memoryCount= currentUserShared.getInt("memoryCount",0);
                intent.putExtra("memoryCount",memoryCount);
                intent.putExtra("intentType", ADD_MEMORY_CODE);
                startActivityForResult(intent, ADD_MEMORY_CODE);
            }
        });

        memoryRecyclerView= view.findViewById(R.id.memoryRecycleView);
        memoryRecyclerView.setHasFixedSize(true);
        adapter= new MemoryAdapter(memories, currentUserId, getActivity(), new MemoryAdapter.MemoryOnClick() {
            @Override
            public void onUpdate(int position) {
                updateMemory(position);
                updatePosition=position;
            }
            @Override
            public void onDelete(int position) {
                Memory memory= memories.get(position);
                memories.remove(position);
                adapter.notifyDataSetChanged();
                updateMemoryListString();
                deleteMemoryInFirebase(memory.getImageId());
            }
        });
        LinearLayoutManager layoutManager= new LinearLayoutManager(getActivity());
        memoryRecyclerView.setLayoutManager(layoutManager);
        memoryRecyclerView.setAdapter(adapter);
    }

    private void setupMemory() {
        memories= new ArrayList<>();
            String memoryList= currentUserShared.getString("memory","");
            if(memoryList.equals("")){
                return;
            }else {
                String[] listMemory= memoryList.split("-");
                for(String s: listMemory){
                    if(s.equals("")){
                        continue;
                    }
                    String[] memoryDetail= s.split(",");
                    String date= memoryDetail[0];
                    String name= memoryDetail[1];
                    int imageId= Integer.parseInt(memoryDetail[2]);
                    Memory memory= new Memory(date, name, imageId);
                    memories.add(memory);
                }
            }
    }

    private void getMemoryCount(){

    }

    private void updateMemory(int position){
        Intent intent= new Intent(getActivity(), AddMemoryActivity.class);
        Memory memory= memories.get(position);
        intent.putExtra("date", memory.getDate());
        intent.putExtra("name", memory.getName());
        intent.putExtra("imageId", memory.getImageId());
        intent.putExtra("intentType", UPDATE_MEMORY_CODE);
        startActivityForResult(intent, UPDATE_MEMORY_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode== ADD_MEMORY_CODE && resultCode== Activity.RESULT_OK && data!= null){
            String date= data.getStringExtra("date");
            String name= data.getStringExtra("name");
            int imageId= currentUserShared.getInt("memoryCount",0);
            Memory memory= new Memory(date, name, imageId);
            memories.add(memory);
            adapter.notifyDataSetChanged();
            String memoryList= currentUserShared.getString("memory","");
            memoryList= memoryList + date+ "," + name + "," + imageId + "-";
            imageId+=1;
            updateMemoryCounter(imageId);
            currentUserShared.edit().putString("memory", memoryList).apply();
            currentUserShared.edit().putInt("memoryCount", imageId).apply();
        }
        if(requestCode== UPDATE_MEMORY_CODE && resultCode== Activity.RESULT_OK && data!=null){
            String date= data.getStringExtra("date");
            String name= data.getStringExtra("name");
            Memory memory= memories.get(updatePosition);
            memory.setDate(date);
            memory.setName(name);
            adapter.notifyItemChanged(updatePosition);
            updateMemoryListString();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    private void updateMemoryListString(){
        String memoryList= new String();
        for(Memory m: memories){
            String memoryDetail= m.getDate()+ ","+ m.getName()+ "," + m.getImageId();
            memoryList = memoryList + memoryDetail+ "-";
        }
        currentUserShared.edit().putString("memory", memoryList).apply();
    }

    private void deleteMemoryInFirebase(int imageId){
        FirebaseDatabase.getInstance().getReference("Users")
                .child(currentUserId).child("memories")
                .child(imageId+"").removeValue();
        FirebaseStorage.getInstance().getReference("Images")
                .child(currentUserId).child(imageId+"").delete();
    }

    private void updateMemoryCounter(int count){
        FirebaseDatabase.getInstance().getReference("Users")
                .child(currentUserId).child("memoryCount")
                .setValue(count );
    }
}