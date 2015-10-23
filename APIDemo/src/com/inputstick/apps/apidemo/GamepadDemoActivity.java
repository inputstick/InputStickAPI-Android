package com.inputstick.apps.apidemo;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.inputstick.api.ConnectionManager;
import com.inputstick.api.InputStickStateListener;
import com.inputstick.api.basic.InputStickGamepad;
import com.inputstick.api.basic.InputStickHID;

public class GamepadDemoActivity extends Activity implements InputStickStateListener, SensorEventListener {
	
	private static final int DELAY = 20; //update rate in ms, no less than 4ms!
	private Timer t;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;	
	
	private byte axisX;
	private byte axisY;
	
	private Button buttonGamepad1;
	private Button buttonGamepad2;
	private Button buttonGamepad3;
	private Button buttonGamepad4;
	
	private TextView textViewX;
	private TextView textViewY;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(R.layout.activity_gamepad_demo);
		
		textViewX = (TextView)findViewById(R.id.textViewX);
		textViewY = (TextView)findViewById(R.id.textViewY);
		
		buttonGamepad1 = (Button)findViewById(R.id.buttonGamepad1);
		buttonGamepad2 = (Button)findViewById(R.id.buttonGamepad2);
		buttonGamepad3 = (Button)findViewById(R.id.buttonGamepad3);
		buttonGamepad4 = (Button)findViewById(R.id.buttonGamepad4);
		
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}
	
	private void update() {
		if (InputStickHID.isReady()) {
			//send USB report ONLY if InputStick is in READY state
			//check state of buttons:
			byte btn = 0x00;
			if (buttonGamepad1.isPressed()) {
				btn |= 0x01;
			}
			if (buttonGamepad2.isPressed()) {
				btn |= 0x02;
			}
			if (buttonGamepad3.isPressed()) {
				btn |= 0x04;
			}
			if (buttonGamepad4.isPressed()) {
				btn |= 0x08;
			}
			//send report representing current state of the gamepad buttons and axes:
			InputStickGamepad.customReport(btn, (byte)0, axisX, axisY, (byte)0, (byte)0);
		}
	}
	
	@Override
	public void onPause() {
	    if (t != null) {
			t.cancel();
			t = null;	    	
	    }
	    
		if (InputStickHID.isReady()) {
			InputStickGamepad.customReport((byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0);
		}
	    
	    mSensorManager.unregisterListener(this);
	    
		InputStickHID.removeStateListener(this);
	    super.onPause();  
	}	
	
	@Override
	public void onResume() {
	    super.onResume(); 
	    mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);

		InputStickHID.addStateListener(GamepadDemoActivity.this);
	    manageUI(InputStickHID.getState());	    
	    
		t = new Timer();
		t.schedule(new TimerTask() {
			@Override
			public void run() {
				update();
			}
		}, DELAY , DELAY);	
	}	
	
	@Override
	public void onStateChanged(int state) {
		manageUI(state);	
	}
	
	private void manageUI(int state) {
		 System.out.println("manage");
		if (state == ConnectionManager.STATE_READY) {
			enableUI(true);
		} else {
			enableUI(false);
		}
	}

	private void enableUI(boolean enabled) {
		buttonGamepad1.setEnabled(enabled);		
		buttonGamepad2.setEnabled(enabled);
		buttonGamepad3.setEnabled(enabled);		
		buttonGamepad4.setEnabled(enabled);		
	}		
	
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}


	@Override
	public void onSensorChanged(SensorEvent event) {
		float x = event.values[0];
		float z = event.values[2];	
		
		if (x > 5) x = 5;
		if (x < -5) x = -5;			
		if (z > 5) z = 5;
		if (z < -5) z = -5;			
		
		x = -x;
		z = -z;
		axisX = (byte)(x * 25);
		axisY = (byte)(z * 25);
		
		textViewX.setText("X: " + axisX);
		textViewY.setText("Y: " + axisY);
	}
	

}
