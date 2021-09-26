package com.example.sharedchecklist;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CreateFriendReqDialog extends Dialog {

    private Activity a;
    private FriendDialogCompleteListener listener;
    private EditText friendUsername;
    private Button cancel, send;

    public CreateFriendReqDialog(Activity a) {
        super(a);
        this.a = a;
    }

    public void setFriendDialogCompleteListener(FriendDialogCompleteListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.friend_request);

        friendUsername = (EditText) findViewById(R.id.editFriendUsername);
        cancel = (Button) findViewById(R.id.frcancelButton);
        send = (Button) findViewById(R.id.frcreateButton);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String fName;
                fName = String.valueOf(friendUsername.getText());

                if (fName.equals("")) {
                    Toast.makeText(getContext(), "Enter a friend's username", Toast.LENGTH_SHORT).show();
                } else {
                    listener.onDialogComplete(fName);
                    dismiss();
                }
            }
        });
    }

    public interface FriendDialogCompleteListener {
        void onDialogComplete(String friendUsername);
    }
}
