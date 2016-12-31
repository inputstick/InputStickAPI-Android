package com.inputstick.api;

public class HIDInfo {
	
	private int state;
	
	private boolean numLock;
	private boolean capsLock;
	private boolean scrollLock;
	
	private boolean keyboardReportProtocol;
	private boolean mouseReportProtocol;
	
	private boolean keyboardReady;
	private boolean mouseReady;
	private boolean consumerReady;
	private boolean rawHIDReady;
	
	// >= 0.93
	private boolean sentToHostInfo;
	private int keyboardReportsSentToHost;
	private int mouseReportsSentToHost;
	private int consumerReportsSentToHost;
	private int rawHIDReportsSentToHost;
	

	public HIDInfo() {
		keyboardReportProtocol = true;
		mouseReportProtocol = true;
		sentToHostInfo = false;
	}
	
	public void update(byte[] data) {
		state = data[1];
		
		int leds = data[2];
		if ((leds & 0x01) != 0) {
			numLock = true;
		} else {
			numLock = false;
		}
		if ((leds & 0x02) != 0) {
			capsLock = true;
		} else {
			capsLock = false;
		}
		if ((leds & 0x04) != 0) {
			scrollLock = true;
		} else {
			scrollLock = false;
		}	
		
		if (data[3] == 0) {
			keyboardReportProtocol = true;
		} else {
			keyboardReportProtocol = false;
		}
		
		if (data[4] == 0) {
			keyboardReady = false;
		} else {
			keyboardReady = true;
		}
		
		if (data[5] == 0) {
			mouseReportProtocol = true;
		} else {
			mouseReportProtocol = false;
		}			
		
		if (data[6] == 0) {
			mouseReady = false;
		} else {
			mouseReady = true;
		}		
		
		if (data[7] == 0) {
			consumerReady = false;
		} else {
			consumerReady = true;
		}			
		if (data.length >= 12) {	
			if (data[11] == (byte)0xFF) {
				sentToHostInfo = true;
				keyboardReportsSentToHost = data[8] & 0xFF;
				mouseReportsSentToHost = data[9] & 0xFF;
				consumerReportsSentToHost = data[10] & 0xFF;
			}			
		}
		
		if (data.length >= 14) {	
			if (data[12] == 0) {
				rawHIDReady = false;
			} else {
				rawHIDReady = true;
			}	
			rawHIDReportsSentToHost = data[13] & 0xFF;			
		} else {
			//if no info was provided, assume that the raw HID interface is ready (buffer is empty)
			rawHIDReady = true;
		}
		
	}
	
	public void setKeyboardBusy() {
		keyboardReady = false;
	}
	
	public int getState() {
		return state;
	}
	
	public boolean getNumLock() {
		return numLock;
	}
	
	public boolean getCapsLock() {
		return capsLock;
	}
	
	public boolean getScrollLock() {
		return scrollLock;
	}	
	
	public boolean isKeyboardReportProtocol() {
		return keyboardReportProtocol;
	}
	
	public boolean isMouseReportProtocol() {
		return mouseReportProtocol;
	}	
	
	public boolean isKeyboardReady() {
		return keyboardReady;
	}
	
	public boolean isMouseReady() {
		return mouseReady;
	}
	
	public boolean isConsumerReady() {
		return consumerReady;
	}	
	
	public boolean isRawHIDReady() {
		return rawHIDReady;
	}	
	
	
	
	// > v0.93 firmware only
	
	public boolean isSentToHostInfoAvailable() {
		return sentToHostInfo;
	}
	
	public int getKeyboardReportsSentToHost() {
		return keyboardReportsSentToHost;
	}
	
	public int getMouseReportsSentToHost() {
		return mouseReportsSentToHost;
	}
	
	public int getConsumerReportsSentToHost() {
		return consumerReportsSentToHost;
	}
	
	public int getRawHIDReportsSentToHost() {
		return rawHIDReportsSentToHost;
	}
	
	
	
}
