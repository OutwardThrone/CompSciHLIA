package com.example.sharedchecklist;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class DatabaseHandler {



    private enum URLs {
        SIGNUP("https://10.0.0.47/LoginRegister/signup.php"),
        LOGIN("https://10.0.0.47/LoginRegister/login.php"),
        SET_REMINDER("https://10.0.0.47/LoginRegister/addreminder.php"),
        GET_REMINDER("https://10.0.0.47/LoginRegister/getreminders.php"),
        UPDATE_COMPLETED("https://10.0.0.47/LoginRegister/updatecompleted.php"),
        DELETE_REMINDER("https://10.0.0.47/LoginRegister/deletereminder.php"),
        FRIEND_REQUEST("https://10.0.0.47/LoginRegister/friendrequest.php"),
        GET_FRIEND_REQUESTS("https://10.0.0.47/LoginRegister/getfriendrequests.php"),
        ACCEPT_FRIEND_REQUEST("https://10.0.0.47/LoginRegister/acceptfriendrequest.php"),
        GET_FRIENDS("https://10.0.0.47/LoginRegister/getfriends.php"),
        REMOVE_FRIEND("https://10.0.0.47/LoginRegister/removefriend.php"),
        ADD_FRIEND_TO_REMINDER("https://10.0.0.47/LoginRegister/addfriendtoreminder.php"),
        GET_FRIENDS_ON_REMINDER("https://10.0.0.47/LoginRegister/getfriendsonreminder.php");

        private String url;
        URLs(String url) {
            this.url = url;
        }
        public String getUrl() {
            return url;
        }
    }

    private static DatabaseHandler handler;
    public static DatabaseHandler getInstance() {
        if (handler == null) {
            handler = new DatabaseHandler();
        }
        return handler;
    }

    private DatabaseHandler() {
    }

    private String prepBool(boolean b) {
        return b ? "1" : "0"; // 1 is true, 0 is false
    }

    public void getReminders(String username, Context context, AwaitVolleyResponse<String> awaitReminder) {
        String[] fields = {"username"};
        String[] values = {username};
        VolleyRequest vr = new VolleyRequest(arrayToJSON(fields, values), context, URLs.GET_REMINDER.getUrl(), awaitReminder);
        vr.start();
    }

    public void getFriends(String username, Context context, AwaitVolleyResponse<String> awaitFriends) {
        String[] fields = {"username"};
        String[] values = {username};
        VolleyRequest vr = new VolleyRequest(arrayToJSON(fields, values), context, URLs.GET_FRIENDS.getUrl(), awaitFriends);
        vr.start();
    }

    public void removeFriend(String username, int friendID, Context context, AwaitVolleyResponse<String> awaitRemoval) {
        String[] fields = {"username", "friendid"};
        String[] values = {username, String.valueOf(friendID)};
        VolleyRequest vr = new VolleyRequest(arrayToJSON(fields, values), context, URLs.REMOVE_FRIEND.getUrl(), awaitRemoval);
        vr.start();
    }

    public void addFriendToReminder(String friendUsername, String remTitle, String remDes, Context context, AwaitVolleyResponse<String> awaitAddingFriend) {
        String[] fields = {"friendusername", "title", "description"};
        String[] values = {friendUsername, remTitle, remDes};
        VolleyRequest vr = new VolleyRequest(arrayToJSON(fields, values), context, URLs.ADD_FRIEND_TO_REMINDER.getUrl(), awaitAddingFriend);
        vr.start();
    }

    public void getFriendsOnReminder(String username, String title, String description, Context context, AwaitVolleyResponse<String> awaitFriendCheck) {
        String[] fields = {"username", "title", "description"};
        String[] values = {username, title, description};
        VolleyRequest vr = new VolleyRequest(arrayToJSON(fields, values), context, URLs.GET_FRIENDS_ON_REMINDER.getUrl(), awaitFriendCheck);
        vr.start();
    }

    public void getFriendRequests(String username, Context context, AwaitVolleyResponse<String> awaitGetRequests) {
        String[] fields = {"username"};
        String[] values = {username};
        VolleyRequest vr = new VolleyRequest(arrayToJSON(fields, values), context, URLs.GET_FRIEND_REQUESTS.getUrl(), awaitGetRequests);
        vr.start();
    }

    public void acceptFriendRequest(int senderID, String friendName, boolean wasAccepted, Context context, AwaitVolleyResponse<String> awaitAcceptance) {
        acceptFriendRequest(String.valueOf(senderID), friendName, wasAccepted, context, awaitAcceptance);
    }

    public void acceptFriendRequest(String senderID, String friendName, boolean wasAccepted, Context context, AwaitVolleyResponse<String> awaitAcceptance) {
        String[] fields = {"senderid", "friendname", "wasaccepted"};
        String[] values = {senderID, friendName, prepBool(wasAccepted)};
        VolleyRequest vr = new VolleyRequest(arrayToJSON(fields, values), context, URLs.ACCEPT_FRIEND_REQUEST.getUrl(), awaitAcceptance);
        vr.start();
    }

    public void sendFriendRequest(String username, String friendUsername, Context context, AwaitVolleyResponse<String> awaitRequest) {
        String[] fields = {"username", "friendname"};
        String[] values = {username, friendUsername};
        VolleyRequest vr = new VolleyRequest(arrayToJSON(fields, values), context, URLs.FRIEND_REQUEST.getUrl(), awaitRequest);
        vr.start();
    }

    public void logUserIn(String username, String password, Context context, AwaitVolleyResponse<String> awaitLogin) {
        String[] fields = {"username", "password"};
        String[] values = {username, password};
        VolleyRequest vr = new VolleyRequest(arrayToJSON(fields, values), context, URLs.LOGIN.getUrl(), awaitLogin);
        vr.start();
    }

    public void updateCompleted(String username, String title, String description, boolean completed, Context context, AwaitVolleyResponse<String> awaitUpdate) {
        String[] fields = {"username", "title", "description", "completed"};
        String[] values = {username, title, description, prepBool(completed)};
        VolleyRequest vr = new VolleyRequest(arrayToJSON(fields, values), context, URLs.UPDATE_COMPLETED.getUrl(), awaitUpdate);
        vr.start();
    }

    public void createNewUser(String username, String password, String email, String fullname, Context context, AwaitVolleyResponse<String> awaitSignin) {
        String[] fields = {"username", "password", "email", "fullname"};
        String[] values = {username, password, email, fullname};
        //volleyRequest(arrayToJSON(fields, values), context, awaitSignin);
        VolleyRequest vr = new VolleyRequest(arrayToJSON(fields, values), context, URLs.SIGNUP.getUrl(), awaitSignin);
        vr.start();
    }

    public void deleteReminder(String username, String title, String description, Context context, AwaitVolleyResponse<String> awaitDelete) {
        String[] fields = {"username", "title", "description"};
        String[] values = {username, title, description};
        VolleyRequest vr = new VolleyRequest(arrayToJSON(fields, values), context, URLs.DELETE_REMINDER.getUrl(), awaitDelete);
        vr.start();
    }

    private String formatDate(int year, int month, int day) {
        return year + "-" + month + "-" + day;
    }

    public void createNewReminder(String username, String title, String description, int year, int month, int day, boolean weekly, boolean daily, Context context, AwaitVolleyResponse<String> awaitReminder) {
        String[] fields = {"username", "title", "description", "date", "weekly", "daily"};
        String[] values = {username, title, description, formatDate(year, month+1, day), prepBool(weekly), prepBool(daily)};

        VolleyRequest vr = new VolleyRequest(arrayToJSON(fields, values), context, URLs.SET_REMINDER.getUrl(), awaitReminder);
        vr.start();
    }

    private JSONObject arrayToJSON(String[] fields, String[] values) {
        if (fields.length == values.length) {
            JSONObject data = new JSONObject();
            try {
                for (int i = 0; i < fields.length; i++) {
                    data.put(fields[i], values[i]);
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
                System.out.println("JSON data making error");
            }
            System.out.println(data.toString());
            return data;
        } else {
            System.out.println("Field and value arrays not same size");
            return null;
        }
    }

    private class VolleyRequest extends Thread {

        private JSONObject data;
        private Context ctx;
        private AwaitVolleyResponse<String> awaitResponse;
        private String URL;

        private VolleyRequest(JSONObject data, Context context, String URL, AwaitVolleyResponse<String> awaitResponse) {
            this.data = data;
            this.ctx = context;
            this.awaitResponse = awaitResponse;
            this.URL = URL;
        }

        private void volleyRequest() {
            System.out.println("beginning volley request");
            RequestQueue queue = Volley.newRequestQueue(this.ctx);
            StringRequest strReq = new StringRequest(Request.Method.POST, this.URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    synchronized (awaitResponse) {
                        System.out.println("Volley " + response);
                        awaitResponse.setResponse(response);
                        awaitResponse.notifyAll();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    synchronized (awaitResponse) {
                        System.out.println("VolleyError " + error.toString());
                        awaitResponse.setResponse(error.toString());
                        awaitResponse.notifyAll();
                    }
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    Iterator<String> iterator = data.keys();
                    while (iterator.hasNext()) {
                        try {
                            String nextKey = iterator.next();
                            String nextVal = String.valueOf(data.get(nextKey));
                            params.put(nextKey, nextVal);
                        } catch (JSONException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    return params;
                }
            };

            queue.add(strReq);

            // https://stackoverflow.com/questions/33573803/how-to-send-a-post-request-using-volley-with-string-body
        }

        @Override
        public void run() {
            volleyRequest();
        }
    }

    /**
     * Enables https connections
     */
    @SuppressLint("TrulyRandom")
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        } catch (Exception ignored) {
        }
    }

}
