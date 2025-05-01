package com.example.message;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.function.Consumer;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private final ArrayList<String> users;
    private final Consumer<Integer> onUserClick;
    private int selectedPosition = -1;

    public UserAdapter(ArrayList<String> users, Consumer<Integer> onUserClick) {
        this.users = users;
        this.onUserClick = onUserClick;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(users.get(position), position == selectedPosition);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public void setSelectedPosition(int position) {
        int prevSelected = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(prevSelected);
        notifyItemChanged(selectedPosition);
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUsername;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUsername = itemView.findViewById(R.id.tvUsername);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    setSelectedPosition(position);
                    onUserClick.accept(position);
                }
            });
        }

        public void bind(String username, boolean isSelected) {
            tvUsername.setText(username);

            if (isSelected) {
                itemView.setBackgroundResource(R.drawable.bg_user_selected);
                tvUsername.setTextColor(Color.WHITE);
            } else {
                itemView.setBackgroundResource(R.drawable.bg_user_normal);
                tvUsername.setTextColor(Color.BLACK);
            }
        }
    }
}