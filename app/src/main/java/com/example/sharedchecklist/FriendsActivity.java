package com.example.sharedchecklist;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.w3c.dom.Text;

import java.util.Set;

public class FriendsActivity extends AppCompatActivity {

    private TextView introText;
    private Button sendFriendRequest, manageChecklists, manageGroups, logout;
    private User user;
    private LinkedLayoutList<FriendRequest> requestList;
    private LinkedLayoutList<Friend> friendsList;
    private TextView incomingView, friendsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DatabaseHandler.handleSSLHandshake();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        introText = (TextView) findViewById(R.id.introText);

        user = ((UserApplication) this.getApplication()).getUser();

        String greeting = user.getFullname() + "'s Friends";
        introText.setText(greeting);

        manageChecklists = (Button) findViewById(R.id.checklistsButton);
        manageGroups = (Button) findViewById(R.id.groupsButton);
        sendFriendRequest = (Button) findViewById(R.id.sendFriendReq);
        logout = (Button) findViewById(R.id.logoutButton2);

        incomingView = (TextView) findViewById(R.id.incomingReqView);
        friendsView = (TextView) findViewById(R.id.friendsView);

        requestList = (LinkedLayoutList<FriendRequest>) findViewById(R.id.friendRequestLinear);
        friendsList = (LinkedLayoutList<Friend>) findViewById(R.id.friendsLinear);

        retrieveRequests(user);
        retrieveFriends();

        manageChecklists.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToChecklists();
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

        sendFriendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateFriendReqDialog crd = new CreateFriendReqDialog(FriendsActivity.this);
                crd.show();
                crd.setFriendDialogCompleteListener(new CreateFriendReqDialog.FriendDialogCompleteListener() {
                    @Override
                    public void onDialogComplete(String friendUsername) {
                        sendRequest(friendUsername);
                    }
                });
            }
        });
    }

    private void addFriend(Friend f) {
        friendsList.addView(f);

        f.setFriendUpdateListener(new Friend.FriendUpdateListener() {
            @Override
            public void friendRemoved(String response) {
                runOnUiThread(() -> {
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);
                    Toast.makeText(getApplicationContext(), response, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void retrieveFriends() {
        friendsList.removeAllViews();
        DatabaseHandler dbhandler = DatabaseHandler.getInstance();
        Context ctx = getApplicationContext();
        AwaitVolleyResponse<String> awaitFriends = new AwaitVolleyResponse<>();

        dbhandler.getFriends(user.getUsername(), ctx, awaitFriends);

        Thread main = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (awaitFriends) {
                        while (!awaitFriends.hasGotResponse()) {
                            awaitFriends.wait();
                        }

                        String res = awaitFriends.getResponse();

                        if (res.substring(0, 7).equals("Success")) {
                            JsonElement extra = new JsonParser().parse(res.substring(8));
                            Set<String> friendIDs = extra.getAsJsonObject().keySet();
                            for (String friendID : friendIDs) {
                                JsonElement friendInfo = extra.getAsJsonObject().get(friendID).getAsJsonObject();
                                String fullname = friendInfo.getAsJsonObject().get("fullname").getAsString();
                                String email = friendInfo.getAsJsonObject().get("email").getAsString();
                                String username = friendInfo.getAsJsonObject().get("username").getAsString();

                                Friend f = new Friend(ctx, user, Integer.valueOf(friendID), username, fullname, email);

                                runOnUiThread(() -> addFriend(f));
                            }
                        } else {
                            runOnUiThread(() -> Toast.makeText(ctx, res, Toast.LENGTH_LONG).show());
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        };

        main.start();
    }

    private void addFriendRequest(FriendRequest fr) {
        requestList.addView(fr);

        fr.setRequestUpdateListener(new FriendRequest.RequestUpdateListener() {
            @Override
            public void requestAccepted(boolean wasAccepted) {
                DatabaseHandler dbhandler = DatabaseHandler.getInstance();
                Context ctx = getApplicationContext();
                AwaitVolleyResponse<String> awaitAcceptance = new AwaitVolleyResponse<>();

                dbhandler.acceptFriendRequest(fr.getSenderID(), user.getUsername(), wasAccepted, ctx, awaitAcceptance);

                Thread wait = new Thread() {
                    @Override
                    public void run() {
                        try {
                            synchronized (awaitAcceptance) {
                                while (!awaitAcceptance.hasGotResponse()) {
                                    awaitAcceptance.wait();
                                }

                                String res = awaitAcceptance.getResponse();

                                runOnUiThread(() -> {
                                    finish();
                                    overridePendingTransition(0, 0);
                                    startActivity(getIntent());
                                    overridePendingTransition(0, 0);
                                    Toast.makeText(ctx, res, Toast.LENGTH_SHORT).show();
                                });
                            }
                        } catch (InterruptedException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                };

                wait.start();
            }
        });

        requestList.invalidate();
    }

    private void retrieveRequests(User user) {
        requestList.removeAllViews();
        DatabaseHandler dbhandler = DatabaseHandler.getInstance();
        Context ctx = getApplicationContext();
        AwaitVolleyResponse<String> awaitGetRequests = new AwaitVolleyResponse<>();

        dbhandler.getFriendRequests(user.getUsername(), ctx, awaitGetRequests);

        Thread main = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (awaitGetRequests) {
                        while (!awaitGetRequests.hasGotResponse()) {
                            awaitGetRequests.wait();
                        }

                        String res = awaitGetRequests.getResponse();

                        if (res.substring(0, 7).equals("Success")) {
                            JsonElement extra = new JsonParser().parse(res.substring(8));
                            Set<String> senderIDs = extra.getAsJsonObject().keySet();
                            for (String senderID : senderIDs) {
                                String fName = extra.getAsJsonObject().get(senderID).getAsString();

                                FriendRequest fr = new FriendRequest(ctx, fName, Integer.valueOf(senderID));

                                runOnUiThread(() -> addFriendRequest(fr));
                            }
                        } else if (res.substring(0, 19).equals("No friend requests.")) {
                            runOnUiThread(() -> incomingView.setText("No Friend Requests"));
                        } else if (res.substring(0, 16).equals("Already Friends.")) {
                            runOnUiThread(() -> Toast.makeText(ctx, "Already Friends", Toast.LENGTH_SHORT).show());
                        } else {
                            runOnUiThread(() -> Toast.makeText(ctx, res, Toast.LENGTH_LONG).show());
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        };

        main.start();
    }

    private void sendRequest(String fName) {
        DatabaseHandler dbhandler = DatabaseHandler.getInstance();
        Context ctx = getApplicationContext();
        AwaitVolleyResponse<String> awaitRequest = new AwaitVolleyResponse<>();

        dbhandler.sendFriendRequest(user.getUsername(), fName, ctx, awaitRequest);

        Thread main = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (awaitRequest) {
                        while (!awaitRequest.hasGotResponse()) {
                            awaitRequest.wait();
                        }

                        String res = awaitRequest.getResponse();

                        runOnUiThread(() -> Toast.makeText(ctx, res, Toast.LENGTH_SHORT).show());
                        //TODO: refresh
                    }
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        };

        main.start();
    }

    private void sendToChecklists() {
        Intent i = new Intent(this, HomeActivity.class);
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