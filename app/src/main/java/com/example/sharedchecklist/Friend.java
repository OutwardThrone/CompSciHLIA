package com.example.sharedchecklist;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

public class Friend extends ConstraintLayout {

    private String fullname, username, email;
    private TextView usernameView, fullnameView;
    private int id;
    private Button removeFriend;
    private FriendUpdateListener listener;
    private User user;

    public Friend(@NonNull Context context, User user, int id, String username, String fullname, String email) {
        super(context);
        this.fullname = fullname;
        this.email = email;
        this.username = username;
        this.id = id;
        this.user = user;
        init(context);
    }

    public Friend(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.friend_layout, this, true);

        usernameView = (TextView) layout.findViewById(R.id.usernameView);
        fullnameView = (TextView) layout.findViewById(R.id.fullnameView);
        removeFriend = (Button) layout.findViewById(R.id.removeFriendButton);

        usernameView.setText(username);
        fullnameView.setText(fullname);

        removeFriend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHandler dbhandler = DatabaseHandler.getInstance();
                AwaitVolleyResponse<String> awaitRemoval = new AwaitVolleyResponse<>();
                dbhandler.removeFriend(user.getUsername(), id, context, awaitRemoval);

                Thread wait = new Thread() {
                    @Override
                    public void run() {
                        try {
                            synchronized (awaitRemoval) {
                                while (!awaitRemoval.hasGotResponse()) {
                                    awaitRemoval.wait();
                                }
                                String res = awaitRemoval.getResponse();
                                listener.friendRemoved(res);
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

    public interface FriendUpdateListener {
        void friendRemoved(String response);
    }

    public void setFriendUpdateListener(FriendUpdateListener listener) {
        this.listener = listener;
    }
}
