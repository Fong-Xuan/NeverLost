package com.nthu.project.wifiP2PApp.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.nthu.project.wifiP2PApp.R;
import com.nthu.project.wifiP2PApp.WiFiDirectActivity;

/**
 * Created by fongxuan on 9/3/16.
 */
public class EditProfileDialog extends DialogFragment {
    private WiFiDirectActivity activity;
    private  ImageView imv_userIcon;
    private TextView tv_userOldName;
    public static EditProfileDialog newInstance(String userName){
        EditProfileDialog fragment = new EditProfileDialog();
        Bundle bundle = new Bundle();
        bundle.putString("Name", userName);
        fragment.setArguments(bundle);
        return fragment;
    }

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
        final View dialogView = inflater.inflate(R.layout.dialog_edit_profile,null);

        //user image
        imv_userIcon = (ImageView) dialogView.findViewById(
                R.id.edit_dialog_userIcon);
        imv_userIcon.setOnClickListener(new EditDialogOnClickListener());
        int no = WiFiDirectActivity.getLocalUser().getUserIconindex();
        switch(no){
            case 1:
                imv_userIcon.setImageResource(R.mipmap.icon1);
                break;
            case 2:
                imv_userIcon.setImageResource(R.mipmap.icon2);
                break;
            case 3:
                imv_userIcon.setImageResource(R.mipmap.icon3);
                break;
            case 4:
                imv_userIcon.setImageResource(R.mipmap.icon4);
                break;
            case 5:
                imv_userIcon.setImageResource(R.mipmap.icon5);
                break;
            case 6:
                imv_userIcon.setImageResource(R.mipmap.icon6);
                break;
        }

        //current user name
        tv_userOldName = (TextView) dialogView.findViewById(
                R.id.edit_dialog_oldName_text2);
        tv_userOldName.setText(WiFiDirectActivity.getLocalUser().getUserName());


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(dialogView)
                // Add action buttons
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
//                        LayoutInflater factory = LayoutInflater.from(getActivity());
                        TextView tv_userName = (TextView)dialogView.findViewById(
                                R.id.edit_dialog_newName_input);
                        String newUserName = tv_userName.getText().toString();

                        if(!newUserName.equals("")){
                            WiFiDirectActivity.getLocalUser().setUserName(newUserName);
                            Toast.makeText(getActivity(), "New User Name:"+newUserName,
                                    Toast.LENGTH_LONG).show();



                        }
                        else {
//                            Toast.makeText(getActivity(), R.string.edit_dialog_invalid_user_name,
//                                    Toast.LENGTH_LONG).show();

                        }

                        //!!Update: handle changes about name and icon
                        activity.updateLocalUser();

                    }
                })
                //add no button
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EditProfileDialog.this.getDialog().cancel();
                    }
                });

        Log.d("in dialog", "finish creating dialog");
        return builder.create();
    }

    private class EditDialogOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            if(view.getId() == R.id.edit_dialog_userIcon){
                final View items = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_edit_user_icon_choose, null);
                RadioGroup iconGroup = (RadioGroup) items.findViewById(R.id.iconGroup);

                iconGroup.setOnCheckedChangeListener(new IconChooseCheckChangedListener());


                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.edit_dialog_choose_user_icon)
                        .setView(items)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                int no = WiFiDirectActivity.getLocalUser().getUserIconindex();
                                switch(no){
                                    case 1:
                                        imv_userIcon.setImageResource(R.mipmap.icon1);
                                        break;
                                    case 2:
                                        imv_userIcon.setImageResource(R.mipmap.icon2);
                                        break;
                                    case 3:
                                        imv_userIcon.setImageResource(R.mipmap.icon3);
                                        break;
                                    case 4:
                                        imv_userIcon.setImageResource(R.mipmap.icon4);
                                        break;
                                    case 5:
                                        imv_userIcon.setImageResource(R.mipmap.icon5);
                                        break;
                                    case 6:
                                        imv_userIcon.setImageResource(R.mipmap.icon6);
                                        break;
                                }
                            }
                        })
                        .show();
            }

        }
    }

    private class IconChooseCheckChangedListener implements RadioGroup.OnCheckedChangeListener{

        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int id) {

            switch (id){
                case R.id.userIcon_choose_1:
                    WiFiDirectActivity.getLocalUser().setUserIconIndex(1);
                    break;
                case R.id.userIcon_choose_2:
                    WiFiDirectActivity.getLocalUser().setUserIconIndex(2);
                    break;
                case R.id.userIcon_choose_3:
                    WiFiDirectActivity.getLocalUser().setUserIconIndex(3);
                    break;
                case R.id.userIcon_choose_4:
                    WiFiDirectActivity.getLocalUser().setUserIconIndex(4);
                    break;
                case R.id.userIcon_choose_5:
                    WiFiDirectActivity.getLocalUser().setUserIconIndex(5);
                    break;
                case R.id.userIcon_choose_6:
                    WiFiDirectActivity.getLocalUser().setUserIconIndex(6);
                    break;
                default:
                    break;

            }
        }
    }


}