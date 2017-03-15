package com.example.skyv.weather_bd;

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;


import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.skyv.weather_bd.gson.JsonBean;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public LocationClient mLocationClient;
    private TextView weat;
    private TextView temp;
    private TextView city;
    private TextView pos;
    private  TextView type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLocationClient = new LocationClient(getApplicationContext());
        mLocationClient.registerLocationListener(new MyLocationListener());
        setContentView(R.layout.activity_main);

        final Button button = (Button) findViewById(R.id.refresh_button);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Toast.makeText(MainActivity.this, "天气刷新", Toast.LENGTH_SHORT).show();
                        mLocationClient.requestLocation();
                    }
                });
            }
        });

        weat = (TextView)findViewById(R.id.wea_text_view);
        temp = (TextView)findViewById(R.id.tmp_text_view);
        city = (TextView)findViewById(R.id.city_text_view);
        pos = (TextView)findViewById(R.id.pos_text_view);
        type = (TextView)findViewById(R.id.posType_text_view);

        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissionList.isEmpty()) {
            String [] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1);
        } else {

            requestLocation();
        }

    }

    private void requestLocation() {
        initLocation();
        mLocationClient.start();
    }

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setScanSpan(10000);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mLocationClient.setLocOption(option);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.stop();
    }

    public void onRequestPermissionsResult(int requestCode, String[] permission, int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "必须同意所有权限才能使用本程序", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                    requestLocation();
                } else {
                    Toast.makeText(this, "发生未知错误", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
        }
    }

    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation bdLocation) {
            final StringBuilder weatherUrl = new StringBuilder();
            weatherUrl.append("https://api.thinkpage.cn/v3/weather/now.json?key=ywk5iemwmylygjcj&location=")
                .append(bdLocation.getLatitude()).append(":").append(bdLocation.getLongitude())
                .append("&language=zh-Hans&unit=c");
            final StringBuilder Lat_Long = new StringBuilder();
            Lat_Long.append(bdLocation.getLongitude()).append("     ").append(bdLocation.getLatitude());
            final StringBuilder pos_tpye = new StringBuilder();
            if (bdLocation.getLocType() == BDLocation.TypeNetWorkLocation) {
                pos_tpye.append("网络");
            } else {
                pos_tpye.append("GPS");
            }

            OkHttpClient mOkHttpClient = new OkHttpClient();
            final Request request = new Request.Builder()
                    .url(weatherUrl.toString())
                    .build();
            Call call = mOkHttpClient.newCall(request);
            call.enqueue(new Callback()
            {
                @Override
                public void onFailure(Call call, IOException e) {

                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    final String htmlStr =  response.body().string();
                    JsonBean resultBean = new Gson().fromJson(htmlStr, JsonBean.class);
                    final List<JsonBean.Results> weather_zx = resultBean.getResults();
                    runOnUiThread(new Runnable() {
                        public void run() {
                            city.setText("所处城市：" + weather_zx.get(0).location.name);
                            pos.setText("经纬度：" + Lat_Long);
                            type.setText("定位方式：" + pos_tpye);
                            weat.setText("天气情况：" + weather_zx.get(0).now.text);
                            temp.setText("气温：" + weather_zx.get(0).now.temperature + "℃");
                            //Toast.makeText(MainActivity.this, "天气已经刷新",  Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }




}

