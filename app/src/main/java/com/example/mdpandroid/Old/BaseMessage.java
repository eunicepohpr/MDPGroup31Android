package com.example.mdpandroid.Old;

/**
 * A class that is an object for messages
 * used in Chat(box)
 * id: stores the user who sent the message
 * messsage: stores the actual message
 */
public class BaseMessage {
    int id;
    String message;

    public int getId() {
        return id;
    }

    public String getMessage() {
        return message;
    }

    //constructor for BaseMessage
    BaseMessage(int id, String msg){
        this.id = id;
        this.message = msg;
    }
}
