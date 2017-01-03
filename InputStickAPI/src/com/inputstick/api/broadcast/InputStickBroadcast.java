package com.inputstick.api.broadcast;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.inputstick.api.DownloadDialog;
import com.inputstick.api.Util;


/*
 * IMPORTANT:
 * 
 * Using InputStickBroadcast is the easiest and fastest way to use InputStick with your application.
 * InputStickUility takes care of almost everything:
 * -enabling Bluetooth if necessary,
 * -selecting InputStick device (if more than one is available),
 * -establishing connection,
 * -deals with potential connection problems (connection failed, lost),
 * -user preferences (keyboard layout, typing speed).
 * 
 * as a result of that, your application has little to no control over:
 * -connection,
 * -buffers
 * -timing,
 * 
 * 
 * Using InputStickBroadcast is recommended only for simple use cases: typing strings.
 * Example: barcode scanner app, assuming that user will use InputStick to type only some of scanned codes.
 * 
 * Using InputStickBroadcast is NOT recommended if
 * -timing is critical,
 * -low latency is necessary,
 * -many actions can be executed in a short period of time
 * 
 * Example: remote control app.
 * 
 * In such case use classes from com.inputstick.api.hid package and implement all necessary callbacks.
 * 
 */


public class InputStickBroadcast {
	
	private static boolean AUTO_SUPPORT_CHECK;
	
	public static final String PARAM_REQUEST = 		"REQUEST";
	public static final String PARAM_RELEASE = 		"RELEASE";
	public static final String PARAM_CLEAR =		"CLEAR";
	
	public static final String PARAM_TEXT = 		"TEXT";
	public static final String PARAM_LAYOUT = 		"LAYOUT";
	public static final String PARAM_MULTIPLIER = 	"MULTIPLIER";
	public static final String PARAM_KEY = 			"KEY";
	public static final String PARAM_MODIFIER = 	"MODIFIER";
	public static final String PARAM_REPORT_KEYB = 	"REPORT_KEYB";
	public static final String PARAM_REPORT_EMPTY =	"REPORT_EMPTY";
	
	public static final String PARAM_REPORT_MOUSE = "REPORT_MOUSE";
	public static final String PARAM_MOUSE_BUTTONS ="MOUSE_BUTTONS";
	public static final String PARAM_MOUSE_CLICKS =	"MOUSE_CLICKS";
	
	public static final String PARAM_CONSUMER = 	"CONSUMER";

	public static final String PARAM_REPORT_TOUCH = "REPORT_TOUCHSCREEN";
	public static final String PARAM_TOUCH_CLICKS =	"TOUCH_CLICKS";	
	public static final String PARAM_TOUCH_X =		"TOUCH_X";
	public static final String PARAM_TOUCH_Y =		"TOUCH_Y";
	
	
	
	/*
	 * Checks whether InputStickUtility is installed and supports intents (version code >= 11). 
	 * Optionally download dialog can be displayed if InputStickUtility is not installed.
	 * 
	 * @param ctx			context	
	 * @param allowMessages	when true, download dialog will be displayed if necessary	
	 * 
	 */
	public static boolean isSupported(Context ctx, boolean allowMessages) {
		PackageInfo pInfo;
		try {
			pInfo = ctx.getPackageManager().getPackageInfo("com.inputstick.apps.inputstickutility", 0);
			//System.out.println("ver: " + pInfo.versionName + " code: " + pInfo.versionCode);
			if (pInfo.versionCode < 11) {
				if (allowMessages) {
					DownloadDialog.getDialog(ctx, DownloadDialog.NOT_UPDATED).show();
				}
				return false;
			} else {
				return true;
			}
		} catch (NameNotFoundException e) {
			//e.printStackTrace();
			//InputStickUtility not installed
			if (allowMessages) {
				DownloadDialog.getDialog(ctx, DownloadDialog.NOT_INSTALLED).show();
			}
			return false;
		}		
	}
	
	
	/*
	 * When Auto Support Check is enabled, isSupported(ctx, true) will be called each time before sending broadcast.
	 * You do not have to check support manually. Download dialog will be displayed if InputStickUtility is not installed.
	 * 
	 * WARNING: checking support each time after sending broadcast can be very time consuming!!!   
	 * 
	 * @param enabled	true to enable Auto Support Check, false to disable
	 */
	public static void setAutoSupportCheck(boolean enabled) {
		AUTO_SUPPORT_CHECK = enabled;
	}

	
	/*
	 * Indicates that it is very likely that this application will want to use InputStick within next few seconds.  
	 * Depending on user preferences this action may be ignored! In such case InputStickUtility will wait until some data arrives (text etc.).
	 * In many cases this will allow to reduce delay between requesting some action and executing it (typing text etc).
	 * 
	 * @param ctx			context used to send broadcast.	
	 */
	public static void requestConnection(Context ctx) {
		Intent intent = new Intent();	
		intent.putExtra(PARAM_REQUEST, true);
		send(ctx, intent);	
	}
	
	
	/*
	 * Indicates that application will no longer need InputStick in nearest future. 
	 * Allows to save power.
	 * Depending on user preferences this action may be ignored!
	 * Ignored if not connected.
	 * 
	 * @param ctx			context used to send broadcast.	
	 */
	public static void releaseConnection(Context ctx) {
		Intent intent = new Intent();	
		intent.putExtra(PARAM_RELEASE, true);
		send(ctx, intent);	
	}
	
	
	/*
	 * Removes all actions from queue. Clears all interface buffers.
	 * Use to immediately stop all actions
	 * Depending on user preferences this action may be ignored!
	 * 
	 * @param ctx		context used to send broadcast.	 
	 */
	public static void clearQueue(Context ctx) {
		Intent intent = new Intent();				
		intent.putExtra(PARAM_CLEAR, true);			
		send(ctx, intent);	
	}
	
	
	
	
	//#######################################################################################################
	//##### KEYBOARD INTERFACE ##############################################################################
	//#######################################################################################################
	
	
	/*
	 * Puts "type text" action into queue. Fastest typing speed, use en-US layout.
	 * 
	 * @param ctx			context used to send broadcast.	
	 * @param text			text to be typed. \n and \t characters are allowed.
	 */
	public static void type(Context ctx, String text) {
		type(ctx, text, null, 1);
	}
	
	
	/*
	 * Puts "type text" action into queue. Fastest typing speed.
	 * 
	 * Keyboard layout must match layout used by USB host. en-US is used by default.
	 * Depending on user preferences value of layoutCode may be ignored!
	 * 
	 * @param ctx			context used to send broadcast	
	 * @param text			text to be typed. \n and \t characters are allowed.
	 * @param layoutCode	keyboard layout to be used: en-US, de-DE, pl-PL etc.
	 */
	public static void type(Context ctx, String text, String layoutCode) {
		type(ctx, text, layoutCode, 1);
	}
	
	
	/*
	 * Puts "type text" action into queue.
	 *  
	 * Keyboard layout must match layout used by USB host. en-US is used by default.
	 * Depending on user preferences value of layoutCode may be ignored!
	 * 
	 * When multiplier is set to 1, keys will be "pressed" at fastest possible speed. Increase value of this parameter to obtain slower typing speed, by multiplying number of HID keyboard reports.
	 * Depending on user preferences value of multiplier may be ignored!
	 * 
	 * @param ctx			context used to send broadcast	
	 * @param text			text to be typed. \n and \t characters are allowed.
	 * @param layoutCode	keyboard layout to be used: en-US, de-DE, pl-PL etc.
	 * @param multiplier	controls typing speed.
	 */
	public static void type(Context ctx, String text, String layoutCode, int multiplier) {
		Intent intent = new Intent();	
		
		intent.putExtra(PARAM_TEXT, text);		
		if (layoutCode != null) {
			intent.putExtra(PARAM_LAYOUT, layoutCode);
		}	
		if (multiplier > 1) {
			intent.putExtra(PARAM_MULTIPLIER, multiplier);
		}	
		send(ctx, intent);	
	}
	
	
	/*
	 * Puts "press and release key" action into queue.
	 * 
	 * @param ctx			context used to send broadcast.	
	 * @param modifiers		modifier keys: Shift, Alt, Ctrl, Gui/Win/Command keys, (see HIDKeycodes class.
	 * @param key			any non-modifier key, see HIDKeycodes class.
	 */
	public static void pressAndRelease(Context ctx, byte modifiers, byte key) {
		pressAndRelease(ctx, modifiers, key, 1);
	}
	
	
	/*
	 * Puts "press and release key" action into queue.
	 * When multiplier is set to 1, keys will be "pressed" at fastest possible speed. Increase value of this parameter to obtain slower typing speed, by multiplying number of HID reports.
	 * 
	 * @param ctx			context used to send broadcast.	
	 * @param modifiers		modifier keys: Shift, Alt, Ctrl, Gui/Win/Command keys, (see HIDKeycodes class).
	 * @param key			any non-modifier key, see HIDKeycodes class.
	 * @param multiplier	controls typing speed.
	 */
	public static void pressAndRelease(Context ctx, byte modifiers, byte key, int multiplier) {
		Intent intent = new Intent();		
		
		intent.putExtra(PARAM_MODIFIER, modifiers);		
		intent.putExtra(PARAM_KEY, key);
		if (multiplier > 1) {
			intent.putExtra(PARAM_MULTIPLIER, multiplier);
		}		
		send(ctx, intent);	
	}
	
	
	/*
	 * Puts single HID keyboard report into queue.
	 * HID keyboard report represents state of keyboard (which keys are pressed) at a given moment.
	 * Must be 8 bytes long:
	 * report[0] = modifier keys
	 * report[1] = 0x00
	 * report[2] = key1
	 * report[3] = key2
	 * report[4] = key3
	 * report[5] = key4
	 * report[6] = key5
	 * report[7] = key6
	 * To avoid keys getting "stuck" they should be released (by adding empty report).
	 * 
	 * @param ctx				context used to send broadcast.
	 * @param report			HID keyboard report.
	 * @param addEmptyReport	empty keyboard report (all keys released) will be added if true.
	 */
	public static void keyboardReport(Context ctx, byte[] report, boolean addEmptyReport) {
		Intent intent = new Intent();			
		intent.putExtra(PARAM_REPORT_KEYB, report);		
		if (addEmptyReport) {
			intent.putExtra(PARAM_REPORT_EMPTY, true);	
		}
		send(ctx, intent);	
	}
	
	
	/*
	 * Puts single HID keyboard report into queue.
	 * HID keyboard report represents state of keyboard (which keys are pressed) at a given moment.
	 * To avoid keys getting "stuck" they should be released (by adding empty report).
	 * 
	 * @param ctx				context used to send broadcast.
	 * @param modifiers			modifier keys: Shift, Alt, Ctrl, Gui/Win/Command keys, (see HIDKeycodes class).
	 * @param key1				any non-modifier key, see HIDKeycodes class.
	 * @param key2				any non-modifier key, see HIDKeycodes class.
	 * @param key3				any non-modifier key, see HIDKeycodes class.
	 * @param key4				any non-modifier key, see HIDKeycodes class.
	 * @param key5				any non-modifier key, see HIDKeycodes class.
	 * @param key6				any non-modifier key, see HIDKeycodes class.
	 * @param addEmptyReport	empty keyboard report (all keys released) will be added if true.
	 */
	public static void keyboardReport(Context ctx, byte modifiers, byte key1, byte key2, byte key3, byte key4, byte key5, byte key6, boolean addEmptyReport) {
		byte[] report = new byte[8];
		report[0] = modifiers;
		report[2] = key1;
		report[3] = key2;
		report[4] = key3;
		report[5] = key4;
		report[6] = key5;
		report[7] = key6;
		keyboardReport(ctx, report, addEmptyReport);
	}
	

	
	
	//#######################################################################################################
	//##### MOUSE INTERFACE #################################################################################
	//#######################################################################################################
	
	
	/*
	 * Puts single HID mouse report into queue.
	 * HID mouse report represents change in state of a mouse.
	 * Must be 4 bytes long:
	 * report[0] = buttons
	 * report[1] = x axis displacement
	 * report[2] = y axis displacement
	 * report[3] = scroll wheel displacement
	 * 
	 * @param ctx		context used to send broadcast.
	 * @param report	HID mouse report.
	 */
	public static void mouseReport(Context ctx, byte[] report) {
		Intent intent = new Intent();			
		intent.putExtra(PARAM_REPORT_MOUSE, report);			
		send(ctx, intent);	
	}
	
	
	/*
	 * Puts single HID mouse report into queue.
	 * Left mouse button = 0x01
	 * Right mouse button = 0x02
	 * Middle mouse button = 0x04
	 * 
	 * @param ctx		context used to send broadcast.
	 * @param buttons	mouse buttons to click.
	 * @param dx		x axis displacement.
	 * @param dy		y axis displacement.
	 * @param scroll	scroll wheel displacement.
	 */
	public static void mouseReport(Context ctx, byte buttons, byte dx, byte dy, byte scroll) {
		byte[] report = new byte[4];
		report[0] = buttons;
		report[1] = dx;
		report[2] = dy;
		report[3] = scroll;
		mouseReport(ctx, report);
	}
	
	
	/*
	 * Puts mouse click (button(s) press-release) action into queue.
	 * Left mouse button = 0x01
	 * Right mouse button = 0x02
	 * Middle mouse button = 0x04
	 * 
	 * @param ctx		context used to send broadcast.
	 * @param buttons	mouse buttons to click.
	 * @param n			number of clicks.
	 */
	public static void mouseClick(Context ctx, byte buttons, int n) {
		Intent intent = new Intent();			
		intent.putExtra(PARAM_MOUSE_BUTTONS, buttons);			
		intent.putExtra(PARAM_MOUSE_CLICKS, n);			
		send(ctx, intent);	
	}
	
	
	/*
	 * Puts mouse move action into queue.
	 * 
	 * @param ctx		context used to send broadcast.
	 * @param dx		x axis displacement.
	 * @param dy		y axis displacement. 
	 */
	public static void mouseMove(Context ctx, byte dx, byte dy) {
		mouseReport(ctx, (byte)0x00, dx, dy, (byte)0x00);
	}
	
	
	/*
	 * Puts mouse scroll action into queue.
	 * Positive values: scroll up; negative values: scroll down
	 * 
	 * @param ctx		context used to send broadcast.
	 * @param scroll	scroll wheel displacement.
	 */
	public static void mouseScroll(Context ctx, byte scroll) {
		mouseReport(ctx, (byte)0x00, (byte)0x00, (byte)0x00, scroll);
	}
	
	
	
	
	//#######################################################################################################
	//##### CONSUMER CONTROL INTERFACE ######################################################################
	//#######################################################################################################
	
	/*
	 * Puts "consumer" action into queue. See InputStickConsumer class for list available actions.
	 * 
	 * @param ctx		context used to send broadcast.
	 * @param action	code of consumer action.
	 */
	public static void consumerControlAction(Context ctx, int action) {
		Intent intent = new Intent();			
		intent.putExtra(PARAM_CONSUMER, action);			
		send(ctx, intent);		
	}
	
	
	private static void send(Context ctx, Intent intent) {		
		intent.setAction("com.inputstick.apps.inputstickutility.HID");
		intent.setClassName("com.inputstick.apps.inputstickutility", "com.inputstick.apps.inputstickutility.service.HIDReceiver");

		//if necessary, show download dialog message
		if (AUTO_SUPPORT_CHECK) {
			if (isSupported(ctx, true)) {
				ctx.sendBroadcast(intent);	
			}
		} else {
			ctx.sendBroadcast(intent);	
		}				
	}

	
	
	
	//#######################################################################################################
	//##### TOUCH SCREEN INTERFACE ##########################################################################
	//#######################################################################################################	
	
	/*
	 * Puts single touch screen report into queue. 
	 * Requires firmware 0.98D or later and InputStickUtility 1.49 or later!
	 * 
	 * HID touch screen report represents current state of touch screen interface.
	 * Must be 6 bytes long:
	 * report[0] = 4 (touch screen interface report ID, constant)
	 * report[1] = 1st bit: tip switch (same as left mouse button, 1: pressed, 0: released); 2nd bit: in range (1: in range, 0: out of range)
	 * report[2] = x coordinate (LSB)
	 * report[3] = x coordinate (MSB)
	 * report[4] = y coordinate (LSB)
	 * report[5] = y coordinate (MSB)
	 * 
	 * Note: for x,y 10000 = 100% of vertical/horizontal resolution of the screen. 
	 * 
	 * @param ctx		context used to send broadcast.
	 * @param report	HID touch screen report.
	 */
	public static void touchScreenReport(Context ctx, byte[] report) {
		Intent intent = new Intent();			
		intent.putExtra(PARAM_REPORT_TOUCH, report);			
		send(ctx, intent);	
	}
	
	
	/*
	 * Puts single HID touch screen report into queue. 
	 * Requires firmware 0.98D or later and InputStickUtility 1.49 or later!
	 * 
	 * @param ctx		context used to send broadcast.
	 * @param tipSwitch	button clicked (same as left mouse button)
	 * @param inRange	touch device is in range (going out of range will hide additional UI on Windows OS)
	 * @param x			x axis position (10000 = 100% of screen width)
	 * @param y			y axis position (10000 = 100% of screen height)
	 */
	public static void touchScreenReport(Context ctx, boolean tipSwitch, boolean inRange, int x, int y) {
		byte[] report = new byte[6];
		report[0] = 0x04;
		
		if (tipSwitch) {
			report[1] = 0x01;
		}
		if (inRange) {
			report[1] += 0x02;
		} 		
		report[2] = Util.getLSB(x);
		report[3] = Util.getMSB(x);		
		report[4] = Util.getLSB(y);
		report[5] = Util.getMSB(y);
		touchScreenReport(ctx, report);
	}	
		
	
	/*
	 * Move mouse pointer (using touch screen interface) to position specified by x and y. 
	 * Requires firmware 0.98D or later and InputStickUtility 1.49 or later!
	 * 
	 * @param ctx		context used to send broadcast.
	 * @param x			x position of mouse pointer (10000 = 100% of screen width) 
	 * @param y			y position of mouse pointer (10000 = 100% of screen height)
	 */	
	public static void touchScreenMove(Context ctx, int x, int y) {
		touchScreenReport(ctx, false, true, x, y);
	}
	
	
	/*
	 * Perform N clicks at position specified by x,y (press and release) using touch screen interface. Same as left mouse button clicks.
	 * Requires firmware 0.98D or later and InputStickUtility 1.49 or later!
	 * 
	 * @param ctx		context used to send broadcast.
	 * @param n			number of clicks
	 * @param x			x position of mouse pointer (10000 = 100% of screen width) 
	 * @param y			y position of mouse pointer (10000 = 100% of screen height) 
	 */		
	public static void touchScreenClick(Context ctx, int n, int x, int y) {
		Intent intent = new Intent();			
		intent.putExtra(PARAM_TOUCH_CLICKS, n);	
		intent.putExtra(PARAM_TOUCH_X, x);	
		intent.putExtra(PARAM_TOUCH_Y, y);	
		send(ctx, intent);		
	}
	
	
	/*
	 * Move mouse pointer to top left corner of the screen (using touch screen interface) and simulates stylus (finger) going out of range
	 * Requires firmware 0.98D or later and InputStickUtility 1.49 or later!
	 * 
	 * @param ctx		context used to send broadcast.
	 */	
	public static void touchScreenGoOutOfRange(Context ctx) {
		touchScreenReport(ctx, false, false, 0, 0);
	}

}
