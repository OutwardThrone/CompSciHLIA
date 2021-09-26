package com.example.sharedchecklist;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.location.GnssAntennaInfo;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

public class CreateReminderDialog extends Dialog  {

    private EditText title, des;
    private Button cancel, create;
    private Activity a;
    private DialogCompleteListener listener;
    private DatePicker datePicker;
    private static Date today;
    private Switch weekly, daily;
    private LinkedLayoutList<FriendView> friendsList;

    public CreateReminderDialog(Activity a) {
        super(a);
        this.a = a;
        Calendar.getInstance().clear();
        today = Calendar.getInstance().getTime();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.create_reminder);

        title = (EditText) findViewById(R.id.editTitle);
        des = (EditText) findViewById(R.id.editDescription);

        cancel = (Button) findViewById(R.id.cancelButton);
        create = (Button) findViewById(R.id.createButton);
        datePicker = (DatePicker) findViewById(R.id.datePicker);

        weekly = (Switch) findViewById(R.id.weeklySwitch);
        daily = (Switch) findViewById(R.id.dailySwitch);

        friendsList = (LinkedLayoutList<FriendView>) findViewById(R.id.friendsLinear);

        retrieveFriends();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String stitle, sdes;
                stitle = String.valueOf(title.getText());
                sdes = String.valueOf(des.getText());

                boolean bweekly = weekly.isChecked();
                boolean bdaily = daily.isChecked();

                int year, month, day;
                year = datePicker.getYear();
                month = datePicker.getMonth();
                day = datePicker.getDayOfMonth();
                Calendar setDate = new GregorianCalendar();
                setDate.set(year, month, day);

                ArrayList<String> chosenFriends = new ArrayList<>();
                for (int i = 0; i < friendsList.size(); i++) {
                    FriendView f = friendsList.get(i);
                    if (f.isChosen()) {
                        chosenFriends.add(f.getUsername());
                    }
                }

                if (stitle.equals("")) {
                    Toast.makeText(getContext(), "Title is required", Toast.LENGTH_SHORT).show();
                } else if (today.after(setDate.getTime())) {
                    Toast.makeText(getContext(), "Enter a future date", Toast.LENGTH_SHORT).show();
                } else {
                    listener.onDialogueClose(stitle, sdes, month, day, year, bweekly, bdaily, chosenFriends);
                    dismiss();
                }
            }
        });
    }

    public interface DialogCompleteListener {
        void onDialogueClose(String title, String des, int month, int day, int year, boolean weekly, boolean daily, ArrayList<String> chosenFriends);
    }

    public void setDialogCompleteListener(DialogCompleteListener listener) {
        this.listener = listener;
    }

    private void retrieveFriends() {
        friendsList.removeAllViews();
        DatabaseHandler dbhandler = DatabaseHandler.getInstance();
        Context ctx = getContext();
        AwaitVolleyResponse<String> awaitFriends = new AwaitVolleyResponse<>();

        dbhandler.getFriends(((UserApplication)a.getApplication()).getUser().getUsername(), ctx, awaitFriends);

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

                                FriendView f = new FriendView(ctx, username, fullname, email);

                                a.runOnUiThread(() -> addFriend(f));
                            }
                        } else {
                            a.runOnUiThread(() -> Toast.makeText(ctx, res, Toast.LENGTH_LONG).show());
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        };

        main.start();
    }
    private void addFriend(FriendView f) {
        friendsList.addView(f);
    }

}
