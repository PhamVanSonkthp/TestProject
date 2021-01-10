package com.skthome.testproject;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class MainActivity2 extends AppCompatActivity implements SensorEventListener {

    TextView textView , txtPWM;
    ImageView imageView;
    SensorManager sensorManager;
    private Sensor accelerometer,magnetometer,gyroscopeSensor , rotationVectorSensor;

    float[] lastAccelerometer = new float[3];
    float[] lastMagnetometer = new float[3];
    float[] rotateMatrix = new float[9];
    float[] orentation = new float[3];

    boolean isLastAccelerometerArrayCopied = false;
    boolean isLastMagnetometerArrayCopied = false;

    long lastUpdateTime = 0;
    long lastUpdateTimeMove = 0;
    float currentDegree = 0f;

    int degree = 0;

    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    Set<BluetoothDevice> pairedDevices;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String address = null , name=null;
    TextView t1;

    Button btnEnable , btnDecrea , btnIncrea;
    boolean isEnable = false , movement = false;
    int pwm = 127;
    private float velocityMin = 0.5f;
    private float[] mGravity;
    private float mAccelLast;
    private float mAccelCurrent;
    private float mAccel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        textView = findViewById(R.id.txt);
        imageView = findViewById(R.id.image);
        t1= findViewById(R.id.textView1);
        btnEnable= findViewById(R.id.btnEnable);
        btnDecrea= findViewById(R.id.btnDecreaPWM);
        btnIncrea= findViewById(R.id.btnIncreaPWM);
        txtPWM = findViewById(R.id.txt_pwm);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mAccelLast = SensorManager.GRAVITY_EARTH;
        mAccel = 0.00f;
        mAccelCurrent = SensorManager.GRAVITY_EARTH;

        try {
            setw();
        } catch (IOException e) {
            e.printStackTrace();
        }
        addEvents();
    }

    private void addEvents() {
        btnEnable.setOnClickListener(view -> {
            JSONObject jsonData = new JSONObject();
            try {
                isEnable = !isEnable;
                jsonData.put("getter" , "arduino");
                jsonData.put("enable" , isEnable);
                btnEnable.setText(isEnable+"");
                led_on_off(jsonData.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        btnDecrea.setOnClickListener(view -> {
            JSONObject jsonData = new JSONObject();
            try {
                jsonData.put("getter" , "arduino");
                jsonData.put("pwm" , --pwm);
                txtPWM.setText(pwm+"");
                led_on_off(jsonData.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        btnIncrea.setOnClickListener(view -> {
            JSONObject jsonData = new JSONObject();
            try {
                jsonData.put("getter" , "arduino");
                jsonData.put("pwm" , ++pwm);
                txtPWM.setText(pwm+"");
                led_on_off(jsonData.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
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
        sensorManager.registerListener(this , magnetometer , SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor sensor = sensorEvent.sensor;
        if (sensorEvent.sensor == accelerometer){
            System.arraycopy(sensorEvent.values , 0  , lastAccelerometer , 0 , sensorEvent.values.length);
            isLastAccelerometerArrayCopied = true;
        }else if(sensorEvent.sensor == magnetometer){
            System.arraycopy(sensorEvent.values , 0  , lastMagnetometer , 0 , sensorEvent.values.length);
            isLastMagnetometerArrayCopied = true;
        }

        if (isLastMagnetometerArrayCopied && isLastMagnetometerArrayCopied && System.currentTimeMillis() - lastUpdateTime > 100){
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
            lastUpdateTime = System.currentTimeMillis();
            degree =  (int) azimuthInDegree;

            if (degree < 0) {
                degree = 360 + degree;
                //degree = -degree;
            }
            textView.setText(degree+"");

            JSONObject jsonData = new JSONObject();
            try {
                jsonData.put("getter" , "arduino");
                jsonData.put("conner_robot" , degree);
                led_on_off(jsonData.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        if (sensor.getType() == Sensor.TYPE_ACCELEROMETER ){
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
                lastUpdateTimeMove = System.currentTimeMillis();
                if (!movement){
                    movement = true;
                    JSONObject jsonData = new JSONObject();
                    try {
                        jsonData.put("getter" , "arduino");
                        //jsonData.put("speed_robot" , mAccel);
                        jsonData.put("move" , "true");
                        led_on_off(jsonData.toString());
                        Log.e("AAAA" , movement+"");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }else {
                if (movement && System.currentTimeMillis() - lastUpdateTimeMove > 500){
                    lastUpdateTimeMove = System.currentTimeMillis();
                    movement = false;
                    JSONObject jsonData = new JSONObject();
                    try {
                        jsonData.put("getter" , "arduino");
                        //jsonData.put("speed_robot" , mAccel);
                        jsonData.put("move" , "false");
                        led_on_off(jsonData.toString());
                        Log.e("AAAA" , movement+"");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    public void increaseVel(View view) {
        velocityMin += 0.1f;
    }

    public void decreaseVel(View view) {
        velocityMin -= 0.1f;
    }
}