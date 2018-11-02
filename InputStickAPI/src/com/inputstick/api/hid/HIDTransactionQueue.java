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
	private int mBufferEmptyCnt;
	
	
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
		int freedSpace = 0;
		boolean bufferEmpty = false;
		switch (mInterfaceType) {
			case InputStickHID.INTERFACE_KEYBOARD:
				freedSpace = hidInfo.getKeyboardReportsSentToHost();
				bufferEmpty = hidInfo.isKeyboardReady();
				break;
			case InputStickHID.INTERFACE_MOUSE:
				freedSpace = hidInfo.getMouseReportsSentToHost();
				bufferEmpty = hidInfo.isMouseReady();
				break;
			case InputStickHID.INTERFACE_CONSUMER:
				freedSpace = hidInfo.getConsumerReportsSentToHost();
				bufferEmpty = hidInfo.isConsumerReady();
				break;
			case InputStickHID.INTERFACE_RAW_HID:
				freedSpace = hidInfo.getRawHIDReportsSentToHost();
				bufferEmpty = hidInfo.isRawHIDReady();
				break;				
		}
		
		mFreeSpace += freedSpace;
		
		//failsafe:
		if (mFreeSpace > mBufferCapacity) {
			mFreeSpace = mBufferCapacity;
		}				
		if (bufferEmpty) {
			mBufferEmptyCnt++;
			if (mBufferEmptyCnt == 10) {
				mFreeSpace = mBufferCapacity;
			}
		} else {
			mBufferEmptyCnt = 0;
		}
		
		
		
		if (queue.isEmpty()) {
			if ((mFreeSpace == mBufferCapacity) && (mSentSinceLastNotification != 0)) {
				mSentSinceLastNotification = 0;
				notifyOnRemoteBufferEmpty();			
			}
		} else {
			sendFromQueue();				
		}
	}
	
	
	public synchronized void sendFromQueue() {		
		boolean didSend;
		do {
			didSend = false;
			if ( !queue.isEmpty() && mFreeSpace > 0) {			
				byte reports = 0;		
				int remainingReports = (mFreeSpace > mMaxReportsPerPacket) ? mMaxReportsPerPacket : mFreeSpace;  			
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
				
				if (reports > 0) {
					didSend = true;
				}
			} 			
		} while (didSend);
	}	
	
	public synchronized void addTransaction(HIDTransaction transaction, boolean sendNow) {
		//split transaction if necessary to make sure if can fit into: single packet	
		while (transaction.getReportsCount() > mMaxReportsPerPacket) {
			HIDTransaction t = transaction.split(mMaxReportsPerPacket);
			queue.add(t);
		}
		
		queue.add(transaction);
		if (sendNow) {
			sendFromQueue();
		}
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
