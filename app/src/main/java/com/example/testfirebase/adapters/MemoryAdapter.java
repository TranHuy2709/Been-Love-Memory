package com.example.testfirebase.adapters;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.testfirebase.R;
import com.example.testfirebase.models.Memory;
import com.example.testfirebase.utils.FileUtils;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;

public class MemoryAdapter extends RecyclerView.Adapter<MemoryAdapter.ViewHolder> {

    public static final int MAX_BYTE_DOWNLOAD= 100000000;

    private List<Memory> memories;
    private String currentUserId;
    private Activity activity;
    private MemoryOnClick memoryOnClick;

    StorageReference reference;

    public MemoryAdapter(List<Memory> memories, String currentUserId, Activity activity, MemoryOnClick memoryOnClick){
        this.memories= memories;
        this.currentUserId= currentUserId;
        this.activity= activity;
        this.memoryOnClick= memoryOnClick;
        reference = FirebaseStorage.getInstance().getReference("Images")
                .child(currentUserId);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater=LayoutInflater.from(parent.getContext());
        View view=layoutInflater.inflate(R.layout.item_memory_view,parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        View view= holder.getView();
        setupView(view, position);
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return memories.size();
    }

    private void setupView(View view, int position){
        Memory memory= memories.get(position);

        TextView memoryDate= view.findViewById(R.id.txtMemoryDate);
        memoryDate.setText(memory.getDate());

        TextView memoryName= view.findViewById(R.id.txtMemoryName);
        memoryName.setText(memory.getName());

        ImageView updateMemory= view.findViewById(R.id.imgUpdate);
        updateMemory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                memoryOnClick.onUpdate(position);
            }
        });

        ImageView deleteMemory = view.findViewById(R.id.imgDelete);
        deleteMemory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                memoryOnClick.onDelete(position);
            }
        });

        ImageView memoryImage = view.findViewById(R.id.imgMemory);
        String imageFilePath= FileUtils.grenateFilePath(activity, memory.getImageId()+"");
        Glide.with(activity).load(imageFilePath)
                .override(1024,1024)
                .signature(new ObjectKey(System.currentTimeMillis()))
                .into(memoryImage);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View view;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.view= itemView;
        }

        View getView(){
            return view;
        }
    }

    public interface MemoryOnClick{
        void onUpdate(int position);
        void onDelete(int position);
    }
}
