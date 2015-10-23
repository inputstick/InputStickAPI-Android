package com.inputstick.apps.broadcastdemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.inputstick.api.basic.InputStickConsumer;
import com.inputstick.api.basic.InputStickMouse;
import com.inputstick.api.broadcast.InputStickBroadcast;
import com.inputstick.api.hid.HIDKeycodes;

public class DemoActivity extends Activity {
	
	//There are only InputStick-related actions available in this activity.
	//Assumption: it is very likely (90%?) that InputStick will be used within next few seconds	
	//Assumption: we already checked that InputStickUtility app is installed

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo);
		
		Button b;
		
		b = (Button)findViewById(R.id.buttonTypeSlow);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {	
				//use default layout, decrease typing speed 10 times.
				InputStickBroadcast.type(DemoActivity.this, "hello", null, 10);
			}
		});		
		b = (Button)findViewById(R.id.buttonTypeDe);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {	
				//type qwerty using de-DE layout. Assumes that USB host uses German (de-DE) keyboard layout.
				//Note: use de-DE-mac when working with OSX. some special characters are mapped in a different way
				//if default (en-US) layout is used, this action will result in typing: qwertz instead.				
				InputStickBroadcast.type(DemoActivity.this, "qwerty", "de-DE");				
			}
		});				
		b = (Button)findViewById(R.id.buttonKeyCtrlAltDel);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {	
				//press ctrl+alt+delete key combination:
				InputStickBroadcast.pressAndRelease(DemoActivity.this, (byte)(HIDKeycodes.ALT_LEFT | HIDKeycodes.CTRL_LEFT), HIDKeycodes.KEY_DELETE);
			}
		});
		
		
		//type, stop
		b = (Button)findViewById(R.id.buttonTypeLong);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {	
				InputStickBroadcast.type(DemoActivity.this, "qwertyuiop1234567890qwertyuiop1qwertyuiop1234567890qwertyuiop12qwertyuiop1234567890qwertyuiop123");
			}
		});
		b = (Button)findViewById(R.id.buttonStop);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//remove all pending actions, stop current action
				InputStickBroadcast.clearQueue(DemoActivity.this);
			}
		});
		
		//press & release
		b = (Button)findViewById(R.id.buttonPress);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {	
				//press "A" key, hold (do not release)
				InputStickBroadcast.keyboardReport(DemoActivity.this, (byte)0x00, HIDKeycodes.KEY_A, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, false);
				
				//press & release (by adding empty report):
				//InputStickIntent.keyboardReport(DemoActivity.this, (byte)0x00, HIDKeycodes.KEY_A, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, true);
			}
		});
		b = (Button)findViewById(R.id.buttonRelease);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//release all keys
				InputStickBroadcast.keyboardReport(DemoActivity.this, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, false);
			}
		});		
		
		//mouse
		b = (Button)findViewById(R.id.buttonClick);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {	
				//2x left mouse button click
				InputStickBroadcast.mouseClick(DemoActivity.this, InputStickMouse.BUTTON_LEFT, 2);
			}
		});		
		b = (Button)findViewById(R.id.buttonMove);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {	
				//move cursor				
				InputStickBroadcast.mouseReport(DemoActivity.this, (byte)0x00, (byte)50, (byte)(-25), (byte)0x00);
				//note: IntentAPI is not a good choice when low latency is required (sending several mouse reports one after another). 
				//If using mouse interface is important part of your app, you should consider using standard API
			}
		});		
		b = (Button)findViewById(R.id.buttonScroll);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {	
				//scroll down
				InputStickBroadcast.mouseReport(DemoActivity.this, (byte)0x00, (byte)0x00, (byte)0x00, (byte)(-10));
			}
		});
		
		//consumer control
		b = (Button)findViewById(R.id.buttonMute);
		b.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {	
				//mute or unmute audio output
				InputStickBroadcast.consumerControlAction(DemoActivity.this, InputStickConsumer.VOL_MUTE);
			}
		});		
	}
	
	
	@Override
	public void onResume() {
		super.onResume(); 	
		//establishing connection takes about 2-3 seconds (BT2.1) or 1 seconds (BT4.0).
		//if started now, it is possible that connection will be established by the time user presses a button.
		InputStickBroadcast.requestConnection(this);	    
	}	   
}
