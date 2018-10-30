package com.inputstick.api;

import java.util.Arrays;
import java.util.Random;
import java.util.zip.CRC32;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.inputstick.api.bluetooth.BTService;

public class PacketManager {
	
	public static final int MAX_PAYLAOD = 64;
	public static final int HEADER_OFFSET = 2;
	public static final int CRC_OFFSET = 4;
	public static final int PACKET_SIZE = 16;
	
	private final BTService mBTService;
	private final AES mAes;
	private byte[] mKey;
	private byte[] cmpData;
	private final CRC32 mCrc;	
	private boolean mEncryption;
	
	private byte[] mHMACKey;	
	private int mHmacCounter;
	
	public PacketManager(BTService btService, byte[] key) {
		mBTService = btService;
		mCrc = new CRC32();
		mAes = new AES();
		mKey = key;
		mEncryption = false;
	}
	
	public boolean setEncryption(byte[] cmp, boolean encryptOut, byte[] encryptedHMACKey) {
		byte[] cmpDec = mAes.decrypt(cmp);	
		mHMACKey = null;
		if (Arrays.equals(cmpDec, cmpData)) {
			mEncryption = encryptOut;		
			if (encryptedHMACKey != null) {
				mHMACKey = mAes.decrypt(encryptedHMACKey);
			}
			mHmacCounter = 0;
			return true;
		} else {
			mEncryption = false;
			return false;
		}
	}
	
	public boolean isEncrypted() {
		return mEncryption;
	}
	
	public void changeKey(byte[] key){
		mKey = key;
	}
	
	public Packet encPacket(boolean enable, boolean hmac) {
		Random r = new Random();         
		Packet p;
		if (hmac) {
			p = new Packet(true, Packet.CMD_INIT_AUTH_HMAC);
		} else {
			p = new Packet(true, Packet.CMD_INIT_AUTH);
		}
		if (enable) {
			p.addByte((byte)1);
		} else {
			p.addByte((byte)0);
		}	
		
		byte[] iv = mAes.init(mKey);		
		p.addBytes(iv);
		
		//Util.printHex(mKey, "key: "); 	
		//Util.printHex(iv, "IV: ");
		
		byte[] initData = new byte[16];
		r.nextBytes(initData);		
		mCrc.reset();
		mCrc.update(initData, 4, 12); //only 12 bytes!
		long crcValue = mCrc.getValue();
		initData[3] = (byte)crcValue;
		crcValue >>= 8;
		initData[2] = (byte)crcValue;
		crcValue >>= 8;
		initData[1] = (byte)crcValue;
		crcValue >>= 8;
		initData[0] = (byte)crcValue;		
		initData = mAes.encrypt(initData);
		p.addBytes(initData);				
		
		//Util.printHex(initData, "InitData: ");
		
		cmpData = new byte[16];
		r.nextBytes(cmpData);
		p.addBytes(cmpData);
		
		//Util.printHex(cmpData, "CmpData: ");		
		return p;
	}
	
	
	
	public byte[] bytesToPacket(byte[] data) {
		byte[] payload;		
		long crcValue, crcCompare;
		
		//Util.printHex(data, "RX DATA: "); // TODO prnt
		
		payload = Arrays.copyOfRange(data, 2, data.length); //remove TAG, info
		if ((data[1] & Packet.FLAG_ENCRYPTED) != 0) {
			//Util.log("DECRYPT");
			if (mAes.isReady()) {
				payload = mAes.decrypt(payload);
			} else {
				return null;
			}
		}
		
		//Util.printHex(payload, "DATA IN: ");
	
		//check CRC		
		crcCompare = Util.getLong(payload[0], payload[1], payload[2], payload[3]);
		mCrc.reset();
		mCrc.update(payload, CRC_OFFSET, payload.length - CRC_OFFSET);
		crcValue = mCrc.getValue();
		//System.out.println("CMP: " + crcCompare + " VAL: " + crcValue); 				
		
		if (crcValue == crcCompare) {
			payload = Arrays.copyOfRange(payload, 4, payload.length); //remove CRC
			return payload;
		} else {
			return null; //TODO			
		}		
		
	}
	
	public void setStatusUpdateInterval(int interval) {
		mBTService.setStatusUpdateInterval(interval);
	}
	
	public void sendRAW(byte[] data) {
		mBTService.write(data);
	}	
	
	public void sendPacket(Packet p) {
		if (p != null) {
			sendPacket(p, mEncryption);
		}
	}
	
	public void sendPacket(Packet p, boolean encrypt) {
		byte[] result, header, data;
		int length;
		int packets;
		long crcValue;		
		
		//if data > MAX_PAYLAOD -> error
		
		data = p.getBytes();
		
		length = data.length + CRC_OFFSET; //include 4bytes for CRC32		
		packets = ((length - 1) >> 4) + 1; //how many 16 bytes data sub-packets are necessary		

		result = new byte[packets * PACKET_SIZE];
		System.arraycopy(data, 0, result, CRC_OFFSET, data.length);
				
		//add CRC32
		mCrc.reset();
		mCrc.update(result, CRC_OFFSET, result.length - CRC_OFFSET);		
		crcValue = mCrc.getValue();
		result[3] = (byte)crcValue;
	    crcValue >>= 8;
	    result[2] = (byte)crcValue;
		crcValue >>= 8;
		result[1] = (byte)crcValue;
		crcValue >>= 8;
		result[0] = (byte)crcValue;			
		
		if (encrypt) {
			result = mAes.encrypt(result);
		}
		
		header = new byte[2];
		header[0] = Packet.START_TAG;
		header[1] = (byte)packets;
		if (encrypt) {
			header[1] |= Packet.FLAG_ENCRYPTED;
		}
		if (p.getRespond()) {
			header[1] |= Packet.FLAG_RESPOND;
		}
		
		byte[] hmacPacket = null;
		if ((encrypt) && (mHMACKey != null)) {							    
			try {
				Mac sha256_HMAC;
				byte[] hmacOutput;
				int counter = mHmacCounter;
				mHmacCounter++;
				
				hmacPacket = new byte[20];
				hmacPacket[0] = (byte)counter;
				counter >>= 8;
				hmacPacket[1] = (byte)counter;
				counter >>= 8;
				hmacPacket[2] = (byte)counter;
				counter >>= 8;
				hmacPacket[3] = (byte)counter;						
				//calculate HMAC (counter|data)
				sha256_HMAC = Mac.getInstance("HmacSHA256");
			    sha256_HMAC.init(new SecretKeySpec(mHMACKey, "HmacSHA256"));
			    sha256_HMAC.update(hmacPacket, 0, 4); //counter
			    sha256_HMAC.update(result, 0, result.length); //data
			    hmacOutput = sha256_HMAC.doFinal();			    
			    System.arraycopy(hmacOutput, 0, hmacPacket, 4, 16);
			    
			    header[1] |= Packet.FLAG_HMAC;
			} catch (Exception e) {				
				hmacPacket = null;
			}
		}
		
		mBTService.write(header);
		mBTService.write(result);	
		if (hmacPacket != null) {
			mBTService.write(hmacPacket);
		}

	}

}
