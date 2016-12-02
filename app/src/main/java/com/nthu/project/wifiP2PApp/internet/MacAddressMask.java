package com.nthu.project.wifiP2PApp.internet;

/**
 * Created by ander on 2016/10/2.
 */

public class MacAddressMask {

    public static String maskLastTwoBits(String address)
    {
        char[] maskedAddr = address.toCharArray();
        int firstByte = Character.getNumericValue(maskedAddr[1]);
        int maskedByte = firstByte & (~3);
        char maskedChar = Integer.toHexString(maskedByte).charAt(0);
        maskedAddr[1] = maskedChar;
        String target = new String(maskedAddr);
        return target;
    }

}
