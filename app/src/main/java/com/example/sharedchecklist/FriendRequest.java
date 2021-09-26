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

public class FriendRequest extends ConstraintLayout {

    private String fName;
    private Button accept, decline;
    private RequestUpdateListener listener;
    private TextView nameDiplay;
    private int fID;

    public FriendRequest(@NonNull Context context, String friendUsername, int friendID) {
        super(context);
        this.fName = friendUsername;
        this.fID = friendID;
        init(context);
    }

    public FriendRequest(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.friend_request_layout, this, true);

        accept = (Button) layout.findViewById(R.id.acceptButton);
        decline = (Button) layout.findViewById(R.id.declineButton);

        nameDiplay = (TextView) layout.findViewById(R.id.friendUsername);
        nameDiplay.setText(fName);

        accept.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.requestAccepted(true);
            }
        });

        decline.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.requestAccepted(false);
            }
        });
    }

    public interface RequestUpdateListener {
        void requestAccepted(boolean wasAccepted);
    }

    public int getSenderID() {
        return fID;
    }

    public String getSenderUsername() {
        return fName;
    }

    public void setRequestUpdateListener(RequestUpdateListener listener) {
        this.listener = listener;
    }
}
