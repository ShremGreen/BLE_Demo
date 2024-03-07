package com.example.ble_demo_2.BLETools;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.ble_demo_2.BLEUtils;
import com.example.ble_demo_2.MainActivity;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@SuppressLint("MissingPermission")
public class BLEGattCallBack extends BluetoothGattCallback {

    public interface BluetoothDataListener {
        void onDataReceived(String data);
    }
    private final String TAG = BLEGattCallBack.class.getSimpleName();
    private BLEOperator bleOperator;
    private BluetoothDataListener dataListener;
    public BLEGattCallBack(BLEOperator bleOperator) {
        this.bleOperator = bleOperator;
    }

    public void setDataListener(BluetoothDataListener dataListener) {
        this.dataListener = dataListener;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        switch(newState) {
            case BluetoothProfile.STATE_CONNECTED:
                BLETool.setIsConnected(true);
                Log.d(TAG, "连接成功");
                BLETool.showMsg(bleOperator.getContext(), "连接成功");
                //关闭lay_connecting_loading等布局
                bleOperator.turnLayout("lay_connecting_loading", View.GONE);
                bleOperator.turnLayout("rv_device", View.GONE);
                //修改mtu大小,触发onMtuChanged()回调
                gatt.requestMtu(512);
                //跳转页面
                bleOperator.jumpToAnotherActivity(MainActivity.class);
                break;
            case BluetoothProfile.STATE_DISCONNECTED:
                BLETool.setIsConnected(false);
                Log.d(TAG, "断开连接");
                //BLETool.showMsg(MainActivity.context, "断开连接");
                //设置lay_connecting_loading等布局
                bleOperator.turnLayout("lay_connecting_loading", View.GONE);
                bleOperator.turnLayout("rv_device", View.VISIBLE);
                break;
            default:
                break;
        }
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        Log.d(TAG, "onMtuChanged, MTU=" + mtu);
        //开启发现服务，触发onServicesDiscovered()回调
        gatt.discoverServices();
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Log.d(TAG, "onServicesDiscovered");
        if(status == BluetoothGatt.GATT_SUCCESS) {
            Log.d(TAG, "onServicesDiscovered");
            //获取UART服务
            BluetoothGattService uartService = gatt.getService(UUID.fromString(BLEUtils.UART_SERVICE_UUID));
            //为避免线程抢占资源，需要加一点延迟，保证先gatt成功获取到service
            //测试蓝牙接收数据
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    //蓝牙接收数据
                    String message = "HelloWorld";
                    byte[] messageBytes = message.getBytes();
                    sendViaRxCharacteristic(gatt, messageBytes);
                }
            }, 100);

            //开启TX通知
            if(uartService != null) {
                BluetoothGattCharacteristic txCharacteristic = uartService.getCharacteristic(UUID.fromString(BLEUtils.UART_TX_CHARACTERISTIC_UUID));
                if(txCharacteristic != null) {
                    //开启TX通知用于发送数据，成功后进入onDescriptorWrite()回调
                    gatt.setCharacteristicNotification(txCharacteristic, true);
                    BluetoothGattDescriptor descriptor = txCharacteristic.getDescriptor(UUID.fromString(BLEUtils.DESCRIPTOR_UUID));
                    if(descriptor != null) {
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        //回调onDescriptorWrite()
                        gatt.writeDescriptor(descriptor);
                        //此时TX特质服务已开启，一旦特征值变化则回调onCharacteristicChanged()
                    }
                }
            }
        }
    }

    // 通过RX特征接收数据
    public void sendViaRxCharacteristic(BluetoothGatt gatt, byte[] data) {
        Log.d(TAG, "sendViaRxCharacteristic");
        if (gatt == null) return;
        BluetoothGattService uartService = gatt.getService(UUID.fromString(BLEUtils.UART_SERVICE_UUID));
        if (uartService != null) {
            BluetoothGattCharacteristic rxCharacteristic = uartService.getCharacteristic(UUID.fromString(BLEUtils.UART_RX_CHARACTERISTIC_UUID));
            if (rxCharacteristic != null) {
                rxCharacteristic.setValue(data);
                //回调onCharacteristicWrite()
                gatt.writeCharacteristic(rxCharacteristic);
            }
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.d(TAG, "onCharacteristicWrite");
        byte[] txNotifyValue = characteristic.getValue();
        String msg = new String(txNotifyValue, StandardCharsets.UTF_8);
        Log.d(TAG, "onCharacteristicWrite -> 收到数据：" + msg);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        if(BLEUtils.DESCRIPTOR_UUID.equals(descriptor.getUuid().toString().toLowerCase())) {
            if(status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onDescriptorWrite: 通知开启成功");
            } else {
                Log.d(TAG, "onDescriptorWrite: 通知开启失败");
            }
        }
    }

    //实际在用的是这个
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        if (BLEUtils.UART_TX_CHARACTERISTIC_UUID.equals(characteristic.getUuid().toString())) {
            byte[] data = characteristic.getValue();
            // 处理接收到的数据
            String receivedData = new String(data, StandardCharsets.UTF_8);
            Log.d(TAG, "onCharacteristicChanged接收到数据: " + receivedData);
            //发送到MainActivity中
            if(dataListener != null) {
                dataListener.onDataReceived(receivedData);
            }
        }
    }

    //不是这个
    @Override
    public void onCharacteristicChanged(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattCharacteristic characteristic, @NonNull byte[] value) {
        Log.d(TAG, "onCharacteristicChanged: 2");
        if (BLEUtils.UART_TX_CHARACTERISTIC_UUID.equals(characteristic.getUuid().toString())) {
            String receivedData = new String(value, StandardCharsets.UTF_8);
            Log.d(TAG, "接收到的数据: " + receivedData);
        }
    }
}
