package br.edu.ifsp.scl.sensorsports;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.hardware.Sensor.TYPE_ACCELEROMETER;
import static android.hardware.Sensor.TYPE_GYROSCOPE;
import static androidx.core.content.FileProvider.getUriForFile;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    final short POLL_FREQUENCY = 200; //in milliseconds
    private long lastUpdate = -1;
    private Boolean started = false;
    String[] appPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private static final int PERMISSION_REQUEST_CODE = 1240;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor gyroscope;

    OutputStreamWriter dlog;
    File file;
    FileOutputStream log;

    TextView accX;
    TextView accY;
    TextView accZ;
    TextView gyroX;
    TextView gyroY;
    TextView gyroZ;
    Button btnStart;
    Button btnStop;
    Button btnShare;

    float[] accelerometerMatrix = new float[3];
    float[] gyroscopeMatrix = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (checkPermissions()) {
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
            btnStart = findViewById(R.id.btnStart);
            btnStop = findViewById(R.id.btnStop);
            btnShare = findViewById(R.id.btnShare);
            btnStart.setOnClickListener(this);
            btnStop.setOnClickListener(this);
            btnShare.setOnClickListener(this);
        } else {
            System.exit(0);
        }
    }

    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, appPermissions[0]) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, appPermissions[1]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, appPermissions, PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    public void onClick(View v) {
        if (v == btnStart && !started) {
            started = true;
            eraseFile();
            startSensors();
        } else if (v == btnStop) {
            started = false;
            stopSensors();
        } else if (v == btnShare) {
            stopSensors();
            shareData();
        }
    }

    private void startSensors() {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void stopSensors() {
        sensorManager.unregisterListener(this);
        accX.setText("");
        accY.setText("");
        accZ.setText("");
        gyroX.setText("");
        gyroY.setText("");
        gyroZ.setText("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (started)
            startSensors();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopSensors();
    }

    private void shareData() {
        File file = new File(getApplicationContext().getFilesDir() + "/DadosSensor.txt");
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/*");
        Uri contentUri = getUriForFile(this, "br.edu.ifsp.scl.sensorsports.fileprovider", file);
        sharingIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
        if (contentUri != null) {
            // Grant temporary read permission to the content URI
            sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        startActivity(Intent.createChooser(sharingIntent, "Share Sensor Data With:"));
    }

    public void eraseFile() {
        try {
            File file = new File(getApplicationContext().getFilesDir() + "/DadosSensor.txt");
            if (file.exists())
                file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeData() {
        try {
            file = getApplicationContext().getFilesDir();
            Log.d("Sensors", "File: " + file.toString());
            log = new FileOutputStream(file.toString() + "/DadosSensor.txt", true);
            Log.d("Sensors", "nome:" + file.getPath());
            dlog = new OutputStreamWriter(log);
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss:SSS", Locale.getDefault());
            String s = "\n" + accelerometerMatrix[0] + ";" + accelerometerMatrix[1] + ";" + accelerometerMatrix[2] + ";";
            s = s + gyroscopeMatrix[0] + ";" + gyroscopeMatrix[1] + ";" + gyroscopeMatrix[2] + ";";

            s = s + df.format(new Date()) + ";";
            dlog.append(s);
            dlog.flush();
        } catch (FileNotFoundException e) {
            Log.d("Sensors", "Arquivo nao encontrado");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("Sensors", "Erro ao escrever arquivo");
            e.printStackTrace();
        }

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
            accX.setText(String.format(Locale.getDefault(), "%.2f", accelerometerMatrix[0]));
            accY.setText(String.format(Locale.getDefault(), "%.2f", accelerometerMatrix[1]));
            accZ.setText(String.format(Locale.getDefault(), "%.2f", accelerometerMatrix[2]));
            gyroX.setText(String.format(Locale.getDefault(), "%.2f", gyroscopeMatrix[0]));
            gyroY.setText(String.format(Locale.getDefault(), "%.2f", gyroscopeMatrix[1]));
            gyroZ.setText(String.format(Locale.getDefault(), "%.2f", gyroscopeMatrix[2]));
            writeData();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
