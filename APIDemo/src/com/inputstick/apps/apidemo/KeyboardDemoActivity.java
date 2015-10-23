package com.inputstick.apps.apidemo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.inputstick.api.ConnectionManager;
import com.inputstick.api.InputStickKeyboardListener;
import com.inputstick.api.InputStickStateListener;
import com.inputstick.api.basic.InputStickHID;
import com.inputstick.api.basic.InputStickKeyboard;
import com.inputstick.api.hid.HIDKeycodes;

public class KeyboardDemoActivity extends Activity implements InputStickStateListener, InputStickKeyboardListener {
	
	private EditText editText;
	private Spinner spinner;
	
	private Button buttonTypeASCII;
	private Button buttonTypeLayout;
	private Button buttonPressEnter;
	private Button buttonPressTab;
	private Button buttonPressEsc;
	private Button buttonCtrlAltDel;
	private Button buttonPressA;
	private Button buttonReleaseAll;
	private Button buttonNumLock;
	private Button buttonCapsLock;
	private Button buttonScrollLock;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_keyboard_demo);
		
		editText = (EditText)findViewById(R.id.editText);
		spinner = (Spinner)findViewById(R.id.spinner);
		
		/* Note: since buttons are disabled when InputStick is not ready,
		 * connection state is not checked again, after user clicks a button
		 */
		
		buttonTypeASCII = (Button)findViewById(R.id.buttonTypeASCII);
		buttonTypeASCII.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//type text assuming that USB host uses en-US keyboard layout:
				InputStickKeyboard.typeASCII(editText.getText().toString());		
			}
		});
		buttonTypeLayout = (Button)findViewById(R.id.buttonTypeLayout);
		buttonTypeLayout.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//get layout name, examples: "de-DE", "en-US", "pl-PL"
				//full list: http://inputstick.com/index.php/developers/keyboard-layouts
				String layoutName = spinner.getSelectedItem().toString();
				/* it is recommended to always use this way of typing text
				 * keyboard layout should always match the one used by USB host
				 * otherwise invalid characters will appear, example:
				 * USB host uses German keyboard layout,
				 * typeASCII("a[abc123XYZ]"); is called
				 * instead of expected result: a[abc123XYZ]
				 * appears: aüABC123XZY+y
				 *  
				 *  since it is not possible to learn what layout is used by USB host
				 *  using USB interface, such information must be provided by user
				 */
				
				/*
				//previous API version:
				KeyboardLayout layout;
				layout = KeyboardLayout.getLayout(layoutName); //example: "de-DE"	
				//now all you can use all characters available for de-DE (German) layout will be accepted, example:
				//layout.type("üßö");
				layout.type(editText.getText().toString());
				*/
								
				//updated API:
				InputStickKeyboard.type(editText.getText().toString(), layoutName);
				
				// '\n' and '\t' characters are supported		
			}
		});
		buttonPressEnter = (Button)findViewById(R.id.buttonPressEnter);
		buttonPressEnter.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//press and release ENTER key
				InputStickKeyboard.pressAndRelease(HIDKeycodes.NONE, HIDKeycodes.KEY_ENTER);				
			}
		});
		buttonPressTab = (Button)findViewById(R.id.buttonPressTab);
		buttonPressTab.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//press and release TAB key
				InputStickKeyboard.pressAndRelease(HIDKeycodes.NONE, HIDKeycodes.KEY_TAB);				
			}
		});
		buttonPressEsc = (Button)findViewById(R.id.buttonPressEsc);
		buttonPressEsc.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//press and release ESC key
				InputStickKeyboard.pressAndRelease(HIDKeycodes.NONE, HIDKeycodes.KEY_ESCAPE);				
			}
		});
		buttonCtrlAltDel = (Button)findViewById(R.id.buttonCtrlAltDel);
		buttonCtrlAltDel.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//press and release CTRL + ALT + DELETE key combination
				InputStickKeyboard.pressAndRelease((byte)(HIDKeycodes.CTRL_LEFT | HIDKeycodes.ALT_LEFT), HIDKeycodes.KEY_DELETE);				
			}
		});
		buttonPressA = (Button)findViewById(R.id.buttonPressA);
		buttonPressA.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//press "A" key, in this case key will NOT be released
				InputStickKeyboard.customReport((byte)0, HIDKeycodes.KEY_A, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0);		
			}
		});
		buttonReleaseAll = (Button)findViewById(R.id.buttonReleaseAll);
		buttonReleaseAll.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//release all keys
				InputStickKeyboard.customReport((byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0);	
			}
		});
		buttonNumLock = (Button)findViewById(R.id.buttonNumLock);
		buttonNumLock.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//request USB host to change state of NumLock
				InputStickKeyboard.toggleNumLock();	
			}
		});
		buttonCapsLock = (Button)findViewById(R.id.buttonCapsLock);
		buttonCapsLock.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//request USB host to change state of CapsLock
				InputStickKeyboard.toggleCapsLock();		
			}
		});
		buttonScrollLock = (Button)findViewById(R.id.buttonScrollLock);
		buttonScrollLock.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				//request USB host to change state of ScrollLock
				InputStickKeyboard.toggleScrollLock();			
			}
		});
		
		
	}
	
	@Override
	public void onPause() {	    
		//remove all listeners:
		InputStickHID.removeStateListener(this);
		InputStickKeyboard.removeKeyboardListener(this);
		super.onPause();  
	}	
	
	@Override
	public void onResume() {
	    super.onResume(); 
	    //callback will occur when connection state changes
		InputStickHID.addStateListener(KeyboardDemoActivity.this);
	    //callback will occur when USB host changes state of NumLock, CapsLock or ScrollLock LEDs 
	    InputStickKeyboard.addKeyboardListener(KeyboardDemoActivity.this);
	    
	    //get current connection state and adjust UI accordingly:
	    manageUI(InputStickHID.getState());	   
	    //get state of keyboard LEDs
	    setLEDs(InputStickKeyboard.isNumLock(), InputStickKeyboard.isCapsLock(), InputStickKeyboard.isScrollLock());
	}	
	
	@Override
	public void onLEDsChanged(boolean numLock, boolean capsLock, boolean scrollLock) {
		//set new state of keyboard LEDs
		setLEDs(numLock, capsLock, scrollLock);
	}
	
	@Override
	public void onStateChanged(int state) {
		//
		manageUI(state);	
	}
	
	private void manageUI(int state) {
		if (state == ConnectionManager.STATE_READY) {
			//if InputStick is ready to accept keyboard reports, enable buttons
			enableUI(true);
		} else {
			//InputStick is not ready, do not allow to send any USB reports
			enableUI(false);
		}
	}

	private void enableUI(boolean enabled) {
		//disabling UI will prevent sending keyboard and mouse actions when not connected or USB host is not ready
		buttonTypeASCII.setEnabled(enabled);
		buttonTypeLayout.setEnabled(enabled);
		buttonPressEnter.setEnabled(enabled);
		buttonPressTab.setEnabled(enabled);
		buttonPressEsc.setEnabled(enabled);
		buttonCtrlAltDel.setEnabled(enabled);
		buttonPressA.setEnabled(enabled);
		buttonReleaseAll.setEnabled(enabled);
		buttonNumLock.setEnabled(enabled);
		buttonCapsLock.setEnabled(enabled);
		buttonScrollLock.setEnabled(enabled);				
	}	
	
	private void setLEDs(boolean numLock, boolean capsLock, boolean scrollLock) {
		if (numLock) {
			buttonNumLock.setTextColor(Color.GREEN);
		} else {
			buttonNumLock.setTextColor(Color.BLACK);
		}
		if (capsLock) {
			buttonCapsLock.setTextColor(Color.GREEN);
		} else {
			buttonCapsLock.setTextColor(Color.BLACK);
		}
		if (scrollLock) {
			buttonScrollLock.setTextColor(Color.GREEN);
		} else {
			buttonScrollLock.setTextColor(Color.BLACK);
		}
	}
	
}
