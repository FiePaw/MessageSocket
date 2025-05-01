package com.example.message;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private final ArrayList<String> messages;

    public MessageAdapter(ArrayList<String> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        holder.bind(messages.get(position));
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvMessage;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(android.R.id.text1);
        }

        public void bind(String message) {
            tvMessage.setText(message);

            // Atur tampilan berdasarkan pengirim
            if (message.startsWith("Anda:")) {
                tvMessage.setTextColor(0xFF388E3C); // Hijau untuk pesan sendiri
                tvMessage.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_END);
            } else {
                tvMessage.setTextColor(0xFF1976D2); // Biru untuk pesan lain
                tvMessage.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            }
        }
    }
}