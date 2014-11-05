package com.fantastic.makcal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class SocketOperator extends Service {
	public static final String SERVER_ADDRESS = "192.168.137.1";
	public static final int PORT = 8008;
	private boolean status;

	// private static final String REQUEST_FAILED = null;
	
	
	public void receiveMessage()
	{
		try {
			
			new Thread(new Runnable()
			{
				ServerSocket serverSocket = new ServerSocket(8008);
				Socket socket = serverSocket.accept();
				ObjectInputStream in = null;
			    
				@Override
				public void run() {

					try {
						
						in = new ObjectInputStream(
								socket.getInputStream());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

			    	while(true)
			    	{
			    		ChatMessage msg = null;
						try {
							msg = (ChatMessage) in.readObject(); //  readLine();
							Log.d("","MSGGG:  "+ msg.toString());
							
							//msgList.add(msg);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (ClassNotFoundException e) {
							 
							e.printStackTrace();
						}
			    		if(msg == null)
			    		{
			    			break;
			    		}
			    		else
			    		{
			    			getReceivedMessage(msg);
			    		}
			    	}
				
				}
				
			}).start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void getReceivedMessage(ChatMessage receivedMessage) {

		
		ChatMessage cMessage = receivedMessage;
		
				if (cMessage.getType() == ChatMessage.LOGIN) {
					// do something if message is received
					NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
			    	.setSmallIcon(R.drawable.ic_launcher)
			    	.setContentTitle("Server")
			    	.setContentText(cMessage.getMessage()); 
					mBuilder.build();
					
				} if (cMessage.getType() == ChatMessage.MESSAGE) {
					// do something if message is received
					Intent toMsgView = new Intent(this, MessageView.class);
					toMsgView.putExtra("username", cMessage
							.getSendTo().toString());
					toMsgView.putExtra("message", cMessage
							.getMessage().toString());
					
					startActivity(toMsgView);
				}else if (cMessage.getType() == ChatMessage.WHOISIN) {

					// remember: server sends back username as
					// message string and
					// ipAddress as sendTo string
					String clients = cMessage.getMessage();
					String Ips = cMessage.getSendTo();
					
					OtherUsers.clientList(clients, Ips);
					
				} else {// hence its a call
					if (cMessage.getType() == ChatMessage.CALL) {
						Intent toVoip = new Intent(this, Voip.class);
						// remember: clients sends call object
						//with username as message string
						// receiver as sendTo string
						toVoip.putExtra("callerID",
								cMessage.getMessage());
						startActivity(toVoip);
					}
				}
			}
	
	
	
	@SuppressWarnings("resource")
	public boolean sendMessage(ChatMessage msg) throws IOException {

		ObjectOutputStream out = null;

		try {
			final Socket socket = new Socket(SERVER_ADDRESS, PORT);
			//socket.setSoTimeout(5000);
			out = new ObjectOutputStream(socket.getOutputStream());

			out.writeObject(msg);
			// out.close(); could be causing error
			out.flush();
			status = true;

		} catch (final IOException e) {
			e.printStackTrace();
			
			status = false;
		}
		finally {
//			out.close();
		}
		return status;

	}
	
//	public void stop (){ //causing stackoverflowerror
//	
//		this.stop();
//		
//	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}