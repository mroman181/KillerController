package com.example.dam2a21.controller;

import android.graphics.Color;
import android.os.StrictMode;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ConnectionServer implements Runnable {

    private int PORT = 1234; // server details
    private String HOST = "172.16.203.17";
    private Socket sock;
    private BufferedReader in; // i/o for the client
    private PrintWriter out;
    private String lastComand = "st";
    private boolean closed = false;
    private MainActivity ma;
    public static ServerSocket serverSock;
    private int timeout;
    private boolean killed = false;

    public ConnectionServer() {

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    public void run() {
        try {
            Log.d("comunic", getIP());
            serverSock = new ServerSocket(PORT);
            Log.d("comunic", "ServerSocket creado");
        } catch (Exception e) {
            Log.d("comunic", "Error:" + e.getMessage());
        }

        while (!this.killed) {

            this.ma.setColorConnection(true);
            timeout = 0;

            Thread t = new Thread() {
                @Override
                public void run() {
                    Log.d("comunic", "Conectado");
                    while (!closed) {
                        try {
                            Thread.sleep(50);
                        } catch (Exception e) {
                        }
                        timeout++;
                        if (timeout > 20) {
                            closed = true;
                            try {
                                Log.d("comunic", "CERRADO");
                                sock.close();
                            } catch (IOException e) {

                            }
                        }
                    }
                }
            };

            t.start();

            while (!closed && !this.killed) {
                processClient(in, out);
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                }

            }
            // Close client connection
            this.ma.setColorConnection(false);
            try {
                sock.close();
            } catch (Exception e) {
            }

            Log.d("comunic", "Desconectado");
            while (closed && !this.killed) {
                this.reconnect();
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                }
            }

        }
    }

    private void processClient(BufferedReader in, PrintWriter out) {
        String line;
        boolean done = false;
        try {
            while (!done) {
                if ((line = in.readLine()) == null) {
                    done = true;
                } else {
                    System.out.println("Client msg: " + line);
                    if (line.trim().equals("bye")) {
                        done = true;
                        this.closeLink();
                    } else {
                        doRequest(line, out);
                    }
                }
            }
        } catch (IOException e) {
            closed = true;
        }
    }

    private void doRequest(String line, PrintWriter out) {

        if (line.substring(0, 4).equals("dead")) {
            this.ma.die();
        }

        if (line.substring(0, 4).equals("uwin")) {
            this.ma.win();
        }

        if (line.substring(0, 4).equals("full")) {
            this.ma.alertPlayer();
        }

        if (line.substring(0, 4).equals("full")) {
            this.ma.alertPlayer();
        }

        if (line.substring(0, 4).equals("comu")) {
            this.sendCommand("comok");
            this.timeout = 0;
        }

    }

    public int getPORT() {
        return this.PORT;
    }

    public void setPORT(int PORT) {
        this.PORT = PORT;
    }

    public String getHOST() {
        return HOST;
    }

    public void setHOST(String HOST) {
        this.HOST = HOST;
    }

    public void setMainActivity(MainActivity ma) {
        this.ma = ma;
    }

    public boolean makeContact() {
        try {
            sock = new Socket(HOST, PORT);
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out = new PrintWriter(sock.getOutputStream(), true);
            return true;
        } catch (Exception e) {
            System.out.println("No se ha podido connectar");
        }
        return false;
    }

    public void closeLink() {
        try {
            out.println("bye"); // tell server
            sock.close();
            serverSock.close();
            this.killed = true;
            Log.d("comunic", "BYE sended");
        } catch (Exception e) {
            System.out.println(e);
        }
        //System.exit(0);
    }

    public void sendCommand(String comand) {

        try {
            out.println(comand);
            //   Log.d("comunic", "Command "  + comand +" sended");
        } catch (Exception ex) {
            System.out.println("Problem sending " + comand + "\n");
        }

    }

    public void sendMoveCommand(String comand) {

        if (!this.lastComand.equals(comand)) {
            try {
                out.println(comand);
                this.lastComand = comand;
                //      Log.d("comunic", "Command "  + comand +" sended");
            } catch (Exception ex) {
                System.out.println("Problem sending " + comand + "\n");
            }
        }
    }

    private void reconnect() {
        try {
            //  serverSock = new ServerSocket(PORT);
            Log.d("comunic", "Esperando peticion");
            sock = serverSock.accept();
            Log.d("comunic", "Peticion llegada");
            out = new PrintWriter(sock.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String line;
            boolean done = false;
            while (!done) {
                if ((line = in.readLine()) == null) {
                    done = true;
                } else {
                    done = true;
                    if (line.trim().equals("mcone")) {
                        closed = false;
                        this.ma.setCs(this);
                    }

                }
            }

        } catch (Exception e) {
            Log.d("comunic", "Error esperando peticion");
        }

    }

    public static String getIP() {
        List<InetAddress> addrs;
        String address = "";
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress() && addr instanceof Inet4Address) {
                        address = addr.getHostAddress().toUpperCase(new Locale("es", "MX"));
                    }
                }
            }
        } catch (Exception e) {
            // Log.w(TAG, "Ex getting IP value " + e.getMessage());
        }
        return address;
    }
}
