package com.inputstick.api.hid;

import java.util.LinkedList;

import com.inputstick.api.ConnectionManager;
import com.inputstick.api.HIDInfo;
import com.inputstick.api.Packet;
import com.inputstick.api.basic.InputStickHID;

public class HIDTransactionQueue {
	
	private static final int DEFAULT_BUFFER_SIZE = 32;
	private static final int DEFAULT_MAX_REPORTS_PER_PACKET = 32;

	private final LinkedList<HIDTransaction> queue;
	private final ConnectionManager mConnectionManager;
	private final byte cmd;
	private final int mBufferCapacity;	
	
	private int mFreeSpace;	
	private int mSentSinceLastNotification;	
	private int mMaxReportsPerPacket;	
	private int mInterfaceType;
	
	
	public HIDTransactionQueue(int interfaceType, ConnectionManager connectionManager, int bufferCapacity, int maxReportsPerPacket) {
		mBufferCapacity = bufferCapacity;
		mFreeSpace = bufferCapacity;
		mMaxReportsPerPacket = maxReportsPerPacket;
		mSentSinceLastNotification = 0;
		
		queue = new LinkedList<HIDTransaction>();
		mConnectionManager = connectionManager;
		
		mInterfaceType = interfaceType;
		switch (interfaceType) {
			case InputStickHID.INTERFACE_KEYBOARD:
				cmd = Packet.CMD_HID_DATA_KEYB;
				break;
			case InputStickHID.INTERFACE_MOUSE:
				cmd = Packet.CMD_HID_DATA_MOUSE;
				break;
			case InputStickHID.INTERFACE_CONSUMER:
				cmd = Packet.CMD_HID_DATA_CONSUMER;
				break;
			case InputStickHID.INTERFACE_RAW_HID:
				cmd = Packet.CMD_HID_DATA_RAW;
				break;				
			default:
				cmd = Packet.CMD_DUMMY;
		}
	}
	
	public HIDTransactionQueue(int interfaceType, ConnectionManager connectionManager) {
		this(interfaceType, connectionManager, DEFAULT_BUFFER_SIZE, DEFAULT_MAX_REPORTS_PER_PACKET);
	}
	
	
	public synchronized void update(HIDInfo hidInfo) {
		switch (mInterfaceType) {
			case InputStickHID.INTERFACE_KEYBOARD:
				mFreeSpace += hidInfo.getKeyboardReportsSentToHost();
				break;
			case InputStickHID.INTERFACE_MOUSE:
				mFreeSpace += hidInfo.getMouseReportsSentToHost();
				break;
			case InputStickHID.INTERFACE_CONSUMER:
				mFreeSpace += hidInfo.getConsumerReportsSentToHost();
				break;
			case InputStickHID.INTERFACE_RAW_HID:
				mFreeSpace += hidInfo.getRawHIDReportsSentToHost();
				break;				
		}
		if (mFreeSpace > mBufferCapacity) {
			mFreeSpace = mBufferCapacity;
		}
		
		/* TODO111 leave as failsafe?
		if (interfaceReady) {
			interfaceReadyCnt++;
			if (interfaceReadyCnt == 10) {
				bufferFreeSpace = bufferSize;
			}
		} else {
			interfaceReadyCnt = 0;
		}*/
		
		if (queue.isEmpty()) {
			if ((mFreeSpace == mBufferCapacity) && (mSentSinceLastNotification != 0)) {
				mSentSinceLastNotification = 0;
				notifyOnRemoteBufferEmpty();			
			}
		} else {
			sendFromQueue();
		}
	}
	
	
	private synchronized void sendFromQueue() {		
		if ( !queue.isEmpty() && mFreeSpace > 0) {
			int remainingReports = mFreeSpace;  

			byte reports = 0;		
			Packet p = new Packet(false, cmd, reports);
			
			HIDTransaction transaction = queue.peek();
			//allow only one type of transaction (consumer control/touch-screen/gamepad) to be sent in a single packet (consumer control interface specific)
			byte firstTransactionCmd = transaction.getTransactionTypeCmd();
			byte transactionCmd;
			
			while(true) {
				transaction = queue.peek();
				if (transaction == null) {
					break;
				}
				transactionCmd = transaction.getTransactionTypeCmd();
				if (transactionCmd != firstTransactionCmd) {
					break;
				}				
				if (transaction.getReportsCount() > remainingReports) {
					break;
				}
								
				remainingReports -= transaction.getReportsCount();
				reports += transaction.getReportsCount();
				while (transaction.hasNext()) {
					p.addBytes(transaction.getNextReport());
				}						
				queue.removeFirst();
			}
			
			if (reports > 0) {
				if (firstTransactionCmd != HIDTransaction.TRANSACTION_CMD_DEFAULT) {
					p.modifyByte(0, firstTransactionCmd);
				}
				p.modifyByte(1, reports);
				mConnectionManager.sendPacket(p);
				mFreeSpace -= reports;
				mSentSinceLastNotification += reports;
			}
			
			if (queue.isEmpty()) {
				notifyOnLocalBufferEmpty();
			}
		}
				
	}	
	
	public synchronized void addTransaction(HIDTransaction transaction) {
		//split transaction if necessary to make sure if can fit into: single packet	
		while (transaction.getReportsCount() > mMaxReportsPerPacket) {
			HIDTransaction t = transaction.split(mMaxReportsPerPacket);
			queue.add(t);
		}
		
		queue.add(transaction);
		sendFromQueue();
	}		
	
	private void notifyOnRemoteBufferEmpty() {
		InputStickHID.sendEmptyBufferNotifications(1, mInterfaceType);
	}
	
	private void notifyOnLocalBufferEmpty() {
		InputStickHID.sendEmptyBufferNotifications(2, mInterfaceType);
	}
	
	public synchronized boolean isLocalBufferEmpty() {
		return queue.isEmpty();
	}
	
	public synchronized boolean isRemoteBufferEmpty() {
		return ((queue.isEmpty()) && (mFreeSpace == mBufferCapacity));
	}
	
	public synchronized void clearBuffer() {
		queue.clear();
	}	

	
}
