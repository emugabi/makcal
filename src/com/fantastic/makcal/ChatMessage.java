package com.fantastic.makcal;

import java.io.*;
/*
 * This class defines the different type of messages that will be exchanged between the
 * Clients and the Server. 
 */
public class ChatMessage implements Serializable {

	protected static final long serialVersionUID = 1112122200L;

	// The different types of message sent by the Client
	// WHOISIN to receive the list of the users connected
	// MESSAGE an ordinary message
	// LOGOUT to disconnect from the Server
	static final int WHOISIN = 0, MESSAGE = 1, CALL = 2, LOGIN = 3 , LOGOUT = 4;
	private int type;
	private String message;
	private String sendTo;
	// constructor
	ChatMessage(int type, String message, String sendTo) {
		this.type = type;
		this.message = message;
		this.sendTo = sendTo;
	}
	
	@Override
	public String toString() {
		return "ChatMessage [type=" + type + ", message=" + message
				+ ", sendTo=" + sendTo + "]";
	}

	// getters
	public int getType() {
		return type;
	}
	public String getMessage() {
		return message;
	}
	
	public String getSendTo() {
		return sendTo;
	}

	
}
