package com.inputstick.api;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.annotation.SuppressLint;
import android.util.Log;


public abstract class Util {
	
	public static final int LOG_MAX_CAPACITY = 					500;
	
	public static final int FLAG_LOG_API =				 		0x00000001;
	
	public static final int FLAG_LOG_BT_CALLS = 				0x00000100;
	public static final int FLAG_LOG_BT_ADAPTER = 				0x00000200;
	public static final int FLAG_LOG_BT_PACKET = 				0x00000400;
	public static final int FLAG_LOG_BT_EXCEPTION = 			0x00000800;
	public static final int FLAG_LOG_BT_SERVICE =	 			0x00001000;	
	
	public static final int FLAG_LOG_UTILITY_CALLS = 			0x00010000;	
	public static final int FLAG_LOG_UTILITY_SERVICE = 			0x00020000;
	public static final int FLAG_LOG_UTILITY_UI = 				0x00040000;	
	public static final int FLAG_LOG_UTILITY_PACKET = 			0x00080000;	
	public static final int FLAG_LOG_UTILITY_EXCEPTION = 		0x00100000;
	
	public static final int FLAG_NONE = 						0x00000000;
	public static final int FLAG_ALL = 							0xFFFFFFFF;
	
	
	private static boolean logCatEnabled;
	private static int eventLogFlags;
	
	private static String[] logMessages;
	private static int[] logFlags;
	private static long[] logTimeStamps;
	private static int cnt;
	private static int totalCnt;
	
	
	public static boolean debug = false;
	public static boolean flashingToolMode = false;
	
	
	private static void initLog() {
		if ((logMessages == null) || (logFlags == null) || (logTimeStamps == null)) {
			logMessages = new String[LOG_MAX_CAPACITY];			
			logFlags = new int[LOG_MAX_CAPACITY];			
			logTimeStamps = new long[LOG_MAX_CAPACITY];		
			cnt = 0;
			totalCnt = 0;
		}
	}
	
	public static void clearLog() {
		logMessages = null;
		logFlags = null;
		logTimeStamps = null;
		initLog();
	}
	
	public static void setLogOptions(int logFlags, boolean logCat) {
		eventLogFlags = logFlags;
		logCatEnabled = logCat;
	}
	
	public static void log(int flag, String message) {
		if ((eventLogFlags & flag) != 0) {		
			initLog();
			logMessages[cnt] = message;
			logFlags[cnt] = flag;
			logTimeStamps[cnt] = System.currentTimeMillis();
			cnt++;
			totalCnt++;
			if (cnt == LOG_MAX_CAPACITY) {
				cnt = 0;
			}
			
			if (logCatEnabled) {		
				String msg;
				msg = "[" + getNameOfFlag(flag) + "] " + message;
				Log.d("InputStickUtility", msg);
			}
		}
	}
	
	public static String getLog(int printFlags) {
		String result = "";		
		initLog();
		if (totalCnt == 0) {
			return null;
		} else {
			int logged = cnt;
			if (cnt > LOG_MAX_CAPACITY) {
				logged = LOG_MAX_CAPACITY;
			}
			result += "[" + System.currentTimeMillis() + "] [LOG] Logged messages: " + logged + " (total: " + totalCnt + ")\n";
			for (int i = cnt; i < LOG_MAX_CAPACITY; i++) {
				if (logMessages[i] != null) {
					result += "[" + logTimeStamps[i] + "] ";
					result += "[" + getNameOfFlag(logFlags[i]) + "] ";								
					result += logMessages[i] + "\n";
				}
			}
			for (int i = 0; i < cnt; i++) {
				if (logMessages[i] != null) {
					result += "[" + logTimeStamps[i] + "] ";
					result += "[" + getNameOfFlag(logFlags[i]) + "] ";								
					result += logMessages[i] + "\n";
				}
			}
			return result;
		}		
	}		
	
	private static String getNameOfFlag(int flag) {
		switch (flag) {
			case FLAG_LOG_BT_PACKET:
				return "BT PACKET";
			case FLAG_LOG_UTILITY_PACKET:
				return "UTILITY PACKET";	
				
			case FLAG_LOG_BT_CALLS:
				return "BT CALL";
			case FLAG_LOG_BT_ADAPTER:
				return "BT ADAPTER";

			case FLAG_LOG_BT_EXCEPTION:
				return "BT EXCEPTION";
			case FLAG_LOG_BT_SERVICE:
				return "BT SERVICE";
				
				
			case FLAG_LOG_UTILITY_CALLS:
				return "UTILITY CALLS";
			case FLAG_LOG_UTILITY_SERVICE:
				return "UTILITY SERVICE";				
			case FLAG_LOG_UTILITY_UI:
				return "UTILITY UI";							
			case FLAG_LOG_UTILITY_EXCEPTION:
				return "UTILITY EXCEPTION";
				
			case FLAG_LOG_API:
				return "API";				
				
			default:
				return "UNKNOWN";
		}
	}	

	
	public static void printHex(byte[] toPrint, String info) {
		if (debug) {
			System.out.println(info);
			printHex(toPrint);
		}
	}

	@SuppressLint("DefaultLocale")
	public static String byteToHexString(byte b) {
		String s;
    	//0x0..0xF = 0x00..0x0F
    	if ((b < 0x10) && (b >= 0)) {
    		s = Integer.toHexString((int)b);
    		s = "0" + s;
    	} else {
        	s = Integer.toHexString((int)b);
        	if (s.length() > 2) {
        		s = s.substring(s.length() - 2);
        	}
    	}        	        	
    	s = s.toUpperCase();
    	return s;
	}

	public static void printHex(byte[] toPrint) {
		if (debug) {
			if (toPrint != null) {
				int cnt = 0;
				byte b;
		        for (int i = 0; i < toPrint.length; i++) {
		        	b = toPrint[i];  

		        	System.out.print("0x" + byteToHexString(b) + " ");
		        	cnt++;
		        	if (cnt == 8) {
		        		System.out.println("");
		        		cnt = 0;
		        	}
		        }
		        
			} else {
				System.out.println("null");
			}
			System.out.println("\n#####");
		}
	}
	
	
    public static byte getLSB(int n) {
        return (byte)(n & 0x00FF);
    }
    
    public static byte getMSB(int n) {
        return (byte)((n & 0xFF00) >> 8);
    }   
    
    public static int getInt(byte b) {
    	int bInt = b & 0xFF;
    	return bInt;
    }
    
    public static int getInt(byte msb, byte lsb) {
    	int msbInt = msb & 0xFF;
    	int lsbInt = lsb & 0xFF;
    	return (msbInt << 8) + lsbInt;    	
    } 	
	
	public static long getLong(byte b0, byte b1, byte b2, byte b3) {
		long result;		
		result = (b0) & 0xFF;
		result <<= 8;
		result += (b1) & 0xFF;
		result <<= 8;
		result += (b2) & 0xFF;
		result <<= 8;
		result += (b3) & 0xFF;				
		return result;
	}
	
	
	public static byte[] getPasswordBytes(String plainText) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");	
			return md.digest(plainText.getBytes("UTF-8"));
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public static String[] convertToStringArray(CharSequence[] charSequences) {
	    String[] result = new String[charSequences.length];
	    for (int i = 0; i < charSequences.length; i++) {
	    	result[i] = charSequences[i].toString();
	    }

	    return result;
	}
	

}
