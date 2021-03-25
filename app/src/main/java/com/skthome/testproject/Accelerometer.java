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
    TextView t1;

    ImageView imageView;

    StringBuilder builder = new StringBuilder();

    float [] history = new float[2];
    String [] direction = {"NONE","NONE"};

    ArrayList<Float> arrHistoryAsisX = new ArrayList<>();
    ArrayList<Float> arrHistoryAsisY = new ArrayList<>();


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

    TextView txtD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);


        imageView = findViewById(R.id.image);
        txtD = findViewById(R.id.txt_d);

        t1= findViewById(R.id.textView1);

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

        if (sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION ){
            float xChange = history[0] - sensorEvent.values[0];
            float yChange = history[1] - sensorEvent.values[1];

            history[0] = sensorEvent.values[0];
            history[1] = sensorEvent.values[1];


            if(Math.abs(sensorEvent.values[0])  > velocityMin){
                arrHistoryAsisX.add(sensorEvent.values[0]);
            }

            if(Math.abs(sensorEvent.values[1]) > velocityMin){
                arrHistoryAsisY.add(sensorEvent.values[1]);
            }

            if (xChange > 2){
                direction[0] = "LEFT";
            }
            else if (xChange < -2){
                direction[0] = "RIGHT";
            }

            if (yChange > 2){
                direction[1] = "DOWN";
            }
            else if (yChange < -2){
                direction[1] = "UP";
            }

            builder.setLength(0);
            builder.append("x: ");
            builder.append(direction[0]);
            builder.append(" y: ");
            builder.append(direction[1]);


//            Log.e("AAAA" , "Y = " + yChange +"");

            //textView.setText(builder.toString());

            if (System.currentTimeMillis() - lastUpdateTime > 2000){

                float vechX = 0;
                float vechY = 0;

                for (int i = 0 ; i < arrHistoryAsisX.size() ; i++){
                    vechX += arrHistoryAsisX.get(i);
                }
                for (int i = 0 ; i < arrHistoryAsisY.size() ; i++){
                    vechY += arrHistoryAsisY.get(i);
                }

                if (isLastMagnetometerArrayCopied && isLastMagnetometerArrayCopied){
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
                        //degree = -degree;
                    }
                }

                double thePath = Math.sqrt(Math.pow(Math.abs(vechX) , 2) + Math.pow(Math.abs(vechY) , 2));

                txtD.setText(thePath +"");

                JSONObject jsonData1 = new JSONObject();
                try {
                    jsonData1.put("getter" , "arduino");
                    jsonData1.put("degree" , degree);
                    led_on_off(jsonData1.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if(Math.abs(vechX) > Math.abs(vechY)){
                    // move left || right
                    if (vechX > 0 ){
                        // move right

                        JSONObject jsonData = new JSONObject();
                        try {
                            jsonData.put("getter" , "arduino");
                            jsonData.put("moved" , (int)thePath*10);
                            jsonData.put("status" , "left");
                            //jsonData.put("degree" , degree);
                            led_on_off(jsonData.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        Log.e("AAAA" , "move left = " + thePath * 10 +" cm");
                    }else{
                        JSONObject jsonData = new JSONObject();
                        try {
                            jsonData.put("getter" , "arduino");
                            jsonData.put("moved" , (int)thePath*10);
                            jsonData.put("status" , "right");
                            //jsonData.put("degree" , degree);
                            led_on_off(jsonData.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.e("AAAA" , "move right = " + thePath * 10 +" cm");
                    }
                }else{
                    if (vechY > 0 ){
                        JSONObject jsonData = new JSONObject();
                        try {
                            jsonData.put("getter" , "arduino");
                            jsonData.put("moved" , (int)thePath*10);
                            jsonData.put("status" , "down");
                            //jsonData.put("degree" , degree);
                            led_on_off(jsonData.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.e("AAAA" , "move down = " + thePath * 10 +" cm");
                    }else{
                        JSONObject jsonData = new JSONObject();
                        try {
                            jsonData.put("getter" , "arduino");
                            jsonData.put("moved" , (int)thePath*10);
                            jsonData.put("status" , "up");
                            //jsonData.put("degree" , degree);
                            led_on_off(jsonData.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.e("AAAA" , "move up = " + thePath * 10 +" cm");
                    }
                }

                //Log.e("AAAA" , "thePath = " + thePath +"");

                double degreePri = Math.toDegrees(Math.tan(vechX/vechY));





//                JSONObject jsonData = new JSONObject();
//                try {
//                    jsonData.put("getter" , "arduino");
//                    jsonData.put("x" , vechX);
//                    jsonData.put("y" , vechY);
//                    jsonData.put("moved" , thePath);
//                    jsonData.put("degree" , degree);
//                    led_on_off(jsonData.toString());
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }

                //Log.e("AAAA" , "Moved = " + thePath +"");

                //Log.e("AAAA" , "Da lech = " + degree +"");

                //Log.e("AAAA" , "Da lech " + Math.toDegrees(Math.tan(vechX/vechY)) +"");


                //if(Math.abs(vechY) > 1 || Math.abs(vechX) > 1)
                //Log.e("AAAA" , Math.toDegrees(Math.tan(vechX/vechY)) +"");

                //arrHistoryAsis.add(new Point(vechX , vechY));

                arrHistoryAsisX.clear();
                arrHistoryAsisY.clear();

//                double path = 0f;
//
//                for (int i = arrHistoryAsis.size() - 1 ; i > 0 ; i--){
//                    double thePath = Math.sqrt(Math.pow(arrHistoryAsis.get(i).getX() - arrHistoryAsis.get(i).getY() , 2) - Math.pow(arrHistoryAsis.get(i-1).getX() - arrHistoryAsis.get(i-1).getY() , 2));
//                    if(!Double.isNaN(thePath)){
//                        path -= thePath;
//                    }
//
//                }

                //Log.e("AAAA" , "da cach tam = " + path);

                lastUpdateTime = System.currentTimeMillis();
            }
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing to do here
    }
}