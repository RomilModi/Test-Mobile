package com.example.rmodi.checkconstantlocation.Adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.rmodi.checkconstantlocation.R;
import com.example.rmodi.checkconstantlocation.bean.Attandancebean;
import com.example.rmodi.checkconstantlocation.bean.UserAttendance;

import java.util.ArrayList;

/**
 * Created by rmodi on 10/21/2016.
 */

public class AdapterAttandance extends RecyclerView.Adapter<AdapterAttandance.MyViewHolder> {


    private ArrayList<UserAttendance> mArrayAttandance;
    private Context mCon;

    public AdapterAttandance(Context c, ArrayList<UserAttendance> mArrayAttandance) {
        this.mCon = c;
        this.mArrayAttandance = mArrayAttandance;

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView id, user_activity, created_at;

        public MyViewHolder(View view) {
            super(view);
            id = (TextView) view.findViewById(R.id.id);
            user_activity = (TextView) view.findViewById(R.id.user_activity);
            created_at = (TextView) view.findViewById(R.id.created_at);
        }
    }


    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        UserAttendance movie = mArrayAttandance.get(position);
        holder.id.setText(movie.getUserId());
        holder.user_activity.setText(movie.getUserActivity());
        holder.created_at.setText(movie.getCreatedAt());
    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_attandance, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return mArrayAttandance.size();
    }
}
