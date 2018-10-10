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
	private static final int HW_DA = 2;
	
	private static final int CONNECTION_TIMEOUT = 10000;
    
	//CC2540/HM10
	private static final String MOD_CHARACTERISTIC_CONFIG = 	"00002902-0000-1000-8000-00805f9b34fb";
	private static final String MOD_CONF = 						"0000ffe0-0000-1000-8000-00805f9b34fb";
	private static final String MOD_RX_TX = 					"0000ffe1-0000-1000-8000-00805f9b34fb";															  	
	private static final UUID 	UUID_HM_RX_TX = 				UUID.fromString(MOD_RX_TX);
	
	//DA14580
	private static final String DA_SPS = 					"0783b03e-8535-b5a0-7140-a304d2495cb7";
	private static final String DA_SPS_TX =					"0783b03e-8535-b5a0-7140-a304d2495cb8";	
	private static final String DA_SPS_RX =					"0783b03e-8535-b5a0-7140-a304d2495cba";	
	private static final String DA_DESC =					"00002901-0000-1000-8000-00805f9b34fb";
	
	private static final UUID 	DA_UUID_RX = 				UUID.fromString(DA_SPS_RX);	 
	private static final UUID 	DA_UUID_TX = 				UUID.fromString(DA_SPS_TX);	
	private static final UUID 	DA_UUID_DESC = 				UUID.fromString(DA_DESC);
    
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;

    private LinkedList<byte[]> txBuffer;
    private boolean canSend;
    private long lastRxTime;
    private long connectTime;
    
    private boolean isConnecting;
    private Handler handler;
    
    private int hardwareType; //connected hardware type: hm10/da14580
    
    BluetoothGattCharacteristic characteristicRx;
    BluetoothGattCharacteristic characteristicTx;
	
    public BT40Connection(Application app, BTService btService, String mac, boolean reflections) {
    	super(app, btService, mac, reflections);    	     	
    	BluetoothManager bluetoothManager = (BluetoothManager) (mCtx.getSystemService(Context.BLUETOOTH_SERVICE));
		mBluetoothAdapter = bluetoothManager.getAdapter();    	
    }
    
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
	public void write(byte[] out) {
    	byte[] tmp;
    	int offset = 0;
    	
    	//SPECIAL CASES for flashing utility
    	if (Util.flashingToolMode) {
    		//txBuffer.add(out);
    		//return;
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
	
	private synchronized void sendNext() {
		long time = System.currentTimeMillis();
		if ((time > lastRxTime + 55) && (time > connectTime + 3000)) {
			long diff = time - lastRxTime;
			System.out.println("deny: " + time + " / " + lastRxTime + " / " + diff);
			return;
		}		
		
		if (canSend) {
			byte[] data = getData();
			if (data != null) {
				canSend = false;
				boolean r1 = false;
				boolean r2 = false;
				
				if (hardwareType == HW_HM) {
					BluetoothGattService gattService = mBluetoothGatt.getService(UUID.fromString(MOD_CONF));
					BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(UUID_HM_RX_TX);
					r1 = gattCharacteristic.setValue(data);
					gattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);		
					r2 = mBluetoothGatt.writeCharacteristic(gattCharacteristic);
					System.out.println("SEND");
				}
				if (hardwareType == HW_DA) {
					BluetoothGattService gattService = mBluetoothGatt.getService(UUID.fromString(DA_SPS));  
					BluetoothGattCharacteristic gattCharacteristic = gattService.getCharacteristic(DA_UUID_RX);  					
					r1 = gattCharacteristic.setValue(data);
					gattCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);		
					r2 = mBluetoothGatt.writeCharacteristic(gattCharacteristic);
				}
				
				Util.log(Util.FLAG_LOG_BT_PACKET, "sendNext: " + r1 + " / " + r2);
			} else {
				Util.log(Util.FLAG_LOG_BT_PACKET, "sendNext: no data to send");
			}
		}
	}
	
	
	
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		
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

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {								
			if (status == BluetoothGatt.GATT_SUCCESS) {		
				Util.log(Util.FLAG_LOG_BT_ADAPTER, "onServicesDiscovered: OK");
				List<BluetoothGattService> gattServices = null;		
		        if (mBluetoothGatt != null) {
		        	gattServices = mBluetoothGatt.getServices();
		        }
		        //BluetoothGattCharacteristic characteristicRxTx = null;
		        if (gattServices != null) {
			        String uuid = null;
			        
			        for (BluetoothGattService gattService : gattServices) {
			            uuid = gattService.getUuid().toString();
			            Util.log(Util.FLAG_LOG_BT_ADAPTER, "uuid: " + uuid);
			            if (MOD_CONF.equals(uuid)) {			            	
			            	characteristicRx = gattService.getCharacteristic(UUID_HM_RX_TX);
			            	characteristicTx = characteristicRx;
				    		 if (characteristicRx == null) {
				    			 mBTservice.connectionFailed(false, InputStickError.ERROR_BLUETOOTH_BT40_NO_SPP_SERVICE);
				    		 } else {
				    			 hardwareType = HW_HM;
				    		 }
				    		 break;
			            }
			            
			            if (DA_SPS.equals(uuid)) {		//
			            	characteristicRx = gattService.getCharacteristic(DA_UUID_RX);
				    		characteristicTx = gattService.getCharacteristic(DA_UUID_TX);				    		 
							if (characteristicTx == null || characteristicRx == null) {
								mBTservice.connectionFailed(false, InputStickError.ERROR_BLUETOOTH_BT40_NO_SPP_SERVICE);
							} else {
								hardwareType = HW_DA;
							}
			            }
			        }				
		        }
		        if (hardwareType == HW_HM) {
	            	Util.log(Util.FLAG_LOG_BT_ADAPTER, "Serial service discovered (HM type)");
		        	//enable notifications
	            	boolean result;		            	
		            result = mBluetoothGatt.setCharacteristicNotification(characteristicRx, true);	
		            Util.log(Util.FLAG_LOG_BT_ADAPTER, "setCharacteristicNotification: " + result);
		            
	                BluetoothGattDescriptor descriptor = characteristicRx.getDescriptor(UUID.fromString(MOD_CHARACTERISTIC_CONFIG));		                
	                
	                result = descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);	
					Util.log(Util.FLAG_LOG_BT_ADAPTER, "setValue: " + result);
					
	                result = mBluetoothGatt.writeDescriptor(descriptor);	
	                Util.log(Util.FLAG_LOG_BT_ADAPTER, "writeDescriptor: " + result);
	                Util.log(Util.FLAG_LOG_BT_ADAPTER, "Descriptor UUID: " + descriptor.getUuid());
		        } else if (hardwareType == HW_DA) {
		        	Util.log(Util.FLAG_LOG_BT_ADAPTER, "Serial service discovered (DA type)");
		        	//enable notifications
		        	boolean result;		
		        	result = mBluetoothGatt.setCharacteristicNotification(characteristicRx, true);	
		        	Util.log(Util.FLAG_LOG_BT_ADAPTER, "setCharacteristicNotification: " + result);
		        	result = mBluetoothGatt.setCharacteristicNotification(characteristicTx, true);	
		        	Util.log(Util.FLAG_LOG_BT_ADAPTER, "setCharacteristicNotification: " + result);
		        	
		        	BluetoothGattDescriptor descriptor = characteristicTx.getDescriptor(DA_UUID_DESC);		        	
		        	
		        	result = descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);	
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
            connectTime = System.currentTimeMillis();
            lastRxTime = connectTime;
			System.out.println("init rx " + lastRxTime);
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
				mBTservice.onByteRx(b);
				
				lastRxTime = System.currentTimeMillis();
				System.out.println("rx " + lastRxTime);
				sendNext();
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
