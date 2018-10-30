package com.inputstick.api.basic;

import java.util.Arrays;
import java.util.Vector;

import com.inputstick.api.InputStickRawHIDListener;
import com.inputstick.api.hid.HIDTransaction;
import com.inputstick.api.hid.RawHIDReport;

public class InputStickRawHID {
	
	private static Vector<InputStickRawHIDListener> mRawHIDListeners = new Vector<InputStickRawHIDListener>();
	
	/*
	 * Send raw HID data to USB host. Data longer than 64B will be splitted into multiple raw HID reports.
	 * Note: requires firmware 0.98D or later. Raw HID interface must be manually enabled (InputStickUtility -> Device configuration -> USB Configuration
	 * PC demo apps: http://www.pjrc.com/teensy/rawhid.html
	 * Note: to make it work without any software modifications, in USB Configuration change VID and PID to 16C0 and 0480 respectively
	 * 
	 * @param data		data to send. 
	 */
	public static void sendRawHIDData(byte[] data) {
		if (data != null) {
			HIDTransaction t = new HIDTransaction();		
			int start, end;
			start = 0;
			while (start < data.length) {
				end = start + 64;
				if (end > data.length) {
					end = data.length;
				}
				t.addReport(new RawHIDReport(Arrays.copyOfRange(data, start, end)));
				InputStickHID.addRawHIDTransaction(t, true);
				start = end;
			}
		}
	}
		
	
	/*
	 * Adds InputStickRawHIDListener. Listener will be notified when USB hosts sends new HID report to raw HID interface.
	 * Note: requires firmware 0.98D or later. 
	 * 
	 * @param listener	listener to add
	 */
	public static void addRawHIDListener(InputStickRawHIDListener listener) {
		if (listener != null) {
			if ( !mRawHIDListeners.contains(listener)) {
				mRawHIDListeners.add(listener);
			}
		}
	}
	
	
	/*
	 * Removes InputStickRawHIDListener.
	 * Note: requires firmware 0.98D or later.
	 * 
	 * @param listener	listener to remove
	 */	
	public static void removeRawHIDListener(InputStickRawHIDListener listener) {
		if (listener != null) {
			mRawHIDListeners.remove(listener);
		}
	}
	
	
	
	protected static void notifyRawHIDListeners(byte[] data) {
		for (InputStickRawHIDListener listener : mRawHIDListeners) {
			listener.onRawHIDData(data);
		}		
	}

}
