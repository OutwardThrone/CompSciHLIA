package com.example.sharedchecklist;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText username, password;
    private Button login, signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DatabaseHandler.handleSSLHandshake();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (EditText) findViewById(R.id.editTextUsername);
        password = (EditText) findViewById(R.id.editTextPassword);
        login = (Button) findViewById(R.id.logInButton);
        signup = (Button) findViewById(R.id.signUpButton);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String susername, spassword;
                susername = String.valueOf(username.getText());
                spassword = String.valueOf(password.getText());

                if (!susername.equals("") && !spassword.equals("")) {
                    logUserIn(susername, spassword);
                } else {
                    Toast.makeText(getApplicationContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToSignUp();
            }
        });
    }

    private void logUserIn(String username, String password) {
        //show loading
        DatabaseHandler dbhandler = DatabaseHandler.getInstance();
        AwaitVolleyResponse<String> awaitLogin = new AwaitVolleyResponse<>();
        Context ctx = getApplicationContext();

        dbhandler.logUserIn(username, password, getApplicationContext(), awaitLogin);

        Thread main = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (awaitLogin) {
                        while (!awaitLogin.hasGotResponse()) {
                            awaitLogin.wait();
                        }
                        //hide loading
                        final String res = awaitLogin.getResponse();
                       // System.out.println(res);
                        if (res.substring(0, 13).equals("Login Success")) {
                            JsonElement extra = new JsonParser().parse(res.substring(14));
                            String fullname, email;
                            fullname = extra.getAsJsonObject().get("fullname").getAsString();
                            email = extra.getAsJsonObject().get("email").getAsString();
                            runOnUiThread(() -> Toast.makeText(ctx, "Successful login", Toast.LENGTH_SHORT).show());
                            sendToHomePage(username, password, email, fullname);
                        } else if (res.equals("Username or Password Wrong")) {
                            runOnUiThread(() -> Toast.makeText(ctx, "Incorrect username or password. Try again", Toast.LENGTH_LONG).show());
                        } else {
                            runOnUiThread(() -> Toast.makeText(ctx, "Error logging in. "  + res, Toast.LENGTH_LONG).show());
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println(e.getMessage());
                    System.out.println("Login await interrupted");
                }
            }
        };

        main.start();
    }

    private void sendToHomePage(String username, String password, String email, String fullname) {
        Intent i = new Intent(this, HomeActivity.class);

        User user = new User(username, password, email, fullname);

        ((UserApplication) this.getApplication()).setUser(user);

        startActivity(i);
    }

    private void sendToSignUp() {
        Intent i = new Intent(this, SigninActivity.class);
        startActivity(i);
    }
}