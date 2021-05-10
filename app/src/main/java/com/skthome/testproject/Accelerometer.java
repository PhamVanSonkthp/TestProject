package com.skthome.testproject;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class Accelerometer extends AppCompatActivity implements SensorEventListener {

    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    Set<BluetoothDevice> pairedDevices;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String address = null , name=null;
    TextView t1 , txtDegree;

    ImageView imageView;

    private Sensor accelerometer , accelerometerCompass , magnetometerCompass;
    SensorManager sensorManager;

    long lastUpdateTime = 0;

    int degree = 0;

    float currentDegree = 0f;
    float[] rotateMatrix = new float[9];
    float[] orentation = new float[3];
    float[] lastAccelerometer = new float[3];
    float[] lastMagnetometer = new float[3];
    boolean isLastAccelerometerArrayCopied = false;
    boolean isLastMagnetometerArrayCopied = false;

    float velocityMin = 0.5f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);


        imageView = findViewById(R.id.image);
        t1= findViewById(R.id.textView1);
        txtDegree= findViewById(R.id.txtDegree);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        accelerometerCompass = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometerCompass = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        try {
            setw();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void setw() throws IOException {
        bluetooth_connect_device();
    }

    private void bluetooth_connect_device() throws IOException{
        try{
            myBluetooth = BluetoothAdapter.getDefaultAdapter();
            address = myBluetooth.getAddress();
            pairedDevices = myBluetooth.getBondedDevices();
            if (pairedDevices.size()>0){
                for(BluetoothDevice bt : pairedDevices){
                    address=bt.getAddress();name = bt.getName();
                    Toast.makeText(getApplicationContext(),"Connected", Toast.LENGTH_SHORT).show();
                }
            }
        }
        catch(Exception we){}
        myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
        BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
        btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
        btSocket.connect();
        try { t1.setText("BT Name: "+name+"\nBT Address: "+address); }
        catch(Exception e){}
    }

    private void led_on_off(String i){
        try{
            if (btSocket!=null){
                btSocket.getOutputStream().write(i.getBytes());
            }
        }
        catch (Exception e){
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this , accelerometer , SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this , accelerometerCompass , SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this , magnetometerCompass , SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;
        if (sensorEvent.sensor == accelerometerCompass){
            System.arraycopy(sensorEvent.values , 0  , lastAccelerometer , 0 , sensorEvent.values.length);
            isLastAccelerometerArrayCopied = true;
        }else if(sensorEvent.sensor == magnetometerCompass){
            System.arraycopy(sensorEvent.values , 0  , lastMagnetometer , 0 , sensorEvent.values.length);
            isLastMagnetometerArrayCopied = true;
        }

        if (System.currentTimeMillis() - lastUpdateTime > 200 && isLastMagnetometerArrayCopied && isLastMagnetometerArrayCopied){
            SensorManager.getRotationMatrix(rotateMatrix , null , lastAccelerometer , lastMagnetometer);
            SensorManager.getOrientation(rotateMatrix , orentation);

            float azimuthInRadians = orentation[0];
            float azimuthInDegree = (float) Math.toDegrees(azimuthInRadians);


            RotateAnimation rotateAnimation = new RotateAnimation(currentDegree , -azimuthInDegree , Animation.RELATIVE_TO_SELF , 0.5f , Animation.RELATIVE_TO_SELF , 0.5f);
            rotateAnimation.setDuration(100);
            rotateAnimation.setRepeatCount(0);
            rotateAnimation.setFillAfter(true);
            imageView.startAnimation(rotateAnimation);
            currentDegree = -azimuthInDegree;
            degree =  (int) azimuthInDegree;

            if (degree < 0) {
                degree = 360 + degree;
            }
            txtDegree.setText(degree+"");
            JSONObject jsonData1 = new JSONObject();
            try {
                jsonData1.put("getter" , "arduino");
                jsonData1.put("degree" , degree);
                led_on_off(jsonData1.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            lastUpdateTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing to do here
    }
}