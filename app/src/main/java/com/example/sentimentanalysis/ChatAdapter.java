package com.example.sentimentanalysis;

import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<ChatMessage> messages = new ArrayList<>();

    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSenderType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ChatMessage.SENDER_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_bubble, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.bot_bubble, parent, false);
            return new BotViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).textView.setText(message.getText());

        } else if (holder instanceof BotViewHolder) {
            String rawText = message.getText();

            // Convert markdown-style bold to HTML <b>
            rawText = rawText.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");

            // Handle different bullet types (at line start)
            rawText = rawText.replaceAll("(?m)^\\s*[-–•*]\\s+", "&#8226; ");

            // Optional: Add spacing for numbered lists (e.g. 1. ...)
            rawText = rawText.replaceAll("(?m)^\\s*(\\d+)\\.\\s+", "<br><b>$1.</b> ");

            // Convert line breaks to HTML <br>
            rawText = rawText.replaceAll("\n", "<br>");

            // Parse as HTML
            Spanned formattedText = Html.fromHtml(rawText, Html.FROM_HTML_MODE_LEGACY);

            ((BotViewHolder) holder).textView.setText(formattedText);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        UserViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.message_text);
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        BotViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_bot);
        }
    }
}
