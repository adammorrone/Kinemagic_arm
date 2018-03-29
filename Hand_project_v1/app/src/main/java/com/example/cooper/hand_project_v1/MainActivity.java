package com.example.cooper.hand_project_v1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    //Connection status text views
    TextView textConnectionStatus;
    ListView pairedListView;

    //Bluetooth member fields
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    //An EXTRA to take the device MAC to next activity
    public static String EXTRA_DEVICE_ADDRESS;

    //Method to easily display toasts.
    public void Msg(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkBTState();

        textConnectionStatus = findViewById(R.id.connecting);
        textConnectionStatus.setTextSize(40);
        textConnectionStatus.setText(" ");

        //Initialize array adapter for paired devices
        mPairedDevicesArrayAdapter =
                new ArrayAdapter(this, android.R.layout.simple_list_item_1);

        //Find and set up the Listview for paired devices
        pairedListView = findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        //Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        //Get a set of currently paired devices and append it to paired devices list
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        //Add previously paired devices to the array
        if(pairedDevices.size() > 0) {
            findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedDevicesArrayAdapter.add(noDevices);
        }
    }

    //method to check if the device has Bluetooth and if it is on.
    //Also prompts user to turn it back on
    private void checkBTState()
    {
        // Check device has Bluetooth and that it is turned on
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBtAdapter == null) {
            Msg("Device does not support Bluetooth.");
            finish();
        } else {
            if(!mBtAdapter.isEnabled()) {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //set up on-click listener for the ListView
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            textConnectionStatus.setText("Connecting...");
            //Get the device MAC address, which is the last 17 chars in the view
            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);

            //Make intent to start next activity while taking an MAC address extra
            Intent intent = new Intent(MainActivity.this, ArduinoControl.class);
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            startActivity(intent);

        }
    };
}