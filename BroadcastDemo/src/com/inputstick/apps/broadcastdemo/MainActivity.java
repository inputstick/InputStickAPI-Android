package com.inputstick.apps.broadcastdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.inputstick.api.broadcast.InputStickBroadcast;

public class MainActivity extends Activity {
	
	private Button buttonType;
	private Button buttonDemo;
	private Button buttonDemo1;
	private Button buttonDemo2;		
	private Button buttonDemo3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_main);
		
		buttonType = (Button)findViewById(R.id.buttonType);
		buttonType.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				InputStickBroadcast.type(MainActivity.this, "hello");
				//If InputStickUtility app is not installed, nothing will happen
				//If InputStickBroadcast.setAutoSupportCheck(true); was not called, user will not get any feedback!
			}
		});
		
		buttonDemo = (Button)findViewById(R.id.buttonDemo);
		buttonDemo.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {	
				//DemoActivity offers ONLY InputStick-related actions.
				//There is no point in launching this activity if InputStickUtility is not installed.
				//Allow to display download dialog if necessary.				
				if (InputStickBroadcast.isSupported(MainActivity.this, true)) {
					Intent intent = new Intent(MainActivity.this, DemoActivity.class);
					startActivity(intent);
				}
			}
		});
		
		
		buttonDemo1 = (Button)findViewById(R.id.buttonDemo1);
		buttonDemo1.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {	
				Intent intent = new Intent(MainActivity.this, Demo1Activity.class);
				startActivity(intent);
			}
		});
		buttonDemo2 = (Button)findViewById(R.id.buttonDemo2);
		buttonDemo2.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {	
				Intent intent = new Intent(MainActivity.this, Demo2Activity.class);
				startActivity(intent);
			}
		});
		buttonDemo3 = (Button)findViewById(R.id.buttonDemo3);
		buttonDemo3.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {	
				Intent intent = new Intent(MainActivity.this, Demo3Activity.class);
				startActivity(intent);
			}
		});
		
		//If InputStick is required by your app, check if InputStickUtility is installed.
		//Display download dialog if necessary (true).
		InputStickBroadcast.isSupported(this, true);		
		//In such case, it may be a good idea to start establishing connection right now:
		//InputStickIntent.requestConnection(this);	
				
		//If InputStick is only optional, do not bug user at this moment. Wait until InputStick-related action is requested?
	}	

	
	
	//This application will no longer need InputStick. 
	//Let InputStickUtility know that it can now disconnect.
	//There is no need to check if InputStickUtility is installed.
	//Depending on user preferences InputStickUtility may ignore this request.
	//If not called, InputStickUtility will disconnect after max inactivity period anyway.	
	@Override
	public void onBackPressed() {
		InputStickBroadcast.releaseConnection(this);
		super.onBackPressed();
	}	
	@Override
	public void onDestroy() {
		InputStickBroadcast.releaseConnection(this);
		super.onDestroy();
	}	
	
}
