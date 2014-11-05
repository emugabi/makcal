package com.fantastic.makcal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.fantastic.voip.Voip;

public class MyService extends Service {
	public static final String SERVER_ADDRESS = "192.168.137.1";
	NotificationManager mNotification;
	public static final int PORT = 8008;
	static ObjectOutputStream sOutput;
	ObjectInputStream sInput;
	Socket socket;
	InetAddress serverAddr;
	Handler mhandler = new Handler();
	MessageView messageView = new MessageView();
	OtherUsers otherUsers = new OtherUsers();
	public static final String NOTIFICATION = "com.fantastic.makcal.MESSAGE";

	@Override
	public IBinder onBind(Intent intent) {
		/*Toast.makeText(this, "Service created...", Toast.LENGTH_LONG).show();
		Log.i("tag", "Service created...");*/
		return myBinder;
	}

	private final IBinder myBinder = new LocalBinder();

	// TCPClient mTcpClient = new TCPClient();

	public class LocalBinder extends Binder {
		public MyService getService() {
			System.out.println("I am in Localbinder ");
			return MyService.this;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		System.out.println("I am in on create");
		mNotification = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		

	}

	public boolean checkWifi() {
		boolean status;
		ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifi.isAvailable()) {
			status = true;
		} else {

			Toast.makeText(this, "Wifi Not Detected", Toast.LENGTH_SHORT)
					.show();
			status = true;
		}
		return status;
	}

	/*public void IsBoundable() {
		Toast.makeText(this, "I bind like butter", Toast.LENGTH_LONG).show();
	}*/

	public void sendMessage(final ChatMessage message) {

		if (sOutput != null) {
		
			Log.v("TCP Client", ""+message.toString());

			try {
				sOutput.writeObject(message);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				sOutput.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			Log.e("TCP Client", "not sent line 99");
			
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		System.out.println("I am in on start");
		// Toast.makeText(this, "Service created ...",
		// Toast.LENGTH_LONG).show();

		Runnable connect = new connectSocket();
		new Thread(connect).start();

		return START_STICKY;
	}

	public class connectSocket implements Runnable {
		@Override
		public void run() {
			try {
				Log.i("TCP Client", "C: Connecting...");
				// create a socket to make the connection with the server
				Socket socket = new Socket(SERVER_ADDRESS, PORT);
				startListening(socket);
				try {
					// send the message to the server
					if (socket.isConnected()) {
						sOutput = new ObjectOutputStream(
								socket.getOutputStream());
						
						Log.i("TCP Client", "C: Sent.");
						Log.i("TCP Client", "C: Done.");
						
						
					} else {
						showToast("Cant find Server!");
						Log.e("TCP Client", "Cant find Server!");
					}

				} catch (Exception e) {
					Log.e("TCP Client", "S: Error", e);
				}
			} catch (ConnectException e) {

				Log.e("TCP Client", "Can't Find Server on " + SERVER_ADDRESS, e);
			} catch (Exception e) {
				Log.e("TCP Client", "C: Error", e);
			}
		}
	}

	// works okay above

	public void startListening(final Socket socket) {
		new Thread(new Runnable() {

			@Override
			public void run() {

				if (socket.isConnected()) {
					try {
						sInput = new ObjectInputStream(socket.getInputStream());
						Log.i("TCP Client", "object inputstream open");
					} catch (StreamCorruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					try {
						// Receive messages one-by-one, forever
						while (true) {
							// Get the next message
							ChatMessage message = null;
							try {
								message = (ChatMessage) sInput.readObject();
								Log.i("TCP Client", "message object received");
							} catch (ClassNotFoundException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							// Print it to our text window
							messageReceived(message);
							Log.i("TCP Client",
									"message object has been processed");
						}
					} catch (IOException ie) {
						System.out.println(ie);
					}
				}
			}

		}).start();
	}

	// works fine below
	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			Log.e("Service", "Service DEstroyed");
			socket.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		socket = null;
	}

	public void messageReceived(final ChatMessage message) {

		if (message.getType() == ChatMessage.LOGIN) {
			// do something if message is received
			Log.i("LOGIN", "LOGIN success");

			mhandler.post(new Runnable() {

				@Override
				public void run() {
					OtherUsers.clientList("Test","192.168.137.20");
					showToast(message.getMessage());
				}
			});

			// OtherUsers.refresh();
		}
		if (message.getType() == ChatMessage.MESSAGE) {
			Log.i("TCP Client", "ChatMessage.MESSAGE received");
			Log.i("TCP Client", ""+message.toString());
			
			NotificationCompat.Builder mBuilder = new
			NotificationCompat.Builder(
			this).setSmallIcon(R.drawable.ic_launcher)
			.setContentTitle("MakCal") .setContentText(message.getMessage());
			
			Intent i = new Intent(this, MessageView.class);
			i.putExtra("contact", message.getSendTo());
			i.putExtra("message",message.getMessage()); 
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0, i, 0);
			mBuilder.setContentIntent(contentIntent);
			
			mBuilder.setContentText("New message from " + message.getSendTo()
			+ ": " + message.getMessage()); mNotification.notify(("Server " +
			message.getMessage()).hashCode(), mBuilder.build());
			 

			//broadCastMessage(message.getMessage(), message.getSendTo());

			messageView.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					String contact = message.getSendTo();
					String messageText = message.getMessage();
					MessageView.appendToMessageHistory(contact, messageText);

				}
			});

		} else if (message.getType() == ChatMessage.WHOISIN) {
			Log.i("TCP Client", "ChatMessage.LOGIN received");

			/*
			 * Other // remember: server sends back username as // message
			 * string and // ipAddress as sendTo string
			 */
			final String clients = message.getMessage();
			final String Ips = message.getSendTo();
			
			
			otherUsers.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					OtherUsers.clientList(clients, Ips);

				}
			});
			
		} else if (message.getType() == ChatMessage.CALL) {
			
			  Intent toVoip = new Intent(this, Voip.class); // remember:
			  //clients sends call object //with username as message string //
			  //receiver as sendTo string 
				toVoip.putExtra("callerID", message.getSendTo());
				toVoip.putExtra("ipAddress", message.getMessage());
				toVoip.putExtra("callType", 0);
				startActivity(toVoip);
			 
		}
	}

	public void showToast(final String value) {

		Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
	}

}