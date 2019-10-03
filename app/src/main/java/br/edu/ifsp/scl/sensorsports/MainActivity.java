package br.edu.ifsp.scl.sensorsports;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Locale;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.Sensor.TYPE_GYROSCOPE;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    final short POLL_FREQUENCY = 200; //in milliseconds
    private long lastUpdate = -1;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;

    TextView accX;
    TextView accY;
    TextView accZ;
    TextView gyroX;
    TextView gyroY;
    TextView gyroZ;

    float[] accelerometerMatrix = new float[3];
    float[] gyroscopeMatrix = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(TYPE_ACCELEROMETER);
        gyroscope = sensorManager.getDefaultSensor(TYPE_GYROSCOPE);

        //Componentes da Interface
        accX = findViewById(R.id.valueAccX);
        accY = findViewById(R.id.valueAccY);
        accZ = findViewById(R.id.valueAccZ);
        gyroX = findViewById(R.id.valueGyroX);
        gyroY = findViewById(R.id.valueGyroY);
        gyroZ = findViewById(R.id.valueGyroZ);

        //listSensors();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;

        int i = sensor.getType();
        if (i == TYPE_ACCELEROMETER) {
            accelerometerMatrix = event.values;
        } else if (i == TYPE_GYROSCOPE) {
            gyroscopeMatrix = event.values;
        }

        long curTime = System.currentTimeMillis();
        long diffTime = (curTime - lastUpdate);

        // only allow one update every POLL_FREQUENCY.
        if (diffTime > POLL_FREQUENCY) {
            lastUpdate = curTime;

            accX.setText(String.format(Locale.getDefault(),"%.2f", accelerometerMatrix[0]));
            accY.setText(String.format(Locale.getDefault(),"%.2f", accelerometerMatrix[1]));
            accZ.setText(String.format(Locale.getDefault(),"%.2f", accelerometerMatrix[2]));
            gyroX.setText(String.format(Locale.getDefault(),"%.2f", gyroscopeMatrix[0]));
            gyroY.setText(String.format(Locale.getDefault(),"%.2f", gyroscopeMatrix[1]));
            gyroZ.setText(String.format(Locale.getDefault(),"%.2f", gyroscopeMatrix[2]));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
