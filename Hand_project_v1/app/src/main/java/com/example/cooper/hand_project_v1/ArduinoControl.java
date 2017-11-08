package com.example.cooper.hand_project_v1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class ArduinoControl extends AppCompatActivity {

    //Declare Seek bar
    SeekBar seekbar;
    TextView seekbar_value;

    //Member Fields
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;

    //UUID service - This is the type of bluetooth device our Module
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //MAC - address of Bluetooth Module
    public String newAddress = null;

    //Method to easily display toasts.
    public void Msg(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arduino_control);

        //Initialising seekbar
        seekbar = (SeekBar) findViewById(R.id.seekbar);
        seekbar_value = (TextView) findViewById(R.id.seekbar_value);
        seekbar.setMax(255);

        //getting the bluetooth adapter value and calling check BT state function
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        //Set seekbar listener
        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                seekbar_value.setText(Integer.toString(progress));
                sendData(Integer.toString(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Get MAC address from MainActivity
        Intent intent = getIntent();
        newAddress = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);

        //Set up a pointer to the remote device using its address
        BluetoothDevice device = btAdapter.getRemoteDevice(newAddress);

        //Attempt to create a bluetooth socket for comms
        try {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch(IOException e1) {
            Msg("ERROR - Could not create Bluetooth socket");
        }
        //Establish the connection
        try{
            btSocket.connect();
        } catch (IOException e) {
            try{
                btSocket.close();
            } catch(IOException e2) {
                Msg("ERROR - Could not close Bluetooth socket");
            }
        }

        //Create a data stream so we can talk to the device
        try{
            outStream = btSocket.getOutputStream();
        } catch(IOException e) {
            Msg("ERROR - Could not create bluetooth outstream");
        }
        //When the activity is resumed, send junk data to force a failure if not connected
        sendData("x");
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Close all connections so resources are not waisted

        //Close BT socket to device
        try{
            btSocket.close();
        } catch (IOException e2){
            Msg("ERROR - Failed to close Bluetooth socket");
        }
    }

    //Takes UUID and creates a comms socket
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return device.createRfcommSocketToServiceRecord(MY_UUID);
    }

    //method to check if the device has Bluetooth and if it is on.
    //Also prompts user to turn it back on
    private void checkBTState()
    {
        // Check device has Bluetooth and that it is turned on
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if(btAdapter == null) {
            Msg("Device does not support Bluetooth.");
            finish();
        } else {
            if(!btAdapter.isEnabled()) {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    //Method to send data
    private void sendData(String data) {
        byte[] dataBuffer = data.getBytes();

        try{
            //attempt to place data in the output stream of the BT device
            outStream.write(dataBuffer);
        } catch(IOException e) {
            Msg("ERROR - Device not found");
            finish();
        }
    }

    public void addKeyListener() {

    }
}
