package com.example.sharedchecklist;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SigninActivity extends AppCompatActivity {

    private EditText username, password, email, fullname;
    private Button login, signin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DatabaseHandler.handleSSLHandshake();

        //https://www.youtube.com/watch?v=X8oD4q3XtQQ&t=155s
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);

        username = (EditText) findViewById(R.id.signUpUsername);
        email = (EditText) findViewById(R.id.signUpEmail);
        password = (EditText) findViewById(R.id.signUpPassword);
        fullname = (EditText) findViewById(R.id.signUpFullName);

        login = (Button) findViewById(R.id.logInButton);
        signin = (Button) findViewById(R.id.signUpButton);

        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String susername, spassword, semail, sfullname;
                susername = String.valueOf(username.getText());
                spassword = String.valueOf(password.getText());
                semail = String.valueOf(email.getText());
                sfullname = String.valueOf(fullname.getText());

                if (!susername.equals("") && !spassword.equals("") && !semail.equals("") && !sfullname.equals("")) {
                    createNewAccount(susername, spassword, semail, sfullname);
                } else {
                    Toast.makeText(getApplicationContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToLogIn();
            }
        });
    }

    public Object searchLock = new Object();
    private void searchAPI() {
        Thread main = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (searchLock) {
                        //blah
                        searchLock.wait();
                    }
                } catch(InterruptedException e) {
                    System.out.println(e.getMessage());
                }
            }
        };

        main.start();
    }


    private void fetch() {
        Thread apiThread = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (searchLock) {
                        searchLock.notify();
                    }
                } catch(Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        };

        apiThread.start();
    }

    private void createNewAccount(String username, String password, String email, String fullname) {
        //show loading
        DatabaseHandler dbhandler = DatabaseHandler.getInstance();
        AwaitVolleyResponse<String> awaitSignin = new AwaitVolleyResponse<>();
        Context ctx = getApplicationContext();

        dbhandler.createNewUser(username, password, email, fullname, ctx, awaitSignin);

        Thread main = new Thread() {
            @Override
            public void run() {
                try {
                    synchronized (awaitSignin) {
                        while (!awaitSignin.hasGotResponse()) {
                            awaitSignin.wait();
                        }
                        //hide loading
                        final String res = awaitSignin.getResponse();
                       // System.out.println(res);
                        switch (res) {
                            case "Sign Up Success":
                                runOnUiThread(() -> Toast.makeText(ctx, "Successful sign up, please login", Toast.LENGTH_SHORT).show());
                                sendToLogIn();
                                break;
                            default:
                                runOnUiThread(() -> Toast.makeText(ctx, "Error signing in. " + res, Toast.LENGTH_LONG).show());
                                break;
                        }
                    }
                } catch (InterruptedException e) { //interrupted exception
                    System.out.println(e.getMessage());
                    System.out.println("Signup await interrupted");
                }

            }
        };

        main.start();

    }

    private void sendToLogIn() {
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }
}