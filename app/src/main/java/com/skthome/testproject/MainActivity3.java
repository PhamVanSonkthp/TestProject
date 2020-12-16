package com.skthome.testproject;

import androidx.appcompat.app.AppCompatActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;

import static android.view.KeyCharacterMap.ALPHA;

public class MainActivity3 extends AppCompatActivity implements SensorEventListener{

    private Sensor accelerometer,magnetometer,gyroscopeSensor , rotationVectorSensor;
    SensorManager mSenManager;

    boolean started = true;

    float mag[] = new float[3];
    float grav[] = new float[3];
    float acc_3[] = new float[3];
    float acc_4[] = new float[4];
    float input_R[] = new float[9];
    float I[] = new float[9];
    float invert_R[] = new float[9];
    float acc_Earth_coordinates[] = new float[3];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        mSenManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSenManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSenManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        gyroscopeSensor = mSenManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        RegisterSensor();
    }

    public void RegisterSensor() {
        mSenManager.registerListener(this,
                mSenManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);
        mSenManager.registerListener(this,
                mSenManager.getDefaultSensor(Sensor.TYPE_GRAVITY),
                SensorManager.SENSOR_DELAY_FASTEST);
        mSenManager.registerListener(this,
                mSenManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_FASTEST);

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
    public void onSensorChanged(SensorEvent event) {
        if (started) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                acc_3 = lowPass(event.values.clone(), acc_3);
                acc_Earth_coordinates[0] = event.values[0];
                acc_Earth_coordinates[1] = event.values[1];
                acc_Earth_coordinates[2] = event.values[2];
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mag = lowPass(event.values.clone(), mag);
            }
            if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                grav = event.values.clone();
            }

            if (acc_3 != null && grav != null && mag != null) {

                if (SensorManager.getRotationMatrix(input_R, I, grav, mag)) {

                    android.opengl.Matrix.invertM(invert_R, 0, input_R, 0);
                    acc_4[0] = acc_3[0];
                    acc_4[1] = acc_3[1];
                    acc_4[2] = acc_3[2];
                    acc_4[3] = 0;
                    long t = System.currentTimeMillis();

                    android.opengl.Matrix.multiplyMV(acc_Earth_coordinates, 0,
                            invert_R, 0, acc_4, 0);

                    AccelData accel = new AccelData(t,
                            acc_Earth_coordinates[0], acc_Earth_coordinates[1],
                            acc_Earth_coordinates[2]);
                    //AccelSensor.add(accel);
                    Log.e("AAAA" , accel.getX()+"");
                    Log.e("AAAA" , accel.getY()+"");
                    Log.e("AAAA" , accel.getZ()+"");

                    acc_3 = null;
                    grav = null;
                    mag = null;
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}