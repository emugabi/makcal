package com.fantastic.makcal;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SingleUser extends Activity implements View.OnClickListener {
	String contactname;
	String ipAddress;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	setContentView(R.layout.singleuser);
	
	Bundle singleUser = getIntent().getExtras();
	
	contactname = singleUser.getString("contactname");
	ipAddress = singleUser.getString("ipAddress");
	
	TextView contact = (TextView) findViewById(R.id.contactname);
	contact.setText(contactname);
	
	
	Button call = (Button) findViewById(R.id.call);
	Button message = (Button) findViewById(R.id.message);
	
	
	call.setOnClickListener(this);
	message.setOnClickListener(this);
	
}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.call:
			Intent toVoip = new Intent(SingleUser.this, Voip.class);
			toVoip.putExtra("CallerID", contactname);
			toVoip.putExtra("ipAddress", ipAddress);
			startActivity(toVoip);
			break;
			
		case R.id.message:
			Intent toMessageView = new Intent(SingleUser.this, MessageView.class);
			toMessageView.putExtra("username", contactname);
			startActivity(toMessageView);
		
		}
			
		
	}

}
