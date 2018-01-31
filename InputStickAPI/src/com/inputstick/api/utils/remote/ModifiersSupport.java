package com.inputstick.api.utils.remote;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.inputstick.api.ConnectionManager;
import com.inputstick.api.hid.HIDKeycodes;

public class ModifiersSupport {
	
	private RemoteSupport mRemote;
	
	private ViewGroup layoutMain;
	private ToggleButton toggleButtonCtrl;
	private ToggleButton toggleButtonShift;
	private ToggleButton toggleButtonAlt;
	private ToggleButton toggleButtonGui;
	private ToggleButton toggleButtonAltGr;
	private Button buttonContext;	
	
	private boolean isResetting;	
	
	public ModifiersSupport(RemoteSupport remote, ViewGroup layout, ToggleButton ctrl, ToggleButton shift, ToggleButton alt, ToggleButton gui, ToggleButton altGr, Button context) {
		mRemote = remote;
		layoutMain = layout;
		toggleButtonCtrl = ctrl;
		toggleButtonShift = shift;
		toggleButtonAlt = alt;
		toggleButtonGui = gui;
		toggleButtonAltGr = altGr;
		buttonContext = context;
		
		toggleButtonCtrl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
				if (isResetting) return;
				update();
			}
		});
		toggleButtonShift.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
				if (isResetting) return;
				update();
			}
		});
		toggleButtonAlt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
				if (isResetting) return;
				update();
			}
		});
		toggleButtonGui.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
				if (isResetting) return;
				update();
			}
		});
		toggleButtonAltGr.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
				if (isResetting) return;				
				update();
			}
		});		
		
		
		
		toggleButtonCtrl.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mRemote.pressAndRelease(HIDKeycodes.CTRL_LEFT, HIDKeycodes.NONE);
				return true;
			}			
		});
		
		toggleButtonShift.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mRemote.pressAndRelease(HIDKeycodes.SHIFT_LEFT, HIDKeycodes.NONE);
				return true;
			}			
		});
		
		toggleButtonAlt.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mRemote.pressAndRelease(HIDKeycodes.ALT_LEFT, HIDKeycodes.NONE);
				return true;
			}			
		});		
		
		toggleButtonGui.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mRemote.pressAndRelease(HIDKeycodes.GUI_LEFT, HIDKeycodes.NONE);
				return true;
			}			
		});		
		
		toggleButtonAltGr.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				mRemote.pressAndRelease(HIDKeycodes.ALT_RIGHT, HIDKeycodes.NONE);
				return true;
			}			
		});
		
		if (buttonContext != null) {
			buttonContext.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {					
					mRemote.pressAndRelease(getModifiers(), HIDKeycodes.KEY_APPLICATION); 
				}			
			});
		}
	}
		
	
	public void resetModifiers() {
		isResetting = true;
		toggleButtonCtrl.setChecked(false);
		toggleButtonShift.setChecked(false);
		toggleButtonAlt.setChecked(false);
		toggleButtonGui.setChecked(false);
		toggleButtonAltGr.setChecked(false);
		isResetting = false;
		update();
	}
	
	public void manageUI(int state) {
		boolean enabled = (state == ConnectionManager.STATE_READY);
		
		if (mRemote.getPreferences().showModifiersArea()) {			
			layoutMain.setVisibility(View.VISIBLE);			
			
			toggleButtonCtrl.setEnabled(enabled);
			toggleButtonShift.setEnabled(enabled);
			toggleButtonAlt.setEnabled(enabled);
			toggleButtonGui.setEnabled(enabled);
			toggleButtonAltGr.setEnabled(enabled);
			if (buttonContext != null) {
				buttonContext.setEnabled(enabled);
			}
		} else {
			layoutMain.setVisibility(View.GONE);
		}
	}
	
    public byte getModifiers() {
    	byte modifier = 0;    	
    	if (mRemote.getPreferences().showModifiersArea()) {    	
			if (toggleButtonCtrl.isChecked()) {
				modifier |= HIDKeycodes.CTRL_LEFT;
			}
			if (toggleButtonShift.isChecked()) {
				modifier |= HIDKeycodes.SHIFT_LEFT;
			}
			if (toggleButtonAlt.isChecked()) {
				modifier |= HIDKeycodes.ALT_LEFT;
			}
			if (toggleButtonGui.isChecked()) {
				modifier |= HIDKeycodes.GUI_LEFT;
			}
			if (toggleButtonAltGr.isChecked()) {
				modifier |= HIDKeycodes.ALT_RIGHT;
			}
    	}
		return modifier;
    }	
    
    private void update() {
    	byte modifiers = getModifiers();    	
    	mRemote.keyboardReport(modifiers, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0, (byte)0);
    }

}
