package com.example.ble_demo_2.BLETools;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.example.ble_demo_2.BLEDevice;

public interface BLEOperator {
    /***
     * 连接蓝牙
     * @param bleDevice 蓝牙设备
     */
    void connectBle(BLEDevice bleDevice);

    /***
     * 操作布局（打开或关闭）
     * @param layoutName 布局名称，对应xml中名称
     * @param visibility 是否可见
     */
    void turnLayout(String layoutName, int visibility);

    /***
     * 跳转页面
     * @param clazz 需要跳转到的页面class
     */
    void jumpToAnotherActivity(Class clazz);

    /***
     * 获取当前类的context对象
     * @return
     */
    Context getContext();
}
