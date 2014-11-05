package com.fantastic.makcal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class OtherUsers extends Activity implements View.OnClickListener {
	public static final int SETTINGS_ID = Menu.FIRST;
	public static final int VOIP_ID = Menu.FIRST + 1;
	public static final int EXIT_APP_ID = Menu.FIRST + 2;

	private boolean status;
	private static ArrayList<String> usernames; // usernames <eventually bind to ip
	// address
	public static ArrayAdapter<String> msgListAdapter;
	ListView chatroom;
	private SocketOperator connection = new SocketOperator();
	TextView username;
	String userName;
	
	Button logout;
	Button refresh;

	final Handler mHandler = new Handler();
	public static HashMap<String, String> hashList = new HashMap<String, String>();

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.otherusers);
		

		SharedPreferences myPrefs = getSharedPreferences("myPrefs", 0);
		userName = myPrefs.getString("username", null);
		 
		login();
		/*
		 * Bundle Credentials = getIntent().getExtras(); username = (TextView)
		 * findViewById(R.id.username);
		 * 
		 * userName = Credentials.getString("username", "Default");
		 */
		username = (TextView) findViewById(R.id.contactname);
		username.setText(userName);

		refresh = (Button) findViewById(R.id.refresh);
		chatroom = (ListView) findViewById(R.id.chatroom);

		usernames = new ArrayList<String>();
		usernames.add("Moses");
		hashList.put("Moses", "192.168.37.1");
		usernames.add("Ronny");
		usernames.add("James");
		usernames.add("Racheal");

		msgListAdapter = new ArrayAdapter<String>(this,
				R.layout.contactlist, usernames);

		chatroom.setAdapter(msgListAdapter);

		chatroom.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				String item = ((TextView) view).getText().toString();
				Toast.makeText(getBaseContext(), hashList.get(item),
						Toast.LENGTH_SHORT).show();

				Intent toSingleUser = new Intent(OtherUsers.this,
						SingleUser.class);
				toSingleUser.putExtra("contactname", username.getText()
						.toString());
				toSingleUser.putExtra("ipAddress", hashList.get(item));

				startActivity(toSingleUser);

			}
		});

		refresh.setOnClickListener(this);

		// if (checkWifi()) {// test if we can start the Client
		

		//startListening();

		// }

	}// end of Bundle

	private void login() {
		// TODO Auto-generated method stub
		if(getIntent().getExtras().isEmpty()){
			sendMessageToServer(new ChatMessage(ChatMessage.LOGIN, userName, null));
		}
	}

	public void showToast(final String value) {
		Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.refresh) {

			// if (checkWifi()) {
			sendMessageToServer(new ChatMessage(ChatMessage.WHOISIN, "", null));
			// }

		}
	}

	public void sendMessageToServer(final ChatMessage cMessage) {

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					if (connection.sendMessage(cMessage)) {
						//showToast("Sent!");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}

	public boolean checkWifi() {
		ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (wifi.isAvailable()) {
			status = true;
		} else {

			showToast("Wifi Not Detected");
			status = true;
		}
		return status;
	}

	private static void addItemsToList(String username) {
		usernames.clear();
		usernames.add(username);
		msgListAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, SETTINGS_ID, 0, R.string.settings);
		menu.add(0, VOIP_ID, 0, "VoIP");
		menu.add(0, EXIT_APP_ID, 0, R.string.exit_application);
		return result;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		switch (item.getItemId()) {
		case SETTINGS_ID:
			Intent toSettings = new Intent(OtherUsers.this, Settings.class);
			startActivity(toSettings);
			return true;

		case EXIT_APP_ID:
			finish();
			return true;

		case VOIP_ID:
			Intent toVoip = new Intent(OtherUsers.this, Voip.class);
			toVoip.putExtra("username", username.getText().toString());
			startActivity(toVoip);
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}

	public static void clientList(String clients, String Ips){
		
		hashList.put(clients, Ips);
		OtherUsers.addItemsToList(clients);
	}
					
	/*@Override
	protected void onResume() {
		super.onResume();
		if (checkWifi()) { // correct this immediately :P spent 30 minutes 

			sendMessageToServer(new ChatMessage(ChatMessage.LOGIN, userName,
					null));
			

		} else {
			Intent toLogin = new Intent(getApplicationContext(),
					UserLogin.class);
			startActivity(toLogin);
		}

	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		sendMessageToServer(new ChatMessage(ChatMessage.LOGOUT, userName, null));
		//connection.stop();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		sendMessageToServer(new ChatMessage(ChatMessage.LOGOUT, userName, null));
	}*/

}
