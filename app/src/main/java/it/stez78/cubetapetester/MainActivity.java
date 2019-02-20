package it.stez78.cubetapetester;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.stez78.cubetapetester.adapters.OnItemClickListener;
import it.stez78.cubetapetester.adapters.PairedDeviceAdapter;
import it.stez78.cubetapetester.models.PairedDevice;

import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements OnItemClickListener {

    private final static int REQUEST_ENABLE_BT = 1;

    FloatingActionButton fab;
    RecyclerView recyclerView;
    TextView title;

    private RecyclerView.LayoutManager layoutManager;
    private PairedDeviceAdapter pairedDeviceAdapter;

    List<PairedDevice> pairedDeviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        title = findViewById(R.id.text);
        recyclerView = findViewById(R.id.activity_main_paired_devices_rw);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        fab = findViewById(R.id.fab);
        fab.setOnClickListener(view ->
                Snackbar.make(view, "Verifica che il CubeTape sia acceso!", Snackbar.LENGTH_LONG).setAction("Action", null).show()
        );

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress();
                    PairedDevice pd = new PairedDevice();
                    pd.setName(deviceName);
                    pd.setMacAddress(deviceHardwareAddress);
                    pairedDeviceList.add(pd);
                }
                String devicesFound = getResources().getQuantityString(R.plurals.paired_devices, pairedDevices.size(), pairedDevices.size());
                Toast.makeText(this, devicesFound, Toast.LENGTH_SHORT).show();
                pairedDeviceAdapter = new PairedDeviceAdapter(this, pairedDeviceList, this);
                recyclerView.setAdapter(pairedDeviceAdapter);
                pairedDeviceAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(Object item) {
        PairedDevice element= (PairedDevice) item;
        Intent launchPairedDeviceActivityIntent = new Intent(this, PairedDeviceActivity.class);
        launchPairedDeviceActivityIntent.putExtra("device",element);
        startActivity(launchPairedDeviceActivityIntent);
    }
}
