package com.nthu.project.wifiP2PApp.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.nthu.project.wifiP2PApp.R;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class AlertService extends IntentService {

    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";
    private static final int SOCKET_TIMEOUT = 4000;

    public AlertService() {
        super("AlertService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Context context = this;
        //boolean ring_state = false;
        MediaPlayer mediaPlayer;
        mediaPlayer = MediaPlayer.create(context, R.raw.ring);
        mediaPlayer.setLooping(true);


        if (intent != null) {
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
            while(true) {
                Socket socket = new Socket();
                try {
                   // String[] request = new String[10];
                    Log.d("AlertService", "Opening client socket - ");
                    socket.bind(null);
                    socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
                    Log.d("AlertService", "AlertService detect  - " + socket.isConnected());
                    //ObjectOutputStream toServer = new ObjectOutputStream(socket.getOutputStream());
                    //toServer.flush();
                    //request[0] = "ap";
                    //toServer.writeUnshared(request);
                    socket.close();
                    Thread.sleep(15000); //sleep for 15 seconds

                } catch (IOException e) {
                    //Exception thrown when general network I/O error occurs
                    Log.e("AlertService", e.getMessage());
                    Log.e("AlertService", "general network I/O error occurs");
                    //mediaPlayer.start();
                } catch (InterruptedException e) {
                    //Exception thrown when network timeout occurs
                    Log.e("AlertService", "network timeout occurs");
                    //mediaPlayer.start();
                    //e.printStackTrace();
                } finally {
                    if (socket != null) {
                        if (socket.isConnected()) {
                            try {
                                socket.close();
                            } catch (IOException e) {
                                // Give up
                                e.printStackTrace();
                            }
                        }
                    }
                }


            }


        }
    }



}
