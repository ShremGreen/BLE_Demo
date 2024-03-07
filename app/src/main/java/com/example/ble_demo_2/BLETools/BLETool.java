package com.example.ble_demo_2.BLETools;

import static android.content.Context.BLUETOOTH_SERVICE;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;

import com.example.ble_demo_2.Adapters.BLEAdapter;
import com.example.ble_demo_2.BLEDevice;
import com.example.ble_demo_2.MainActivity;
import com.example.ble_demo_2.databinding.ActivityPermissionBinding;

import java.util.ArrayList;
import java.util.List;

public class BLETool {
    public static BluetoothAdapter bluetoothAdapter;
    public static BLEAdapter bleAdapter;
    public static BluetoothLeScanner scanner;
    public static ActivityResultLauncher<Intent> openBluetooth;
    public static ActivityResultLauncher<String> requestBluetoothConnect;
    public static ActivityResultLauncher<String> requestBluetoothScan;
    public static final List<BLEDevice> deviceList = new ArrayList<>();
    public static BLEGattCallBack bleGattCallBack;
    public static BluetoothGatt bluetoothGatt;
    public static boolean isConnected = false;
    public static boolean isScanning = false;
    public static Handler mainHandler = new Handler(Looper.getMainLooper());



    public static void setIsConnected(boolean isConnected) {
        BLETool.isConnected = isConnected;
    }

    public static void showMsg(final Context context, final String msg) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static boolean isAndroid12() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
    }

    public static boolean isBluetoothOpen(Context context) {
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = manager != null ? manager.getAdapter() : null;
        return adapter != null && adapter.isEnabled();
    }

    public static boolean hasPermission(Context context, String str) {
        return ContextCompat.checkSelfPermission(context, str) == PackageManager.PERMISSION_GRANTED;
    }
}
