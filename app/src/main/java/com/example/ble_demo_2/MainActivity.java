package com.example.ble_demo_2;

import static com.example.ble_demo_2.BLETools.BLETool.bleGattCallBack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.icu.text.Transliterator;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;

import com.example.ble_demo_2.BLETools.BLEGattCallBack;
import com.example.ble_demo_2.BLETools.BLETool;
import com.example.ble_demo_2.databinding.ActivityMainBinding;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressLint("MissingPermission")
public class MainActivity extends AppCompatActivity implements BLEGattCallBack.BluetoothDataListener {
    private Context context;
    private ActivityMainBinding binding;
    private TextView textView;
    private final String TAG = MainActivity.class.getSimpleName();
    private LineChart lineChart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //初始化视图
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //获取全局context
        context = getApplicationContext();
        //初始化TextView
        textView = binding.showData.receivedData;
        //向bleGattCallBack设置BluetoothDataListener
        bleGattCallBack.setDataListener(this);
        //初始化
        initChartLine();
    }

    @Override
    public void onDataReceived(String data) {
        Log.d(TAG, "onDataReceived: " + "收到数据: " + data);
        //获取当前时间
        String nowTime = formatTime(new Date()) + ": ";
        String fullText = nowTime + data + "\n";
        //设置文本颜色
        SpannableString spannableString = new SpannableString(fullText);
        int nowTimeColor = ContextCompat.getColor(context, R.color.medium_gray);
        spannableString.setSpan(new ForegroundColorSpan(nowTimeColor), 0, nowTime.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        //显示文本
        runOnUiThread(() -> {
            textView.append(spannableString);
            // 解析数据并更新图表
            double number = getNumFromData(data);
            if (!Double.isNaN(number)) {
                addEntry((float) number);
            }
        });

    }

    // 添加数据到图表
    private void addEntry(float value) {
        LineData data = lineChart.getData();
        if (data != null) {
            LineDataSet set = (LineDataSet) data.getDataSetByIndex(0);
            // 创建一个数据集，如果还没有的话
            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }
            // 添加新的数据点
            data.addEntry(new Entry(set.getEntryCount(), value), 0);
            data.notifyDataChanged();
            // 数据更新notify
            lineChart.notifyDataSetChanged();
            lineChart.setVisibleXRangeMaximum(50);
            lineChart.moveViewToX(data.getEntryCount());
        }
    }

    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "环境温度（°C）");
        set.setLineWidth(2.5f);
        set.setColor(Color.BLUE);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        return set;
    }

    //初始化图表
    public void initChartLine() {
        //获取chart图表
        lineChart = binding.showData.chart;
        //初始化图表数据
        LineData data = new LineData();
        lineChart.getAxisRight().setEnabled(false);
        lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        lineChart.setData(data);
        Description description = lineChart.getDescription();
        description.setText("时间（s）");
        description.setTextColor(Color.BLACK);
        description.setTextSize(10);

        //设置图例
        Legend legend = lineChart.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);

    }

    //从字符串中获取浮点数
    public double getNumFromData(String data) {
        // 使用正则表达式匹配数字（包括负号和小数点）
        Pattern pattern = Pattern.compile("-?\\d+\\.\\d+");
        Matcher matcher = pattern.matcher(data);
        // 如果找到匹配项，将其转换为double类型
        if (matcher.find()) {
            return Double.parseDouble(matcher.group());
        } else {
            return Double.NaN;
        }
    }

    //定义时间输出格式
    public String formatTime(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        return formatter.format(date);
    }
}