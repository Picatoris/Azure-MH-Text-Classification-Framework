package com.example.sentimentanalysis;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DoctorAdapter extends RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder> {

    List<DoctorModel> doctorList;

    public DoctorAdapter(List<DoctorModel> doctorList) {
        this.doctorList = doctorList;
    }

    @NonNull
    @Override
    public DoctorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_doctor_card, parent, false);
        return new DoctorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DoctorViewHolder holder, int position) {
        DoctorModel doctor = doctorList.get(position);
        holder.name.setText(doctor.name);
        holder.description.setText(doctor.description);
        holder.languages.setText("Languages: " + doctor.languages);
        holder.expertise.setText("Expertise: " + doctor.expertise);
        holder.availability.setText("Availability: " + doctor.availability);
        holder.image.setImageResource(doctor.imageResId);
    }

    @Override
    public int getItemCount() {
        return doctorList.size();
    }

    static class DoctorViewHolder extends RecyclerView.ViewHolder {
        TextView name, description, languages, expertise, availability;
        ImageView image;

        public DoctorViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.doctorName);
            description = itemView.findViewById(R.id.doctorDesc);
            languages = itemView.findViewById(R.id.languages);
            expertise = itemView.findViewById(R.id.expertise);
            availability = itemView.findViewById(R.id.availability);
            image = itemView.findViewById(R.id.doctorImage);
        }
    }
}
