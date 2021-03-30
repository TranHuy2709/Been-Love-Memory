package com.example.testfirebase.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.testfirebase.R;
import com.example.testfirebase.utils.DateUtils;
import com.example.testfirebase.utils.FileUtils;
import com.example.testfirebase.utils.PermissionUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

import static android.app.Activity.RESULT_OK;
import static com.example.testfirebase.AddMemoryActivity.PICK_IMAGE;
import static com.example.testfirebase.AddMemoryActivity.REQUEST_CAMERA_CODE;
import static com.example.testfirebase.MainActivity.CURRENT_USER_ID;


public class DateCountFragment extends Fragment implements View.OnClickListener {

    public static String MALE_BIRTHDAY = "Male Birthday";
    public static String FEMALE_BIRTHDAY = "Female Birthday";
    public static String FIRST_DATE = "Fist Date";
    public static String MALE_AVATAR = "maleAvatar";
    public static String FEMALE_AVATAR = "femaleAvatar";

    private TextView txtNumberOfDay, txtMaleBirthday, txtMaleHoroscope, txtFemaleHoroscope, txtFemaleBirthday
            , txtYear, txtMonth, txtWeek, txtFirstDate;
    private ImageView imgMaleAvatar, imgFemaleAvatar;
    private boolean maleAvatarClick, maleBirthdayClick, femaleBirthdayClick, firstDayClick;

    private String currentUserId;
    private StorageReference storageRef;

    private SharedPreferences currentUserShared;

    public DateCountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        currentUserShared = getActivity().getSharedPreferences(CURRENT_USER_ID, Context.MODE_PRIVATE);
        currentUserId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        storageRef= FirebaseStorage.getInstance().getReference("Images")
                .child(currentUserId);
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_date_count, container, false);
        setupView(view);
        return view;
    }

    private void setupView(View view) {
        txtNumberOfDay = view.findViewById(R.id.txtNumberOfDay);
        imgMaleAvatar = view.findViewById(R.id.maleAvatar);
        imgMaleAvatar.setOnClickListener(this);
        setAvatar(MALE_AVATAR);
        imgFemaleAvatar = view.findViewById(R.id.femaleAvatar);
        imgFemaleAvatar.setOnClickListener(this);
        setAvatar(FEMALE_AVATAR);
        txtMaleBirthday = view.findViewById(R.id.maleBirthday);
        txtMaleBirthday.setOnClickListener(this);
        txtFemaleBirthday = view.findViewById(R.id.femaleBirthday);
        txtFemaleBirthday.setOnClickListener(this);
        txtFirstDate = view.findViewById(R.id.firstDate);
        txtFirstDate.setOnClickListener(this);
        setDateFromFirebase();
        txtMaleHoroscope= view.findViewById(R.id.maleHoroscope);
        txtMaleHoroscope.setText(DateUtils.setHoroscope(txtMaleBirthday.getText().toString()));
        txtFemaleHoroscope= view.findViewById(R.id.femaleHoroscope);
        txtFemaleHoroscope.setText(DateUtils.setHoroscope(txtFemaleBirthday.getText().toString()));
        txtYear = view.findViewById(R.id.year);
        txtMonth = view.findViewById(R.id.month);
        txtWeek = view.findViewById(R.id.week);
        setupTime();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.maleAvatar:
                maleAvatarClick= true;
                showPopupMenu(imgMaleAvatar);
                break;
            case R.id.femaleAvatar:
                maleAvatarClick= false;
                showPopupMenu(imgFemaleAvatar);
                break;
            case R.id.maleBirthday:
                maleBirthdayClick= true;
                femaleBirthdayClick=false;
                firstDayClick=false;
                pickDate();
                break;
            case R.id.femaleBirthday:
                maleBirthdayClick= false;
                femaleBirthdayClick=true;
                firstDayClick=false;
                pickDate();
                break;
            case R.id.firstDate:
                maleBirthdayClick= false;
                femaleBirthdayClick=false;
                firstDayClick=true;
                pickDate();
                break;
        }
    }

    private void showPopupMenu(ImageView imageView) {
        PopupMenu popupMenu = new PopupMenu(getActivity(), imageView);
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
        boolean galleryPermission = PermissionUtils.hasGalleryPerrmission(getActivity());
        if(galleryPermission){
            intentGallery();
        }else{
            PermissionUtils.requestGalleryPermission(getActivity(), PICK_IMAGE);
        }
    }

    private void checkCameraPermission() {
        boolean cameraPermission= PermissionUtils.hasCameraPerrmission(getActivity());
        if(cameraPermission){
            intentCamera();
        }else {
            PermissionUtils.requestCameraPermission(getActivity(), REQUEST_CAMERA_CODE);
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
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bitmapData = baos.toByteArray();
            if(maleAvatarClick){
                imgMaleAvatar.setImageBitmap(bitmap);
                saveImageToFireBase(MALE_AVATAR, bitmapData);
                String path= FileUtils.saveMemoryBitmapToInternalStorage(getActivity(), bitmap, MALE_AVATAR);
                currentUserShared.edit().putString(MALE_AVATAR, path).apply();
            }else {
                imgFemaleAvatar.setImageBitmap(bitmap);
                saveImageToFireBase(FEMALE_AVATAR, bitmapData);
                String path= FileUtils.saveMemoryBitmapToInternalStorage(getActivity(), bitmap, FEMALE_AVATAR);
                currentUserShared.edit().putString(FEMALE_AVATAR, path).apply();
            }
        }else if(requestCode== PICK_IMAGE && resultCode== RESULT_OK && data !=null){
            InputStream inputStream= new InputStream() {
                @Override
                public int read() throws IOException {
                    return 0;
                }
            };
            try {
                inputStream = getActivity().getContentResolver().openInputStream(data.getData());
                Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] bitmapData = baos.toByteArray();
                if(maleAvatarClick){
                    imgMaleAvatar.setImageBitmap(bitmap);
                    saveImageToFireBase(MALE_AVATAR, bitmapData);
                    String path= FileUtils.saveMemoryBitmapToInternalStorage(getActivity(), bitmap, MALE_AVATAR);
                    currentUserShared.edit().putString(MALE_AVATAR, path).apply();
                }else {
                    imgFemaleAvatar.setImageBitmap(bitmap);
                    saveImageToFireBase(FEMALE_AVATAR, bitmapData);
                    String path= FileUtils.saveMemoryBitmapToInternalStorage(getActivity(), bitmap, FEMALE_AVATAR);
                    currentUserShared.edit().putString(FEMALE_AVATAR, path).apply();
                }
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
        DatePickerDialog datePickerDialog= new DatePickerDialog(getContext(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                boolean checkDate= DateUtils.checkDate(year, month, dayOfMonth);
                if(!checkDate){
                    Toast.makeText(getActivity(),"Invalid date", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    Calendar pickedCalendar= Calendar.getInstance();
                    pickedCalendar.set(year, month, dayOfMonth);
                    String str= DateUtils.dateFormat.format(pickedCalendar.getTime());
                    if(maleBirthdayClick) {
                        txtMaleBirthday.setText(str);
                        txtMaleHoroscope.setText(DateUtils.setHoroscope(str));
                        setDate(str, MALE_BIRTHDAY);
                    }
                    if(femaleBirthdayClick) {
                        txtFemaleBirthday.setText(str);
                        txtFemaleHoroscope.setText(DateUtils.setHoroscope(str));
                        setDate(str, FEMALE_BIRTHDAY);
                    }
                    if(firstDayClick) {
                        txtFirstDate.setText(str);
                        setupTime();
                        setDate(str, FIRST_DATE);
                    }

                }
            }},currentYear, currentMonth, currentDay);
        datePickerDialog.show();
    }

    private void setDate(String date, String type){
        FirebaseDatabase.getInstance().getReference("Users")
                .child(currentUserId).child("dates").child(type)
                .setValue(date).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(getActivity(), "Save "+ type+ " success.", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Save "+ type+ " failed.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setDateFromFirebase(){
        String maleBirthday= currentUserShared.getString(MALE_BIRTHDAY, "");
        if(maleBirthday!=""){
            txtMaleBirthday.setText(maleBirthday);
        }
        String femaleBirthday= currentUserShared.getString(FEMALE_BIRTHDAY, "");
        if(maleBirthday!=""){
            txtFemaleBirthday.setText(femaleBirthday);
        }
        String firstDate= currentUserShared.getString(FIRST_DATE, "");
        if(firstDate!=""){
            txtFirstDate.setText(firstDate);
        }
    }

    private void saveImageToFireBase(String type, byte[] data){
        storageRef.child(type).putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getActivity(), "Save "+ type+ " success.", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getActivity(), "Save "+ type+ " failed.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setAvatar(String type){
        String path= FileUtils.grenateFilePath(getActivity(), type);
        if(type.equals(MALE_AVATAR)){
            Glide.with(getActivity()).load(path)
                    .override(1024,1024)
                    .signature(new ObjectKey(System.currentTimeMillis()))
                    .into(imgMaleAvatar);
        }else if(type.equals(FEMALE_AVATAR)){
            Glide.with(getActivity()).load(path)
                    .override(1024,1024)
                    .signature(new ObjectKey(System.currentTimeMillis()))
                    .into(imgFemaleAvatar);
        }
    }

    private void setupTime(){
        Calendar currentTime= Calendar.getInstance();
        int currentYear= currentTime.get(Calendar.YEAR);
        int currentMonth= currentTime.get(Calendar.MONTH)+1;
        int currentDay= currentTime.get(Calendar.DATE);
        currentTime.set(currentYear, currentMonth, currentDay);
        Calendar firstDate= Calendar.getInstance();
        String date= txtFirstDate.getText().toString();
        String[] dates= date.split("/");
        firstDate.set(Integer.parseInt(dates[2]), Integer.parseInt(dates[1]), Integer.parseInt(dates[0]));
        int day= (int)((currentTime.getTimeInMillis()- firstDate.getTimeInMillis())/1000)/86400;
        txtNumberOfDay.setText(day+"");
        int week= day/7;
        txtWeek.setText(week+"");
        int month= day/30;
        txtMonth.setText(month+"");
        int year= day/365;
        txtYear.setText(year+"");
    }
}