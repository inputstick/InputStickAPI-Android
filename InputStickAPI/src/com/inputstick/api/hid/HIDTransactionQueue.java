package com.inputstick.api.hid;

import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import com.inputstick.api.ConnectionManager;
import com.inputstick.api.HIDInfo;
import com.inputstick.api.OnEmptyBufferListener;
import com.inputstick.api.Packet;
import com.inputstick.api.basic.InputStickHID;

public class HIDTransactionQueue {
	
	private static final int DEFAULT_BUFFER_SIZE = 32;
	private static final int BT_DELAY = 50; //additional delay for BT overhead
	
	private static final int MAX_PACKETS_PER_UPDATE = 10;
	private static final int MAX_IMMEDIATE_PACKETS = 3;

	private final LinkedList<HIDTransaction> queue;
	private final ConnectionManager mConnectionManager;
	private final byte cmd;
	private final int bufferSize;
	private boolean ready;
	
	private int mInterfaceType;
	private boolean mustNotify;
	
	private Timer t;
	private boolean timerCancelled;
	private boolean sentAhead;
	private long lastTime;
	private long minNextTime;
	private int lastReports;	
	
	
	// >= FW 0.93
	private boolean bufferInitDone;
	private boolean constantUpdateMode;
	private int bufferFreeSpace;
	private int immediatePacketsLeft;
	private int packetsSentSinceLastUpdate;
	
	private int interfaceReadyCnt; //fix BT4.0 lost packet problem
	
	public HIDTransactionQueue(int interfaceType, ConnectionManager connectionManager, int bufferSize) {
		this.bufferSize = bufferSize;
		constantUpdateMode = false;
		bufferFreeSpace = bufferSize;
		interfaceReadyCnt = 0;
		
		queue = new LinkedList<HIDTransaction>();
		mConnectionManager = connectionManager;
		ready = false;
		sentAhead = false;
		minNextTime = 0;
		
		mustNotify = false;
		
		mInterfaceType = interfaceType;
		switch (interfaceType) {
			case InputStickHID.INTERFACE_KEYBOARD:
				cmd = Packet.CMD_HID_DATA_KEYB;
				//TODO mod
				//cmd = Packet.CMD_HID_DATA_KEYB_FAST;
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
		this(interfaceType, connectionManager, DEFAULT_BUFFER_SIZE);
	}
	
	private int sendNext(int maxReports) {
		HIDTransaction transaction;		
		byte firstTransactionCmd;
		byte transactionCmd;
		
		//assume there is at least 1 element in queue		
		transaction = queue.peek();
		if (transaction.getReportsCount() > maxReports) {
			// v0.92
			//we can late a little longer for the buffer to free up some space, do not split the transaction yet
			if (maxReports < bufferSize) {
				return 0;
			}
			
			//transaction too big to fit single packet! split
			transaction = transaction.split(bufferSize);
		} else {
			queue.removeFirst();						
		}
		
		byte reports = 0;
		ready = false;		
		Packet p = new Packet(false, cmd, reports);
		firstTransactionCmd = transaction.getTransactionTypeCmd();
				
		while (transaction.hasNext()) {
			p.addBytes(transaction.getNextReport());
			reports++;
		}		
		
		while(true) {
			if (queue.isEmpty()) {
				break;
			}
			
			transaction = queue.peek();
			transactionCmd = transaction.getTransactionTypeCmd();
			//do not allow to send multiple transaction types (
			if (transactionCmd != firstTransactionCmd) {
				break;
			}
			
			if (reports + transaction.getReportsCount() < maxReports) {
				queue.removeFirst();	
				while (transaction.hasNext()) {
					p.addBytes(transaction.getNextReport());			
					reports++;
				}				
			} else {
				break;
			}
		}
		
		//!! total number of reports must be < 32 ! (max packet limitation)
		p.modifyByte(1, reports); //set reports count
		
		//solves touchscreen/gamepad buffering problem:
		//if transaction is not using interface-default command, change the command (used for consumer control interface: handling consumer/gamepad/touchscreen reports)
		if (firstTransactionCmd != HIDTransaction.TRANSACTION_CMD_DEFAULT) {
			p.modifyByte(0, firstTransactionCmd);
		}
		mConnectionManager.sendPacket(p);			
		
		interfaceReadyCnt = 0;
		lastReports = reports;
		lastTime = System.currentTimeMillis();
		minNextTime = lastTime + (lastReports * 4) + BT_DELAY;
		
		if (queue.isEmpty()) {
			notifyOnLocalBufferEmpty();
		}
		
		return reports;
	}
	
	private void notifyOnRemoteBufferEmpty() {
		Vector<OnEmptyBufferListener> listeners = InputStickHID.getBufferEmptyListeners();
		for (OnEmptyBufferListener listener : listeners) {
			listener.onRemoteBufferEmpty(mInterfaceType);
		}
	}
	
	private void notifyOnLocalBufferEmpty() {
		Vector<OnEmptyBufferListener> listeners = InputStickHID.getBufferEmptyListeners();
		for (OnEmptyBufferListener listener : listeners) {
			listener.onLocalBufferEmpty(mInterfaceType);
		}
	}
	
	public synchronized boolean isLocalBufferEmpty() {
		return queue.isEmpty();
	}
	
	public synchronized boolean isRemoteBufferEmpty() {
		if ((queue.isEmpty()) && (bufferFreeSpace == bufferSize)) {
			return true;
		}
		
		if (queue.isEmpty() && ( !mustNotify)) {
			return true;
		} else {
			return false;
		}
	}
	
	public synchronized void clearBuffer() {
		queue.clear();
	}
	
	public synchronized void addTransaction(HIDTransaction transaction) {
		if ( !bufferInitDone) {
			queue.add(transaction);		
			return;
		}
		
		
		if (constantUpdateMode) {
			queue.add(transaction);		
			sendToBuffer(true);
			return;
		}
		
		
		mustNotify = true;
		//using sentAhead will slow down mouse. FW0.92 will solve the problems
		if ((queue.isEmpty()) && (System.currentTimeMillis() > minNextTime) /*&& ( !sentAhead)*/) {
			sentAhead = true;
			ready = true;
		} 
		
		queue.add(transaction);						
		if (ready) {
			sendNext(bufferSize);
		} 		
	}	

	private synchronized void timerAction() {
		if ( !timerCancelled) {
			if (sentAhead) {
				deviceReady(null, 0); //will set sentAhead to false;
				sentAhead = true; //restore value
			} else {
				deviceReady(null, 0);
			}
		}
	}	
	
	public synchronized void deviceReady(HIDInfo hidInfo, int reportsSentToHost) {
		//it is possible that in the meantime some packets has been sent to IS!!!
		
		bufferInitDone = true;
		
		if (hidInfo != null) {			
			if (hidInfo.isSentToHostInfoAvailable()) {
				
				//BT4.0 lost packets fix:
				if (bufferFreeSpace < bufferSize) {
					boolean interfaceReady = false;
					if (mInterfaceType == InputStickHID.INTERFACE_KEYBOARD) {
						interfaceReady = hidInfo.isKeyboardReady();
					}
					if (mInterfaceType == InputStickHID.INTERFACE_MOUSE) {
						interfaceReady = hidInfo.isMouseReady();
					}
					if (mInterfaceType == InputStickHID.INTERFACE_CONSUMER) {
						interfaceReady = hidInfo.isConsumerReady();
					}
					if (mInterfaceType == InputStickHID.INTERFACE_RAW_HID) {
						interfaceReady = hidInfo.isRawHIDReady();
					}
					if (interfaceReady) {
						interfaceReadyCnt++;
						if (interfaceReadyCnt == 10) {
							bufferFreeSpace = bufferSize;
						}
					} else {
						interfaceReadyCnt = 0;
					}
				}
				
				
				constantUpdateMode = true;
				// >= FW 0.93
				bufferFreeSpace += reportsSentToHost;
				if ((bufferFreeSpace == bufferSize) && (queue.isEmpty())) {
					notifyOnRemoteBufferEmpty();
				}
				immediatePacketsLeft = MAX_IMMEDIATE_PACKETS;
				//reportsSentSinceLastUpdate = 0;
				packetsSentSinceLastUpdate = 0;						
				sendToBuffer(false);
				return;
			} 			
		}
		
		
		
		long now = System.currentTimeMillis();
		//System.out.println("v90 HID update");
		if (now < minNextTime) {
			//set timer, just in case if deviceReady won't be called again					
			timerCancelled = false;
			t = new Timer();
			t.schedule(new TimerTask() {
				@Override
				public void run() {
					timerAction();
				}
			}, (minNextTime - now + 1));						
		} else {	
			timerCancelled = true;
			sentAhead = false;
			if (!queue.isEmpty()) {
				sendNext(bufferSize);
			} else {			
				ready = true;
				//queue is empty, InputStick reported that buffer is empty, data was added since last notification
				if (mustNotify) {
					notifyOnRemoteBufferEmpty();
					mustNotify = false;
				}
			}
		}
	}		
	
	public synchronized void sendToBuffer(boolean justAdded) {
		if ((justAdded) && (immediatePacketsLeft <= 0)) {
			return;
		}
		
		if ( !InputStickHID.isReady()) {
			return;
		}	
		
		if (queue.isEmpty()) {
			return;
		}
		if (bufferFreeSpace <= 0) {
			return;
		}
		if (packetsSentSinceLastUpdate >= MAX_PACKETS_PER_UPDATE) {
			return;
		}		
		
		int reportsSent = sendNext(bufferFreeSpace);
		if (reportsSent > 0) {
			if (justAdded) {
				immediatePacketsLeft --;
			}
			bufferFreeSpace -= reportsSent;
			packetsSentSinceLastUpdate ++;	
		}
	}
	
}
