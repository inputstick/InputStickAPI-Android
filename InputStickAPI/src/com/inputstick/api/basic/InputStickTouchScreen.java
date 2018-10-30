package com.inputstick.api.basic;

import com.inputstick.api.Packet;
import com.inputstick.api.hid.HIDTransaction;
import com.inputstick.api.hid.TouchScreenReport;

public class InputStickTouchScreen {
	
	/*
	 * Touch screen interface works with Windows 7 or later (tested on 7 and 10), Linux (tested on Linux Mint) and OS X (tested on 10.12)
	 * Touch screen interface uses absolute screen coordinates. It always allows to move mouse pointer to desired point on the screen, no matter what was its initial position.
	 * (mouse interface uses relative coordinates)
	 * Note: requires firmware 0.98D or later. 
	 * 
	 */
	
	private InputStickTouchScreen() {
		
	}

	/*
	 * Move mouse pointer to position specified by x and y
	 * 
	 * @param x	x position of mouse pointer (10000 = 100% of screen width) 
	 * @param y	y position of mouse pointer (10000 = 100% of screen height)
	 */
	public static void moveTouchPointer(int x, int y) {
		moveTouchPointer(false, x, y);		
	}
	
	
	/*
	 * Move mouse pointer to position specified by x and y, optionally pressing tip switch (left mouse button)
	 * 
	 * @param buttonPressed	state of tip switch (same as left mouse button)
	 * @param x	x position of mouse pointer (10000 = 100% of screen width) 
	 * @param y	y position of mouse pointer (10000 = 100% of screen height)
	 */
	public static void moveTouchPointer(boolean buttonPressed, int x, int y) {
		HIDTransaction t = new HIDTransaction(Packet.CMD_HID_DATA_TOUCHSCREEN);
		t.addReport(new TouchScreenReport(buttonPressed, true, x, y));
		InputStickHID.addConsumerTransaction(t, true);		
	}
	
	
	/*
	 * Clicks tip switch (left mouse button) N times at position specified by x and y
	 * 
	 * @param n	number of clicks (press and release events)
	 * @param x	x position of mouse pointer (10000 = 100% of screen width) 
	 * @param y	y position of mouse pointer (10000 = 100% of screen height)
	 */
	public static void click(int n, int x, int y) {
		HIDTransaction t = new HIDTransaction(Packet.CMD_HID_DATA_TOUCHSCREEN);				
		t.addReport(new TouchScreenReport(false, true, x, y));
		for (int i = 0; i < n; i++) {								
			t.addReport(new TouchScreenReport(true, true, x, y)); //press
			t.addReport(new TouchScreenReport(false, true, x, y)); //release			
		}			
		InputStickHID.addConsumerTransaction(t, true);	
	}
	
	
	/*
	 * Move mouse pointer to upper left corner (0,0) and simulates device (stylus) going out of range. Once out of range, Windows will no longer display touchscreen interface. Can behave differently on other OS.
	 * 
	 */
	public static void goOutOfRange() {
		HIDTransaction t = new HIDTransaction(Packet.CMD_HID_DATA_TOUCHSCREEN);
		t.addReport(new TouchScreenReport(false, false, 0, 0));
		InputStickHID.addConsumerTransaction(t, true);
	}
	
	
	/*
	 * Move mouse pointer to position specified by x and y and simulates device (stylus) going out of range. Once out of range, Windows will no longer display touchscreen interface UI. Can behave differently on other OS.
	 * 
	 * @param x	x position of mouse pointer (10000 = 100% of screen width) 
	 * @param y	y position of mouse pointer (10000 = 100% of screen height)	 
	 */
	public static void goOutOfRange(int x, int y) {
		HIDTransaction t = new HIDTransaction(Packet.CMD_HID_DATA_TOUCHSCREEN);
		t.addReport(new TouchScreenReport(false, false, x, y));
		InputStickHID.addConsumerTransaction(t, true);
	}	
	
	
	/*
	 * Send custom touch screen report
	 * 
	 * @param tipSwitch	state of tip switch (same as left mouse button)
	 * @param inRange	specifies if emulated mouse pointing device is in range
	 * @param x	x position of mouse pointer (10000 = 100% of screen width) 
	 * @param y	y position of mouse pointer (10000 = 100% of screen height)
	 */
	public static void customTouchPointerReport(boolean tipSwitch, boolean inRange, int x, int y) {
		HIDTransaction t = new HIDTransaction(Packet.CMD_HID_DATA_TOUCHSCREEN);
		t.addReport(new TouchScreenReport(tipSwitch, inRange, x, y));
		InputStickHID.addConsumerTransaction(t, true);		
	}
	

}
