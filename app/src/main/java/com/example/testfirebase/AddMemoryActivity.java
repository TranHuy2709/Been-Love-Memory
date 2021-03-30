package com.example.testfirebase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.testfirebase.models.Memory;
import com.example.testfirebase.utils.DateUtils;
import com.example.testfirebase.utils.FileUtils;
import com.example.testfirebase.utils.PermissionUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import static com.example.testfirebase.fragments.MemoryListFragment.ADD_MEMORY_CODE;

public class AddMemoryActivity extends AppCompatActivity {

    public static final int REQUEST_CAMERA_CODE = 3;
    public static final int PICK_IMAGE = 4;
    TextView txtAddMemory, txtDate;
    EditText edtName;
    ImageView imgNewMemory;
    Button btnAddMemory;
    int imageId;

    Bitmap bitmap;
    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Users")
            .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
    StorageReference storageRef= FirebaseStorage.getInstance().getReference("Images")
            .child(FirebaseAuth.getInstance().getCurrentUser().getUid());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_memory);

        setupView();

    }

    private void setupView(){

        txtAddMemory= findViewById(R.id.txtAddMemory);
        txtDate= findViewById(R.id.txtPickDate);
        txtDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDate();
            }
        });
        edtName= findViewById(R.id.edtMemoryName);
        imgNewMemory= findViewById(R.id.imgNewMemory);
        imgNewMemory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(imgNewMemory);
            }
        });
        btnAddMemory= findViewById(R.id.btnAddMemory);
        int intentType= getIntent().getIntExtra("intentType",0);
        imageId= getIntent().getIntExtra("memoryCount",0);
        if(intentType== ADD_MEMORY_CODE){
            txtAddMemory.setText(R.string.addmemory);
            btnAddMemory.setText(R.string.add);
            btnAddMemory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addNewMemory();
                    finish();
                }
            });
        }else {
            txtAddMemory.setText(R.string.updatememory);
            String date= getIntent().getStringExtra("date");
            String name= getIntent().getStringExtra("name");
            txtDate.setText(date);
            edtName.setText(name);
            btnAddMemory.setText(R.string.update);
            btnAddMemory.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateMemory();
                    finish();
                }
            });
            int memoryImageId= getIntent().getIntExtra("imageId",0);
            String imageFilePath= FileUtils.grenateFilePath(this, memoryImageId+"");
            Glide.with(this).load(imageFilePath)
                    .override(1024,1024)
                    .signature(new ObjectKey(System.currentTimeMillis()))
                    .into(imgNewMemory);
        }
    }

    private void showPopupMenu(ImageView imageView) {
        PopupMenu popupMenu = new PopupMenu(AddMemoryActivity.this, imageView);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.takeCamera) {
                    checkCameraPermission();
                }else if (item.getItemId()== R.id.choosePicture) {
                    checkPicturePermission();
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void checkPicturePermission() {
        boolean galleryPermission = PermissionUtils.hasGalleryPerrmission(this);
        if(galleryPermission){
            intentGallery();
        }else{
            PermissionUtils.requestGalleryPermission(this, PICK_IMAGE);
        }
    }

    private void checkCameraPermission() {
        boolean cameraPermission= PermissionUtils.hasCameraPerrmission(this);
        if(cameraPermission){
            intentCamera();
        }else {
            PermissionUtils.requestCameraPermission(this,REQUEST_CAMERA_CODE);
        }
    }

    private void intentCamera() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA_CODE);
    }

    private void intentGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "data"), PICK_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode== PICK_IMAGE && grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            intentGallery();
        }
        if(requestCode== REQUEST_CAMERA_CODE && grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            intentCamera();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == REQUEST_CAMERA_CODE && resultCode == RESULT_OK && data != null) {
            bitmap = (Bitmap) data.getExtras().get("data");
            imgNewMemory.setImageBitmap(bitmap);
        }else if(requestCode== PICK_IMAGE && resultCode== RESULT_OK && data !=null){
            InputStream inputStream= new InputStream() {
                @Override
                public int read() throws IOException {
                    return 0;
                }
            };
            try {
                inputStream = this.getContentResolver().openInputStream(data.getData());
                bitmap= BitmapFactory.decodeStream(inputStream);
                imgNewMemory.setImageBitmap(bitmap);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void pickDate() {
        //get today todayCalendar
        Calendar todayCalendar= Calendar.getInstance();
        int currentYear= todayCalendar.get(Calendar.YEAR);
        int currentMonth= todayCalendar.get(Calendar.MONTH);
        int currentDay= todayCalendar.get(Calendar.DATE);

        //set today to pickerDialog
        DatePickerDialog datePickerDialog= new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                boolean checkDate= DateUtils.checkDate(year, month, dayOfMonth);
                if(!checkDate){
                    Toast.makeText(AddMemoryActivity.this,"Invalid date", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    Calendar pickedCalendar= Calendar.getInstance();
                    pickedCalendar.set(year, month, dayOfMonth);
                    txtDate.setText(DateUtils.dateFormat.format(pickedCalendar.getTime()));
                }
            }},currentYear, currentMonth, currentDay);
        datePickerDialog.show();
    }

    private void addNewMemory() {
        String date= txtDate.getText().toString();
        String name= edtName.getText().toString();
        if(date.isEmpty()){
            txtDate.setError("Please pick memory date");
            txtDate.requestFocus();
            return;
        }
        if(name.isEmpty()){
            edtName.setError("Please enter memory name");
            edtName.requestFocus();
            return;
        }


        Memory memory = new Memory(date, name, imageId);

        databaseRef.child("memories").child(imageId + "")
                .setValue(memory).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddMemoryActivity.this, "Failed to add memory", Toast.LENGTH_LONG).show();
            }
        });

        if(bitmap==null){
            Toast.makeText(AddMemoryActivity.this, "Don't forget to take a photo", Toast.LENGTH_LONG).show();
            return;
        }
        FileUtils.saveMemoryBitmapToInternalStorage(this,bitmap, memory.getImageId()+"");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        storageRef.child(imageId+"")
                .putBytes(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(AddMemoryActivity.this,"Failed", Toast.LENGTH_LONG).show();
            }
        });

        Intent intent= new Intent(AddMemoryActivity.this, MainAppActivity.class);
        intent.putExtra("date", date);
        intent.putExtra("name", name);
        setResult(Activity.RESULT_OK, intent);
    }

    private void updateMemory() {
        String date= txtDate.getText().toString();
        String name= edtName.getText().toString();
        if(date.isEmpty()){
            txtDate.setError("Please pick memory date");
            txtDate.requestFocus();
            return;
        }
        if(name.isEmpty()){
            edtName.setError("Please enter memory name");
            edtName.requestFocus();
            return;
        }

        int imageId= getIntent().getIntExtra("imageId",0);
        storageRef.child(imageId+"").delete();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();
        storageRef.child(imageId+ "").putBytes(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                }
            }
        });
        FileUtils.saveMemoryBitmapToInternalStorage(this, bitmap, imageId+"");
        String intentDate= getIntent().getStringExtra("date");
        String intentName= getIntent().getStringExtra("name");

        if(!date.equals(intentDate) || !name.equals(intentName)){
            Memory memory= new Memory(date,name, imageId);
            databaseRef.child("memories").child(imageId + "")
                    .removeValue();
            databaseRef.child("memories").child(imageId + "")
                    .setValue(memory).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {

                }
            });
        }


        Intent intent= new Intent(AddMemoryActivity.this,MainAppActivity.class);
        intent.putExtra("date", date);
        intent.putExtra("name", name);
        setResult(Activity.RESULT_OK, intent);

    }
}