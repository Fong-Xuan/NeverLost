package com.nthu.project.wifiP2PApp.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import com.nthu.project.wifiP2PApp.internet.ApManager;
import com.nthu.project.wifiP2PApp.R;
import com.nthu.project.wifiP2PApp.WiFiDirectActivity;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerService extends IntentService {

    public ServerService() {
        super("ServerService");
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            Log.d("ServerService", "onHandleIntent");
            Context context = this;
            boolean ring_state = false;
            MediaPlayer mediaPlayer;
            mediaPlayer = MediaPlayer.create(context, R.raw.ring);
            mediaPlayer.setLooping(true);

            while (true) {
                String[] receiveMsg;
                ServerSocket serverSocket = new ServerSocket(8988);
                Log.d(WiFiDirectActivity.TAG, "Server: Socket opened");
                Socket client = serverSocket.accept();
                Log.d(WiFiDirectActivity.TAG, "Server: connection done");

                ObjectInputStream input = new ObjectInputStream(client.getInputStream());
                receiveMsg = (String[]) input.readObject();
                Log.d(WiFiDirectActivity.TAG, "receive "+receiveMsg[0]);
                if (receiveMsg[0].equals("ring")){
                    if (!ring_state) {
                        Log.d("ring test ", "start");
                        mediaPlayer.start();
                        ring_state = true;

                    }
                    else {
                        Log.d("ring test ", "stop");
                        mediaPlayer.pause();
                        ring_state = false;

                    }


                }
                else if(receiveMsg[0].equals("ap")){
                    //Detail modify
                    ApManager.configApState(context);
                    //ApManager.setHotspotName("LinTzuAn",context);
                    Log.e("group_owner ap mode", "after toggling");
                    try {
                        Log.e("group_owner ap mode", "before sleep");
                        Thread.sleep(20000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }finally {
                        Log.e("group_owner ap mode", "finally");
                        ApManager.configApState(context);
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Intent intent2 = new Intent(context, bkDiscover.class);
                        context.startService(intent2);
                    }

                }
                serverSocket.close();
                Log.d(WiFiDirectActivity.TAG, "Server: Socket closed");

            }
        } catch (IOException e) {
            Log.e(WiFiDirectActivity.TAG, e.getMessage());

        } catch (ClassNotFoundException e) {
            e.printStackTrace();

        }
    }



}
