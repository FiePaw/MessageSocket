package com.example.message;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {
    private Socket mSocket;
    private EditText edtMessage;
    private Button btnSend;
    private RecyclerView rvUsers, rvMessages;
    private UserAdapter userAdapter;
    private MessageAdapter messageAdapter;
    private ArrayList<String> onlineUsers = new ArrayList<>();
    private ArrayList<String> messages = new ArrayList<>();
    private String currentUser = "android_user_" + System.currentTimeMillis() % 1000;
    private String selectedUser = "";
    private TextView tvSelectedUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI
        edtMessage = findViewById(R.id.edtMessage);
        btnSend = findViewById(R.id.btnSend);
        rvUsers = findViewById(R.id.rvUsers);
        rvMessages = findViewById(R.id.rvMessages);
        tvSelectedUser = findViewById(R.id.tvSelectedUser);

        // Setup RecyclerViews
        setupAdapters();

        // Connect to Socket.IO server
        connectSocket();

        // Set click listeners
        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void setupAdapters() {
        userAdapter = new UserAdapter(onlineUsers, position -> {
            selectedUser = onlineUsers.get(position);
            tvSelectedUser.setText("Chat dengan: " + selectedUser);
            userAdapter.setSelectedPosition(position);
        });

        rvUsers.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvUsers.setAdapter(userAdapter);

        messageAdapter = new MessageAdapter(messages);
        rvMessages.setLayoutManager(new LinearLayoutManager(this));
        rvMessages.setAdapter(messageAdapter);
    }

    private void connectSocket() {
        try {
            IO.Options options = new IO.Options();
            options.reconnection = true;
            options.timeout = 5000;

            mSocket = IO.socket("http://64.235.61.125:46058", options);
            setupSocketEvents();
            mSocket.connect();
        } catch (Exception e) {
            Toast.makeText(this, "Connection error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void setupSocketEvents() {
        mSocket.on(Socket.EVENT_CONNECT, args -> runOnUiThread(() -> {
            Toast.makeText(MainActivity.this, "Connected to server", Toast.LENGTH_SHORT).show();
            mSocket.emit("register", currentUser);
        }));

        mSocket.on("user_list", args -> runOnUiThread(() -> {
            try {
                JSONArray users = (JSONArray) args[0];
                onlineUsers.clear();
                for (int i = 0; i < users.length(); i++) {
                    String user = users.getString(i);
                    if (!user.equals(currentUser)) {
                        onlineUsers.add(user);
                    }
                }
                userAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                Log.e("SocketIO", "Error parsing user list", e);
            }
        }));

        mSocket.on("incoming_message", args -> runOnUiThread(() -> {
            try {
                JSONObject data = (JSONObject) args[0];
                String from = data.getString("from");
                String msg = data.getString("message");

                messages.add(from + ": " + msg);
                messageAdapter.notifyItemInserted(messages.size() - 1);
                rvMessages.smoothScrollToPosition(messages.size() - 1);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }));

        mSocket.on(Socket.EVENT_DISCONNECT, args -> runOnUiThread(() ->
                Toast.makeText(MainActivity.this, "Disconnected", Toast.LENGTH_SHORT).show()));
    }

    private void sendMessage() {
        String message = edtMessage.getText().toString().trim();
        if (message.isEmpty()) return;

        if (selectedUser.isEmpty()) {
            Toast.makeText(this, "Pilih user terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject data = new JSONObject();
            data.put("from", currentUser);
            data.put("to", selectedUser);
            data.put("message", message);

            mSocket.emit("send_to_pc", data);
            messages.add("Anda: " + message);
            messageAdapter.notifyItemInserted(messages.size() - 1);
            edtMessage.setText("");
            rvMessages.smoothScrollToPosition(messages.size() - 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket.off();
        }
    }
}