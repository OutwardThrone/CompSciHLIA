package com.example.sharedchecklist;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    private TextView introText;
    private Button createReminder, manageFriends, manageGroups, logout;
    private User user;
    private LinkedLayoutList<Reminder> reminderList;
    private RadioButton refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DatabaseHandler.handleSSLHandshake();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        introText = (TextView) findViewById(R.id.introText);

        user = ((UserApplication) this.getApplication()).getUser();

        String greeting = user.getFullname() + "'s Checklists";
        introText.setText(greeting);

        manageFriends = (Button) findViewById(R.id.friendsButton);
        manageGroups = (Button) findViewById(R.id.groupsButton);
        createReminder = (Button) findViewById(R.id.createReminderButton);
        logout = (Button) findViewById(R.id.logoutButton);
        refresh = (RadioButton) findViewById(R.id.refreshButton);
        refresh.setVisibility(View.GONE);
        reminderList = (LinkedLayoutList<Reminder>) findViewById(R.id.linearLayout);

        retrieveReminders(user);

        manageFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToFriends();
            }
        });

        manageGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToGroups();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToLogin();
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(() -> {
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                });
                refresh.setChecked(false);
            }
        });

        createReminder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateReminderDialog crd = new CreateReminderDialog(HomeActivity.this);
                crd.show();
                crd.setDialogCompleteListener(new CreateReminderDialog.DialogCompleteListener() {
                    @Override
                    public void onDialogueClose(String title, String des, int month, int day, int year, boolean weekly, boolean daily, ArrayList<String> chosenFriends) {
                        //add to db
                        AwaitVolleyResponse<String> awaitReminder = new AwaitVolleyResponse<>();
                        uploadReminder(title, des, year, month, day, weekly, daily, awaitReminder, chosenFriends);
                        //add friends to reminder{

                        Thread wait = new Thread() {
                            @Override
                            public void run() {
                                try {
                                    synchronized (awaitReminder) {
                                        while (!awaitReminder.hasGotResponse()) {
                                            awaitReminder.wait();
                                            System.out.println("waitin");
                                        }
                                        System.out.println(chosenFriends);
                                        for (String friend : chosenFriends) {
                                            addFriendToReminder(friend, title, des);
                                        }
                                    }
                                } catch (InterruptedException e) {
                                    System.out.println(e.getMessage());
                                }
                            }
                        };
                        wait.start();

                    }
                });
            }
        });
    }

    private void addFriendToReminder(String friendUsername, String title, String description) {
        System.out.println("going to add " + friendUsername);
        DatabaseHandler dbhandler = DatabaseHandler.getInstance();
        AwaitVolleyResponse<String> awaitAddingFriend = new AwaitVolleyResponse<>();
        Context ctx = getApplicationContext();

        dbhandler.addFriendToReminder(friendUsername, title, description, ctx, awaitAddingFriend);

        Thread addFriend = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (awaitAddingFriend) {
                        while (!awaitAddingFriend.hasGotResponse()) {
                            awaitAddingFriend.wait();
                        }

                        String res = awaitAddingFriend.getResponse();
                        runOnUiThread(() -> Toast.makeText(ctx, res, Toast.LENGTH_SHORT).show());
                    }
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        };
        addFriend.start();
    }

    private void addReminder(Reminder r) {
        reminderList.addView(r);
        r.setReminderUpdateListener(new Reminder.ReminderUpdateListener() {
            @Override
            public void onReminderUpdate(String response) {
                runOnUiThread(() -> {
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                });
            }
        });
        reminderList.invalidate();
    }

    private void retrieveReminders(User user) {
        reminderList.removeAllViews();
        DatabaseHandler dbhandler = DatabaseHandler.getInstance();
        Context ctx = getApplicationContext();
        AwaitVolleyResponse<String> awaitReminders = new AwaitVolleyResponse<>();

        dbhandler.getReminders(user.getUsername(), ctx, awaitReminders);

        Thread main = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (awaitReminders) {
                        while (!awaitReminders.hasGotResponse()) {
                            awaitReminders.wait();
                        }

                        final String res = awaitReminders.getResponse();

                        if (res.substring(0, 7).equals("Success")) {
                            JsonElement extra = new JsonParser().parse(res.substring(8));
                            int i = 0;
                            JsonElement rem;
                            while ((rem = extra.getAsJsonObject().get(String.valueOf(i))) != null) {
                                i++;
                                String title = rem.getAsJsonObject().get("title").getAsString();
                                String des = rem.getAsJsonObject().get("description").getAsString();
                                String date = rem.getAsJsonObject().get("date").getAsString();
                                String weekly = rem.getAsJsonObject().get("weekly").getAsString();
                                String daily = rem.getAsJsonObject().get("daily").getAsString();
                                String completed = rem.getAsJsonObject().get("completed").getAsString();
                                //System.out.println(title + " " + des + " " + date + " " + weekly + " " + daily + " " + completed);

                                Reminder r = new Reminder(ctx, HomeActivity.this, user, title, des, date, weekly, daily, completed);

                                runOnUiThread(() -> addReminder(r));
                            }
                        } else {
                            runOnUiThread(() -> Toast.makeText(ctx, "Error retrieving reminders", Toast.LENGTH_LONG).show());
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        };

        main.start();
    }

    private void uploadReminder(String title, String des, int year, int month, int day, boolean weekly, boolean daily, AwaitVolleyResponse<String> awaitReminder, ArrayList<String> chosenFriends) {
        DatabaseHandler dbhandler = DatabaseHandler.getInstance();
        //AwaitVolleyResponse<String> awaitReminder = new AwaitVolleyResponse<>();
        Context ctx = getApplicationContext();

        dbhandler.createNewReminder(user.getUsername(), title, des, year, month, day, weekly, daily, ctx, awaitReminder);

        Thread main = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (awaitReminder) {
                        while (!awaitReminder.hasGotResponse()) {
                            awaitReminder.wait();
                        }

                        final String res = awaitReminder.getResponse();

                        switch (res) {
                            case "Added Reminder Successfully":
                                runOnUiThread(() -> {
                                    Toast.makeText(ctx, "Created reminder!", Toast.LENGTH_SHORT).show();

                                    retrieveReminders(user);
                                });
                                //refresh
                                break;
                            default:
                                runOnUiThread(()-> Toast.makeText(ctx,"Error creating reminder. " + res, Toast.LENGTH_LONG).show());
                                break;
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        };

        main.start();
    }

    private void sendToFriends() {
        Intent i = new Intent(this, FriendsActivity.class);
        startActivity(i);
    }

    private void sendToGroups() {
        Intent i = new Intent(this, GroupsActivity.class);
        startActivity(i);
    }

    private void sendToLogin() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }
}