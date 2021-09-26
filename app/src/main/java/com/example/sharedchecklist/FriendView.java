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

import com.google.android.material.chip.Chip;

public class FriendView extends ConstraintLayout {

    private String fullname, username, email;
    private TextView usernameView;
    private Chip selected;

    public FriendView(@NonNull Context context, String username, String fullname, String email) {
        super(context);
        this.fullname = fullname;
        this.email = email;
        this.username = username;
        init(context);
    }

    public FriendView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.friend_view_layout, this, true);

        usernameView = (TextView) layout.findViewById(R.id.usernameView);

        usernameView.setText(username);

        selected = (Chip) layout.findViewById(R.id.friendViewChip);
        selected.setText(fullname);
    }

    public boolean isChosen() {
        return selected.isChecked();
    }

    public String getUsername() {
        return username;
    }
}
