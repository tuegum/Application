package com.xuzheng.mBtChat.Chat;

public class Chat {
    private String text;
    private boolean self;

    public Chat(String text, boolean self) {
        this.text = text;
        this.self = self;
    }

    public Chat() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isSelf() {
        return self;
    }

    public void setSelf(boolean self) {
        this.self = self;
    }
}
