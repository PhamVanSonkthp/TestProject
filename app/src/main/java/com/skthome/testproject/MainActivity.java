package com.skthome.testproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private ImageView imageView;
    float[] mGravity = new float[3];
    float[] mGGeomagetic = new float[3];
    float azimuth = 0f;
    float currectAzimuth = 0f;
    SensorManager mSensorManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.image);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

    }

    @Override
    protected void onResume() {
        super.onResume();

        mSensorManager.registerListener(this , mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) , SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this , mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) , SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        final float alpha = 0.97f;
        synchronized (this){
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
                mGravity[0] = alpha*mGravity[0]+(1-alpha)*sensorEvent.values[0];
                mGravity[1] = alpha*mGravity[1]+(1-alpha)*sensorEvent.values[1];
                mGravity[2] = alpha*mGravity[2]+(1-alpha)*sensorEvent.values[2];
            }
            if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
                mGGeomagetic[0] = alpha*mGGeomagetic[0]+(1-alpha)*sensorEvent.values[0];
                mGGeomagetic[1] = alpha*mGGeomagetic[1]+(1-alpha)*sensorEvent.values[1];
                mGGeomagetic[2] = alpha*mGGeomagetic[2]+(1-alpha)*sensorEvent.values[2];
            }

            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R , I , mGravity , mGGeomagetic);
            if (success){
                float orentation[] = new float[3];
                SensorManager.getOrientation(R , orentation);
                azimuth = (float) Math.toDegrees(orentation[0]);
                azimuth = (azimuth + 360) % 360;
                Animation animation = new RotateAnimation(-currectAzimuth , -azimuth , Animation.RELATIVE_TO_SELF , 0.5f , Animation.RELATIVE_TO_SELF , 0.5f);
                currectAzimuth = azimuth;
                animation.setDuration(500);
                animation.setRepeatCount(0);
                animation.setFillAfter(true);

                imageView.startAnimation(animation);

                Log.e("AAAA" , azimuth+"");
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}