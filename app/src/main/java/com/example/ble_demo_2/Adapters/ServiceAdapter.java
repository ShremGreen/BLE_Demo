package com.example.ble_demo_2.Adapters;

import android.bluetooth.BluetoothGattService;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ble_demo_2.BLETools.BLEOperator;
import com.example.ble_demo_2.BLEUtils;
import com.example.ble_demo_2.R;

import java.util.List;

public class ServiceAdapter extends RecyclerView.Adapter<ServiceAdapter.ViewHolder> {

    private List<BluetoothGattService> services;
    private final BLEOperator bleOperate;
    public ServiceAdapter(List<BluetoothGattService> services, BLEOperator bleOperate) {
        this.services = services;
        this.bleOperate = bleOperate;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View itemView = layoutInflater.inflate(R.layout.service_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.tvServiceName.setText(BLEUtils.getServiceName(services.get(position).getUuid()));
        holder.tvServiceUuid.setText(BLEUtils.getServiceUUID(services.get(position).getUuid()));
    }

    @Override
    public int getItemCount() {
        return services.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceName;
        TextView tvServiceUuid;
        public ViewHolder(View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tv_service_name);
            tvServiceUuid = itemView.findViewById(R.id.tv_service_uuid);
        }
    }
}