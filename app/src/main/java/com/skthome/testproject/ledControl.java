package com.skthome.testproject;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Set;
import java.util.UUID;

public class ledControl extends AppCompatActivity implements SensorEventListener {

    TextView t1 , txtTimeDelay , txtVelocityMin , txtConnerMin , txtPWMTimeRotate;
    Button btnEnableRobot , btnEnableRotate;
    String address = null , name=null;

    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    Set<BluetoothDevice> pairedDevices;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private SensorManager sensorManager;
    private Sensor accelerometer,magnetometer,gyroscopeSensor , rotationVectorSensor;

    private int lastConner = 0 , pwmRotate = 50;


    private float[] mGravity;
    private float mAccel;
    private float mAccelCurrent;
    private float mAccelLast;

    private int timeDelay = 50;
    private float velocityMin = 0.5f;
    private int connerMin = 10;

    private boolean enableRobot = false , enableRotate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_control);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        rotationVectorSensor =
                sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        if(rotationVectorSensor != null){
            sensorManager.registerListener(ledControl.this,rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }


        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(ledControl.this,accelerometer, SensorManager.SENSOR_DELAY_UI);
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;
        mAccelLast = SensorManager.GRAVITY_EARTH;

        try {setw();} catch (Exception e) {}
    }

    private void setw() throws IOException{
        t1= findViewById(R.id.textView1);
        txtTimeDelay= findViewById(R.id.txt_time_delay);
        txtVelocityMin= findViewById(R.id.txt_velocity_min);
        txtConnerMin= findViewById(R.id.txt_conner_min);
        txtPWMTimeRotate= findViewById(R.id.txt_time_pwm);
        btnEnableRobot= findViewById(R.id.btnEnableRobot);
        btnEnableRotate= findViewById(R.id.btnEnableRotate);
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
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;
        if(sensor.getType() == Sensor.TYPE_GYROSCOPE) {
//            if(sensorEvent.values[2] > 0.5f) { // anticlockwise
//                getWindow().getDecorView().setBackgroundColor(Color.BLUE);
//                Log.e("AAAA" , "Blue");
//            } else if(sensorEvent.values[2] < -0.5f) { // clockwise
//                getWindow().getDecorView().setBackgroundColor(Color.YELLOW);
//                Log.e("AAAA" , "yellow");
//            }
        }else if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
            float[] rotationMatrix = new float[16];
            SensorManager.getRotationMatrixFromVector(
                    rotationMatrix, sensorEvent.values);

            // Remap coordinate system
            float[] remappedRotationMatrix = new float[16];
            SensorManager.remapCoordinateSystem(rotationMatrix,
                    SensorManager.AXIS_X,
                    SensorManager.AXIS_Z,
                    remappedRotationMatrix);

// Convert to orientations
            float[] orientations = new float[3];
            SensorManager.getOrientation(remappedRotationMatrix, orientations);
            for(int i = 0; i < 3; i++) {
                orientations[i] = (float)(Math.toDegrees(orientations[i]));
            }

//            if(orientations[2] > 45) {
//                getWindow().getDecorView().setBackgroundColor(Color.YELLOW);
//            } else if(orientations[2] < -45) {
//                getWindow().getDecorView().setBackgroundColor(Color.BLUE);
//            } else if(Math.abs(orientations[2]) < 10) {
//                getWindow().getDecorView().setBackgroundColor(Color.WHITE);
//            }

            //Log.e("AAAA0" , orientations[0]+"");
            //Log.e("AAAA1" , orientations[1]+"");
            //Log.e("AAAA2" , (int)orientations[2]+"");
            //led_on_off((int)orientations[2]+"");
            if (Math.abs(lastConner - (int)orientations[2]) < connerMin){
                return;
            }
            lastConner = (int)orientations[2];
            JSONObject jsonData = new JSONObject();
            try {
                jsonData.put("getter" , "arduino");
                jsonData.put("conner_robot" , (int)orientations[2]);
                led_on_off(jsonData.toString());
                Log.e("AAAA" , (int)orientations[2]+"");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else if (sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            mGravity = sensorEvent.values.clone();
            // Shake detection
            float x = mGravity[0];
            float y = mGravity[1];
            float z = mGravity[2];
            mAccelLast = mAccelCurrent;
            mAccelCurrent = (float) Math.sqrt(x*x + y*y + z*z);
            float delta = mAccelCurrent - mAccelLast;
            mAccel = mAccel * 0.9f + delta;
            // Make this higher or lower according to how much
            // motion you want to detect
            if(mAccel > velocityMin){
                // do something
                JSONObject jsonData = new JSONObject();
                try {
                    jsonData.put("getter" , "arduino");
                    //jsonData.put("speed_robot" , mAccel);
                    jsonData.put("speed_robot" , String.format("%.2f", mAccel).replace("," , "."));
                    led_on_off(jsonData.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void decreaseTime(View view) {
        timeDelay -= 10;
        txtTimeDelay.setText(String.format("%s", timeDelay));
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("getter" , "arduino");
            jsonData.put("time_delay" , timeDelay);
            led_on_off(jsonData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void increaseTime(View view) {
        timeDelay += 10;
        txtTimeDelay.setText(String.format("%s", timeDelay));
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("getter" , "arduino");
            jsonData.put("time_delay" , timeDelay);
            led_on_off(jsonData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void decreaseVelocity(View view) {
        velocityMin -= 0.1;
        txtVelocityMin.setText(String.format("%s", velocityMin));
    }

    public void increaseVelocity(View view) {
        velocityMin += 0.1;
        txtVelocityMin.setText(String.format("%s", velocityMin));
    }

    public void decreaseConner(View view) {
        connerMin -= 5;
        txtConnerMin.setText(String.format("%s", connerMin));
    }

    public void increaseConner(View view) {
        connerMin += 5;
        txtConnerMin.setText(String.format("%s", connerMin));
    }

    public void decreasePWMRotate(View view) {
        pwmRotate -= 10;
        txtPWMTimeRotate.setText(String.format("%s", pwmRotate));
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("getter" , "arduino");
            jsonData.put("time_rotate" , pwmRotate);
            led_on_off(jsonData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void increasePWMRotate(View view) {
        pwmRotate += 10;
        txtPWMTimeRotate.setText(String.format("%s", pwmRotate));
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("getter" , "arduino");
            jsonData.put("time_rotate" , pwmRotate);
            led_on_off(jsonData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void enable_robot(View view) {
        enableRobot = !enableRobot;
        if (enableRobot){
            btnEnableRobot.setText("BẬT");
        }else {
            btnEnableRobot.setText("TẮT");
        }
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("getter" , "arduino");
            jsonData.put("enable_robot" , enableRobot ? "true" : "false");
            led_on_off(jsonData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void enable_rotate(View view) {
        enableRotate = !enableRotate;
        if (enableRotate){
            btnEnableRotate.setText("Bật");
        }else {
            btnEnableRotate.setText("Tắt");
        }
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("getter" , "arduino");
            jsonData.put("enable_rotate" , enableRotate ? "true" : "false");
            led_on_off(jsonData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void move(View view) {
        JSONObject jsonData = new JSONObject();
        try {
            jsonData.put("getter" , "arduino");
            //jsonData.put("speed_robot" , mAccel);
            jsonData.put("speed_robot" , 2000);
            led_on_off(jsonData.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}