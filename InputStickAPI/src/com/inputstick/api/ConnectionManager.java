package com.inputstick.api;

import java.util.ArrayList;
import java.util.Vector;

public abstract class ConnectionManager {
	
	public static final int STATE_DISCONNECTED = 0;
	public static final int STATE_FAILURE = 1;
	public static final int STATE_CONNECTING = 2;
	public static final int STATE_CONNECTED = 3;
	public static final int STATE_READY = 4;
	
	public static final int DISC_REASON_UNKNOWN = 				0x00;		//not specified, most likely an old version of InputStickUtility app is installed
	public static final int DISC_REASON_ERROR = 				0x01;		//disconnected due to an error
	public static final int DISC_REASON_APP_DISCONNECTED = 		0x10;		//disconnect was requested by the application
	public static final int DISC_REASON_UTILITY_DISCONNECTED = 	0x21;		//disconnect was requested by user using InputStickUtility app UI
	public static final int DISC_REASON_UTILITY_CANCELLED = 	0x22;		//connection attempt was cancelled by user using InputStickUtility app UI
	public static final int DISC_REASON_UTILITY_FORCED = 		0x23;		//connection was terminated because InputStickUtility requested exclusive access to the device (configuration, firmware update etc.)
	
	protected Vector<InputStickStateListener> mStateListeners = new Vector<InputStickStateListener>();
	protected Vector<InputStickDataListener> mDataListeners = new Vector<InputStickDataListener>();
	
	protected int mState;
	private int mErrorCode;
	private int mDisconnectReasonCode;	
	
	public abstract void connect();
	public abstract void disconnect();
	public abstract void sendPacket(Packet p);
	
	protected void stateNotify(int state) {
		stateNotify(state, false);
	}    
	
	protected void stateNotify(int state, boolean forceNotification) {
		if (( !forceNotification) && (mState == state )) {
			//do nothing
		} else {
			//notify all listeners
			mState = state;
			synchronized(mStateListeners) {
				ArrayList<InputStickStateListener> tmp = new ArrayList<InputStickStateListener>();
				for (InputStickStateListener listener : mStateListeners) {
					tmp.add(listener);
				}
				for (InputStickStateListener listener : tmp) {
					listener.onStateChanged(state);
				}
			}
		}
	}  
	
	public int getState() {
		return mState;
	}
	
	public boolean isReady() {
		if (mState == STATE_READY) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isConnected() {
		if ((mState == STATE_READY) || (mState == STATE_CONNECTED)) {
			return true;
		} else {
			return false;
		}
	}
	
	protected void resetErrorCode() {
		mErrorCode = InputStickError.ERROR_NONE;
		setDisconnectReason(DISC_REASON_UNKNOWN);
	}
	
	protected void setErrorCode(int code) {
		if (code != InputStickError.ERROR_NONE) {
			setDisconnectReason(DISC_REASON_ERROR);
		}
		mErrorCode = code;
	}
	
	public int getErrorCode() {
		return mErrorCode;
	}	
	
	protected void setDisconnectReason(int code) {
		mDisconnectReasonCode = code;
	}
	
	public int getDisconnectReason() {
		return mDisconnectReasonCode;
	}	
	
	protected void onData(byte[] data) {
		synchronized(mDataListeners) {
			ArrayList<InputStickDataListener> tmp = new ArrayList<InputStickDataListener>();
			for (InputStickDataListener listener : mDataListeners) {
				tmp.add(listener);
			}
			for (InputStickDataListener listener : tmp) {
				listener.onInputStickData(data);
			} 
		}
	}
	
	public void addStateListener(InputStickStateListener listener) {
		if (listener != null) {
			synchronized(mStateListeners) {
				if ( !mStateListeners.contains(listener)) {
					mStateListeners.add(listener);
				}
			}
		}	
	}
	
	public void removeStateListener(InputStickStateListener listener) {
		if (listener != null) {
			synchronized(mStateListeners) {
				mStateListeners.remove(listener);
			}
		}	
	}
	
	public void addDataListener(InputStickDataListener listener) {
		if (listener != null) {
			synchronized(mDataListeners) {
				if ( !mDataListeners.contains(listener)) {
					mDataListeners.add(listener);
				}
			}
		}				
	}
	
	public void removeDataListener(InputStickDataListener listener) {
		if (listener != null) {
			synchronized(mDataListeners) {
				mDataListeners.remove(listener);
			}
		}			
	}

}
