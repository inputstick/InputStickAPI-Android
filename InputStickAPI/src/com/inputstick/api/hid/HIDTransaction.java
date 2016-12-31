package com.inputstick.api.hid;

import java.util.LinkedList;

public class HIDTransaction {
	
	public static final byte TRANSACTION_CMD_DEFAULT = 0;

	private int mID;
	private byte mTransactionTypeCmd;
	private LinkedList<HIDReport> reports;
	
	public HIDTransaction(byte transactionTypeCmd) {
		mTransactionTypeCmd = transactionTypeCmd;
		reports = new LinkedList<HIDReport>();
	}
	
	public HIDTransaction() {
		this(TRANSACTION_CMD_DEFAULT);
	}
	
	public void addReport(HIDReport report) {
		reports.add(report);
	}
	
	public int getReportsCount() {
		return reports.size();
	}
	
	public void setID(int id) {
		mID = id;
	}
	
	public int getID() {
		return mID;
	}
	
	public byte getTransactionTypeCmd() {
		return mTransactionTypeCmd;
	}
	
	public boolean hasNext() {
		return !reports.isEmpty();
	}
	
	public byte[] getNextReport() {
		return reports.poll().getBytes();
	}
	
	public HIDReport getHIDReportAt(int pos) {
		return reports.get(pos);
	}
	
	public HIDTransaction split(int n) {
		HIDTransaction result = new HIDTransaction();
		HIDReport report;
		if (n <= reports.size()) {
			while(n > 0) {
				report = reports.poll();
				result.addReport(report);
				n--;
			}		
		}
		
		return result;
	}
	
}
