/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nthu.project.wifiP2PApp;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;
import android.widget.Toast;

import com.nthu.project.wifiP2PApp.fragment.DeviceDetailFragment;
import com.nthu.project.wifiP2PApp.fragment.DeviceListFragment;


/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private Channel channel;
    private WiFiDirectActivity activity;
    private WifiManager wiFiManager;

    /**
     * @param manager WifiP2pManager system service
     * @param channel Wifi p2p channel
     * @param activity activity associated with the receiver
     */
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                       WiFiDirectActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // UI update to indicate wifi p2p status.
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                // Wifi Direct mode is enabled
                activity.setIsWifiP2pEnabled(true);
                activity.setWifiSwitchItem(true);
                activity.updateLocalUser();
            } else {
                activity.setIsWifiP2pEnabled(false);
                activity.resetData();
                activity.setWifiSwitchItem(false);

            }
            Log.d(WiFiDirectActivity.TAG, "P2P state changed - " + state);
        }
        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            Log.d(WiFiDirectActivity.TAG, "P2P peers changed");
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (manager != null) {
                manager.requestPeers(channel, (PeerListListener) activity.getFragmentManager()
                        .findFragmentById(R.id.frag_list));
            }

        }
        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {

            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {

                // we are connected with the other device, request connection
                // info to find group owner IP
                //Detail modify
              DeviceDetailFragment fragment = (DeviceDetailFragment) activity
                    .getFragmentManager().findFragmentById(R.id.frag_detail);
             manager.requestConnectionInfo(channel, fragment);
            } else {
                /*
                if(!WiFiDirectActivity.isScanBtnPressed){

                    MediaPlayer mediaPlayer;
                    mediaPlayer = MediaPlayer.create(context, R.raw.ring);
                    mediaPlayer.setLooping(true);
                    mediaPlayer.start();


                }*/

                activity.resetData();
            }
        }
        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            DeviceListFragment fragment = (DeviceListFragment) activity.getFragmentManager()
                    .findFragmentById(R.id.frag_list);
            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));


            //Edit: reset the local user and the info in the drawer
            activity.getLocalUser().setUserDevice((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));

        }
        else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
            //wifi p2p start or stop
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, 10000);
            //10000 is the value to be returned if no value of the desired type is stored with the given name.
            if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED)
            {
                // Wifi P2P discovery started.
                Toast.makeText(context, "discover started",
                        Toast.LENGTH_SHORT).show();
            }
            else
            {
                // Wifi P2P discovery stopped.
                // Do what you want to do when discovery stopped
                Toast.makeText(context, "discover stoped",
                        Toast.LENGTH_SHORT).show();
            }


        }

        else if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {

            WiFiDirectActivity.results = WiFiDirectActivity.wifi.getScanResults();
            int size =  WiFiDirectActivity.results.size() - 1;
            Log.d("WifiBroadcastReceiver", size+": WifiManager.SCAN_RESULTS_AVAILABLE_ACTION");
            while(size >= 0)
            {

                String bssid = WiFiDirectActivity.targetBSSID;
                if(bssid.equals("")) {
                    size--;
                    continue;
                }
                if(WiFiDirectActivity.results.get(size).BSSID.equals(bssid))
                {
                    int signal_strengh = WiFiDirectActivity.results.get(size).level;
                    String siganlInfo = WiFiDirectActivity.results.get(size).SSID + " strength:" + new String(WiFiDirectActivity.results.get(size).level+" dBm");
                    Log.i("AP INFO", WiFiDirectActivity.results.get(size).SSID + " strength:" + new String(WiFiDirectActivity.results.get(size).level+" dBm"));
                    Log.i("AP INFO", "BSSID:" + WiFiDirectActivity.results.get(size).BSSID);
                    /*Toast.makeText(context, siganlInfo,
                        Toast.LENGTH_LONG).show();*/
                    int iconId = R.drawable.signalempty;
                    if(signal_strengh >= -100 && signal_strengh <= -80 )
                        iconId = R.drawable.signal0;
                    else if(signal_strengh >= -79 && signal_strengh <= -50 )
                        iconId = R.drawable.signal1;
                    else if(signal_strengh >= -49 && signal_strengh <= -30 )
                        iconId = R.drawable.signal2;
                    else if(signal_strengh >= -29 )
                        iconId = R.drawable.signal3;


                    new AlertDialog.Builder(context)
                        .setTitle("Signal_Strength")
                            .setIcon(iconId)
                        .setMessage(siganlInfo)
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialoginterface, int i)
                                    {

                                    }
                                }
                        )
                        .show();
                }
                size--;


            }
            Log.d("WifiBroadcastReceiver", "finish: WifiManager.SCAN_RESULTS_AVAILABLE_ACTION");

        }

    }

}
