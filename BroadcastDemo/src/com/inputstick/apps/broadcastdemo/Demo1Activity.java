package com.inputstick.apps.broadcastdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.inputstick.api.broadcast.InputStickBroadcast;

public class Demo1Activity extends Activity {
	
	private Button buttonToast1;
	private Button buttonToast2;
	private Button buttonType;	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo1);
		
		buttonToast1 = (Button)findViewById(R.id.buttonToast1);
		buttonToast1.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {	
				Toast.makeText(Demo1Activity.this, "Some action 1", Toast.LENGTH_SHORT).show();
			}
		});	
		buttonToast2 = (Button)findViewById(R.id.buttonToast2);
		buttonToast2.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {	
				Toast.makeText(Demo1Activity.this, "Some action 2", Toast.LENGTH_SHORT).show();
			}
		});		
		
		buttonType = (Button)findViewById(R.id.buttonType);
		buttonType.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//no need to manually check if InputStick is supported (it will be checked automatically)
				InputStickBroadcast.type(Demo1Activity.this, "Demo1");
			}
		});
	}
	
	@Override
	public void onResume() {
		//support will be checked each time InputStick action is called.
		//Note: if InputStickUtility is not present, each check will be time consuming (several ms)
		InputStickBroadcast.setAutoSupportCheck(true);
		super.onResume();		
	}
	
	@Override
	public void onPause() {
		//restore value to false if other activities use different way to check support 
		InputStickBroadcast.setAutoSupportCheck(false);
		super.onPause();		
	}
	
	
	@Override
	public void onDestroy() {
		/*
		//Use only if there is low chance that user will need InputStick in next few minutes.
		//Otherwise keep connection alive, InputStickUtility will close it after reaching max inactivity period.
		//It is not necessary to check if InputStick is supported.
		InputStickIntent.releaseConnection(this); 
		*/	
		super.onDestroy();
	}
	
	
}
