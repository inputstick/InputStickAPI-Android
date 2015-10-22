package com.inputstick.apps.broadcastdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.inputstick.api.broadcast.InputStickBroadcast;

public class Demo2Activity extends Activity {

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
				Toast.makeText(Demo2Activity.this, "Some action 1", Toast.LENGTH_SHORT).show();
			}
		});	
		buttonToast2 = (Button)findViewById(R.id.buttonToast2);
		buttonToast2.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {	
				Toast.makeText(Demo2Activity.this, "Some action 2", Toast.LENGTH_SHORT).show();
			}
		});		
		
		buttonType = (Button)findViewById(R.id.buttonType);
		buttonType.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {	
				//no need to manually check if InputStick is supported (if not, button is disabled)
				InputStickBroadcast.type(Demo2Activity.this, "Demo2");
			}
		});
	}
	
	@Override
	public void onResume() {
		super.onResume();	
		//check if InputStick is supported.		
		boolean enable = InputStickBroadcast.isSupported(this, true);
		//if not, disable all related UI elements		
		buttonType.setEnabled(enable);
		//do the same for any other InputStick-related UI items.
	}
	
	@Override
	public void onPause() {
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
