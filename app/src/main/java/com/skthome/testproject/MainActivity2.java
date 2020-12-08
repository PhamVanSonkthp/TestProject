package com.skthome.testproject;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity2 extends AppCompatActivity implements SensorEventListener {

    TextView textView;
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
    float currentDegree = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        textView = findViewById(R.id.txt);
        imageView = findViewById(R.id.image);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

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
            int x = (int) azimuthInDegree;
            textView.setText(x+"");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}