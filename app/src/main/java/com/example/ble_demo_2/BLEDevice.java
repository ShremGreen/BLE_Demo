package com.example.ble_demo_2;

import android.bluetooth.BluetoothDevice;

public class BLEDevice {
    private BluetoothDevice bleDevice;
    private int rssi;

    public BLEDevice(BluetoothDevice bleDevice, int rssi) {
        this.bleDevice = bleDevice;
        this.rssi = rssi;
    }

    public BluetoothDevice getBleDevice() {
        return bleDevice;
    }

    public void setBleDevice(BluetoothDevice bleDevice) {
        this.bleDevice = bleDevice;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
