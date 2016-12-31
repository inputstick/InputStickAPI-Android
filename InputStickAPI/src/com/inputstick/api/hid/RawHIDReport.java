package com.inputstick.api.hid;

public class RawHIDReport extends HIDReport {
	
	public static final int SIZE = 64;
	
	private byte[] data;

	public RawHIDReport(byte[] rawData) {
		data = new byte[SIZE];		
		
		int max;
		if (rawData == null) {
			max = 0;
		} else {
			max = rawData.length;
		}
		if (max > SIZE) {
			max = SIZE;
		}
		
		for (int i = 0; i < max; i++) {
			data[i] = rawData[i];
		}
	}
	
	public RawHIDReport() {
		this(null);
	}	
	
	public byte[] getBytes() {
		return data;
	}
	
	public int getBytesCount() {
		return SIZE;
	}	
	
}
