package com.inputstick.apps.apidemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.inputstick.api.ConnectionManager;
import com.inputstick.api.InputStickStateListener;
import com.inputstick.api.basic.InputStickHID;
import com.inputstick.api.basic.InputStickTouchScreen;

//touch screen interface requires firmware 0.98D or newer!!!
//touch screen interface uses absolute screen coordinates. It always allows to move mouse pointer to desired point on the screen, no matter what was its initial position.
//(mouse interface uses relative coordinates)
public class TouchScreenDemoActivity extends Activity implements InputStickStateListener {
	
	private Button buttonTouchCenter;
	private Button buttonTouchOutOfRange;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_touch_screen_demo);		
		
		buttonTouchCenter = (Button) findViewById(R.id.buttonTouchCenter);
		buttonTouchCenter.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//move mouse pointer to the center of the screen using touch screen interface
				//x,y range: 0 - 10000; 10000 = 100% of vertical/horizontal screen resolution 
				InputStickTouchScreen.moveTouchPointer(5000, 5000);
			}
		});		
		
		buttonTouchOutOfRange = (Button) findViewById(R.id.buttonTouchOutOfRange);
		buttonTouchOutOfRange.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//simulate finger/stylus going out of range. Windows OS should hide additional touch screen UI.
				//pointer will be moved to top left corner of the screen.
				InputStickTouchScreen.goOutOfRange();
			}
		});	
	}
	
	@Override
	public void onPause() {	    
		InputStickHID.removeStateListener(TouchScreenDemoActivity.this);
		super.onPause();  
	}	
	
	@Override
	public void onResume() {
	    super.onResume(); 
		InputStickHID.addStateListener(TouchScreenDemoActivity.this);
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
		buttonTouchCenter.setEnabled(enabled);
		buttonTouchOutOfRange.setEnabled(enabled);	
	}	
	
}
