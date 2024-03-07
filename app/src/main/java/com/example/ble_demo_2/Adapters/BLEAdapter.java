package com.example.ble_demo_2.Adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ble_demo_2.BLEDevice;
import com.example.ble_demo_2.BLETools.BLEOperator;
import com.example.ble_demo_2.databinding.DeviceItemBinding;

import java.util.List;

@SuppressLint("MissingPermission")
public class BLEAdapter extends RecyclerView.Adapter<BLEAdapter.ViewHolder> {
    private final List<BLEDevice> list;
    private final BLEOperator bleOperate;

    public BLEAdapter(List<BLEDevice> list, BLEOperator bleOperate) {
        this.list = list;
        this.bleOperate = bleOperate;
    }

    //为每个新的列表项创建一个新的ViewHolder
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        DeviceItemBinding binding = DeviceItemBinding.inflate(LayoutInflater.from(parent.getContext()),parent, false);
        return new ViewHolder(binding);
    }

    //为每行数据绑定ViewHolder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //获取到创建的ViewHolder根视图
        DeviceItemBinding binding = DataBindingUtil.getBinding(holder.binding.getRoot());
        if (binding != null) {
            BLEDevice bleDevice = list.get(position);
            //将指定位置的device对象设置到绑定类中（未执行绑定）
            binding.setDevice(bleDevice);
            //立即执行绑定，确保数据更改后立即更新UI
            binding.executePendingBindings();
            //设置列表项点击事件
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    bleOperate.connectBle(bleDevice);
                }
            });
        }
    }

    //获取数据集大小
    @Override
    public int getItemCount() {
        return list.size();
    }

    //自定义ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public DeviceItemBinding binding;
        public ViewHolder(@NonNull DeviceItemBinding itemTextDataRvBinding) {
            super(itemTextDataRvBinding.getRoot());
            binding = itemTextDataRvBinding;
        }
    }
}
