package com.inputstick.api.utils.remote;

import com.inputstick.api.basic.InputStickKeyboard;
import com.inputstick.api.basic.InputStickMouse;
import com.inputstick.api.basic.InputStickTouchScreen;

public class RemoteSupport {
	
	protected RemotePreferences preferences;
	
	protected boolean usedKeyboard;
	protected boolean usedMouse;
	protected boolean usedTouchscreen;
	
	protected int lastX, lastY;
	protected long lastActionTime;
	
	public RemoteSupport(RemotePreferences preferences) {
		this.preferences = preferences;
		lastActionTime = 0;
	}
	
	public RemotePreferences getPreferences() {
		return preferences;
	}
	
	public long getLastActionTime() {
		return lastActionTime;
	}
	
	//KEYBOARD:
	public void keyboardReport(byte modifiers, byte key0, byte key1, byte key2, byte key3, byte key4, byte key5) {
		InputStickKeyboard.customReport(modifiers, key0, key1, key2, key3, key4, key5);
		usedKeyboard = true;
		lastActionTime = System.currentTimeMillis();
	}
	
	public void pressAndRelease(byte modifiers, byte key) {		
		InputStickKeyboard.pressAndRelease(modifiers, key, preferences.getTypingSpeed());
		usedKeyboard = true;
		lastActionTime = System.currentTimeMillis();
	}
	
	public void type(String text, byte modifiers) {
		preferences.getKeyboardLayout().type(text, modifiers, preferences.getTypingSpeed());
		usedKeyboard = true;
		lastActionTime = System.currentTimeMillis();
	}

	
	//MOUSE:
	public void mouseReport(byte buttons, byte x, byte y, byte wheel) {
		InputStickMouse.customReport(buttons, x, y, wheel);
		usedMouse = true;
		lastActionTime = System.currentTimeMillis();
	}
	
	public void mouseClick(byte button, int n) {
		InputStickMouse.click(button, n);
		usedMouse = true;
		lastActionTime = System.currentTimeMillis();
	}
	
	
	//TOUCHSCREEN:
	public void moveTouchPointer(boolean buttonPressed, int x, int y) {
		InputStickTouchScreen.moveTouchPointer(buttonPressed, x, y);
		lastX = x;
		lastY = y;
		usedTouchscreen = true;
		lastActionTime = System.currentTimeMillis();
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
		lastActionTime = 0;
	}	
	
}
