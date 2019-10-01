package br.edu.ifsp.scl.sensorsports;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private float axiosX = 0, axiosY = 0, axiosZ = 0;

    TextView tX, tY, tZ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);

        //Componentes da Interface
        tX = (TextView) findViewById(R.id.res_axiosX);
        tY = (TextView) findViewById(R.id.res_axiosY);
        tZ = (TextView) findViewById(R.id.res_axiosZ);

        //listSensors();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    private void listSensors() {
        List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for(Sensor s: deviceSensors){
            Log.d("Sensors: ", s.getName());
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        axiosX = event.values[0];
        axiosY = event.values[1];
        axiosZ = event.values[2];

        tX.setText( String.valueOf(axiosX));
        tY.setText( String.valueOf(axiosY));
        tZ.setText( String.valueOf(axiosZ));

//        Log.d("Result", "Eixos: "+axiosX +"(X) - "+axiosY +"(Y) - "+axiosZ +"(Z)");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
