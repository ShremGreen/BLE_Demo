package com.example.ble_demo_2;

import static com.example.ble_demo_2.BLETools.BLETool.deviceList;
import static com.example.ble_demo_2.BLETools.BLETool.hasPermission;
import static com.example.ble_demo_2.BLETools.BLETool.isBluetoothOpen;
import static com.example.ble_demo_2.BLETools.BLETool.isConnected;
import static com.example.ble_demo_2.BLETools.BLETool.isScanning;
import static com.example.ble_demo_2.BLETools.BLETool.mainHandler;
import static com.example.ble_demo_2.BLETools.BLETool.openBluetooth;
import static com.example.ble_demo_2.BLETools.BLETool.requestBluetoothConnect;
import static com.example.ble_demo_2.BLETools.BLETool.requestBluetoothScan;
import static com.example.ble_demo_2.BLETools.BLETool.bluetoothAdapter;
import static com.example.ble_demo_2.BLETools.BLETool.scanner;
import static com.example.ble_demo_2.BLETools.BLETool.bleAdapter;
import static com.example.ble_demo_2.BLETools.BLETool.bleGattCallBack;
import static com.example.ble_demo_2.BLETools.BLETool.bluetoothGatt;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.ble_demo_2.Adapters.BLEAdapter;
import com.example.ble_demo_2.BLETools.BLEGattCallBack;
import com.example.ble_demo_2.BLETools.BLEOperator;
import com.example.ble_demo_2.BLETools.BLETool;
import com.example.ble_demo_2.databinding.ActivityPermissionBinding;

import java.util.List;

@SuppressLint("MissingPermission")
public class PermissionActivity extends AppCompatActivity implements BLEOperator {
    private Context context;
    private Intent intent;
    private ActivityPermissionBinding binding;
    private final String TAG = PermissionActivity.class.getSimpleName();
    private final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.d(TAG, "onScanResult: ");
            BluetoothDevice device = result.getDevice();
            if(device == null || device.getName() == null || device.getAddress() == null) {
                return;
            } else {
                Log.d(TAG, "name: " + device.getName() + ", address: " + device.getAddress());
                addDevice(new BLEDevice(device, result.getRssi()));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化视图
        binding = ActivityPermissionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //初始化context
        context = getApplicationContext();
        //初始化intent
        intent = new Intent(context, MainActivity.class);
        //初始化，打开权限意向
        openPermissionIntent();
        //初始化，按钮点击
        btnClick();
    }

    //权限意向，
    private void openPermissionIntent() {
        //打开蓝牙权限意向
        requestBluetoothConnect = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            Log.d(TAG, "打开蓝牙权限");
            if (result) {
                //转换UI
                turnLayout("need_permission", View.GONE);
                turnLayout("need_open", View.VISIBLE);
            } else {
                BLETool.showMsg(context,"Android未获取蓝牙权限");
            }
        });
        //打开蓝牙意向
        openBluetooth = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode() == RESULT_OK) {
                    if(BLETool.isBluetoothOpen(context)) {
                        BLETool.showMsg(context,"蓝牙已打开");
                        //转换UI
                        turnLayout("need_open", View.GONE);
                        turnLayout("need_scan", View.VISIBLE);
                    } else {
                        BLETool.showMsg(context,"蓝牙未打开");
                    }
                }
            }
        });
        //申请蓝牙搜索权限
        requestBluetoothScan = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
            if(result) {
                BLETool.showMsg(context,"已获取蓝牙扫描权限");
                BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
                bluetoothAdapter = manager.getAdapter();
                scanner = bluetoothAdapter.getBluetoothLeScanner();
                turnLayout("need_scan", View.GONE);
                turnLayout("rv_device", View.VISIBLE);
                startScan();
            } else {
                BLETool.showMsg(context,"Android未获取蓝牙扫描权限");
            }
        });
    }

    //定义按钮点击事件
    private void btnClick() {
        //根据系统现状跳转初始UI
        if(hasPermission(context, Manifest.permission.BLUETOOTH_CONNECT) && !isBluetoothOpen(context)) {
            turnLayout("need_open", View.VISIBLE);
        } else if(!hasPermission(context, Manifest.permission.BLUETOOTH_CONNECT)) {
            turnLayout("need_permission", View.VISIBLE);
        } else {
            turnLayout("need_scan", View.VISIBLE);
        }
        //点击监听事件，获取蓝牙权限
        binding.needPermission.btPermissionBtn.setOnClickListener(view -> {
            Log.d(TAG, "btnClick: btPermissionBtn");
            if(BLETool.isAndroid12()) {
                if(!BLETool.hasPermission(context, Manifest.permission.BLUETOOTH_CONNECT)) {
                    requestBluetoothConnect.launch(Manifest.permission.BLUETOOTH_CONNECT);
                }
            }
        });
        //点击监听事件，打开系统蓝牙
        binding.needOpen.btOpenBtn.setOnClickListener(view -> {
            if(BLETool.isBluetoothOpen(context)) {
                BLETool.showMsg(context,"蓝牙已开启");
                turnLayout("need_open", View.GONE);
                turnLayout("need_scan", View.VISIBLE);
                return;
            }
            //安卓12蓝牙需要先开启蓝牙权限，再打开蓝牙。非安卓12则直接打开蓝牙
            if(BLETool.isAndroid12()) {
                if(BLETool.hasPermission(context, Manifest.permission.BLUETOOTH_CONNECT)) {
                    openBluetooth.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
                } else {
                    requestBluetoothConnect.launch(Manifest.permission.BLUETOOTH_CONNECT);
                }
            } else {
                openBluetooth.launch(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            }
        });
        //点击监听事件，扫描蓝牙设备
        binding.needScan.btScanBtn.setOnClickListener(view -> {
            if(BLETool.isAndroid12()) {
                if(!BLETool.hasPermission(context, Manifest.permission.BLUETOOTH_CONNECT)) {
                    requestBluetoothConnect.launch(Manifest.permission.BLUETOOTH_CONNECT);
                    return;
                }
                //是否有蓝牙扫描权限，没有的话先开启权限，转换UI，有的话获取scanner对象并开始扫描
                if(!BLETool.hasPermission(context, Manifest.permission.BLUETOOTH_SCAN)) {
                    requestBluetoothScan.launch(Manifest.permission.BLUETOOTH_SCAN);
                } else {
                    BluetoothManager manager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
                    bluetoothAdapter = manager.getAdapter();
                    scanner = bluetoothAdapter.getBluetoothLeScanner();
                    turnLayout("need_scan", View.GONE);
                    turnLayout("rv_device", View.VISIBLE);
                    startScan();
                }
            }
        });
        //初始化适配器和列表
        bleAdapter = new BLEAdapter(deviceList, this);
        binding.rvDevice.setLayoutManager(new LinearLayoutManager(this));
        binding.rvDevice.setAdapter(bleAdapter);
    }

    //开始扫描
    private void startScan() {
        Log.d(TAG, "startScan: ");
        if (!isScanning) {
            scanner.startScan(scanCallback);
            isScanning = true;
            binding.needScan.btScanBtn.setText("停止扫描");
        }
    }

    //停止扫描
    private void stopScan() {
        Log.d(TAG, "stopScan: ");
        if (isScanning) {
            scanner.stopScan(scanCallback);
            isScanning = false;
            binding.needScan.btScanBtn.setText("扫描蓝牙");
        }
    }

    //获取设备索引
    private int getDeviceIndex(BLEDevice bleDevice, List<BLEDevice> deviceList) {
        Log.d(TAG, "getDeviceIndex: ");
        int index = 0;
        for(BLEDevice tempDevice : deviceList) {
            if(tempDevice.getBleDevice().getAddress().equals(bleDevice.getBleDevice().getAddress())) {
                return index;
            }
            index ++;
        }
        return -1;
    }

    //添加设备
    public void addDevice(BLEDevice bleDevice) {
        Log.d(TAG, "addDevice: ");
        int index = getDeviceIndex(bleDevice, deviceList);
        //如果设备是新的，则添加设备
        if(index == -1) {
            deviceList.add(bleDevice);
            bleAdapter.notifyDataSetChanged();
            //如果再次查找到设备，则更新数据（信号）
        } else {
            deviceList.get(index).setRssi(bleDevice.getRssi());
            bleAdapter.notifyItemChanged(index);
        }
    }

    //重写连接方法
    @Override
    public void connectBle(BLEDevice bleDevice) {
        Log.d(TAG, "connectBle: " + bleDevice.getBleDevice().getName());
        //显示加载视图
        binding.layConnectingLoading.setVisibility(View.VISIBLE);
        //获取蓝牙设备
        BluetoothDevice device = bleDevice.getBleDevice();
        //停止扫描
        stopScan();
        //初始化BLEGattCallBack
        bleGattCallBack = new BLEGattCallBack(this);
        //连接蓝牙，回调BLEGattCallBack.onConnectionStateChange()
        bluetoothGatt = device.connectGatt(this, false, bleGattCallBack);
    }

    //断开连接
    private void disconnectBle() {
        if(bluetoothGatt != null && isConnected) {
            bluetoothGatt.disconnect();
        }
    }

    //重写操作布局方法
    @Override
    public void turnLayout(String layoutName, int visibility) {
        Log.d(TAG, "turnLayout: " + layoutName + visibility);
        runOnUiThread(() -> {
            switch (layoutName) {
                case "need_permission":
                    binding.needPermission.getRoot().setVisibility(visibility);
                    break;
                case "need_open":
                    binding.needOpen.getRoot().setVisibility(visibility);
                    break;
                case "need_scan":
                    binding.needScan.getRoot().setVisibility(visibility);
                    break;
                case "rv_device":
                    binding.rvDevice.setVisibility(visibility);
                    break;
                case "lay_connecting_loading":
                    binding.layConnectingLoading.setVisibility(visibility);
                    break;
                default:
                    break;
            }
        });
    }

    //重写跳转页面方法
    @Override
    public void jumpToAnotherActivity(Class clazz) {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.postDelayed(() -> {
            Intent intent = new Intent(this, clazz);
            startActivity(intent);
        }, 1000);
    }

    @Override
    public Context getContext() {
        return context;
    }

}