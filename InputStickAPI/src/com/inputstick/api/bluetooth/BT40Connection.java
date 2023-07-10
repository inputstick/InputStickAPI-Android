package com.inputstick.api.bluetooth;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.inputstick.api.InputStickError;
import com.inputstick.api.Util;

@SuppressLint("NewApi")
public class BT40Connection extends BTConnection {	
	
	private static final int HW_NONE = 0;
	private static final int HW_HM = 1;
	private static final int HW_NRF = 2;
	
	private static final int CONNECTION_TIMEOUT = 10000;
    
	//CC2540/HM10
	public static final String UUID_HM_DESC = 	"00002902-0000-1000-8000-00805f9b34fb";
	public static final String UUID_HM_SPS = 	"0000ffe0-0000-1000-8000-00805f9b34fb";
	public static final String UUID_HM_RX_TX =  "0000ffe1-0000-1000-8000-00805f9b34fb";

	//NRF
	public static final String UUID_NRF_DESC =  "00002902-0000-1000-8000-00805f9b34fb";
	public static final String UUID_NRF_SPS =  	"6e400001-b5a3-f393-e0a9-e50e24dcca9e";
	public static final String UUID_NRF_RX =  	"6e400003-b5a3-f393-e0a9-e50e24dcca9e";
	public static final String UUID_NRF_TX =  	"6e400002-b5a3-f393-e0a9-e50e24dcca9e";

	private Handler handler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
	private BluetoothGattCharacteristic characteristicRx;
	private BluetoothGattCharacteristic characteristicTx;
	private int hardwareType; //connected hardware type: HM10/NRF

    private LinkedList<byte[]> txBuffer;
    private boolean canSend;
    private long lastRxTime;
    private int mStatusUpdateInterval; //prevents packet lost caused by HM10 & BLE stack
    
    private boolean isConnecting;
    

	
    public BT40Connection(Application app, BTService btService, String mac, boolean reflections) {
    	super(app, btService, mac, reflections);    	     	
    	BluetoothManager bluetoothManager = (BluetoothManager) (mCtx.getSystemService(Context.BLUETOOTH_SERVICE));
		mBluetoothAdapter = bluetoothManager.getAdapter();    	
		mStatusUpdateInterval = 0;
    }

	@SuppressLint("MissingPermission")
	@Override
	public void connect() {
		Util.log(Util.FLAG_LOG_BT_CALLS, "Connect (4.0)");		
		final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mMac);
		if (device != null) {			
			mBluetoothGatt = device.connectGatt(mCtx, false, mGattCallback);
			hardwareType = HW_NONE;
			isConnecting = true;
			handler = new Handler(Looper.getMainLooper());
			handler.postDelayed(new Runnable() {
			    @Override
			    public void run() {
					if (isConnecting) {
						disconnect();
						mBTservice.connectionFailed(true, InputStickError.ERROR_BLUETOOTH_CONNECTION_FAILED);
					}
			    }
			}, CONNECTION_TIMEOUT);
		} else {
			mBTservice.connectionFailed(false, InputStickError.ERROR_BLUETOOTH_NO_REMOTE_DEVICE);
		}		
	}

	@SuppressLint("MissingPermission")
	@Override
	public void disconnect() {
		Util.log(Util.FLAG_LOG_BT_CALLS, "Disconnect (4.0)");
		txBuffer = null;
		try {
			if (mBluetoothGatt != null) {
				mBluetoothGatt.close();
				mBluetoothGatt.disconnect();
				mBluetoothGatt = null;
			}
		} catch (Exception e) {
		
		}
	}
	

	@Override
	public void setStatusUpdateInterval(int interval) {
		mStatusUpdateInterval = interval;
		lastRxTime = System.currentTimeMillis() - mStatusUpdateInterval; //force to wait for next update packet
		Util.log(Util.FLAG_LOG_BT_CALLS, "Status Update Interval set to: " + interval);

		if (hardwareType == HW_NRF) {
			mStatusUpdateInterval = 0;
		}
	}

	@Override
	public void write(byte[] out) {
    	byte[] tmp;
    	int offset = 0;
    	
    	//SPECIAL CASES for flashing utility
    	if (Util.flashingToolMode) {
	    	if (out.length == 1) {
	    		txBuffer.add(out);
	    		sendNext();
	    		return;
	    	}
	    	if (out.length == 1026) {
	    		tmp = new byte[2];
	    		tmp[0] = out[0];
	    		tmp[1] = out[1];
	    		txBuffer.add(tmp);    		
	    		offset = 2;
	    		for (int i = 0; i < 64; i++) {
	    			tmp = new byte[16];
	    			System.arraycopy(out, offset, tmp, 0, 16);
	    			offset += 16;
	    			txBuffer.add(tmp);
	    		}
	    		sendNext();
	    		return;
	    	}
    	}
    	    	
    	if (out.length == 2) {
    		addHeader(out);
    	} else if (out.length == 20) {
    		addHMAC(out);
    	} else {    		
    		int loops = out.length / 16;
    		offset = 0;
    		for (int i = 0; i < loops; i++) {
    			tmp = new byte[16];
    			System.arraycopy(out, offset, tmp, 0, 16);
    			offset += 16;
    			addData16(tmp);
    		}
    		sendNext();
    	}
	}	
	
	
	private byte h0;
	private byte h1;
	private boolean header;
	
	private synchronized void addHeader(byte[] data) {
		h0 = data[0];
		h1 = data[1];
		header = true;
	}
	
	private synchronized void addHMAC(byte[] data) {
		if (txBuffer != null) {
			txBuffer.add(data);
		}
	}
	
	private synchronized void addData16(byte[] data) {
		byte[] tmp;
		int offset = 0;
		if (txBuffer != null) {
			if (header) {
				header = false;
				
	    		tmp = new byte[18];
	    		offset = 2;
	    		
	    		tmp[0] = h0;
	    		tmp[1] = h1;    		
			} else {
	    		tmp = new byte[16];
	    		offset = 0;
			}
			System.arraycopy(data, 0, tmp, offset, 16);
			txBuffer.add(tmp);
		}
	}
	
	private synchronized byte[] getData() {
		if (txBuffer != null) {
			if (!txBuffer.isEmpty()) {
				byte[] data = txBuffer.poll();
				return data;
			}
		}
		return null;
	}

	@SuppressLint("MissingPermission")
	private synchronized void sendNext() {
		//prevent packet lost (do not send data if we know that in next few ms data will be received (sending data now could result in a packet lost in some cases)
		long time = System.currentTimeMillis();
		if ((mStatusUpdateInterval > 0) && (time > lastRxTime + mStatusUpdateInterval - 45)) {
			return;
		}		
		
		if (canSend) {
			byte[] data = getData();
			if (data != null) {
				canSend = false;
				boolean r1 = false;
				boolean r2 = false;

				r1 = characteristicTx.setValue(data);
				characteristicTx.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
				r2 = mBluetoothGatt.writeCharacteristic(characteristicTx);
				
				Util.log(Util.FLAG_LOG_BT_PACKET, "sendNext: " + r1 + " / " + r2);
			} else {
				Util.log(Util.FLAG_LOG_BT_PACKET, "sendNext: no data to send");
			}
		}
	}
	
	
	
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

		@SuppressLint("MissingPermission")
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			Util.log(Util.FLAG_LOG_BT_ADAPTER, "onConnectionStateChange" + newState);
			if (newState == BluetoothProfile.STATE_CONNECTED) {				
				isConnecting = false;
				if (mBluetoothGatt != null) {
					boolean result = mBluetoothGatt.discoverServices();
					Util.log(Util.FLAG_LOG_BT_ADAPTER, "Attempting to start service discovery: " + result);
				}
			} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {				
				isConnecting = false;
				mBTservice.connectionFailed(false, InputStickError.ERROR_BLUETOOTH_CONNECTION_LOST);
			}
		}

		@SuppressLint("MissingPermission")
		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				Util.log(Util.FLAG_LOG_BT_ADAPTER, "onServicesDiscovered: OK");

				List<BluetoothGattService> gattServices = null;
				if (mBluetoothGatt != null) {
					gattServices = mBluetoothGatt.getServices();
				}
				if (gattServices != null) {
					for (BluetoothGattService gattService : gattServices) {
						if (UUID_HM_SPS.equals(gattService.getUuid().toString())) {
							characteristicRx = gattService.getCharacteristic(UUID.fromString(UUID_HM_RX_TX));
							characteristicTx = characteristicRx;
							hardwareType = HW_HM;
						}
						if (UUID_NRF_SPS.equals(gattService.getUuid().toString())) {
							characteristicRx = gattService.getCharacteristic(UUID.fromString(UUID_NRF_RX));
							characteristicTx = gattService.getCharacteristic(UUID.fromString(UUID_NRF_TX));
							hardwareType = HW_NRF;
						}
						if (characteristicRx != null && characteristicTx != null) {
							break;
						}
					}
				}

		        if (hardwareType == HW_HM) {
	            	Util.log(Util.FLAG_LOG_BT_ADAPTER, "Serial service discovered (HM type)");
		        	//enable notifications
	            	boolean result;		            	
		            result = mBluetoothGatt.setCharacteristicNotification(characteristicRx, true);	
		            Util.log(Util.FLAG_LOG_BT_ADAPTER, "setCharacteristicNotification: " + result);
		            
	                BluetoothGattDescriptor descriptor = characteristicRx.getDescriptor(UUID.fromString(UUID_HM_DESC));
	                
	                result = descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);	
					Util.log(Util.FLAG_LOG_BT_ADAPTER, "setValue: " + result);
					
	                result = mBluetoothGatt.writeDescriptor(descriptor);	
	                Util.log(Util.FLAG_LOG_BT_ADAPTER, "writeDescriptor: " + result);
	                Util.log(Util.FLAG_LOG_BT_ADAPTER, "Descriptor UUID: " + descriptor.getUuid());
		        } else if (hardwareType == HW_NRF) {
		        	Util.log(Util.FLAG_LOG_BT_ADAPTER, "Serial service discovered (DA type)");
		        	//enable notifications
		        	boolean result;		
		        	result = mBluetoothGatt.setCharacteristicNotification(characteristicRx, true);	
		        	Util.log(Util.FLAG_LOG_BT_ADAPTER, "setCharacteristicNotification: " + result);
		        	
		        	BluetoothGattDescriptor descriptor = characteristicRx.getDescriptor(UUID.fromString(UUID_NRF_DESC));
		        	
		        	result = descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		        	Util.log(Util.FLAG_LOG_BT_ADAPTER, "setValue: " + result);
		        	
		        	result = mBluetoothGatt.writeDescriptor(descriptor);	
	                Util.log(Util.FLAG_LOG_BT_ADAPTER, "writeDescriptor: " + result);
	                Util.log(Util.FLAG_LOG_BT_ADAPTER, "Descriptor UUID: " + descriptor.getUuid());
		        } else {
		        	Util.log(Util.FLAG_LOG_BT_EXCEPTION, "Serial service NOT found");		        	
		        	mBTservice.connectionFailed(false, InputStickError.ERROR_BLUETOOTH_BT40_NO_SPP_SERVICE);
		        }
			} else {
				Util.log(Util.FLAG_LOG_BT_EXCEPTION, "onServicesDiscovered: " + status);
			}
		}
		
		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)  {
			Util.log(Util.FLAG_LOG_BT_ADAPTER, "onDescriptorWrite: " + status);
			
            txBuffer = new LinkedList<byte[]>();		     			            
            canSend = true;
            lastRxTime = System.currentTimeMillis();
            sendNext();
            
            mBTservice.connectionEstablished();
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			Util.log(Util.FLAG_LOG_BT_ADAPTER, "onCharacteristicRead: " + status);
			if (status == BluetoothGatt.GATT_SUCCESS) {
			} 
		}

		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			byte b[] = characteristic.getValue();
			if (b != null) {
				Util.log(Util.FLAG_LOG_BT_PACKET, "onCharacteristicChanged (" + b.length + ")");
				//send next packet only if a complete packet was received
				boolean processedPacket = mBTservice.onByteRx(b);				
				if (processedPacket) {				
					lastRxTime = System.currentTimeMillis();
					sendNext();
				}
			} else {
				Util.log(Util.FLAG_LOG_BT_EXCEPTION, "onCharacteristicChanged (null)");
			}
		}

		@Override
		public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {			
			if (status == BluetoothGatt.GATT_SUCCESS) {
				Util.log(Util.FLAG_LOG_BT_PACKET, "onCharacteristicWrite: OK");
				canSend = true;
				sendNext();
			} else {
				Util.log(Util.FLAG_LOG_BT_EXCEPTION, "onCharacteristicWrite: " + status);
			}
		}
		
	};	

}
