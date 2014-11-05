package com.fantastic.makcal;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MessageView extends Activity {
	private EditText messageText;
	private EditText messageHistoryText;
	private Button sendMessageButton;
	private SocketOperator connection = new SocketOperator();
	

	@Override
	public void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.messageview);
		Bundle extras = getIntent().getExtras();
		final String sendTo = extras.getString("username");


		messageHistoryText = (EditText) findViewById(R.id.messageHistory);

		messageText = (EditText) findViewById(R.id.message);

		messageText.requestFocus();			

		sendMessageButton = (Button) findViewById(R.id.sendMessageButton);
		sendMessageButton.setOnClickListener(new OnClickListener(){
			
			Handler handler = new Handler();
			@Override
			public void onClick(View arg0) {
				
				Thread thread = new Thread(){					
					@Override
					public void run() {
						try {
							
							{
								
								handler.post(new Runnable(){	

									@Override
									public void run() {
										
										sendMessageToServer(new ChatMessage(ChatMessage.MESSAGE, messageText.getText().toString(),
												sendTo));
								        	}
									
								});
							}
						} catch (Exception e) {
							

							e.printStackTrace();
						}
					}						
				};
				thread.start();
									
			}
				
				});

		/*Bundle extras = this.getIntent().getExtras();


		friend.userName = extras.getString(FriendInfo.USERNAME);
		friend.ip = extras.getString(FriendInfo.IP);
		friend.port = extras.getString(FriendInfo.PORT);
		String msg = extras.getString(MessageInfo.MESSAGETEXT);



		setTitle("Messaging with " + friend.userName);*/
		
		
	}
	
	public void sendMessageToServer(final ChatMessage cMessage) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (connection.sendMessage(cMessage)) {
						showToast("Sent!");
						appendToMessageHistory("You",cMessage.getMessage().toString());
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
	
	public  void appendToMessageHistory(String username, String message) {
		if (username != null && message != null) {
			messageHistoryText.append(username + ":\n");								
			messageHistoryText.append(message + "\n");
		}
	}
	public void showToast(final String value) {
		Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
	}
}
