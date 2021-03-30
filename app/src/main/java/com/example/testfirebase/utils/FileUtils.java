package com.example.testfirebase.utils;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.testfirebase.models.Event;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class FileUtils {

    public static String saveMemoryBitmapToInternalStorage(Context context, Bitmap bitmap, String filePathTail) {
        ContextWrapper contextWrapper= new ContextWrapper(context);
        File directory= contextWrapper.getDir("directory", Context.MODE_PRIVATE);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        File memory= new File(directory,filePathTail);
        byte[] bitmapData = bos.toByteArray();
        try {
            FileOutputStream fos = new FileOutputStream(memory);
            fos.write(bitmapData);
            fos.flush();
            fos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        String filePath= memory.getAbsolutePath();
        return filePath;
    }

    public static String grenateFilePath(Context context, String imageId){
        ContextWrapper contextWrapper= new ContextWrapper(context);
        File directory= contextWrapper.getDir("directory", Context.MODE_PRIVATE);
        File memory= new File(directory, imageId);
        String filePath= memory.getAbsolutePath();
        return  filePath;
    }

    public static void deleteInternalStorage(Context context){
        ContextWrapper contextWrapper= new ContextWrapper(context);
        File directory= contextWrapper.getDir("directory", Context.MODE_PRIVATE);
        File[] files= directory.listFiles();
        for(File f: files){
            f.delete();
        }
    }

    public static String grenateEventKey(String date){
        String key="";
        for(String s: date.split("/")){
            key+= s;
        }
        return key;
    }

    public static String grenateEventDate(String key) {
        String day= key.substring(0,2);
        String month= key.substring(2,4);
        String date= day+ "/" + month;
        return  date;
    }
}
