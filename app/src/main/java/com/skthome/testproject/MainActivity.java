package com.skthome.testproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import static android.view.KeyCharacterMap.ALPHA;

public class MainActivity extends AppCompatActivity implements SensorEventListener{

    private ImageView imageView;
    float[] mGravity = new float[3];
    float[] mGGeomagetic = new float[3];
    float[] acc_Earth_coordinates = new float[3];
    float azimuth = 0f;
    float currectAzimuth = 0f;
    SensorManager mSensorManager;
    //AccelData []  acc_4;
    //private Sensor acc_3;

    float acc_3[] , acc_4[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        acc_3 = new float[3];
        acc_4 = new float[4];

        imageView = findViewById(R.id.image);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //acc_3 = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSensorManager.registerListener(this , mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) , SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this , mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) , SensorManager.SENSOR_DELAY_GAME);
//        mSensorManager.registerListener(this,
//                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
//                SensorManager.SENSOR_DELAY_FASTEST);
//        mSensorManager.registerListener(this,
//                mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
//                SensorManager.SENSOR_DELAY_FASTEST);
//        mSensorManager.registerListener(this,
//                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
//                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    protected float[] lowPass(float[] input, float[] output) {
        if (output == null)
            return input;

        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
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

//        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
//            acc_3 = lowPass(sensorEvent.values.clone(), acc_3);
//            acc_Earth_coordinates[0] = sensorEvent.values[0];
//            acc_Earth_coordinates[1] = sensorEvent.values[1];
//            acc_Earth_coordinates[2] = sensorEvent.values[2];
//        }
//
//        if (sensorEvent.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
//            mGGeomagetic = lowPass(sensorEvent.values.clone(), mGGeomagetic);
//        }
//        if (sensorEvent.sensor.getType() == Sensor.TYPE_GRAVITY) {
//            mGravity = sensorEvent.values.clone();
//        }
//
//        if (acc_3 != null && mGravity != null && mGGeomagetic != null) {
//            float input_R[] = new float[9];
//            float I[] = new float[9];
//            float invert_R[] = new float[9];
//            if (SensorManager.getRotationMatrix(input_R, I, mGravity, mGGeomagetic)) {
//
//                android.opengl.Matrix.invertM(invert_R, 0, input_R, 0);
//                acc_4[0] = acc_3[0];
//                acc_4[1] = acc_3[1];
//                acc_4[2] = acc_3[2];
//                acc_4[3] = 0;
//                long t = System.currentTimeMillis();
//
//                android.opengl.Matrix.multiplyMV(acc_Earth_coordinates, 0,
//                        invert_R, 0, acc_4, 0);
//
//                AccelData accel = new AccelData(t,
//                        acc_Earth_coordinates[0], acc_Earth_coordinates[1],
//                        acc_Earth_coordinates[2]);
//                //AccelSensor.add(accel);
//                Log.e("AAAA" , accel.getX()+"");
//                Log.e("AAAA" , accel.getY()+"");
//                Log.e("AAAA" , accel.getZ()+"");
//
//                acc_3 = null;
//                mGravity = null;
//                mGGeomagetic = null;
//            }
//        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    public class AccelData {
        private long timestamp;
        private float x;
        private float y;
        private float z;

        public AccelData(long timestamp2, float x, float y, float z) {
            this.timestamp = timestamp2;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public float getZ() {
            return z;
        }

        public void setZ(float z) {
            this.z = z;
        }

        public String toString() {
            return "t=" + timestamp + ", x=" + x + ", y=" + y + ", z=" + z;
        }
    }
}