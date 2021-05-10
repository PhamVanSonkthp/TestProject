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
import android.view.MotionEvent;
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

public class MainActivity2 extends AppCompatActivity {

    TextView textView, txtPWM;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    Set<BluetoothDevice> pairedDevices;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String address = null, name = null;
    TextView t1;

    Button btnDecrea, btnIncrea, btnDown, btnUp, btnLeft, btnRight , btnEnable;
    int pwm = 0;

    boolean enable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        textView = findViewById(R.id.txt);
        t1 = findViewById(R.id.textView1);
        btnDecrea = findViewById(R.id.btn_decrease);
        btnIncrea = findViewById(R.id.btn_increase);
        btnDown = findViewById(R.id.btnDown);
        btnUp = findViewById(R.id.btnUp);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        btnEnable = findViewById(R.id.btnEnable);
        txtPWM = findViewById(R.id.txt_value);

        try {
            setw();
        } catch (IOException e) {
            e.printStackTrace();
        }
        addEvents();
    }

    private void addEvents() {
        btnEnable.setOnClickListener(v -> {
            enable = !enable;
            if (enable){
                JSONObject jsonData = new JSONObject();
                try {
                    jsonData.put("enable", "enable");
                    txtPWM.setText(pwm + "");
                    led_on_off(jsonData.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else {
                JSONObject jsonData = new JSONObject();
                try {
                    jsonData.put("disable", "disable");
                    txtPWM.setText(pwm + "");
                    led_on_off(jsonData.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
        btnDecrea.setOnClickListener(view -> {
            JSONObject jsonData = new JSONObject();
            try {
                pwm -= 1;
                if (pwm < 1) {
                    pwm = 1;
                }
                jsonData.put("pwm", pwm -= 1);
                txtPWM.setText(pwm + "");
                led_on_off(jsonData.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        btnIncrea.setOnClickListener(view -> {
            JSONObject jsonData = new JSONObject();
            try {
                jsonData.put("pwm", pwm += 1);
                txtPWM.setText(pwm + "");
                led_on_off(jsonData.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        btnDown.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    JSONObject jsonData = new JSONObject();
                    try {
                        jsonData.put("movement", "down");
                        led_on_off(jsonData.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    JSONObject jsonData = new JSONObject();
                    try {
                        jsonData.put("movement", "idle");
                        led_on_off(jsonData.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                return false;
            }
        });

        btnLeft.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    JSONObject jsonData = new JSONObject();
                    try {
                        jsonData.put("movement", "left");
                        led_on_off(jsonData.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    JSONObject jsonData = new JSONObject();
                    try {
                        jsonData.put("movement", "idle");
                        led_on_off(jsonData.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                return false;
            }
        });

        btnRight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    JSONObject jsonData = new JSONObject();
                    try {
                        jsonData.put("movement", "right");
                        led_on_off(jsonData.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    JSONObject jsonData = new JSONObject();
                    try {
                        jsonData.put("movement", "idle");
                        led_on_off(jsonData.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                return false;
            }
        });

        btnUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    JSONObject jsonData = new JSONObject();
                    try {
                        jsonData.put("movement", "up");
                        led_on_off(jsonData.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    JSONObject jsonData = new JSONObject();
                    try {
                        jsonData.put("movement", "idle");
                        led_on_off(jsonData.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                return false;
            }
        });
    }

    private void setw() throws IOException {
        bluetooth_connect_device();
    }

    private void bluetooth_connect_device() throws IOException {
        try {
            myBluetooth = BluetoothAdapter.getDefaultAdapter();
            address = myBluetooth.getAddress();
            pairedDevices = myBluetooth.getBondedDevices();
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice bt : pairedDevices) {
                    address = bt.getAddress();
                    name = bt.getName();
                    Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception we) {
        }
        myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
        BluetoothDevice dispositivo = myBluetooth.getRemoteDevice(address);//connects to the device's address and checks if it's available
        btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
        btSocket.connect();
        try {
            t1.setText("BT Name: " + name + "\nBT Address: " + address);
        } catch (Exception e) {
        }
    }

    private void led_on_off(String i) {
        try {
            if (btSocket != null) {
                btSocket.getOutputStream().write(i.getBytes());
            }
        } catch (Exception e) {
        }
    }
}