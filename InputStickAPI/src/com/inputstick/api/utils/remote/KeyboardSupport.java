package com.inputstick.api.utils.remote;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;

import com.inputstick.api.ConnectionManager;
import com.inputstick.api.basic.InputStickHID;
import com.inputstick.api.hid.HIDKeycodes;
import com.inputstick.api.layout.KeyboardLayout;

public class KeyboardSupport {
	
	private static class LUTEntry {
		private int mKeyCode;
		private byte mHIDKeyCode;
		
		public LUTEntry(int keyCode, byte hidKeyCode) {
			mKeyCode = keyCode;
			mHIDKeyCode = hidKeyCode;
		}
		
		public int getKeyCode() {
			return mKeyCode;
		}
		
		public byte getHIDKeyCode() {
			return mHIDKeyCode;
		}
	}
	
	@SuppressLint("InlinedApi")
	private static final LUTEntry[] LUT_SPECIAL_KEYS = {
		new LUTEntry(KeyEvent.KEYCODE_DPAD_LEFT, HIDKeycodes.KEY_ARROW_LEFT),
		new LUTEntry(KeyEvent.KEYCODE_DPAD_RIGHT, HIDKeycodes.KEY_ARROW_RIGHT),
		new LUTEntry(KeyEvent.KEYCODE_DPAD_UP, HIDKeycodes.KEY_ARROW_UP),
		new LUTEntry(KeyEvent.KEYCODE_DPAD_DOWN, HIDKeycodes.KEY_ARROW_DOWN),
		
		new LUTEntry(KeyEvent.KEYCODE_ESCAPE, HIDKeycodes.KEY_ESCAPE),
		new LUTEntry(KeyEvent.KEYCODE_F1, HIDKeycodes.KEY_F1),
		new LUTEntry(KeyEvent.KEYCODE_F2, HIDKeycodes.KEY_F2),
		new LUTEntry(KeyEvent.KEYCODE_F3, HIDKeycodes.KEY_F3),
		new LUTEntry(KeyEvent.KEYCODE_F4, HIDKeycodes.KEY_F4),
		new LUTEntry(KeyEvent.KEYCODE_F5, HIDKeycodes.KEY_F5),
		new LUTEntry(KeyEvent.KEYCODE_F6, HIDKeycodes.KEY_F6),
		new LUTEntry(KeyEvent.KEYCODE_F7, HIDKeycodes.KEY_F7),
		new LUTEntry(KeyEvent.KEYCODE_F8, HIDKeycodes.KEY_F8),
		new LUTEntry(KeyEvent.KEYCODE_F9, HIDKeycodes.KEY_F9),
		new LUTEntry(KeyEvent.KEYCODE_F10, HIDKeycodes.KEY_F10),
		new LUTEntry(KeyEvent.KEYCODE_F11, HIDKeycodes.KEY_F11),
		new LUTEntry(KeyEvent.KEYCODE_F12, HIDKeycodes.KEY_F12),
		
		new LUTEntry(KeyEvent.KEYCODE_INSERT, HIDKeycodes.KEY_INSERT),
		new LUTEntry(KeyEvent.KEYCODE_FORWARD_DEL, HIDKeycodes.KEY_DELETE),
		new LUTEntry(KeyEvent.KEYCODE_MOVE_HOME, HIDKeycodes.KEY_HOME),
		new LUTEntry(KeyEvent.KEYCODE_MOVE_END, HIDKeycodes.KEY_END),
		new LUTEntry(KeyEvent.KEYCODE_PAGE_UP, HIDKeycodes.KEY_PAGE_UP),
		new LUTEntry(KeyEvent.KEYCODE_PAGE_DOWN, HIDKeycodes.KEY_PAGE_DOWN),
		
		new LUTEntry(KeyEvent.KEYCODE_NUM_LOCK, HIDKeycodes.KEY_NUM_LOCK),
		new LUTEntry(KeyEvent.KEYCODE_CAPS_LOCK, HIDKeycodes.KEY_CAPS_LOCK),
		new LUTEntry(KeyEvent.KEYCODE_SCROLL_LOCK, HIDKeycodes.KEY_SCROLL_LOCK),
		new LUTEntry(KeyEvent.KEYCODE_BREAK, HIDKeycodes.KEY_PASUE),
		new LUTEntry(KeyEvent.KEYCODE_SYSRQ, HIDKeycodes.KEY_PRINT_SCREEN),
		
		new LUTEntry(KeyEvent.KEYCODE_ENTER, HIDKeycodes.KEY_ENTER),
		new LUTEntry(KeyEvent.KEYCODE_DEL, HIDKeycodes.KEY_BACKSPACE),
		new LUTEntry(KeyEvent.KEYCODE_TAB, HIDKeycodes.KEY_TAB),
		new LUTEntry(KeyEvent.KEYCODE_SPACE, HIDKeycodes.KEY_SPACEBAR),
		
		
		new LUTEntry(KeyEvent.KEYCODE_DPAD_LEFT, HIDKeycodes.KEY_ARROW_LEFT),
		new LUTEntry(KeyEvent.KEYCODE_DPAD_LEFT, HIDKeycodes.KEY_ARROW_LEFT),
		new LUTEntry(KeyEvent.KEYCODE_DPAD_LEFT, HIDKeycodes.KEY_ARROW_LEFT),
	};
	
	private static final LUTEntry[] LUT_OTHER_KEYS = {
		new LUTEntry(KeyEvent.KEYCODE_GRAVE, HIDKeycodes.KEY_GRAVE),
		new LUTEntry(KeyEvent.KEYCODE_1, HIDKeycodes.KEY_1),
		new LUTEntry(KeyEvent.KEYCODE_2, HIDKeycodes.KEY_2),
		new LUTEntry(KeyEvent.KEYCODE_3, HIDKeycodes.KEY_3),
		new LUTEntry(KeyEvent.KEYCODE_4, HIDKeycodes.KEY_4),
		new LUTEntry(KeyEvent.KEYCODE_5, HIDKeycodes.KEY_5),
		new LUTEntry(KeyEvent.KEYCODE_6, HIDKeycodes.KEY_6),
		new LUTEntry(KeyEvent.KEYCODE_7, HIDKeycodes.KEY_7),
		new LUTEntry(KeyEvent.KEYCODE_8, HIDKeycodes.KEY_8),
		new LUTEntry(KeyEvent.KEYCODE_9, HIDKeycodes.KEY_9),
		new LUTEntry(KeyEvent.KEYCODE_0, HIDKeycodes.KEY_0),
		new LUTEntry(KeyEvent.KEYCODE_MINUS, HIDKeycodes.KEY_MINUS),
		new LUTEntry(KeyEvent.KEYCODE_EQUALS, HIDKeycodes.KEY_EQUALS),		
		
		new LUTEntry(KeyEvent.KEYCODE_Q, HIDKeycodes.KEY_Q),
		new LUTEntry(KeyEvent.KEYCODE_W, HIDKeycodes.KEY_W),
		new LUTEntry(KeyEvent.KEYCODE_E, HIDKeycodes.KEY_E),
		new LUTEntry(KeyEvent.KEYCODE_R, HIDKeycodes.KEY_R),
		new LUTEntry(KeyEvent.KEYCODE_T, HIDKeycodes.KEY_T),
		new LUTEntry(KeyEvent.KEYCODE_Y, HIDKeycodes.KEY_Y),
		new LUTEntry(KeyEvent.KEYCODE_U, HIDKeycodes.KEY_U),
		new LUTEntry(KeyEvent.KEYCODE_I, HIDKeycodes.KEY_I),
		new LUTEntry(KeyEvent.KEYCODE_O, HIDKeycodes.KEY_O),
		new LUTEntry(KeyEvent.KEYCODE_P, HIDKeycodes.KEY_P),
		new LUTEntry(KeyEvent.KEYCODE_LEFT_BRACKET, HIDKeycodes.KEY_LEFT_BRACKET),
		new LUTEntry(KeyEvent.KEYCODE_RIGHT_BRACKET, HIDKeycodes.KEY_RIGHT_BRACKET),
		
		new LUTEntry(KeyEvent.KEYCODE_A, HIDKeycodes.KEY_A),
		new LUTEntry(KeyEvent.KEYCODE_S, HIDKeycodes.KEY_S),
		new LUTEntry(KeyEvent.KEYCODE_D, HIDKeycodes.KEY_D),
		new LUTEntry(KeyEvent.KEYCODE_F, HIDKeycodes.KEY_F),
		new LUTEntry(KeyEvent.KEYCODE_G, HIDKeycodes.KEY_G),
		new LUTEntry(KeyEvent.KEYCODE_H, HIDKeycodes.KEY_H),
		new LUTEntry(KeyEvent.KEYCODE_J, HIDKeycodes.KEY_J),
		new LUTEntry(KeyEvent.KEYCODE_K, HIDKeycodes.KEY_K),
		new LUTEntry(KeyEvent.KEYCODE_L, HIDKeycodes.KEY_L),
		new LUTEntry(KeyEvent.KEYCODE_SEMICOLON, HIDKeycodes.KEY_SEMICOLON),
		new LUTEntry(KeyEvent.KEYCODE_APOSTROPHE, HIDKeycodes.KEY_APOSTROPHE),
		
		new LUTEntry(KeyEvent.KEYCODE_Z, HIDKeycodes.KEY_Z),
		new LUTEntry(KeyEvent.KEYCODE_X, HIDKeycodes.KEY_X),
		new LUTEntry(KeyEvent.KEYCODE_C, HIDKeycodes.KEY_C),
		new LUTEntry(KeyEvent.KEYCODE_V, HIDKeycodes.KEY_V),
		new LUTEntry(KeyEvent.KEYCODE_B, HIDKeycodes.KEY_B),
		new LUTEntry(KeyEvent.KEYCODE_N, HIDKeycodes.KEY_N),
		new LUTEntry(KeyEvent.KEYCODE_M, HIDKeycodes.KEY_M),
		
		new LUTEntry(KeyEvent.KEYCODE_COMMA, HIDKeycodes.KEY_COMA),
		new LUTEntry(KeyEvent.KEYCODE_PERIOD, HIDKeycodes.KEY_DOT),
		new LUTEntry(KeyEvent.KEYCODE_SLASH, HIDKeycodes.KEY_SLASH),
		new LUTEntry(KeyEvent.KEYCODE_BACKSLASH, HIDKeycodes.KEY_BACKSLASH),	
	};
	
	private static byte getHIDKeyCode(int keyCode, LUTEntry[] lut) {
		for (LUTEntry entry : lut) {
			if (entry.getKeyCode() == keyCode) {
				return entry.getHIDKeyCode();
			}
		}
		return HIDKeycodes.NONE;
	}


	protected RemoteSupport mRemote;
	
	public KeyboardSupport(RemoteSupport remote) {
		mRemote = remote;
	}		
	
	
	public void onPress(KeyboardLayout layout, byte modifier, byte key) {
		mRemote.pressAndRelease(modifier, key);					
	}

	public void onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
    	if ((keyCode == KeyEvent.KEYCODE_UNKNOWN) && (event.getAction() == KeyEvent.ACTION_MULTIPLE)) {
    		String s = event.getCharacters();
    		mRemote.type(s, (byte)0); 
    	}			
	}
	
	@SuppressLint("NewApi")
	private byte getEventModifiers(KeyEvent event, boolean ctrl, boolean shift, boolean alt) {
		byte result = 0;				
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB){
			if ((event.isCtrlPressed()) && (ctrl)) {
				result |= HIDKeycodes.CTRL_LEFT;
			}
		}
		if ((event.isShiftPressed()) && (shift)) {
			result |= HIDKeycodes.SHIFT_LEFT;
		}
		if ((event.isAltPressed()) && (alt)) {
			result |= HIDKeycodes.ALT_LEFT;
		}		
		return result;
	}
	
	public void onKeyDown(byte modifiers, int keyCode, KeyEvent event) {
		if (KeyEvent.isModifierKey(keyCode)) {
			//ignore soft-keyboard modifier-only actions (when only a modifier key is pressed)
		} else {
			// is it a "special" key?
			if (!handleKeyEvent(event, keyCode, modifiers, LUT_SPECIAL_KEYS)) {
				// is it a "standard" character available in currently selected layout?
				boolean unicode = false;				
				char c = (char) event.getUnicodeChar();
				if (KeyboardLayout.getScanCode(mRemote.getPreferences().getKeyboardLayout().getLUT(), c) > 0) {
					unicode = true;
				} else {
					if (KeyboardLayout.findDeadKey(mRemote.getPreferences().getKeyboardLayout().getDeadkeyLUT(), c) > 0) {
						unicode = true;
					}
				}
				
				if (unicode) {
					// get CTRL/ALT modifiers form softkeyboard
					byte eventModifiers = getEventModifiers(event, true, false, true);
					eventModifiers |= modifiers;
					String s = String.valueOf(c);
					if (InputStickHID.getState() == ConnectionManager.STATE_READY) {
						mRemote.type(s, eventModifiers);
					}
				} else {
					handleKeyEvent(event, keyCode, modifiers, LUT_OTHER_KEYS); 
				}
			}
		}
	}
	
	private boolean handleKeyEvent(KeyEvent event, int keyCode, byte modifiers, LUTEntry[] lut) {
		byte key = getHIDKeyCode(keyCode, lut);		
		if (key != HIDKeycodes.NONE) {					
			byte eventModifiers = getEventModifiers(event, true, true, true); // get ALL modifiers form softkeyboard!
			eventModifiers |= modifiers;
			mRemote.pressAndRelease(eventModifiers, key);
			return true;
		} else {
			return false;
		}
	}
	
	
	
	
	
	
    public static AlertDialog getFunctionKeysDialog(Context ctx, final RemoteSupport remote, final ModifiersSupport modifiers, String title) {    	    	
		AlertDialog.Builder dialog = new AlertDialog.Builder(ctx);
		final ScrollView sl = new ScrollView(ctx);			
		dialog.setTitle(title);		
				
    	final LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
    	
    	LinearLayout main = new LinearLayout(ctx);
    	main.setOrientation(LinearLayout.VERTICAL);
    	

    	LinearLayout layout;
    	
    	layout = new LinearLayout(ctx);
    	layout.setOrientation(LinearLayout.HORIZONTAL);    	
    	addButton(ctx, remote, modifiers, layout, llp, "F1", HIDKeycodes.KEY_F1);    	
    	addButton(ctx, remote, modifiers, layout, llp, "F2", HIDKeycodes.KEY_F2);
    	addButton(ctx, remote, modifiers, layout, llp, "F3", HIDKeycodes.KEY_F3);
    	addButton(ctx, remote, modifiers, layout, llp, "F4", HIDKeycodes.KEY_F4);
    	addButton(ctx, remote, modifiers, layout, llp, "F5", HIDKeycodes.KEY_F5);
    	addButton(ctx, remote, modifiers, layout, llp, "F6", HIDKeycodes.KEY_F6);
    	main.addView(layout);
    	
    	layout = new LinearLayout(ctx);
    	layout.setOrientation(LinearLayout.HORIZONTAL);   
    	addButton(ctx, remote, modifiers, layout, llp, "F7", HIDKeycodes.KEY_F7);
    	addButton(ctx, remote, modifiers, layout, llp, "F8", HIDKeycodes.KEY_F8);
    	addButton(ctx, remote, modifiers, layout, llp, "F9", HIDKeycodes.KEY_F9);
    	addButton(ctx, remote, modifiers, layout, llp, "F10", HIDKeycodes.KEY_F10);
    	addButton(ctx, remote, modifiers, layout, llp, "F11", HIDKeycodes.KEY_F11);
    	addButton(ctx, remote, modifiers, layout, llp, "F12", HIDKeycodes.KEY_F12);
    	main.addView(layout);
    	
    	layout = new LinearLayout(ctx);
    	layout.setOrientation(LinearLayout.HORIZONTAL);   
    	addButton(ctx, remote, modifiers, layout, llp, "Insert", HIDKeycodes.KEY_INSERT);
    	addButton(ctx, remote, modifiers, layout, llp, "Delete", HIDKeycodes.KEY_DELETE);
    	addButton(ctx, remote, modifiers, layout, llp, "Home", HIDKeycodes.KEY_HOME);
    	addButton(ctx, remote, modifiers, layout, llp, "End", HIDKeycodes.KEY_END);
    	addButton(ctx, remote, modifiers, layout, llp, "PgUp", HIDKeycodes.KEY_PAGE_UP);
    	addButton(ctx, remote, modifiers, layout, llp, "PgDown", HIDKeycodes.KEY_PAGE_DOWN);
    	main.addView(layout);
    	
    	layout = new LinearLayout(ctx);
    	layout.setOrientation(LinearLayout.HORIZONTAL);   
    	addButton(ctx, remote, modifiers, layout, llp, "NumLock", HIDKeycodes.KEY_NUM_LOCK);
    	addButton(ctx, remote, modifiers, layout, llp, "CapsLock", HIDKeycodes.KEY_CAPS_LOCK);
    	addButton(ctx, remote, modifiers, layout, llp, "ScrLock", HIDKeycodes.KEY_SCROLL_LOCK);
    	addButton(ctx, remote, modifiers, layout, llp, "PrScrn", HIDKeycodes.KEY_PRINT_SCREEN);
    	addButton(ctx, remote, modifiers, layout, llp, "Pause", HIDKeycodes.KEY_PASUE);
    	main.addView(layout);
    	
    	layout = new LinearLayout(ctx);
    	layout.setOrientation(LinearLayout.HORIZONTAL);   
    	addButton(ctx, remote, modifiers, layout, llp, "Esc", HIDKeycodes.KEY_ESCAPE);
    	addButton(ctx, remote, modifiers, layout, llp, "Tab", HIDKeycodes.KEY_TAB);
    	addButton(ctx, remote, modifiers, layout, llp, "`", HIDKeycodes.KEY_GRAVE);
    	addButton(ctx, remote, modifiers, layout, llp, "Space", HIDKeycodes.KEY_SPACEBAR);
    	addButton(ctx, remote, modifiers, layout, llp, "\u2190 Backspace", HIDKeycodes.KEY_BACKSPACE);
    	addButton(ctx, remote, modifiers, layout, llp, "Enter", HIDKeycodes.KEY_ENTER);
    	main.addView(layout);
    	
    	layout = new LinearLayout(ctx);
    	layout.setOrientation(LinearLayout.HORIZONTAL);   
    	addButton(ctx, remote, modifiers, layout, llp, "\u2190", HIDKeycodes.KEY_ARROW_LEFT);
    	addButton(ctx, remote, modifiers, layout, llp, "\u2192", HIDKeycodes.KEY_ARROW_RIGHT);
    	addButton(ctx, remote, modifiers, layout, llp, "\u2191", HIDKeycodes.KEY_ARROW_UP);
    	addButton(ctx, remote, modifiers, layout, llp, "\u2193", HIDKeycodes.KEY_ARROW_DOWN);
    	main.addView(layout);
		
		sl.addView(main);
		dialog.setView(sl);						
		dialog.setPositiveButton(android.R.string.cancel, null);
		    	
		final AlertDialog tmp = dialog.create();
		return tmp;
    }
    
    private static void addButton(Context ctx, final RemoteSupport remote, final ModifiersSupport modifiers, LinearLayout layout, LinearLayout.LayoutParams params, String label, byte keyCode) {
		Button button = new Button(ctx);
		button.setText(label);		
		button.setLayoutParams(params);
		button.setTag(keyCode);
		button.setSingleLine();
		button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Button b = (Button)v;
				byte key = (Byte)b.getTag();
				remote.pressAndRelease(modifiers.getModifiers(), key);
			}
			
		});
		layout.addView(button);
    }
    
	
}
	