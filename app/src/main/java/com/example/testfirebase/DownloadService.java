package com.example.testfirebase;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.testfirebase.models.Memory;
import com.example.testfirebase.utils.FileUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

import static com.example.testfirebase.MainActivity.CURRENT_USER_ID;
import static com.example.testfirebase.MainActivity.DOWNLOAD_TYPE;
import static com.example.testfirebase.fragments.DateCountFragment.FEMALE_AVATAR;
import static com.example.testfirebase.fragments.DateCountFragment.FEMALE_BIRTHDAY;
import static com.example.testfirebase.fragments.DateCountFragment.FIRST_DATE;
import static com.example.testfirebase.fragments.DateCountFragment.MALE_AVATAR;
import static com.example.testfirebase.fragments.DateCountFragment.MALE_BIRTHDAY;

public class DownloadService extends Service {

    public static String DOWNLOAD_MEMORY= "downloadMemory";
    public static String DOWNLOAD_IMAGE= "downloadImage";
    public static String DOWNLOAD_EVENT= "downloadEvent";
    public static String DOWNLOAD_DATE= "downloadDate";
    private static String IMAGE_COMPLETE= "image";

    private String currentUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();
    private SharedPreferences currentUserShared;
    private List<String> imageId;

    private BroadcastReceiver downloadImage= new DownloadImage();

    @Override
    public void onCreate() {
        currentUserShared = getSharedPreferences(CURRENT_USER_ID, MODE_PRIVATE);
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent!=null){
            String type= intent.getStringExtra(DOWNLOAD_TYPE);
            if(type.equals("Memory")){
                downloadMemory();
            }else if(type.equals("Image")){
                IntentFilter filter= new IntentFilter(IMAGE_COMPLETE);
                registerReceiver(downloadImage,filter);
                downloadImageToLocalStorage();
            }else if(type.equals("Event")){
                downloadEvent();
            }else if(type.equals("Date")){
                downloadDate();
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void downloadMemory() {
        FirebaseDatabase.getInstance().getReference("Users")
                .child(currentUserId).child("memories")
                .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    DataSnapshot snapshot= task.getResult();
                    String memoryList= new String();
                    for(DataSnapshot child: snapshot.getChildren()){
                        Memory memory= child.getValue(Memory.class);
                        String memoryDetail= memory.getDate()+ ","+ memory.getName()+ "," + memory.getImageId();
                        memoryList = memoryList + memoryDetail+ "-";
                    }
                    currentUserShared.edit().putString("memory", memoryList).apply();
                    getMemoryCounter();
                }
            }
        });
    }

    private void getMemoryCounter(){
        FirebaseDatabase.getInstance().getReference("Users")
                .child(currentUserId).child("memoryCount")
                .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    DataSnapshot snapshot= task.getResult();
                    int memoryCount= Integer.parseInt(snapshot.getValue().toString());
                    currentUserShared.edit().putInt("memoryCount", memoryCount).apply();
                    sendDownloadMemoryComplete();
                }
            }
        });
    }

    private void sendDownloadMemoryComplete(){
        Intent intent= new Intent(DOWNLOAD_MEMORY);
        intent.putExtra("MemoryComplete",true);
        sendBroadcast(intent);
    }

    private void downloadImageToLocalStorage(){
        String memoryList= currentUserShared.getString("memory","");
        imageId= new ArrayList<>();
        imageId.add(MALE_AVATAR);
        imageId.add(FEMALE_AVATAR);
        if(!memoryList.equals("")){
            String[] memories= memoryList.split("-");
            for(String s: memories){
                String[] memoryDetail= s.split(",");
                imageId.add(memoryDetail[2]);
            }
        }
        downloadImage(0);
    }

    private void downloadImage(int positon){
        long limit= 10000*10000;
        String id= imageId.get(positon);
        FirebaseStorage.getInstance().getReference("Images")
                .child(currentUserId).child(id)
                .getBytes(limit).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                Bitmap bitmap= BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                FileUtils.saveMemoryBitmapToInternalStorage(getApplicationContext(), bitmap, id);
                if(positon==(imageId.size()-1)){
                    sendDownloadMemoryImageComplete();
                    return;
                }
                sendDownloadImageComplete(positon);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String ex= e.getMessage();
                Toast.makeText(DownloadService.this, ex, Toast.LENGTH_LONG).show();
                sendDownloadImageComplete(positon);
            }
        });
    }

    private void sendDownloadImageComplete(int pos){
        Intent intent= new Intent(IMAGE_COMPLETE);
        intent.putExtra("ImageComplete",pos+1);
        sendBroadcast(intent);
    }

    class DownloadImage extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            int pos= intent.getIntExtra("ImageComplete", 0);
            downloadImage(pos);
        }
    }

    private void sendDownloadMemoryImageComplete(){
        Intent intent= new Intent(DOWNLOAD_IMAGE);
        intent.putExtra("ImageComplete",true);
        sendBroadcast(intent);
    }

    private void downloadEvent(){
        FirebaseDatabase.getInstance().getReference("Users")
                .child(currentUserId).child("events")
                .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    DataSnapshot snapshot= task.getResult();
                    String eventList= new String();
                    for(DataSnapshot child: snapshot.getChildren()){
                        String eventDate= FileUtils.grenateEventDate(child.getKey());
                        String eventName= child.getValue().toString();
                        eventList= eventList+ eventDate + "," + eventName +"-";
                    }
                    currentUserShared.edit().putString("event", eventList).apply();
                    sendDownloadEventComplete();
                }
            }
        });
    }

    private void sendDownloadEventComplete(){
        Intent intent= new Intent(DOWNLOAD_EVENT);
        intent.putExtra("EventComplete",true);
        sendBroadcast(intent);
    }

    private void downloadDate(){
        FirebaseDatabase.getInstance().getReference("Users")
                .child(currentUserId).child("dates")
                .get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if(task.isSuccessful()){
                    DataSnapshot snapshot= task.getResult();
                    for (DataSnapshot child: snapshot.getChildren()){
                        String date= child.getValue().toString();
                        if(child.getKey().equals(MALE_BIRTHDAY)){
                            currentUserShared.edit().putString(MALE_BIRTHDAY, date).apply();
                        }else if(child.getKey().equals(FEMALE_BIRTHDAY)){
                            currentUserShared.edit().putString(FEMALE_BIRTHDAY, date).apply();
                        }else if(child.getKey().equals(FIRST_DATE)){
                            currentUserShared.edit().putString(FIRST_DATE, date).apply();
                        }
                    }
                    sendDownloadDateComplete();
                }
            }
        });
    }

    private void sendDownloadDateComplete(){
        Intent intent= new Intent(DOWNLOAD_DATE);
        intent.putExtra("DateComplete",true);
        sendBroadcast(intent);
    }

    public static String convertEventKey(String key){
        String newKey="";
        for(int i=0; i<key.length(); i++){
            if(key.charAt(i) == ';'){
                newKey += '/';
            }else {
                newKey += key.charAt(i);
            }
        }
        return newKey;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(downloadImage);
    }
}//end
