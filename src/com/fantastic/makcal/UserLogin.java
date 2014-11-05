package com.fantastic.makcal;

import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class UserLogin extends Activity {
	public String uName = null;
	public String tel = null;
	public boolean status = false;
	Handler mHandler = new Handler();
	private SocketOperator connection = new SocketOperator();
	ProgressDialog dialog;

	EditText Username;
	EditText Tel;
	Button LoginBTN;
	SharedPreferences myPrefs;
	//
	SharedPreferences.Editor editor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.userlogin);
		myPrefs = getSharedPreferences("myPrefs", 0);
		String username = myPrefs.getString("username", null);
		Username = (EditText) findViewById(R.id.editusername);
		Tel = (EditText) findViewById(R.id.edittel);
		LoginBTN = (Button) findViewById(R.id.loginbtn);

		editor = myPrefs.edit();

		if (myPrefs.getBoolean("my_first_time", true) && (username != null)) {
			// the app is being run for first time
			
			Intent toOtherUsers = new Intent(getApplicationContext(),
					OtherUsers.class);

			toOtherUsers.putExtra("login", 1);
			startActivity(toOtherUsers);
		}

		else { // show layout if ExistingCredentials = false

			LoginBTN.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View arg0) {

					// check for length
					if (Username.length() > 0 && Tel.length() > 0) {
						if (checkWifi()) {
							dialog = ProgressDialog.show(UserLogin.this,
									"Connecting", "Please wait..", true);
							// send username to server
							if(sendUsername(Username.getText().toString())){
								dialog.dismiss();
								editor.putString("username", Username.getText()
										.toString());
								editor.putString("tel", Tel.getText().toString());
								editor.putBoolean("my_first_time", false);
								editor.commit();

								Intent toOtherUsers = new Intent(
										getApplicationContext(), OtherUsers.class);

								toOtherUsers.putExtra("login", 1);
								startActivity(toOtherUsers);
							}
							else{
								showToast("Server unavailable");
							}

						}
					} else {
						Toast.makeText(getApplicationContext(),
								"Fill in both Username and Tel",
								Toast.LENGTH_LONG).show();
					}
				}
			});
		}
	}

	public boolean checkWifi() {

		ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifi = conMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		if (wifi.isAvailable()) {
			showToast("Wifi Connected");
			status = true;

		} else {
			// dialog.dismiss();
			showToast("Wifi Not Detected");
			status = false;

		}

		return status;
	}

	public void showToast(final String value) {

		Toast.makeText(this, value, Toast.LENGTH_SHORT).show();
	}

	public boolean sendUsername(String username) {

		final ChatMessage login = new ChatMessage(ChatMessage.LOGIN, username,
				"");
		new Thread(new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				if (connection.sendMessage(login)) {
					//dialog.dismiss();
					status = true;
					// showToast("Welcome "+Username.getText().toString());

				} else {
					//dialog.dismiss();
					status = (false);
					//showToast("Try Again"); looper.prepare

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		
	}).start();
			/*	mHandler.post(new Runnable() {
					
					@Override
					public void run() {
						
						
					}
				});
				//getStatus();
*/			return status;
	}

}
