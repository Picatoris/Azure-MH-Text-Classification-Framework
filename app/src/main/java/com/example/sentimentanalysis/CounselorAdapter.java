//package com.example.sentimentanalysis;
//
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.ImageView;
//import android.widget.RatingBar;
//import android.widget.TextView;
//import androidx.annotation.NonNull;
//import androidx.cardview.widget.CardView;
//import androidx.recyclerview.widget.RecyclerView;
//import java.util.List;
//
//public class CounselorAdapter extends RecyclerView.Adapter<CounselorAdapter.CounselorViewHolder> {
//
//    private final List<BookConsultationActivity.Counselor> counselors;
//
//    public CounselorAdapter(List<BookConsultationActivity.Counselor> counselors) {
//        this.counselors = counselors;
//    }
//
//    @NonNull
//    @Override
//    public CounselorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_counselor, parent, false);
//        return new CounselorViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull CounselorViewHolder holder, int position) {
//        BookConsultationActivity.Counselor counselor = counselors.get(position);
//        holder.nameText.setText(counselor.name);
//        holder.availabilityText.setText(counselor.availability);
//        holder.descriptionText.setText(counselor.description);
//        holder.languagesText.setText("Languages - " + counselor.languages);
//        holder.expertiseText.setText("Expertise - " + counselor.expertise);
//        holder.availabilityDaysText.setText("Availability - " + counselor.availabilityDays);
//        holder.ratingBar.setRating(counselor.rating);
//
//        holder.bookNowButton.setOnClickListener(v -> {
//            // Add booking logic here
//        });
//    }
//
//    @Override
//    public int getItemCount() {
//        return counselors.size();
//    }
//
//    public static class CounselorViewHolder extends RecyclerView.ViewHolder {
//        ImageView profileImage;
//        TextView nameText, availabilityText, descriptionText, languagesText, expertiseText, availabilityDaysText;
//        RatingBar ratingBar;
//        Button bookNowButton;
//        CardView cardView;
//
//        public CounselorViewHolder(@NonNull View itemView) {
//            super(itemView);
//            cardView = itemView.findViewById(R.id.counselorCard);
//            profileImage = itemView.findViewById(R.id.profileImage);
//            nameText = itemView.findViewById(R.id.nameText);
//            availabilityText = itemView.findViewById(R.id.availabilityText);
//            descriptionText = itemView.findViewById(R.id.descriptionText);
//            languagesText = itemView.findViewById(R.id.languagesText);
//            expertiseText = itemView.findViewById(R.id.expertiseText);
//            availabilityDaysText = itemView.findViewById(R.id.availabilityDaysText);
//            ratingBar = itemView.findViewById(R.id.ratingBar);
//            bookNowButton = itemView.findViewById(R.id.bookNowButton);
//        }
//    }
//}