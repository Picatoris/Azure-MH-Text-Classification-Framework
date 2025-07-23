package com.example.sentimentanalysis;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HelplineAdapter extends RecyclerView.Adapter<HelplineAdapter.HelplineViewHolder> {

    private Context context;
    private ArrayList<HelplineModel> helplineList;

    public HelplineAdapter(Context context, ArrayList<HelplineModel> helplineList) {
        this.context = context;
        this.helplineList = helplineList;
    }

    @NonNull
    @Override
    public HelplineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.helpline_card, parent, false);
        return new HelplineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HelplineViewHolder holder, int position) {
        HelplineModel model = helplineList.get(position);
        holder.title.setText(model.getTitle());
        holder.description.setText(model.getDescription());
        holder.phone.setText(model.getPhone());
        holder.availability.setText("Available: " + model.getAvailability());

        holder.callNow.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + model.getPhone()));
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return helplineList.size();
    }

    public static class HelplineViewHolder extends RecyclerView.ViewHolder {
        TextView title, description, phone, availability;
        Button callNow;

        public HelplineViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.helplineTitle);
            description = itemView.findViewById(R.id.helplineDescription);
            phone = itemView.findViewById(R.id.helplinePhone);
            availability = itemView.findViewById(R.id.helplineAvailability);
            callNow = itemView.findViewById(R.id.callNowButton);
        }
    }
}
