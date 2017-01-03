package com.inputstick.apps.apidemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.inputstick.api.ConnectionManager;
import com.inputstick.api.InputStickRawHIDListener;
import com.inputstick.api.InputStickStateListener;
import com.inputstick.api.Util;
import com.inputstick.api.basic.InputStickHID;
import com.inputstick.api.basic.InputStickRawHID;

/*
 *  
 * Raw HID interface requires firmware 0.98D or newer!!!
 * 
 * Allows for two-way communication with USB host by sending and receiving 64 bytes packets via USB HID reports. 
 * Requires no drivers and is supported on Windows, Linux and OS X.
 * 
 * Raw HID interface is not enabled by default! USB configuration must be changed:
 * InputStickUtility -> My Devices -> select device -> Configuration (padlock icon) -> Connect -> USB Config -> select keyboard+mouse+raw HID -> Set configuration
 * 
 * PC demo apps: https://www.pjrc.com/teensy/rawhid.html
 * 
 * Tip: if you want to use demo applications from PJRC website without and software modifications, 
 * in USB Config step, select "Show advanced" and modify values for USB VID and PID to: 16C0 and 0480 respectively. 
 * This will make InputStick pretend to be Teensy board.
 * Raw HID replaces consumer control (multimedia keys), gamepad and touch screen interfaces!
 * 
 * */ 
 

public class RawHIDDemoActivity extends Activity implements InputStickStateListener, InputStickRawHIDListener {

	private Button buttonRawHIDSend;
	private TextView textViewRawHIDReceived;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_raw_hiddemo);				
		
		buttonRawHIDSend = (Button) findViewById(R.id.buttonRawHIDSend);
		buttonRawHIDSend.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//sends generated data packet as a raw HID report to USB host
				byte[] data = new byte[16];
				for (int i = 0; i < 16; i++) {
					data[i] = (byte)(i + 16);
				}
				InputStickRawHID.sendRawHIDData(data);
			}
		});	
		
		textViewRawHIDReceived = (TextView) findViewById(R.id.textViewRawHIDReceived);
	
	}
	
	@Override
	public void onPause() {	    
		InputStickHID.removeStateListener(this);
		InputStickRawHID.removeRawHIDListener(this);
		super.onPause();  
	}	
	
	@Override
	public void onResume() {
	    super.onResume(); 
		InputStickHID.addStateListener(this);
		InputStickRawHID.addRawHIDListener(this);
	    manageUI(InputStickHID.getState());	    
	}	
	
	@Override
	public void onStateChanged(int state) {
		manageUI(state);	
	}
	
	private void manageUI(int state) {		
		if (state == ConnectionManager.STATE_READY) {
			enableUI(true);
		} else {
			enableUI(false);
		}
	}

	private void enableUI(boolean enabled) {
		buttonRawHIDSend.setEnabled(enabled);
	}

	@Override
	public void onRawHIDData(byte[] data) {		
		//length of data[] is always 64
		String text = "Data received from USB host:\n";
		for (int i = 0; i < data.length; i++) {
			text += "0x" + Util.byteToHexString(data[i]) + " ";
		}
		textViewRawHIDReceived.setText(text);
	}	
	
	
}
