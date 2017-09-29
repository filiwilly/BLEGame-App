package com.cybernetxsystems.bluetooth.le;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;



import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceControlActivity extends Activity implements Serializable{
    private final static String TAG = DeviceControlActivity.class.getSimpleName();

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mDataField;
    private String mDeviceName;
    private String mDeviceAddress;
    public BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    
    EditText edtSend;
	ScrollView svResult;
	Button btnSend;
    Button btmando;
    TextView txt;

	//##FM added 
	private FileUtilities Flogging = new FileUtilities(this);
	String test;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            
            Log.e(TAG, "mBluetoothLeService is okay");
            //mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
			
			// Conexion con exito
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {  
            	Log.e(TAG, "Only gatt, just wait");
				
			  // Desconectar
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) { 
                mConnected = false;
                invalidateOptionsMenu();
                btnSend.setEnabled(false);
                btmando.setEnabled(false);
                clearUI();

            }else if(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
            {
            	mConnected = true;
            	mDataField.setText("");
            	ShowDialog();
                txt.setText("CONECTADO");
            	//btnSend.setEnabled(true);
                btmando.setEnabled(true);
            	Log.e(TAG, "In what we need");
            	invalidateOptionsMenu();
            	
            	//##FM
            	//Flogging.open(test);
            	Flogging.open();
            
			 // Recibe datos
            }else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) { 
            //	SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
           // 	String currentDateandTime = sdf.format(new Date());
            	Log.e(TAG, "RECV DATA");
            	String data = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
            	if (data != null) {
                	if (mDataField.length() > 500)
                		mDataField.setText("");
                    mDataField.append(data); 
                    
                    //##FM save the data to csv file
                  //  Flogging.write(currentDateandTime, data);
                    Flogging.write2(data);
                    svResult.post(new Runnable() {
            			public void run() {
            				svResult.fullScroll(ScrollView.FOCUS_DOWN);
            			}
            		});
                }
            }
        }
    };

    private void clearUI() {
        mDataField.setText(R.string.no_data);
    }

	// initialization
    @Override
    public void onCreate(Bundle savedInstanceState) {                                      
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gatt_services_characteristics);

        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        TextView conexion = (TextView) findViewById(R.id.textView);
        mDataField = (TextView) findViewById(R.id.data_value);
        //edtSend = (EditText) this.findViewById(R.id.edtSend);
        //edtSend.setText("");//#fred
        svResult = (ScrollView) this.findViewById(R.id.svResult);
        
        //btnSend = (Button) this.findViewById(R.id.btnSend);
		//btnSend.setOnClickListener(new ClickEvent());
		//btnSend.setEnabled(false);
        txt= (TextView) this.findViewById(R.id.textView);
        btmando = (Button) this.findViewById(R.id.mando);
        btmando.setEnabled(true);


        getActionBar().setTitle(mDeviceName);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        Log.d(TAG, "Try to bindService=" + bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE));
        
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //this.unregisterReceiver(mGattUpdateReceiver);
        //unbindService(mServiceConnection);
        if(mBluetoothLeService != null)
        {
        	mBluetoothLeService.close();
        	mBluetoothLeService = null;
        }
        Log.d(TAG, "We are in destroy"); 
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

	// Click en el menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {                             
        switch(item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
            	if(mConnected)
            	{
            		mBluetoothLeService.disconnect();
            		mConnected = false;
            	}
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void ShowDialog()
    {
    	Toast.makeText(this, "Conectado", Toast.LENGTH_SHORT).show();
    }
/*
 // button event
	class ClickEvent implements View.OnClickListener {
		@Override
		public void onClick(View v) {
			if (v == btnSend) {
				if(!mConnected) return;
				
				if (edtSend.length() < 1) {
					Toast.makeText(DeviceControlActivity.this, "No Data Sent", Toast.LENGTH_SHORT).show();
					return;
				}
				mBluetoothLeService.WriteValue(edtSend.getText().toString());
				
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if(imm.isActive())
					imm.hideSoftInputFromWindow(edtSend.getWindowToken(), 0);
				//todo Send data
				//##FM
				//clear after sending the data.
				edtSend.setText("");
			}
		}
	}
	*/
    //Evento mando
    public void abremando (View v){
        Intent i = new Intent(this, Gamepad.class );
        i.putExtra(Gamepad.EXTRAS_DEVICE_NAME, mDeviceName);
        i.putExtra(Gamepad.EXTRAS_DEVICE_ADDRESS, mDeviceAddress);
        startActivity(i);
    }
	
	// Registered the event received
    private static IntentFilter makeGattUpdateIntentFilter() {                   
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }
}