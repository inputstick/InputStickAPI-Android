package com.inputstick.apps.apidemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.inputstick.api.ConnectionManager;
import com.inputstick.api.InputStickError;
import com.inputstick.api.InputStickStateListener;
import com.inputstick.api.basic.InputStickHID;

public class MainActivity extends Activity implements InputStickStateListener {
	
	private TextView textViewState;
	
	private Button buttonConnect;
	private Button buttonKeyboard;
	private Button buttonMouse;
	private Button buttonMedia;
	private Button buttonGamepad;
	private Button buttonWeb;
	
	private static final LightingColorFilter f = new LightingColorFilter(0xFFFFFFFF, 0xFFFFFFFF);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		textViewState = (TextView)findViewById(R.id.textViewState);
		
		buttonConnect = (Button)findViewById(R.id.buttonConnect);
		buttonConnect.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {			
				//depending on current state:, choose appropriate action: 
				int state = InputStickHID.getState();
				switch (state) {
					case ConnectionManager.STATE_CONNECTED:
					case ConnectionManager.STATE_CONNECTING:
					case ConnectionManager.STATE_READY:
						//close Bluetooth connection
						InputStickHID.disconnect();
						break;
					case ConnectionManager.STATE_DISCONNECTED:
					case ConnectionManager.STATE_FAILURE:	
						InputStickHID.connect(MainActivity.this.getApplication());
						
						/* note: you may also use direct Bluetooth connection. 
						 * In such case it is not longer necessary that InputStickUtility is installed on the device.
						 * However there are other requirements:
						 * -BLUETOOTH and BLUETOOTH_ADMIN permissions must be added to manifest file,
						 * -MAC address must be manually provided
						 * -if InputStick is password protected, encryption key must be also provided (MD5(password)), use null otherwise
						 * TIP: use Util.getPasswordBytes(plainText) to get key.
						 * 
						 * InputStickHID.connect(MainActivity.this.getApplication(), "30:14:07:31:43:59", null); //InputStick BT2.0
						 * InputStickHID.connect(MainActivity.this.getApplication(), "20:CD:39:B0:D0:26", null, true); //InputStick BT4.0
						 */						
						
						break;											
				}										
			}
		});
		
		buttonKeyboard = (Button)findViewById(R.id.buttonKeyboard);
		buttonKeyboard.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {			
				startActivity(new Intent(MainActivity.this, KeyboardDemoActivity.class));
			}
		});
		buttonMouse = (Button)findViewById(R.id.buttonMouse);
		buttonMouse.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {			
				startActivity(new Intent(MainActivity.this, MouseDemoActivity.class));
			}
		});
		buttonMedia = (Button)findViewById(R.id.buttonMedia);
		buttonMedia.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {			
				startActivity(new Intent(MainActivity.this, MediaDemoActivity.class));
			}
		});
		buttonGamepad = (Button)findViewById(R.id.buttonGamepad);
		buttonGamepad.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {			
				startActivity(new Intent(MainActivity.this, GamepadDemoActivity.class));
			}
		});
		buttonWeb = (Button)findViewById(R.id.buttonWeb);
		buttonWeb.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {			
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.inputstick.com/developers")));	
			}
		});
	}
	
	@Override
	public void onPause() {	    
		//state updates are no longer needed
		InputStickHID.removeStateListener(this);
		super.onPause();  
	}	
	
	@Override
	public void onResume() {
	    super.onResume(); 
	    //onStateChanged will be called when connection state changes
		InputStickHID.addStateListener(MainActivity.this);
	    manageUI(InputStickHID.getState());	    
	}	
	
	@Override
	public void onBackPressed() {
		InputStickHID.disconnect();		
		//you may also choose to keep the connection alive and disconnect only when "buttonConnect" is clicked
		super.onBackPressed();
	}	
	
		
	@Override
	public void onStateChanged(int state) {
		//make UI adjustments for new connection state
		manageUI(state);	
	}
	
	private void manageUI(int state) {
		/*
		 * Depending on connection state:
		 * - set appropriate text on "Connect" button
		 * - display information about connection state
		 * - change color filter of button icons. 
		 * Since the buttons are not responsible for sending any USB reports it is not necessary to enable/disabele them.
		 */
		switch (state) {
			case ConnectionManager.STATE_DISCONNECTED:
				textViewState.setText("State: Disconnected");
				buttonConnect.setText("Connect");
				enableUI(false);				
				break;
			case ConnectionManager.STATE_CONNECTING:
				textViewState.setText("State: Connecting");
				buttonConnect.setText("Cancel");
				enableUI(false);
				break;
			case ConnectionManager.STATE_CONNECTED:
				textViewState.setText("State: Connected");
				//Bluetooth connection was established, it is now possible to send commands to InputStick
				//however USB host is not ready yet to accept keyboard or mouse reports!
				buttonConnect.setText("Disconnect");
				enableUI(false);
				break;
			case ConnectionManager.STATE_READY:
				textViewState.setText("State: Ready");
				//InputStick is ready to communicate with USB host
				buttonConnect.setText("Disconnect");
				enableUI(true);
				break;
			case ConnectionManager.STATE_FAILURE:
				textViewState.setText("State: Failure");
				//after STATE_FAILURE, STATE_DISCONNECTED will occur, so it is not necessary to manage UI here
				//buttonConnect.setText("Connect");
				//enableUI(false);
				
				//maybe InputStickUtility is not installed?
				AlertDialog ad = InputStickHID.getDownloadDialog(MainActivity.this);
				//if that caused the problem, ad will be != null
				if (ad != null) {
					ad.show();
				} else {
					//something else has caused the problem. 
					//Get error code & show error message							
					Toast.makeText(MainActivity.this, "Connection failed: " + InputStickError.getFullErrorMessage(InputStickHID.getErrorCode()), Toast.LENGTH_LONG).show();				
				}								
				break;
			default:	
		}
	}

	private void enableUI(boolean enabled) {
		Drawable d;
		if (enabled) {		
			d = buttonKeyboard.getCompoundDrawables()[2];
			d.clearColorFilter();
			d = buttonMouse.getCompoundDrawables()[2];
			d.clearColorFilter();
			d = buttonMedia.getCompoundDrawables()[2];
			d.clearColorFilter();
			d = buttonGamepad.getCompoundDrawables()[2];
			d.clearColorFilter();
		} else {
			d = buttonKeyboard.getCompoundDrawables()[2];
			d.setColorFilter(f);
			d = buttonMouse.getCompoundDrawables()[2];
			d.setColorFilter(f);
			d = buttonMedia.getCompoundDrawables()[2];
			d.setColorFilter(f);
			d = buttonGamepad.getCompoundDrawables()[2];
			d.setColorFilter(f);
		}		
	}
}
