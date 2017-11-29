package com.example.cooper.hand_project_v1;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ArduinoControl extends AppCompatActivity {

    //Declare Seek bar
    Button send250, send0;
    Handler bluetoothIn;

    //Member Fields
    final int handlerState = 0; //Used to identify handler message
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    //private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    //UUID service - This is the type of bluetooth device our Module
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    //MAC - address of Bluetooth Module
    private static String address;

    //Method to easily display toasts.
    public void Msg(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arduino_control);

        //Initialising buttons
        send250 = (Button) findViewById(R.id.data250Button);
        send0 = (Button) findViewById(R.id.data0Button);

        bluetoothIn = new Handler() {
            //Code for reading data goes here
        };

        //getting the bluetooth adapter value and calling check BT state function
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        // Set up onClick listeners for buttons to send 1 or 0 to turn on/off LED
        send0.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("0");    // Send "0" via Bluetooth
                Msg("Sent 0 to Bluetooth");
            }
        });

        send250.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mConnectedThread.write("250");    // Send "1" via Bluetooth
                Msg("Sent 250 to Bluetooth");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Get MAC address from MainActivity
        Intent intent = getIntent();
        address = intent.getStringExtra(MainActivity.EXTRA_DEVICE_ADDRESS);

        //Set up a pointer to the remote device using its address
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

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

        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        mConnectedThread.write("X");
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

    //Create separate thread for data transfer
    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //create connect thread
        public ConnectedThread(BluetoothSocket socket){
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try{
                //Create I/O stream for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }catch (IOException e){}

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[256];
            int bytes;

            //Keep looping while listen for messages
            while(true){
                try{
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                }catch(IOException e){
                    break;
                }
            }
        }

        public void write(String input){
            byte[] messageBuffer = input.getBytes();
            try{
                mmOutStream.write(messageBuffer);
            }catch(IOException e){
                Msg("Connection Failure");
                finish();
            }
        }


    }

}
