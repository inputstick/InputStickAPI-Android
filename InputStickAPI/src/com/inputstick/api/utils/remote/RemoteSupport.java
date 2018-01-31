package com.inputstick.api.utils.remote;

import com.inputstick.api.basic.InputStickKeyboard;
import com.inputstick.api.basic.InputStickMouse;
import com.inputstick.api.basic.InputStickTouchScreen;

public class RemoteSupport {
	
	private RemotePreferences mPreferences;
	
	private boolean usedKeyboard;
	private boolean usedMouse;
	private boolean usedTouchscreen;
	
	private int lastX, lastY;
	
	public RemoteSupport(RemotePreferences preferences) {
		mPreferences = preferences;
	}
	
	public RemotePreferences getPreferences() {
		return mPreferences;
	}
	
	//KEYBOARD:
	public void keyboardReport(byte modifiers, byte key0, byte key1, byte key2, byte key3, byte key4, byte key5) {
		InputStickKeyboard.customReport(modifiers, key0, key1, key2, key3, key4, key5);
		usedKeyboard = true;
	}
	
	public void pressAndRelease(byte modifiers, byte key) {
		InputStickKeyboard.pressAndRelease(modifiers, key);
		usedKeyboard = true;
	}
	
	public void type(String text, byte modifiers) {
		mPreferences.getKeyboardLayout().type(text, modifiers);
		usedKeyboard = true;
	}

	
	//MOUSE:
	public void mouseReport(byte buttons, byte x, byte y, byte wheel) {
		InputStickMouse.customReport(buttons, x, y, wheel);
		usedMouse = true;
	}
	
	public void mouseClick(byte button, int n) {
		InputStickMouse.click(button, n);
		usedMouse = true;
	}
	
	
	//TOUCHSCREEN:
	public void moveTouchPointer(boolean buttonPressed, int x, int y) {
		InputStickTouchScreen.moveTouchPointer(buttonPressed, x, y);
		lastX = x;
		lastY = y;
		usedTouchscreen = true;
	}
	
	//use last known position
	public void goOutOfRange() {
		if (usedTouchscreen) {
			InputStickTouchScreen.goOutOfRange(lastX, lastY);			
		} else {
			InputStickTouchScreen.goOutOfRange(0, 0);			
		}
		usedTouchscreen = false;
	}
	
	public void goOutOfRange(int x, int y) {
		InputStickTouchScreen.goOutOfRange(x, y);		
		usedTouchscreen = false;
	}
	

	public void resetHIDInterfaces() {
		if (usedKeyboard) {
			InputStickKeyboard.customReport((byte)0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00);
			usedKeyboard = false;
		}
		if (usedMouse) {
			InputStickMouse.customReport((byte)0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00);
			usedMouse = false;
		}
		if (usedTouchscreen) {
			InputStickTouchScreen.goOutOfRange(lastX, lastY);
			usedTouchscreen = false;
		}
	}	
	
}
