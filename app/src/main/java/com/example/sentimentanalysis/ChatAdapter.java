package com.example.sentimentanalysis;

import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final ArrayList<ChatMessage> messages = new ArrayList<>();

    // ---------------- ADD MESSAGE ----------------
    public void addMessage(ChatMessage message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    // ---------------- REMOVE MESSAGE ----------------
    public void removeMessage(int index) {
        if (index >= 0 && index < messages.size()) {
            messages.remove(index);
            notifyItemRemoved(index);
        }
    }

    // ---------------- UPDATE MESSAGE ----------------
    public void updateMessage(int index, ChatMessage newMessage) {
        if (index >= 0 && index < messages.size()) {
            messages.set(index, newMessage);
            notifyItemChanged(index);
        }
    }

    // ---------------- GET TYPING MESSAGE INDEX ----------------
    public int getTypingMessageIndex() {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getSenderType() == ChatMessage.SENDER_BOT_TYPING) {
                return i;
            }
        }
        return -1;
    }

    // ---------------- VIEW TYPE ----------------
    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSenderType();
    }

    // ---------------- CREATE VIEW HOLDER ----------------
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ChatMessage.SENDER_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.user_bubble, parent, false);
            return new UserViewHolder(view);
        } else if (viewType == ChatMessage.SENDER_BOT_TYPING) {
            // Use the same bot layout but show typing differently
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.bot_bubble, parent, false);
            return new BotViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.bot_bubble, parent, false);
            return new BotViewHolder(view);
        }
    }

    // ---------------- BIND VIEW HOLDER ----------------
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = messages.get(position);
        String rawText = message.getText();

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String timestamp = sdf.format(new Date(message.getTimestamp()));

        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).textView.setText(rawText);
            ((UserViewHolder) holder).timeView.setText(timestamp);

        } else if (holder instanceof BotViewHolder) {
            // Typing indicator shows animated dots
            if (message.getSenderType() == ChatMessage.SENDER_BOT_TYPING) {
                ((BotViewHolder) holder).textView.setText("MIND is thinking...");
            } else {
                // Convert markdown-style bold to HTML <b>
                rawText = rawText.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");
                // Handle bullet points
                rawText = rawText.replaceAll("(?m)^\\s*[-–•*]\\s+", "&#8226; ");
                // Handle numbered lists
                rawText = rawText.replaceAll("(?m)^\\s*(\\d+)\\.\\s+", "<br><b>$1.</b> ");
                // Convert line breaks
                rawText = rawText.replaceAll("\n", "<br>");
                // Handle inline code blocks with ``
                rawText = rawText.replaceAll("`(.*?)`", "<code>$1</code>");
                // Parse as HTML
                Spanned formattedText = Html.fromHtml(rawText, Html.FROM_HTML_MODE_LEGACY);
                ((BotViewHolder) holder).textView.setText(formattedText);
            }
            ((BotViewHolder) holder).timeView.setText(timestamp);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    // ---------------- VIEW HOLDERS ----------------
    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView textView, timeView;

        UserViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.message_text);
            timeView = itemView.findViewById(R.id.message_time);
        }
    }

    static class BotViewHolder extends RecyclerView.ViewHolder {
        TextView textView, timeView;

        BotViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.text_bot);
            timeView = itemView.findViewById(R.id.text_bot_time);
        }
    }
}