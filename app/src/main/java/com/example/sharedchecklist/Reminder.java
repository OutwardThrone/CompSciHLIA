package com.example.sharedchecklist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.renderscript.ScriptGroup;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.w3c.dom.Text;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Set;

public class Reminder extends ConstraintLayout {
    private String title, description;
    private boolean daily, weekly, completed;
    private Calendar date;
    private TextView titleView, desView, dateView;
    private Button delete;
    private CheckBox completeBox;
    private ReminderUpdateListener listener;
    private User user;
    private LinearLayout friendLayout;
    private Activity a;

    public Reminder(@NonNull Context context, Activity a, User user, String title, String description, String date, String daily, String weekly, String completed) { //booleans are 0 or 0, Date is in format YYYY-MM-DD
        super(context);
        this.title = title;
        this.description = description;
        this.daily = daily.equals("1");
        this.weekly = weekly.equals("1");
        this.completed = completed.equals("1");
        this.user = user;
        String[] dateComps = date.split("-");
        this.date = new GregorianCalendar();
        this.date.set(Integer.valueOf(dateComps[0]), Integer.valueOf(dateComps[1])-1, Integer.valueOf(dateComps[2]));

        this.a = a;

        init(context);
        //TODO: create linked list for reminder class that extends a list view possibly. Then add the instantiated list to xml and the whole reminder list should pop up
        // implement adding friends on the friends tab. Have friend dialog extend an abstract class which create reminder dialog extends
        // add friend option to create reminder dialog
        // when friend is added to reminder put it in the users_reminders table
    }

    public Reminder(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public interface ReminderUpdateListener {
        void onReminderUpdate(String response);
    }

    public void setReminderUpdateListener(ReminderUpdateListener listener) {
        this.listener = listener;
    }

    private void init(Context context) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.reminder_structure, this, true);

        titleView = (TextView) layout.findViewById(R.id.titleText);
        desView = (TextView) layout.findViewById(R.id.descriptionText);
        dateView = (TextView) layout.findViewById(R.id.dateView);
        completeBox = (CheckBox) layout.findViewById(R.id.completeBox);
        delete = (Button) layout.findViewById(R.id.deleteButton);
        friendLayout = (LinearLayout) layout.findViewById(R.id.horizontalLayout);

        titleView.setText(title);
        desView.setText(description);
        dateView.setText("" + this.date.getDisplayName(Calendar.MONTH, Calendar.SHORT_FORMAT, Locale.US) + " " + this.date.get(Calendar.DAY_OF_MONTH) + ", " + this.date.get(Calendar.YEAR));

        getFriendsOnReminder();

        completeBox.setChecked(completed);
        completeBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                DatabaseHandler dbhandler = DatabaseHandler.getInstance();
                AwaitVolleyResponse<String> awaitUpdate = new AwaitVolleyResponse<>();
                dbhandler.updateCompleted(user.getUsername(), title, description, isChecked, getContext(), awaitUpdate);

                Thread wait = new Thread() {
                    @Override
                    public void run() {
                        try {
                            synchronized (awaitUpdate) {
                                while (!awaitUpdate.hasGotResponse()) {
                                    awaitUpdate.wait();
                                }
                                String res = awaitUpdate.getResponse();
                                System.out.println("response came back");
                                listener.onReminderUpdate("Marked " + title + " as " + (isChecked ? "complete" : "incomplete"));
                            }
                        } catch (InterruptedException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                };
                wait.start();
            }
        });

        delete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHandler dbhandler = DatabaseHandler.getInstance();
                AwaitVolleyResponse<String> awaitDelete = new AwaitVolleyResponse<>();
                dbhandler.deleteReminder(user.getUsername(), title, description, getContext(), awaitDelete);

                Thread wait = new Thread() {
                    @Override
                    public void run() {
                        try {
                            synchronized (awaitDelete) {
                                while (!awaitDelete.hasGotResponse()) {
                                    awaitDelete.wait();
                                }
                                String res = awaitDelete.getResponse();
                                listener.onReminderUpdate("Deleted: " + title);
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

    private void addFriendOnReminder(String username, String fullname, boolean isOwner, boolean completed) {
        Switch s = new Switch(getContext());
        s.setText(fullname + (isOwner ? " - Owner" : ""));
        s.setClickable(false);

        a.runOnUiThread(() -> {
            s.setChecked(completed);
            friendLayout.addView(s);
        });
    }

    private void getFriendsOnReminder() {
        DatabaseHandler dbhandler = DatabaseHandler.getInstance();
        AwaitVolleyResponse<String> awaitFriendCheck = new AwaitVolleyResponse<>();
        Context ctx = getContext();

        dbhandler.getFriendsOnReminder(user.getUsername(), title, description, ctx, awaitFriendCheck);

        Thread wait = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (awaitFriendCheck) {
                        while (!awaitFriendCheck.hasGotResponse()) {
                            awaitFriendCheck.wait();
                        }
                        String res = awaitFriendCheck.getResponse();

                        if (res.substring(0, 7).equals("Success")) {
                            JsonElement extra = new JsonParser().parse(res.substring(8));
                            Set<String> usernames = extra.getAsJsonObject().keySet();

                            for (String username : usernames) {
                                boolean isOwner = extra.getAsJsonObject().get(username).getAsJsonObject().get("isOwner").getAsString().equals("1");
                                boolean completed = extra.getAsJsonObject().get(username).getAsJsonObject().get("completed").getAsString().equals("1");
                                String fullname = extra.getAsJsonObject().get(username).getAsJsonObject().get("fullname").getAsString();

                                addFriendOnReminder(username, fullname, isOwner, completed);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        };
        wait.start();
    }

    public String toString() {
        return "Reminder: " + title + " || " + description;
    }
}
