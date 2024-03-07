package com.example.ble_demo_2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class BLEUtils {

    //设备核心服务UUID，UART
    public static final String UART_SERVICE_UUID = "6e400001-b5a3-f393-e0a9-e50e24dcca9e";
    //RX，接收数据
    public static final String UART_RX_CHARACTERISTIC_UUID = "6e400002-b5a3-f393-e0a9-e50e24dcca9e";
    //TX，发送数据
    public static final String UART_TX_CHARACTERISTIC_UUID = "6e400003-b5a3-f393-e0a9-e50e24dcca9e";
    //TX的descriptor
    public static final String DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";

    //蓝牙通用uuid
    private static String generic = "-0000-1000-8000-00805F9B34FB";

    //Map用于存放蓝牙常见服务
    private static final Map<String, String> serviceNames = new HashMap<>();

    private static final Map<Integer, String> propertiesMap = new HashMap<>();

    public static String getServiceName(UUID uuid) {
        String serviceIdentifier = "0x" + uuid.toString().substring(4, 8).toUpperCase(Locale.getDefault());
        return serviceNames.getOrDefault(serviceIdentifier, "Unknown Service");
    }

    public static String getServiceUUID(UUID uuid) {
        return "0x" + uuid.toString().substring(4, 8).toUpperCase(Locale.getDefault());
    }

    public static List<String> getProperties(int property) {
        List<String> properties = new ArrayList<>();
        for (int i = 0; i <= 7; i++) {
            int key = property & (1 << i);
            if (propertiesMap.containsKey(key)) {
                properties.add(properties.get(key));
            }
        }
        return properties;
    }
}
