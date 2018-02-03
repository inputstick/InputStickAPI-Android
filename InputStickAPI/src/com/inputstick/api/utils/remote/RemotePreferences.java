package com.inputstick.api.utils.remote;

import android.content.SharedPreferences;

import com.inputstick.api.layout.KeyboardLayout;

public class RemotePreferences {	
	
	protected KeyboardLayout layout;	
	protected int typingSpeed;
	protected boolean showModifiers;
	
	protected boolean showMouse;
	protected boolean touchScreenMode;
	protected float ratio;
	protected boolean tapToClick;
	protected int mouseSensitivity;	
	protected int scrollSensitivity;
	protected int proximityThreshold;	
	protected int tapInterval;
	
	
	public RemotePreferences() {		
	}
	
		
	public void reload(SharedPreferences sharedPref) {
		//keyboard
		layout = KeyboardLayout.getLayout(sharedPref.getString("inputstick_keyboard_layout", "en-US"));
		showModifiers = true;
		try {
			typingSpeed = Integer.parseInt(sharedPref.getString("inputstick_typing_speed", "1"));
			if ((typingSpeed < 0) && (typingSpeed > 100)) {
				typingSpeed = 1;
			}
		} catch (Exception e) {
			typingSpeed = 1;
		}		
		
		
		
		//mouse
		showMouse = true;
		touchScreenMode = sharedPref.getString("inputstick_mousepad_mode", "mouse").equals("touchscreen");
		ratio = 0; //fill entire area
		
		tapToClick = sharedPref.getBoolean("inputstick_tap_to_click", true); //if true, tapping mousepad area press left mosue button			
		mouseSensitivity = sharedPref.getInt("inputstick_sensitivity_mouse", 50);
		scrollSensitivity = sharedPref.getInt("inputstick_sensitivity_scroll", 50);		
		
		tapInterval = 500; //500ms, delay between two taps to be registered as a left mouse button click
		/*try {
			tapInterval = Integer.parseInt(sharedPref.getString("inputstick_click_speed", "500"));
		} catch (Exception e) {
		}*/
		
		proximityThreshold = 0; //0 = auto (3,3% of mousepad view diagonal) 
		/*try {
			proximityThreshold = Integer.parseInt(sharedPref.getString("inputstick_mouse_accuracy", "0"));
		} catch (Exception e) {
		}*/
	}
	
	
	//keyboard	
	public KeyboardLayout getKeyboardLayout() {
		return layout;
	}
	
	public boolean showModifiersArea() {
		return showModifiers;
	}
	
	public int getTypingSpeed() {
		return typingSpeed;
	}
	
	
	//mouse
	
	public boolean showMouseArea() {
		return showMouse;
	}
	
	public float getMousePadRatio() {
		if (touchScreenMode) {
			return ratio;
		} else {
			return 0;
		}
	}
	
	public boolean isInTouchScreenMode() {
		return touchScreenMode;
	}

	public boolean isTapToClick() {
		return tapToClick;
	}
	
	public int getMouseSensitivity() {
		return mouseSensitivity;
	}
	
	public int getScrollSensitivity() {
		return scrollSensitivity;
	}
	
	
	public int getTouchProximity() {
		return proximityThreshold;
	}
	
	public int getTapInterval() {
		return tapInterval;
	}
	
}
