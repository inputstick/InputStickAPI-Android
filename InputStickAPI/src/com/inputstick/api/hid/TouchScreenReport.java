package com.inputstick.api.hid;

import com.inputstick.api.Util;

public class TouchScreenReport extends HIDReport {
	
	public static final byte TOUCH_POINTER_REPORT_ID = 4; //TODO move to consumer?
	
	public static final int SIZE = 6;
	
	private byte[] data;
	
	
	public TouchScreenReport(boolean tipSwitch, boolean inRange, int x, int y) {
		data = new byte[SIZE];
		data[0] = TOUCH_POINTER_REPORT_ID;
		
		if (tipSwitch) {
			data[1] = 0x01;
		}
		if (inRange) {
			data[1] += 0x02;
		} 
		
		data[2] = Util.getLSB(x);
		data[3] = Util.getMSB(x);
		
		data[4] = Util.getLSB(y);
		data[5] = Util.getMSB(y);
	}
	
	public byte[] getBytes() {
		return data;
	}
	
	public int getBytesCount() {
		return SIZE;
	}	
	


}