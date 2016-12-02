package com.nthu.project.wifiP2PApp.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nthu.project.wifiP2PApp.R;
import com.nthu.project.wifiP2PApp.WiFiDirectActivity;
import com.nthu.project.wifiP2PApp.fragment.DeviceListFragment;
import com.nthu.project.wifiP2PApp.service.FileTransferService;

/**
 * Created by fongxuan on 9/20/16.
 */
public class DeviceDetailDialog extends DialogFragment implements WifiP2pManager.ConnectionInfoListener {


    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    private View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;

    private WiFiDirectActivity activity;
    private ImageView imv_userIcon;
    private TextView tv_userOldName;

    private TextView tv_address;
    private TextView tv_info;
    private TextView tv_group_owner;
    private TextView tv_group_ip;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (WiFiDirectActivity)activity;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        final LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_device_detail, null);
        device = WiFiDirectActivity.wifiP2pDevice;

        //current user name
        tv_address = (TextView) dialogView.findViewById(
                R.id.dialog_device_detail_device_address);
        tv_info = (TextView) dialogView.findViewById(
                R.id.dialog_device_detail_device_info);
        tv_group_owner = (TextView) dialogView.findViewById(
                R.id.dialog_device_detail_group_owner);
        tv_group_ip = (TextView) dialogView.findViewById(
                R.id.dialog_device_detail_group_ip);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        if (((DeviceListFragment.DeviceActionListener) getActivity()).isConnected()) {
            tv_address.setText(WiFiDirectActivity.wifiP2pDevice.deviceAddress);
            tv_info.setText(WiFiDirectActivity.wifiP2pDevice.toString());
            builder.setView(dialogView)
                    // Add action buttons

                    .setPositiveButton("DISCONNECT", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            ((DeviceListFragment.DeviceActionListener) getActivity()).disconnect();

                        }
                    })
                    .setNeutralButton("RING", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
//                                                Log.d("ClickListener", "onClick btn_ring"); //exct here? yes
                            Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
                            serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
                            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                                    info.groupOwnerAddress.getHostAddress());
                            Log.d("ClickListener", "group owner ip : " + info.groupOwnerAddress.getHostAddress().toString()); //exct here? yes
                            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
                            getActivity().startService(serviceIntent);
                        }
                    })
                    //add no button
                    .setNegativeButton("TO AP MODE", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
                            serviceIntent.setAction(FileTransferService.OPEN_AP);
                            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                                    info.groupOwnerAddress.getHostAddress());
                            serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
                            getActivity().startService(serviceIntent);
                        }
                    });
        }
        else {
            tv_address.setText(WiFiDirectActivity.wifiP2pDevice.deviceAddress);
            tv_info.setText(WiFiDirectActivity.wifiP2pDevice.toString());
            builder.setView(dialogView);


            builder.setView(dialogView)
                    // Add action buttons
                    .setPositiveButton("CONNECT", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            WifiP2pConfig config = new WifiP2pConfig();
                            config.deviceAddress = device.deviceAddress;
                            config.wps.setup = WpsInfo.PBC;
                            if(WiFiDirectActivity.isfinder)
                                config.groupOwnerIntent = 0;
                            ((DeviceListFragment.DeviceActionListener) getActivity()).connect(config);
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {

                        }
                    });
        }

        Log.d("in dialog", "finish creating dialog");
        return builder.create();
    }


    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());
        //view = (TextView) mContentView.findViewById(R.id.device_address);
        //view.setText("mac address:" + device.deviceAddress);
        //deviceAddress is null!!

        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        if (info.groupFormed && info.isGroupOwner) {
            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
                    .getString(R.string.owner_text));
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case, we enable the
            // get file button.
            mContentView.findViewById(R.id.btn_ring).setVisibility(View.VISIBLE);
            mContentView.findViewById(R.id.btn_ap_mode).setVisibility(View.VISIBLE);
            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources()
                    .getString(R.string.client_text));
        }

        // hide the connect button
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
    }

    /**
     * Updates the UI with device data
     *
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {

        //Log.d("test showDetail", "In DeviceDetail Frag showDeatails");
        if(device == null)
            Log.d("test showDetail", "device is null");
        this.device = device;
        Log.d("test showDetail", device.deviceAddress);
        Log.d("test showDetail", device.toString());
        tv_address.setText("mac address:" + device.deviceAddress);
        tv_info.setText(device.toString());
//        tv_group_ip.setText(String.valueOf(info.groupOwnerAddress));
        tv_group_owner.setText("Am I group owner? "+(device.isGroupOwner()?"Yes":"NO"));
    }
}
