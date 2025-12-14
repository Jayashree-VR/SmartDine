package com.SmartDine.dto;

public class AiResponse {

    private String reply;          // Final message shown to the user
    private String intent;         // What the AI understood (ex: "restaurant_search")
    private Object data;           // Any database results (ex: list of restaurants)

    public AiResponse() {}

    public AiResponse(String reply, String intent, Object data) {
        this.reply = reply;
        this.intent = intent;
        this.data = data;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}