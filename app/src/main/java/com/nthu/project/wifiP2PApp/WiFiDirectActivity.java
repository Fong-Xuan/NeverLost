package com.nthu.project.wifiP2PApp;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.nthu.project.wifiP2PApp.data.User;
import com.nthu.project.wifiP2PApp.dialog.EditProfileDialog;
import com.nthu.project.wifiP2PApp.fragment.DeviceDetailFragment;
import com.nthu.project.wifiP2PApp.fragment.DeviceListFragment;
import com.nthu.project.wifiP2PApp.internet.MacAddressMask;
import com.nthu.project.wifiP2PApp.service.ServerService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class WiFiDirectActivity extends AppCompatActivity implements
                                                     WifiP2pManager.ChannelListener,
                                                     DeviceListFragment.DeviceActionListener{
    private DrawerLayout mDrawerLayout;
    private LinearLayout mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;

    public static final String TAG = "wifi direct";
    public static WifiManager wifi;
    public static List<ScanResult> results;
    public static String targetBSSID;
    public static boolean isfinder;
    public static boolean isScanBtnPressed;

    private WifiP2pManager wifiP2pManager;
    private WifiManager wifiManager;
    private boolean isWifiP2pEnabled = false;
    private boolean retryChannel = false;
    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel channel;
    private BroadcastReceiver receiver = null;
    private DeviceDetailFragment detailFragment;
    private DeviceListFragment listFragment;

    private GoogleApiClient client;
    private User user;
    private Button edit;


    //F:10/17
    public static User localUser;
    public boolean isConnected;
    public static WifiP2pDevice wifiP2pDevice;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiManager = (WifiManager)this.getSystemService(Context.WIFI_SERVICE);
        wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = wifiP2pManager.initialize(this, getMainLooper(), null);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        //intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        receiver = new WiFiDirectBroadcastReceiver(wifiP2pManager, channel, this);
        wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        targetBSSID = "";
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();



        //Edit : 2016/9/19 , initialize settings
        setDrawerAndToolBar();
        localUser = new User();
        isScanBtnPressed = false;


    }
    @Override
    public void onStart(){
        super.onStart();
        peerDiscover();
        new AlertDialog.Builder(this)
                .setTitle("Setting")
                .setMessage("尋找還是被尋找的手機")
                .setCancelable(false)
                .setPositiveButton("尋找",
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialoginterface, int i)
                            {
                                isfinder = true;
                            }
                        }
                )
                .setNegativeButton("被尋找",
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialoginterface, int i)
                            {
                                isfinder = false;
                                openServer();

                            }
                        })
                .show();

    }
    public  void openServer(){
        Intent intent = new Intent(this, ServerService.class);
        this.startService(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.toolbar_items, menu);

        MenuItem toolbar_wifi_switch = (MenuItem)menu.findItem(R.id.toolbar_wifi_switch);
        if(wifiManager.isWifiEnabled()){
            toolbar_wifi_switch.setIcon(R.drawable.ic_wifi_on);
            Log.d("wifiCheck", "on");
        }
        else {
            toolbar_wifi_switch.setIcon(R.drawable.ic_wifi_off);
            Log.d("wifiCheck", "off");
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.toolbar_wifi_switch:
                // User chose the "Settings" item, show the app settings UI...
                if(wifiManager.isWifiEnabled()){
                    wifiManager.setWifiEnabled(false);
                    item.setIcon(R.drawable.ic_wifi_off);
                }
                else {
                    wifiManager.setWifiEnabled(true);
                    item.setIcon(R.drawable.ic_wifi_on);
                }
                break;
            case R.id.toolbar_discover:
                peerDiscover();
                break;
            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
        return super.onOptionsItemSelected(item);
    }

    public static void scanAp(){
        wifi.startScan();

    }

    private boolean peerDiscover(){

        wifiP2pManager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(WiFiDirectActivity.this, "Discovery Initiated",
                        Toast.LENGTH_SHORT).show();
                Log.d("discover success", "this is in the onSuccess!");
            }

            @Override
            public void onFailure(int reasonCode) {
                Toast.makeText(WiFiDirectActivity.this, "Discovery Failed : " + reasonCode,
                        Toast.LENGTH_SHORT).show();
                Log.d("discover fail", "this is in the onFailure!");
            }
        });
        return true;
    }


    /**
     * register the BroadcastReceiver with the intent values to be matched
     */
    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);

    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    /**
     * Remove all peers and clear all fields. This is called on
     * BroadcastReceiver receiving a state change event.
     */
    public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
        this.isWifiP2pEnabled = isWifiP2pEnabled;
    }


    @Override
    public void showDetails(WifiP2pDevice device) {
        targetBSSID = MacAddressMask.maskLastTwoBits(device.deviceAddress);
        //implements interface DeviceActionListener
      DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
             .findFragmentById(R.id.frag_detail);
       fragment.showDetails(device);


        this.wifiP2pDevice = device;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("dialog_detail");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        // Create and show the dialog.
        //DeviceDetailDialog newFragment = new DeviceDetailDialog();
        //newFragment.show(getFragmentManager(), "dialog_detail");




    }

    @Override
    public void connect(WifiP2pConfig config) {
        //implements interface DeviceActionListener
        wifiP2pManager.connect(channel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(WiFiDirectActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void disconnect() {
        //implements interface DeviceActionListener
      final DeviceDetailFragment fragment = (DeviceDetailFragment) getFragmentManager()
                .findFragmentById(R.id.frag_detail);
        fragment.resetViews();
     wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {

          @Override
           public void onFailure(int reasonCode) {
               Log.d(TAG, "Disconnect failed. Reason :" + reasonCode);

           }
        @Override
          public void onSuccess() {
              fragment.getView().setVisibility(View.GONE);
           }

        });
    }

    @Override
    public void cancelDisconnect() {
        //implements interface DeviceActionListener
        /*
         * A cancel abort request by user. Disconnect i.e. removeGroup if
         * already connected. Else, request WifiP2pManager to abort the ongoing
         * request
         */
        if (wifiP2pManager != null) {
            if (listFragment.getDevice() == null
                    || listFragment.getDevice().status == WifiP2pDevice.CONNECTED) {
                disconnect();
            } else if (listFragment.getDevice().status == WifiP2pDevice.AVAILABLE
                    || listFragment.getDevice().status == WifiP2pDevice.INVITED) {

                wifiP2pManager.cancelConnect(channel, new WifiP2pManager.ActionListener() {

                    @Override
                    public void onSuccess() {
                        Toast.makeText(WiFiDirectActivity.this, "Aborting connection",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int reasonCode) {
                        Toast.makeText(WiFiDirectActivity.this,
                                "Connect abort request failed. Reason Code: " + reasonCode,
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }

    @Override
    public void onChannelDisconnected() {
        // we will try once more
        if (wifiP2pManager != null && !retryChannel) {
            Toast.makeText(this, "Channel lost. Trying again", Toast.LENGTH_LONG).show();
            resetData();
            retryChannel = true;
            wifiP2pManager.initialize(this, getMainLooper(), this);
        } else {
            Toast.makeText(this,
                    "Severe! Channel is probably lost premanently. Try Disable/Re-Enable P2P.",
                    Toast.LENGTH_LONG).show();
        }
    }


    public void resetData() {
        //Detail later
        detailFragment = (DeviceDetailFragment) getFragmentManager()
            .findFragmentById(R.id.frag_detail);
        if (listFragment != null) {
            listFragment.clearPeers();
        }
     if (detailFragment != null) {
          detailFragment.resetViews();
      }
    }


    public void updateLocalUser(){
        TextView drawer_userName = (TextView)findViewById(R.id.drawer_userName);
        TextView drawer_userStatus = (TextView)findViewById(R.id.drawer_userStatus);
        ImageView userIcon = (ImageView) findViewById(R.id.drawer_userIcon);
        int iconNo = getLocalUser().getUserIconindex();
        Toast.makeText(WiFiDirectActivity.this, "iconNo:"+String.valueOf(iconNo),
                Toast.LENGTH_SHORT).show();

        drawer_userName.setText(getResources().getString(R.string.drawer_userName)
                +getLocalUser().getUserName());
        drawer_userStatus.setText(getResources().getString(R.string.drawer_userStatus)
                +getLocalUser().getUserStatus());


        switch(iconNo){
            case 1:
                userIcon.setImageResource(R.mipmap.icon1);
                break;
            case 2:
                userIcon.setImageResource(R.mipmap.icon2);
                break;
            case 3:
                userIcon.setImageResource(R.mipmap.icon3);
                break;
            case 4:
                userIcon.setImageResource(R.mipmap.icon4);
                break;
            case 5:
                userIcon.setImageResource(R.mipmap.icon5);
                break;
            case 6:
                userIcon.setImageResource(R.mipmap.icon6);
                break;


        }
    }



    private void setDrawerAndToolBar(){
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        myToolbar.inflateMenu(R.menu.menu_main);
        // 打開 up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        mDrawerList = (LinearLayout) findViewById(R.id.drawer_view);

        // 實作 drawer toggle 並放入 toolbar
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        listFragment = (DeviceListFragment) getFragmentManager()
                .findFragmentById(R.id.frag_list);

        //Button Listener
        Button editProfile = (Button)findViewById(R.id.drawer_btn_profileEdit);
        editProfile.setOnClickListener(new DrawerItemClickListener());


    }
    private class DrawerItemClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.drawer_btn_profileEdit){
//                new EditProfileDialog();
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                Fragment prev = getFragmentManager().findFragmentByTag("dialog");
                if (prev != null) {
                    ft.remove(prev);
                }
                ft.addToBackStack(null);
                // Create and show the dialog.
                EditProfileDialog newFragment = new EditProfileDialog();
                newFragment.show(getFragmentManager(), "dialog");

                try {
                    Method m = wifiP2pManager.getClass().getMethod("setDeviceName", new Class[]     {channel.getClass(), String.class,
                            WifiP2pManager.ActionListener.class});
                    m.invoke(wifiP2pManager, channel, getLocalUser().getUserName(), new WifiP2pManager.ActionListener() {

                        @Override
                        public void onSuccess() {
                            Toast.makeText(getApplicationContext(),
                                    "The Wifi-Direct SSID(name) changes to "+getLocalUser().getUserName(),
                                    Toast.LENGTH_LONG).show();
                        }
                        @Override
                        public void onFailure(int reason) {
                        }
                    });
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isConnected(){
        return isConnected;
    }

    public static User getLocalUser(){
        return localUser;
    }

    public void setWifiSwitchItem(boolean isWifiEnabled){
        try{
            MenuItem toolbar_wifi_switch = (MenuItem)menu.findItem(R.id.toolbar_wifi_switch);
            if(isWifiEnabled){
                toolbar_wifi_switch.setIcon(R.drawable.ic_wifi_on);
                Log.d("wifiCheck", "on");
            }
            else {
                toolbar_wifi_switch.setIcon(R.drawable.ic_wifi_off);
            }
        }catch(NullPointerException npe){

        }

    }

}
