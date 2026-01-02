package com.example.alarmclock;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TimePicker timePicker;
    private Button btnSetAlarm;
    private Button btnCancelAlarm;
    private Spinner spinnerCities;
    private TextView tvCurrentTime;
    private TextView tvWeatherInfo;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private RequestQueue requestQueue;
    private Map<String, String> cityTimeZones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timePicker = findViewById(R.id.timePicker);
        btnSetAlarm = findViewById(R.id.btnSetAlarm);
        btnCancelAlarm = findViewById(R.id.btnCancelAlarm);
        spinnerCities = findViewById(R.id.spinnerCities);
        tvCurrentTime = findViewById(R.id.tvCurrentTime);
        tvWeatherInfo = findViewById(R.id.tvWeatherInfo);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        requestQueue = Volley.newRequestQueue(this);

        cityTimeZones = new HashMap<>();
        cityTimeZones.put("New York", "America/New_York");
        cityTimeZones.put("London", "Europe/London");
        cityTimeZones.put("Tokyo", "Asia/Tokyo");
        cityTimeZones.put("Sydney", "Australia/Sydney");
        cityTimeZones.put("Hà Nội", "Asia/Ho_Chi_Minh");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(cityTimeZones.keySet()));
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCities.setAdapter(adapter);

        spinnerCities.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCity = parent.getItemAtPosition(position).toString();
                String timeZone = cityTimeZones.get(selectedCity);
                fetchCurrentTime(timeZone);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnSetAlarm.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, timePicker.getHour());
            calendar.set(Calendar.MINUTE, timePicker.getMinute());
            calendar.set(Calendar.SECOND, 0);

            Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
            pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

            Toast.makeText(MainActivity.this, "Báo thức đã được đặt!", Toast.LENGTH_SHORT).show();
        });

        btnCancelAlarm.setOnClickListener(v -> {
            stopAlarm();
            Intent intent = new Intent(MainActivity.this, AlarmReceiver.class);
            intent.setAction("STOP_ALARM");
            sendBroadcast(intent);
            Toast.makeText(MainActivity.this, "Báo thức đã được hủy!", Toast.LENGTH_SHORT).show();
        });
    }

    private void fetchCurrentTime(String timeZone) {
        String url = "http://worldtimeapi.org/api/timezone/" + timeZone;
        Log.d("fetchCurrentTime", "URL: " + url);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, response -> {
            try {
                Log.d("fetchCurrentTime", "Response: " + response);
                JSONObject jsonObject = new JSONObject(response);
                String dateTime = jsonObject.getString("datetime");
                tvCurrentTime.setText("Giờ hiện tại: " + dateTime);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("fetchCurrentTime", "JSON parsing error: " + e.getMessage());
            }
        }, error -> {
            error.printStackTrace();
            Log.e("fetchCurrentTime", "Volley error: " + error.getMessage());
            Toast.makeText(MainActivity.this, "Lỗi khi lấy giờ", Toast.LENGTH_SHORT).show();
        });

        requestQueue.add(stringRequest);
    }

    private void stopAlarm() {
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }
    }
}
