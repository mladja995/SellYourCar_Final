package com.example.mladen.sellyourcar.ui;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.example.mladen.sellyourcar.R;
import com.example.mladen.sellyourcar.services.BluetoothConnectionService;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.UUID;


public class BluetoothActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    /*Variables*/
    static final String TAG = "BluetoothActivity";
    BluetoothAdapter mBluetoothAdapter;
    Button btnONOFF, btnEnableDiscoverable, btnDiscover, btnStartConnection, btnSend;
    ListView lvDiscoveredDevices;
    ArrayList<BluetoothDevice> discoveredDevices = new ArrayList<BluetoothDevice>();
    ArrayList<String> discoveredDevicesNames = new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;
    BluetoothConnectionService mBluetoothConnection;
    EditText etTextToSend;
    BluetoothDevice mBTdevice;
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    /*Broadcast receivers*/
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED))
            {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);
                switch(state)
                {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }

        }
    };

    private final BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if (action.equals(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED))
            {
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                switch(mode)
                {
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability enabled.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability disabled. Able to receive connections.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "mBroadcastReceiver2: Discoverability disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "mBroadcastReceiver2: Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "mBroadcastReceiver2: Connected.");
                        break;
                }
            }

        }
    };

    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_FOUND))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(!discoveredDevices.contains(device))
                {
                    discoveredDevices.add(device);
                    discoveredDevicesNames.add(device.getName());
                    arrayAdapter.notifyDataSetChanged();
                }
            }

        }
    };

    private final BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
            {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1: bounded already
                if(device.getBondState() == BluetoothDevice.BOND_BONDED)
                {
                    Log.d(TAG, "onReceive: BOND_BOUNDED.");
                    mBTdevice = device;
                    //arrayAdapter.notifyDataSetChanged();

                }
                //case2: creating a bone
                if(device.getBondState() == BluetoothDevice.BOND_BONDING)
                {
                    Log.d(TAG, "onReceive: BOND_BONDING.");
                }
                //case3: breaking a bond
                if(device.getBondState() == BluetoothDevice.BOND_NONE)
                {
                    Log.d(TAG, "onReceive: BOND_NONE.");
                }
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        /*Initalize*/
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btnONOFF = findViewById(R.id.bluetooth_btn_onoff);
        btnEnableDiscoverable = findViewById(R.id.bluetooth_btn_enable_discoverable);
        btnDiscover = findViewById(R.id.bluetooth_btn_discover);
        lvDiscoveredDevices = (ListView) findViewById(R.id.bluetooth_lv_discoverDevices);
        etTextToSend = findViewById(R.id.bluetooth_et_textToSend);
        btnStartConnection = findViewById(R.id.bluetooth_btn_startConnection);
        btnSend = findViewById(R.id.bluetooth_btn_send);

        /*Broadcasts when bond state changes(ie:pairing)*/
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);

        lvDiscoveredDevices.setOnItemClickListener(BluetoothActivity.this);

        /*Listeners*/
        btnONOFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: enabling/disabling bluetooth.");
                enableDisableBT();
            }
        });

        btnEnableDiscoverable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "btnEnableDisable_Discoverable: Making device discoverable for 300 seconds.");
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);

                IntentFilter intentFilter = new IntentFilter(mBluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
                registerReceiver(mBroadcastReceiver2, intentFilter);
            }
        });

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                Log.d(TAG, "btnDiscover: Looking for unpaired devices.");
                if(mBluetoothAdapter.isDiscovering())
                {
                    mBluetoothAdapter.cancelDiscovery();
                    Log.d(TAG, "btnDiscover: Canceling discovery.");

                    //check BT permisssions in manifest
                    checkBTPermissions();

                    mBluetoothAdapter.startDiscovery();
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
                }
                if(!mBluetoothAdapter.isDiscovering())
                {
                    //check BT permisssions in manifest
                    checkBTPermissions();

                    mBluetoothAdapter.startDiscovery();
                    IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);

                }
            }


        });

        btnStartConnection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    startConnection();
                }catch(NullPointerException e){
                    e.printStackTrace();
                }

            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    byte[] bytes = etTextToSend.getText().toString().getBytes(Charset.defaultCharset());
                    mBluetoothConnection.write(bytes);
                }catch(NullPointerException e){
                    e.printStackTrace();
                }

            }
        });

        arrayAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, discoveredDevicesNames);
        lvDiscoveredDevices.setAdapter(arrayAdapter);


    }

    public void startConnection() {
        startBTConnection(mBTdevice, MY_UUID_INSECURE);

    }

    public void startBTConnection(BluetoothDevice device, UUID uuid) {
        Log.d(TAG, "startBTConnection: Iniitalizing RFCOM Bluetooth Connection.");
        mBluetoothConnection.startClient(device, uuid);
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: called.");
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver2);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);
    }

    private void enableDisableBT() {
        if(mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if(!mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        if(mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: disabling BT.");
            mBluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void checkBTPermissions() {
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP)
        {
            int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
            if(permissionCheck != 0)
            {
                this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
            }
            else
            {
                Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLLIPOP");
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        mBluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You Clicked on a device.");
        String deviceName = discoveredDevices.get(i).getName();
        String deviceAddress = discoveredDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR1)
        {
            Log.d(TAG, "Trying to pair with " + deviceName);
            discoveredDevices.get(i).createBond();

            mBTdevice = discoveredDevices.get(i);
            mBluetoothConnection = new BluetoothConnectionService(BluetoothActivity.this);

        }

    }
}
