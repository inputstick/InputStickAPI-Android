package com.inputstick.api.utils.remote;

import java.util.Timer;
import java.util.TimerTask;

import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import com.inputstick.api.ConnectionManager;
import com.inputstick.api.basic.InputStickMouse;

public class MousePadSupport {
	
	private MouseButtonOnTouchListener mMouseButtonOnTouchListener;
	private MouseOnTouchListener mMouseOnTouchListener;
			
	//time values are in [ms]
	private static final int OUT_OF_RANGE_TIMEOUT = 5000; //inactivity time after which finger is considered off the screen in touch-screen mode
	private static final int MOUSE_REFRESH_INTERVAL = 10; //refresh mouse position no more frequent than this interval
	private static final int SCROLL_REFRESH_INTERVAL = 10; //refresh scroll position no more frequent than this interval
	private static final int IDLE_RESET_INTERVAL = 100; //time after which to reset lastX, lastY to current touch position
	private static final int TAP_MIN_INTERVAL = 20; //max click rate
	private static final int DEADZONE_TIMEOUT_PERIOD = 500;
	private static final int MIN_PROXIMITY = 225; //15px (squared)
	private static final float MOUSEPAD_RESCALE_FACTOR = 1.2f; //virtually re-scale mousepad are for touchscreen mode (active area is smaller than mousepad area)
	//re-scaling makes it easier to navigate UI elements close to screen edges - like Windows "start" button
	
	private RemoteSupport mRemote;
	private RemotePreferences mRemotePreferences;
			
	private ViewGroup layoutMain;
	private MousePadView mousePad;
	private View buttonMouseL, buttonMouseM, buttonMouseR;	
	private MouseScrollView mouseScroll;	
	
	
	private int lastX, lastY; //last x,y position of mousepad touch event
	private long lastMoveTime; //when last mousepad touch event was processed
	private long lastHIDUpdateTime; //when last HID report was sent
	
	private int lastScroll; //last y position of scroll touch event
	private long lastTimeScrollTime;	
		
	private long lastTapTime; 
	private int lastTapX, lastTapY;		
		
	private boolean tapState; //if true - next touch event should be treated as click action
	private boolean lmb; //is left mouse button pressed? (by holding finger on mousepad, NOT by pressing button below)
	private boolean buttonStateLeft, buttonStateMiddle, buttonStateRight;		
	
	//touchscreen mode
	private int touchX, touchY; //x,y of last touch-screen HID report
	private boolean deadZone; //is currently in dead-zone?
	private int deadZoneX, deadZoneY; //dead-zone center coordinates
	private long deadZoneTimeout;
	private Timer outOfRangeTimer;	
	
	
	
	public MousePadSupport(RemoteSupport remote, ViewGroup layout, MousePadView pad, View left, View middle, View right, MouseScrollView scroll) {
		mRemote = remote;
		mRemotePreferences = remote.getPreferences();
		
		mMouseOnTouchListener = new MouseOnTouchListener();
		mMouseButtonOnTouchListener = new MouseButtonOnTouchListener();
		
		layoutMain = layout;
		mousePad = pad;		
		buttonMouseL = left;
		buttonMouseM = middle;
		buttonMouseR = right;
		mouseScroll = scroll;

		mousePad.setOnTouchListener(mMouseOnTouchListener);		
		if (buttonMouseL != null) {
			buttonMouseL.setOnTouchListener(mMouseButtonOnTouchListener);
		}
		if (buttonMouseM != null) {
			buttonMouseM.setOnTouchListener(mMouseButtonOnTouchListener);
		}
		if (buttonMouseR != null) {
			buttonMouseR.setOnTouchListener(mMouseButtonOnTouchListener);
		}								
		if (mouseScroll != null) {
			mouseScroll.setOnTouchListener(mMouseOnTouchListener);
		}
	}

	
	private void cancelOutOfRangeTimer() {
		if (outOfRangeTimer != null) {
			outOfRangeTimer.cancel();
			outOfRangeTimer = null;				
		}
	}
	
	public void manageUI(int state) {
		boolean enabled = (state == ConnectionManager.STATE_READY);		
	
		if (mousePad.refreshRatio(mRemotePreferences.getMousePadRatio())) {
			mousePad.requestLayout();
			mousePad.invalidate();
		}
		
		if (mRemote.getPreferences().showMouseArea()) {	
			layoutMain.setVisibility(View.VISIBLE);		
			
			mousePad.setEnabled(enabled);			
			if (buttonMouseL != null) {	
				buttonMouseL.setEnabled(enabled);
			}
			if (buttonMouseM != null) {	
				buttonMouseM.setEnabled(enabled);
			}
			if (buttonMouseR != null) {	
				buttonMouseR.setEnabled(enabled);
			}
			if (mouseScroll != null) {	
				mouseScroll.setEnabled(enabled);
			}
		} else {
			layoutMain.setVisibility(View.GONE);		
		}
		
		

		
	}

	
	
	private int getProximity() {
		int tmp = mRemotePreferences.getTouchProximity();
	    if (tmp == 0) {
	    	int h = mousePad.getHeight();
	    	int w = mousePad.getWidth();
	    	
	    	tmp = ((h * h) + (w * w)) / 1000; //3,3% tolarance 
			if (tmp < MIN_PROXIMITY) {
				tmp = MIN_PROXIMITY;
			}
	    }
		return tmp;
	}
	
	private boolean checkProximity(int x, int y) {
		int d;
		d = (lastTapX - x) * (lastTapX - x);
		d += (lastTapY - y) * (lastTapY - y);
		return (d < getProximity());
	}
	
	private boolean isOutOfDeadZone(int x, int y) {
		int d;
		d = (deadZoneX - x) * (deadZoneX - x);
		d += (deadZoneY - y) * (deadZoneY - y);
		return (d > getProximity()); 
	}
	
	private class MouseOnTouchListener implements OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			long time = System.currentTimeMillis();
			byte toScroll = 0;
			byte toMoveX = 0;
			byte toMoveY = 0;
			boolean update = false;

			int x = (int) event.getX();
			int y = (int) event.getY();
			
			if (v.equals(mousePad)) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (deadZone) {
						deadZoneTimeout = time + DEADZONE_TIMEOUT_PERIOD;
					}
					
					if (tapState) {
						long timeDiff = time - lastTapTime;
						if ((timeDiff < mRemotePreferences.getTapInterval()) && (timeDiff > TAP_MIN_INTERVAL)) {
							if (checkProximity(x, y)) {
								if (mRemotePreferences.isTapToClick()) {
									lmb = true;
									update = true;
								}
							}
						} else {
							tapState = false;
						}
					} 
					lastTapTime = time;					
					if (mRemotePreferences.isInTouchScreenMode()) {
						cancelOutOfRangeTimer();
					}
					
					break;
				case MotionEvent.ACTION_UP:
					if (tapState) {
						if (mRemotePreferences.isTapToClick()) {
							lmb = false;
							update = true;
						}
					} else {
						if (time < lastTapTime + mRemotePreferences.getTapInterval()) {
							tapState = true;							
						}						
					}
					
					lastTapX = x;
					lastTapY = y;
					lastTapTime = time;
					
					if (mRemotePreferences.isInTouchScreenMode()) {
						deadZone = true;
						deadZoneX = x;
						deadZoneY = y;
						deadZoneTimeout = 0;
						
						cancelOutOfRangeTimer();
						outOfRangeTimer = new Timer();
						outOfRangeTimer.schedule(new TimerTask() {          
						    @Override
						    public void run() {			
						    	mRemote.goOutOfRange(touchX, touchY);						    							    	
						    	outOfRangeTimer = null;
						    }
						}, OUT_OF_RANGE_TIMEOUT);
						
					}
					
					break;
				case MotionEvent.ACTION_MOVE:
					if ((x > 0) && (x < mousePad.getWidth()) && (y > 0) && (y < mousePad.getHeight())) {						
						if ((deadZone) && (deadZoneTimeout > 0) && (time > deadZoneTimeout)) {
							deadZone = false; 
						}															
						if (deadZone) {
							if (isOutOfDeadZone(x, y)) {
								deadZone = false;
							}
						}

						if (time > lastMoveTime + MOUSE_REFRESH_INTERVAL) {
							// fix:
							if (time > lastHIDUpdateTime + IDLE_RESET_INTERVAL) {
								lastX = x;
								lastY = y;
							}
							
							lastHIDUpdateTime = time;
							lastMoveTime = time;

							toMoveX = (byte)(x - lastX);
							toMoveY = (byte)(y - lastY);
							toMoveX = (byte)((mRemotePreferences.getMouseSensitivity() * toMoveX) / 50);
							toMoveY = (byte)((mRemotePreferences.getMouseSensitivity() * toMoveY) / 50);  
							update = true;
							lastX = x;
							lastY = y;											
							if ((mRemotePreferences.isInTouchScreenMode()) && ( !deadZone)) {
								setTouchCoords(x,y);
							}							
						} 
					} else {
						lastX = 0;
						lastY = 0;
						//@ TODO same case in ACTION_UP?
					}
					break;
				}
			} // added 
			
			if (v.equals(mouseScroll)) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					lastScroll = y;	
				}
				if (event.getAction() == MotionEvent.ACTION_MOVE) {
					if ((x > 0) && (x < mouseScroll.getWidth()) && (y > 0) && (y < mouseScroll.getHeight())) {
						if (time > lastTimeScrollTime + SCROLL_REFRESH_INTERVAL) {
							toScroll = (byte)(lastScroll - y);
							//new:
							toScroll = (byte)((mRemotePreferences.getScrollSensitivity() * toScroll) / 150);  //ex: 50*7/50 = 7; 75*7/50 = 10; 10*7/50 = 1
							
							if (toScroll != 0) {
								//lastUpdateScroll = time;
								lastTimeScrollTime = time;
								lastScroll = y;	
								update = true;
							}											
						}
					} else {
						lastScroll = 0;
					}
				}
			}
			
			if (update) {
				sendReport(toMoveX, toMoveY, toScroll);
			}			
			return true;
		}

	}
	
	
	private void setTouchCoords(int x, int y) {		
		int w = mousePad.getWidth();
		int h = mousePad.getHeight();
		int center;
		float d;
		
		center = w/2;
		d = center - x;		
		d = d * MOUSEPAD_RESCALE_FACTOR;
		x = (int)(center - d);
		if (x < 0) x = 0;
		if (x > w) x = w;
		
		center = h/2;
		d = center - y;
		d = d * MOUSEPAD_RESCALE_FACTOR;		
		y = (int)(center - d);
		if (y < 0) y = 0;
		if (y > h) y = h;
		
		touchX = (x * 10000 / w);
		touchY = (y * 10000 / h);

	
		if (touchX > 10000) touchX = 10000;
		if (touchX < 0) touchX = 0;					
		if (touchY > 10000) touchY = 10000;
		if (touchY < 0) touchY = 0;

	}
	
	
	private void sendReport(byte toMoveX, byte toMoveY, byte toScroll) {
		byte buttonByte = 0;
		boolean leftButton = false; //for touchscreen interface 
		if ((buttonStateLeft) || (lmb)) {
			buttonByte |= InputStickMouse.BUTTON_LEFT;
			leftButton = true;
		}
		if (buttonStateMiddle) {
			buttonByte |= InputStickMouse.BUTTON_MIDDLE;
		}
		if (buttonStateRight) {
			buttonByte |= InputStickMouse.BUTTON_RIGHT;
		}		
				
		if (mRemotePreferences.isInTouchScreenMode()) {						
			if (toScroll != 0) {
				mRemote.mouseReport(buttonByte, (byte)0, (byte)0, toScroll);
			} else {				
				mRemote.moveTouchPointer(leftButton, touchX, touchY);
			}
		} else {		
			mRemote.mouseReport(buttonByte, toMoveX, toMoveY, toScroll);
		}
	}
		
	
	private class MouseButtonOnTouchListener implements OnTouchListener {
		
		private byte buttonFix; //used to handle right&middle button click when using touchscreen mode

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				//pressModifiers();
				if (v.equals(buttonMouseL)) {
					buttonStateLeft = true;		
				} else if (v.equals(buttonMouseR)) {
					buttonFix = InputStickMouse.BUTTON_RIGHT;
					buttonStateRight = true;
				} else if (v.equals(buttonMouseM)) {
					buttonFix = InputStickMouse.BUTTON_MIDDLE;
					buttonStateMiddle = true;
				}  
				sendReport((byte)0, (byte)0, (byte)0); //buttons are already handled
			}
			if (event.getAction() == MotionEvent.ACTION_UP) {
				//releaseModifiers();
				if (v.equals(buttonMouseL)) {
					buttonStateLeft = false;																
				} else if (v.equals(buttonMouseR)) {
					buttonStateRight = false;
				} else if (v.equals(buttonMouseM)) {
					buttonStateMiddle = false;
				} 
				if (mRemotePreferences.isInTouchScreenMode()) {
					if (buttonFix != 0) {
						mRemote.mouseClick(buttonFix, 1);						
					}
				}
				sendReport((byte)0, (byte)0, (byte)0);
				buttonFix = 0;
			}
			
			return false;
		}
	}	
	
}