package br.edu.ifsp.scl.sensorsports;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener, View.OnClickListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;

    OutputStreamWriter dlog;
    File file;
    FileOutputStream log;

    private float axiosX = 0, axiosY = 0, axiosZ = 0;
    private Boolean started = false;

    TextView tX, tY, tZ;
    Button btnStart, btnStop, btnShare;

    String[] appPermissions = { Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE };
    private static final int PERMISSION_REQUEST_CODE = 1240;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(checkPermissions())
        {
            sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            //sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);

            //Componentes da Interface
            tX = (TextView) findViewById(R.id.res_axiosX);
            tY = (TextView) findViewById(R.id.res_axiosY);
            tZ = (TextView) findViewById(R.id.res_axiosZ);
            btnStart = (Button) findViewById(R.id.btnStart);
            btnStop = (Button) findViewById(R.id.btnStop);
            btnShare = (Button) findViewById(R.id.btnShare);

            btnStart.setOnClickListener(this);
            btnStop.setOnClickListener(this);
            btnShare.setOnClickListener(this);
        }
        else
        {
            System.exit(0);
        }
    }

    private boolean checkPermissions()
    {
        if(ContextCompat.checkSelfPermission(this, appPermissions[0]) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, appPermissions[1]) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, appPermissions , PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    // Implement the OnClickListener callback
    public void onClick(View v) {
        if(v == btnStart)
        {
            started = true;
            eraseFile();
            startSensors();
        }
        else if (v == btnStop)
        {
            started = false;
            stopSensors();
        }
        else if (v == btnShare)
        {
            shareData();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(started)
            startSensors();
    }

    @Override
    protected void onPause() {
        stopSensors();
        super.onPause();
    }

    private void startSensors()
    {
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    private void stopSensors()
    {
        sensorManager.unregisterListener(this);
        tX.setText("");
        tY.setText("");
        tZ.setText("");
    }

    private void shareData()
    {
        File file = new File(Environment.getExternalStorageDirectory() + "/DadosSensor.txt");
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/*");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath()));
        startActivity(Intent.createChooser(sharingIntent, "Share Sensor Data With:"));
    }

    public void eraseFile() {
        try {
            File file = new File(Environment.getExternalStorageDirectory() + "/DadosSensor.txt");
            if (file.exists())
                file.delete();

        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void writeData(float sensorX, float sensorY, float sensorZ){
        try {
            file = Environment.getExternalStorageDirectory();
            Log.d("Sensors", "File: " + file.toString());
            log = new FileOutputStream(file.toString() + "/DadosSensor.txt",true);
            Log.d("Sensors", "nome:" + file.getPath());
            dlog = new OutputStreamWriter(log);
            dlog.append("\n" + sensorX);
            dlog.flush();
            dlog.append(";" + sensorY);
            dlog.flush();
            dlog.append(";" + sensorZ);
            dlog.flush();
        } catch (FileNotFoundException e) {
            Log.d("Sensors", "Arquivo nao encontrado");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d("Sensors", "Erro ao escrever arquivo");
            e.printStackTrace();
        }

    }

    //private void listSensors() {
    //    List<Sensor> deviceSensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
    //    for(Sensor s: deviceSensors){
    //        Log.d("Sensors: ", s.getName());
    //    }
    //}

    @Override
    public void onSensorChanged(SensorEvent event) {
        axiosX = event.values[0];
        axiosY = event.values[1];
        axiosZ = event.values[2];

        tX.setText( String.valueOf(axiosX));
        tY.setText( String.valueOf(axiosY));
        tZ.setText( String.valueOf(axiosZ));
        writeData(axiosX, axiosY, axiosZ);
        Log.d("Sensors: ", "Saiu do escreve");
        try {
            if(dlog != null)
                dlog.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            log.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Log.d("Result", "Eixos: "+axiosX +"(X) - "+axiosY +"(Y) - "+axiosZ +"(Z)");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
