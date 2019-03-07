package com.example.dam2a21.controller;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.os.Vibrator;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ConnectionServer cs;
    private RelativeLayout layout_joystick;
    private JoyStickClass js;
    ImageView image_joystick, image_border;
    private MediaPlayer disparo;
    private Vibrator v;
    private TextView text;
    private Button con;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.text = (TextView) findViewById(R.id.text);
        this.con = (Button) findViewById(R.id.con);
        this.con.setEnabled(false);

        this.disparo = MediaPlayer.create(this, R.raw.disparo);
        this.v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        String connection = getIntent().getExtras().getString("Connection");

        this.cs = new ConnectionServer();
        //GET IP
        String ip = connection.substring(0, connection.indexOf("&"));
        connection = connection.substring(connection.indexOf("&") + 1);

        //GET PORT
        String port = connection.substring(0, connection.indexOf("&"));

        //GET COLOR
        String color = connection.substring(connection.indexOf("&") + 1);

        this.cs.setHOST(ip);
        this.cs.setPORT(Integer.parseInt(port));

        if (this.cs.makeContact()) {
            this.cs.setMainActivity(this);
            new Thread(this.cs).start();
            this.cs.sendCommand("mcone");
            this.sendColor(color);

        } else {
            System.out.println("Error connection");
        }


        layout_joystick = (RelativeLayout) findViewById(R.id.layout_joystick);

        js = new JoyStickClass(getApplicationContext()
                , layout_joystick, R.drawable.image_button, this.cs);
        js.setStickSize(150, 150);
        js.setLayoutSize(500, 500);
        js.setLayoutAlpha(150);
        js.setStickAlpha(100);
        js.setOffset(90);
        js.setMinimumDistance(50);

        layout_joystick.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View arg0, MotionEvent arg1) {
                String dir = "st";
                js.drawStick(arg1);
                if (arg1.getAction() == MotionEvent.ACTION_DOWN
                        || arg1.getAction() == MotionEvent.ACTION_MOVE) {
                    int direction = js.get8Direction();
                    if (direction == JoyStickClass.STICK_UP) {
                        dir = "up";
                    } else if (direction == JoyStickClass.STICK_UPRIGHT) {
                        dir = "ur";
                    } else if (direction == JoyStickClass.STICK_RIGHT) {
                        dir = "ri";
                    } else if (direction == JoyStickClass.STICK_DOWNRIGHT) {
                        dir = "dr";
                    } else if (direction == JoyStickClass.STICK_DOWN) {
                        dir = "do";
                    } else if (direction == JoyStickClass.STICK_DOWNLEFT) {
                        dir = "dl";
                    } else if (direction == JoyStickClass.STICK_LEFT) {
                        dir = "le";
                    } else if (direction == JoyStickClass.STICK_UPLEFT) {
                        dir = "ul";
                    } else if (direction == JoyStickClass.STICK_NONE) {
                        dir = "st";
                    }
                } else if (arg1.getAction() == MotionEvent.ACTION_UP) {
                    dir = "st";
                }
                //Habra que hacer lo del command en connection

                js.getConnectionServer().sendMoveCommand(dir);

                return true;
            }
        });
    }

    @Override
    protected void onPause() {
        this.cs.closeLink();
        MainActivity.this.finish();
        super.onPause();
    }

    public void fire(View v) {
        this.disparo.start();
        this.cs.sendCommand("shoot");
    }

    public void win() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setTextColor(Color.GREEN);
                text.setText("Has ganado");
                Animation fade1 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
                text.startAnimation(fade1);
            }
        });
    }

    public void die() {
        this.v.vibrate(500);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                text.setTextColor(Color.RED);
                text.setText("Has muerto");
                Animation fade1 = AnimationUtils.loadAnimation(MainActivity.this, R.anim.fade_in);
                text.startAnimation(fade1);
            }
        });

    }

    private void sendColor(String color) {

        int red = 0;
        int blue = 0;
        int green = 0;
        switch (color) {
            case "Azul":
                blue = 255;
                break;
            case "Rojo":
                red = 255;
                break;
            case "Verde":
                green = 255;
                break;
            case "Marron":
                red = 128;
                green = 64;
                break;
            default:

                break;
        }
        this.cs.sendCommand("color" + red + "&" + green + "&" + blue);
    }

    public void setCs(ConnectionServer connection) {
        this.cs = connection;
    }

    public void alertPlayer() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle(R.string.dialog_title);
                builder.setMessage(R.string.dialog_message);

                //No se puede cancelar excepto si pulsamos ok
                builder.setCancelable(false);

                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        back();
                    }
                });


                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    private void back() {
        this.cs.closeLink();
        MainActivity.this.finish();
    }

    public void setColorConnection(boolean active) {

        if (active) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    con.setBackgroundColor(Color.GREEN);
                }
            });

        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    con.setBackgroundColor(Color.RED);
                }
            });
        }

    }

}

