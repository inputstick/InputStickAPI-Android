package com.inputstick.apps.apidemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.inputstick.api.ConnectionManager;
import com.inputstick.api.InputStickStateListener;
import com.inputstick.api.basic.InputStickHID;
import com.inputstick.api.basic.InputStickMouse;

public class MouseDemoActivity extends Activity implements InputStickStateListener {
	
	private static final byte DIST = 10;
	private static final byte MDIST = -10;
	
	private Button buttonMouseUpLeft;
	private Button buttonMouseUp;
	private Button buttonMouseUpRight;	
	private Button buttonMouseLeft;
	private Button buttonMouseRight;
	private Button buttonMouseDownLeft;
	private Button buttonMouseDown;
	private Button buttonMouseDownRight;
	
	private Button buttonMouseClickLeft2x;
	private Button buttonMouseClickLeft;
	private Button buttonMouseClickMiddle;
	private Button buttonMouseClickRight;
	
	private Button buttonMouseScrollUp;
	private Button buttonMouseScrollDown;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_mouse_demo);
		
		buttonMouseUpLeft = (Button) findViewById(R.id.buttonMouseUpLeft);
		buttonMouseUpLeft.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//move mouse pointer in top left direction
				InputStickMouse.move(MDIST, MDIST);
			}
		});	
		buttonMouseUp = (Button) findViewById(R.id.buttonMouseUp);
		buttonMouseUp.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				InputStickMouse.move((byte)0, MDIST);
			}
		});	
		buttonMouseUpRight = (Button) findViewById(R.id.buttonMouseUpRight);
		buttonMouseUpRight.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				InputStickMouse.move(DIST, MDIST);
			}
		});	
		buttonMouseLeft = (Button) findViewById(R.id.buttonMouseLeft);
		buttonMouseLeft.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				InputStickMouse.move(MDIST, (byte)0);
			}
		});	
		buttonMouseRight = (Button) findViewById(R.id.buttonMouseRight);
		buttonMouseRight.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				InputStickMouse.move(DIST, (byte)0);
			}
		});	
		buttonMouseDownLeft = (Button) findViewById(R.id.buttonMouseDownLeft);
		buttonMouseDownLeft.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				InputStickMouse.move(MDIST, DIST);
			}
		});	
		buttonMouseDown = (Button) findViewById(R.id.buttonMouseDown);
		buttonMouseDown.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				InputStickMouse.move((byte)0, DIST);
			}
		});	
		buttonMouseDownRight = (Button) findViewById(R.id.buttonMouseDownRight);
		buttonMouseDownRight.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				InputStickMouse.move(DIST, DIST);
			}
		});	
		
		buttonMouseClickLeft2x = (Button) findViewById(R.id.buttonMouseClickLeft2x);
		buttonMouseClickLeft2x.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//double click left mouse button
				InputStickMouse.click(InputStickMouse.BUTTON_LEFT, 2);
			}
		});	
		buttonMouseClickLeft = (Button) findViewById(R.id.buttonMouseClickLeft);
		buttonMouseClickLeft.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//single click left mouse button:
				InputStickMouse.click(InputStickMouse.BUTTON_LEFT, 1);
			}
		});	
		buttonMouseClickMiddle = (Button) findViewById(R.id.buttonMouseClickMiddle);
		buttonMouseClickMiddle.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				InputStickMouse.click(InputStickMouse.BUTTON_MIDDLE, 1);
			}
		});	
		buttonMouseClickRight = (Button) findViewById(R.id.buttonMouseClickRight);
		buttonMouseClickRight.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				InputStickMouse.click(InputStickMouse.BUTTON_RIGHT, 1);
			}
		});	
		
		buttonMouseScrollUp = (Button) findViewById(R.id.buttonMouseScrollUp);
		buttonMouseScrollUp.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//scroll up:
				InputStickMouse.scroll(DIST);
			}
		});	
		buttonMouseScrollDown = (Button) findViewById(R.id.buttonMouseScrollDown);
		buttonMouseScrollDown.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//scroll down:
				InputStickMouse.scroll(MDIST);
			}
		});	
	}
	
	@Override
	public void onPause() {	    
		InputStickHID.removeStateListener(this);
		super.onPause();  
	}	
	
	@Override
	public void onResume() {
	    super.onResume(); 
		InputStickHID.addStateListener(MouseDemoActivity.this);
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
		buttonMouseUpLeft.setEnabled(enabled);
		buttonMouseUp.setEnabled(enabled);		
		buttonMouseUpRight.setEnabled(enabled);		
		buttonMouseLeft.setEnabled(enabled);		
		buttonMouseRight.setEnabled(enabled);		
		buttonMouseDownLeft.setEnabled(enabled);		
		buttonMouseDown.setEnabled(enabled);		
		buttonMouseDownRight.setEnabled(enabled);	
		
		buttonMouseClickLeft2x.setEnabled(enabled);		
		buttonMouseClickLeft.setEnabled(enabled);		
		buttonMouseClickMiddle.setEnabled(enabled);		
		buttonMouseClickRight.setEnabled(enabled);	
		
		buttonMouseScrollUp.setEnabled(enabled);		
		buttonMouseScrollDown.setEnabled(enabled);	
	}	
}
