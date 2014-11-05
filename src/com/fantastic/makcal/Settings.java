package com.fantastic.makcal;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Settings extends Activity {
	EditText Username;
	EditText Tel;
	Button UpdateBTN;
	SharedPreferences myPrefs;
	
	SharedPreferences.Editor editor;

	public static final int MESSAGES_ID = Menu.FIRST;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		Username = (EditText) findViewById(R.id.changeUName);
		Tel = (EditText) findViewById(R.id.changeTel);
		UpdateBTN = (Button) findViewById(R.id.update);
		myPrefs = getSharedPreferences("myPrefs", 0);
		editor = myPrefs.edit();
		
		String username = myPrefs.getString("username", null);
		String tel = myPrefs.getString("tel", null);
		
		if ((username != null) && (tel != null)) {
			Username.setText(username);
			Tel.setText(tel);
		}
		
		UpdateBTN.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				editor.putString("username", Username.getText().toString());
				editor.putString("tel", Tel.getText().toString());
				editor.commit();
				Intent toOtherUsers = new Intent(Settings.this, OtherUsers.class);
				toOtherUsers.putExtra("username", Username.getText().toString());
				startActivity(toOtherUsers);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, MESSAGES_ID, 0, R.string.messages);
		return result;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		switch (item.getItemId()) {
		case MESSAGES_ID:
			Intent i = new Intent(Settings.this, MessageView.class);
			startActivity(i);
			return true;
		}

		return super.onMenuItemSelected(featureId, item);
	}
}
