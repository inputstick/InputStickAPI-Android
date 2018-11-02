package com.inputstick.api.hid;

public class ShortKeyboardReport extends HIDReport {
	
	public static final int SIZE = 2; 
	
	private byte[] data;

	public ShortKeyboardReport(byte modifier, byte key) {
		data = new byte[SIZE];
		data[0] = modifier;
		data[1] = key;
	}
	
	public ShortKeyboardReport() {
		this((byte)0, (byte)0);
	}
	
	public byte[] getBytes() {
		return data;
	}
	
	public int getBytesCount() {
		return SIZE;
	}
	
}