package com.fantastic.makcal;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class Splash extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {/*savedInstanceState can be change to anything aslong as the suoer.oncreate is too change to that 
	similar one*/

		super.onCreate(savedInstanceState);
		//sets the splash screen to be fullscreen mode
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		
		
		setContentView(R.layout.splash_screen);

		Thread timer=new Thread(){
			@Override
			public void run(){
				try{
					sleep(1000);

				}catch(InterruptedException e){
					e.printStackTrace();

				}finally{
					Intent i = new Intent(Splash.this, UserLogin.class);
					startActivity(i);
				}
			}
		};
		timer.start();
	}

}
