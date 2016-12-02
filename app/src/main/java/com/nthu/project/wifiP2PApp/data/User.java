package com.nthu.project.wifiP2PApp.data;

import android.net.wifi.p2p.WifiP2pDevice;

import com.nthu.project.wifiP2PApp.fragment.DeviceListFragment;

/**
 * Created by fongxuan on 8/29/16.
 */
public class User {
    private String userName;
    private String userStatus;
    private int userIconindex;
    private WifiP2pDevice wifiP2pDevice;




    public User(){
        userName = "none";
        userIconindex  = 1;
        userStatus="unavailible";
    }
    public User(String userName, int userIconindex, String userStatus){
        this.userName = userName;
        this.userIconindex = userIconindex;
    }


    public void setUserDevice(WifiP2pDevice wifiP2pDevice){
        this.wifiP2pDevice = wifiP2pDevice;
        this.userName = wifiP2pDevice.deviceName;
        this.userStatus = DeviceListFragment.getDeviceStatus(wifiP2pDevice.status);
    }

    public String getUserName(){
        return userName;
    }
    public int getUserIconindex(){
        return userIconindex;
    }
    public String getUserStatus(){return userStatus;}

    public void setUserName(String userName){
        this.userName = userName;
    }
    public void setUserIconIndex(int i){
        this.userIconindex = i;
    }
    public void setUserStatus(int status){
        this.userStatus = DeviceListFragment.getDeviceStatus(wifiP2pDevice.status);
    }

}
