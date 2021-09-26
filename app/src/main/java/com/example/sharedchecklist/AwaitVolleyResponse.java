package com.example.sharedchecklist;

public class AwaitVolleyResponse<T> {
    private volatile boolean gotResponse;
    T response;

    public AwaitVolleyResponse() {
        gotResponse = false;
    }

    public void setResponse(T res) {
        response = res;
        gotResponse = true;
    }

    public boolean hasGotResponse() {
        return gotResponse;
    }

    public T getResponse() {
        if (hasGotResponse()) return response;
        else return null;
    }

}