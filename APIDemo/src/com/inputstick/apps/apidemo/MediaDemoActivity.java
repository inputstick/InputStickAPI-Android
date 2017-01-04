package com.inputstick.apps.apidemo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.inputstick.api.ConnectionManager;
import com.inputstick.api.InputStickStateListener;
import com.inputstick.api.basic.InputStickConsumer;
import com.inputstick.api.basic.InputStickHID;

public class MediaDemoActivity extends Activity implements InputStickStateListener {
	
	private Button buttonVolDown;
	private Button buttonMute;
	private Button buttonVolUp;		
	private Button buttonTrackPrev;
	private Button buttonPlayPause;
	private Button buttonTrackNext;
	private Button buttonLaunchBrowser;
	private Button buttonLaunchEmail;
	private Button buttonLaunchCalc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_media_demo);
		
		buttonVolDown = (Button)findViewById(R.id.buttonVolDown);
		buttonVolDown.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {				
				InputStickConsumer.consumerAction(InputStickConsumer.VOL_DOWN);				
			}
		});	
		
		buttonMute = (Button)findViewById(R.id.buttonMute);
		buttonMute.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {				
				InputStickConsumer.consumerAction(InputStickConsumer.VOL_MUTE);				
			}
		});	
		
		buttonVolUp = (Button)findViewById(R.id.buttonVolUp);
		buttonVolUp.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {				
				InputStickConsumer.consumerAction(InputStickConsumer.VOL_UP);				
			}
		});	
		
		buttonTrackPrev = (Button)findViewById(R.id.buttonTrackPrev);
		buttonTrackPrev.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {				
				InputStickConsumer.consumerAction(InputStickConsumer.TRACK_PREV);				
			}
		});	
		
		buttonPlayPause = (Button)findViewById(R.id.buttonPlayPause);
		buttonPlayPause.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {				
				InputStickConsumer.consumerAction(InputStickConsumer.PLAY_PAUSE);				
			}
		});	
		
		buttonTrackNext = (Button)findViewById(R.id.buttonTrackNext);
		buttonTrackNext.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {				
				InputStickConsumer.consumerAction(InputStickConsumer.TRACK_NEXT);				
			}
		});	
		
		buttonLaunchBrowser = (Button)findViewById(R.id.buttonLaunchBrowser);
		buttonLaunchBrowser.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {				
				InputStickConsumer.consumerAction(InputStickConsumer.LAUNCH_BROWSER);				
			}
		});	
		
		buttonLaunchEmail = (Button)findViewById(R.id.buttonLaunchEmail);
		buttonLaunchEmail.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {				
				InputStickConsumer.consumerAction(InputStickConsumer.LAUNCH_EMAIL);				
			}
		});	
		
		buttonLaunchCalc = (Button)findViewById(R.id.buttonLaunchCalc);
		buttonLaunchCalc.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {				
				InputStickConsumer.consumerAction(InputStickConsumer.LAUNCH_CALC);				
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
		InputStickHID.addStateListener(this);
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
		buttonVolDown.setEnabled(enabled);		
		buttonMute.setEnabled(enabled);
		buttonVolUp.setEnabled(enabled);
		
		buttonTrackPrev.setEnabled(enabled);		
		buttonPlayPause.setEnabled(enabled);
		buttonTrackNext.setEnabled(enabled);
		
		buttonLaunchBrowser.setEnabled(enabled);		
		buttonLaunchEmail.setEnabled(enabled);
		buttonLaunchCalc.setEnabled(enabled);
	}	
}
