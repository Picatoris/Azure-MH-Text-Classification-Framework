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

    // ---------------- HELPER: REMOVE TYPING INDICATOR ----------------
    public void removeTypingMessage() {
        for (int i = 0; i < messages.size(); i++) {
            if (messages.get(i).getSenderType() == ChatMessage.SENDER_BOT_TYPING) {
                messages.remove(i);
                notifyItemRemoved(i);
                return; // Stop after removing the first one found
            }
        }
    }

    // ---------------- HELPER: CHECK IF TYPING ----------------
    public boolean hasTypingMessage() {
        for (ChatMessage msg : messages) {
            if (msg.getSenderType() == ChatMessage.SENDER_BOT_TYPING) {
                return true;
            }
        }
        return false;
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
        } else {
            // Both SENDER_BOT and SENDER_BOT_TYPING use the bot bubble layout
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
            BotViewHolder botHolder = (BotViewHolder) holder;

            // Check if this is the "Typing..." message
            if (message.getSenderType() == ChatMessage.SENDER_BOT_TYPING) {

                // ✅ UPDATED: Hardcoded to exactly what you requested
                botHolder.textView.setText("MIND is thinking...");

                // Optional: Make it italic so it looks like a status
                botHolder.textView.setTypeface(null, android.graphics.Typeface.ITALIC);

            } else {
                // Reset typeface for normal messages
                botHolder.textView.setTypeface(null, android.graphics.Typeface.NORMAL);

                // --- MARKDOWN PARSING ---
                String parsedText = rawText;

                // Bold (**text**) -> <b>text</b>
                parsedText = parsedText.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");

                // Bullet points
                parsedText = parsedText.replaceAll("(?m)^\\s*[-–•*]\\s+", "&#8226; ");

                // Numbered lists
                parsedText = parsedText.replaceAll("(?m)^\\s*(\\d+)\\.\\s+", "<br><b>$1.</b> ");

                // Line breaks
                parsedText = parsedText.replaceAll("\n", "<br>");

                // Inline code (`code`)
                parsedText = parsedText.replaceAll("`(.*?)`", "<font color='#D81B60'><code>$1</code></font>");

                // Parse as HTML
                Spanned formattedText = Html.fromHtml(parsedText, Html.FROM_HTML_MODE_LEGACY);
                botHolder.textView.setText(formattedText);
            }

            botHolder.timeView.setText(timestamp);
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