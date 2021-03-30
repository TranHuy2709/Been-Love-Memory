package com.example.testfirebase.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testfirebase.R;
import com.example.testfirebase.models.Event;
import com.example.testfirebase.utils.DateUtils;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.ViewHolder> {

    private int UPCOMING_TYPE= 1;
    private int NORMAL_TYPE= 2;

    private List<Event> events;

    public EventAdapter(List<Event> events){
        this.events= events;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId;
        if(viewType== UPCOMING_TYPE){
            layoutId= R.layout.item_love_event_2;
        }else {
            layoutId= R.layout.item_love_event;
        }
        LayoutInflater layoutInflater=LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(layoutId, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        View view= holder.getView();
        setupView(view, position);
    }

    private void setupView(View view, int position) {
        Event event= events.get(position);
        TextView txtDate= view.findViewById(R.id.txtEventDate);
        txtDate.setText(event.getDate());

        TextView txtName= view.findViewById(R.id.txtEventName);
        if(event.getName().contains("|")){
            String name="";
            for(String s: event.getName().split("|")){
                name= name+ s + "\n";
            }
            txtName.setText(name);
        }else {
            txtName.setText(event.getName());
        }
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    @Override
    public int getItemViewType(int position) {
        Event event= events.get(position);
        if(event.isUpcoming()){
            return UPCOMING_TYPE;
        }else {
            return  NORMAL_TYPE;
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        View view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.view= itemView;
        }

        private View getView(){
            return view;
        }
    }
}
