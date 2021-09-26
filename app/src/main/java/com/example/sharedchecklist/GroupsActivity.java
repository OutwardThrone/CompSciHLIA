package com.example.sharedchecklist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GroupsActivity extends AppCompatActivity {

    private TextView introText;
    private Button manageFriends, manageChecklists, logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DatabaseHandler.handleSSLHandshake();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups);

        introText = (TextView) findViewById(R.id.introText);

        User user = ((UserApplication) this.getApplication()).getUser();

        String greeting = user.getFullname() + "'s Groups";
        introText.setText(greeting);

        manageFriends = (Button) findViewById(R.id.friendsButton);
        manageChecklists = (Button) findViewById(R.id.checklistsButton);
        logout = (Button) findViewById(R.id.logoutButton3);

        manageFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToFriends();
            }
        });

        manageChecklists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToChecklists();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToLogin();
            }
        });
    }

    private void sendToFriends() {
        Intent i = new Intent(this, FriendsActivity.class);
        startActivity(i);
    }

    private void sendToChecklists() {
        Intent i = new Intent(this, HomeActivity.class);
        startActivity(i);
    }

    private void sendToLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }
}