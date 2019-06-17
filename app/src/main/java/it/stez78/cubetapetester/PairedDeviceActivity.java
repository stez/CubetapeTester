package it.stez78.cubetapetester;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NavUtils;
import it.stez78.cubetapetester.models.PairedDevice;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.UUID;

public class PairedDeviceActivity extends AppCompatActivity {

    private static final String TAG = "PairedDeviceActivity";

    private static final int HANDLER_CONNECTION_OK = 1;
    private static final int HANDLER_CONNECTION_FAILED = 2;
    private static final int HANDLER_POST_MESSAGE = 3;
    private static final int HANDLER_CONNECTION_CLOSED = 4;

    private TextView outputTv;
    private Button connectButton;
    private Button disconnectButton;
    private Button sendMessageButton;
    private EditText messageEditText;
    private BluetoothAdapter bluetoothAdapter;
    private Handler writeOnOutupHandler;
    private boolean connectionRunning = false;

    private ConnectThread connectThread;

    String output = "*** IN ATTESA DI CONNESSIONE ***";

    @Override
    protected void onResume() {
        super.onResume();
        writeOnOutupHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what){
                    case HANDLER_CONNECTION_OK:
                        connectButton.setEnabled(true);
                        connectButton.setVisibility(View.INVISIBLE);
                        disconnectButton.setVisibility(View.VISIBLE);
                        sendMessageButton.setVisibility(View.VISIBLE);
                        messageEditText.setVisibility(View.VISIBLE);
                        if (msg.obj != null) {
                            output += msg.obj.toString();
                            outputTv.setText(output);
                        }
                        break;
                    case HANDLER_CONNECTION_FAILED:
                        connectButton.setEnabled(true);
                        connectionRunning = false;
                        Toast.makeText(getApplicationContext(),"Impossibile connettersi",Toast.LENGTH_LONG).show();
                        if (msg.obj != null) {
                            output += msg.obj.toString();
                            outputTv.setText(output);
                        }
                        disconnectButton.setVisibility(View.INVISIBLE);
                        sendMessageButton.setVisibility(View.INVISIBLE);
                        messageEditText.setVisibility(View.INVISIBLE);
                        connectButton.setVisibility(View.VISIBLE);
                        break;
                    case HANDLER_CONNECTION_CLOSED:
                        connectButton.setEnabled(true);
                        connectionRunning = false;
                        Toast.makeText(getApplicationContext(),"Connessione terminata",Toast.LENGTH_LONG).show();
                        if (msg.obj != null) {
                            output += msg.obj.toString();
                            outputTv.setText(output);
                        }
                        disconnectButton.setVisibility(View.INVISIBLE);
                        sendMessageButton.setVisibility(View.INVISIBLE);
                        messageEditText.setVisibility(View.INVISIBLE);
                        connectButton.setVisibility(View.VISIBLE);
                        break;
                    case HANDLER_POST_MESSAGE:
                        if (msg.obj != null) {
                            output += msg.obj.toString();
                            outputTv.setText(output);
                        }
                        break;
                }
            }
        };
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_paired_device);
        PairedDevice device = getIntent().getParcelableExtra("device");
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        outputTv = findViewById(R.id.activity_paired_device_output_textview);
        connectButton = findViewById(R.id.activity_paired_device_connect_button);
        disconnectButton = findViewById(R.id.activity_paired_device_disconnect_button);
        sendMessageButton = findViewById(R.id.activity_paired_device_message_send_button);
        messageEditText = findViewById(R.id.activity_paired_device_message_edit_text);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        setTitle(device.getName());
        outputTv.setText(output);
        connectButton.setOnClickListener(view -> {
            String BTAddress = device.getMacAddress();
            UUID SERIAL_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
            BluetoothDevice btDevice = bluetoothAdapter.getRemoteDevice(BTAddress);
            connectThread = new ConnectThread(btDevice, SERIAL_UUID);
            connectThread.start();
            connectButton.setEnabled(false);
        });
        disconnectButton.setOnClickListener(view -> {
            Message m = new Message();
            m.what = HANDLER_CONNECTION_CLOSED;
            m.obj = "Dispositivo disconnesso";
            writeOnOutupHandler.sendMessage(m);
        });
        sendMessageButton.setOnClickListener(view -> {
            String msg = messageEditText.getText().toString();
            connectThread.write(msg.getBytes());
        });

    }

    @Override
    public void onBackPressed() {
        if (connectThread != null){
            connectThread.cancel();
        }
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (connectThread != null){
                    connectThread.cancel();
                }
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void manageMyConnectedSocket(BluetoothSocket socket){
        Runnable readSocket = () -> {
            InputStream socketInputStream = null;
            try {
                socketInputStream = socket.getInputStream();
                byte[] buffer = new byte[256];
                int bytes;
                while (connectionRunning) {
                    try {
                        bytes = socketInputStream.read(buffer);
                        String readMessage = new String(buffer, 0, bytes);
                        Log.i("logging", readMessage + "");
                        Message m = new Message();
                        m.what = HANDLER_POST_MESSAGE;
                        m.obj = readMessage;
                        writeOnOutupHandler.sendMessage(m);
                    } catch (IOException e) {
                        break;
                    }
                }
                Log.i("logging","socket closed!");
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        new Thread(readSocket).start();
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private UUID myUUID;

        public ConnectThread(BluetoothDevice device, UUID myUUID) {
            BluetoothSocket tmp = null;
            mmDevice = device;
            this.myUUID = myUUID;
            try {
                tmp = device.createRfcommSocketToServiceRecord(myUUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            bluetoothAdapter.cancelDiscovery();
            try {
                mmSocket.connect();
            } catch (IOException connectException) {
                Message m = new Message();
                m.what = HANDLER_CONNECTION_FAILED;
                writeOnOutupHandler.sendMessage(m);
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }
            Log.i(TAG, "Connected !");
            Message m = new Message();
            m.what = HANDLER_CONNECTION_OK;
            m.obj = System.lineSeparator() + "Dispositivo connesso: "+ mmDevice.getAddress() + System.lineSeparator();
            writeOnOutupHandler.sendMessage(m);
            connectionRunning = true;
            manageMyConnectedSocket(mmSocket);
            try {
                mmSocket.getOutputStream().write(5);
            } catch (IOException e){

            }

        }

        public void write(byte[] bytes) {
            try {
                OutputStream mmOutStream = mmSocket.getOutputStream();
                String text = new String(bytes, Charset.defaultCharset());
                Log.d(TAG, "write: Writing to outputstream: " + text);
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e(TAG, "Error occurred when sending data", e);
            }
        }


        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }
}
