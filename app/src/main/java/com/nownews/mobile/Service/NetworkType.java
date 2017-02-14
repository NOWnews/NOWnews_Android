package com.nownews.mobile.Service;

import android.telephony.TelephonyManager;

/**
 * @author Cindy
 */

public class NetworkType {

    /**
     * unknown
     */
    private final static int NETWORK_TYPE_UNKNOWN = TelephonyManager.NETWORK_TYPE_UNKNOWN;
    /**
     * GPRS [2.5G]
     * </br> Speed: 約 100 kbps
     */
    private final static int NETWORK_TYPE_GPRS = TelephonyManager.NETWORK_TYPE_GPRS;
    /**
     * EDGE [2.75G]
     * </br> Speed: 約 50-100 kbps
     */
    private final static int NETWORK_TYPE_EDGE = TelephonyManager.NETWORK_TYPE_EDGE;
    /**
     * UMTS
     * </br> Speed: 約 400-7000 kbps
     */
    private final static int NETWORK_TYPE_UMTS = TelephonyManager.NETWORK_TYPE_UMTS;
    /**
     * CDMA: Either IS95A or IS95B [3G]
     * </br> Speed: 約 14-64 kbps
     */
    private final static int NETWORK_TYPE_CDMA = TelephonyManager.NETWORK_TYPE_CDMA;
    /**
     * EVDO revision 0
     * </br> Speed: 約 400-1000 kbps
     */
    private final static int NETWORK_TYPE_EVDO_0 = TelephonyManager.NETWORK_TYPE_EVDO_0;
    /**
     * EVDO revision A
     * </br> Speed: 約 600-1400 kbps
     */
    private final static int NETWORK_TYPE_EVDO_A = TelephonyManager.NETWORK_TYPE_EVDO_A;
    /**
     * 1xRTT
     * </br> Speed: 約 50-100 kbps
     */
    private final static int NETWORK_TYPE_1xRTT = TelephonyManager.NETWORK_TYPE_1xRTT;
    /**
     * HSDPA [3.5G]
     * </br> Speed: 約 2-14 Mbps
     */
    private final static int NETWORK_TYPE_HSDPA = TelephonyManager.NETWORK_TYPE_HSDPA;
    /**
     * HSUPA [3.75G]
     * </br> Speed: 約 1-23 Mbps
     */
    private final static int NETWORK_TYPE_HSUPA = TelephonyManager.NETWORK_TYPE_HSUPA;
    /**
     * HSPA [HSDPA+HSUPA]
     * </br> Speed: 約 700-1700 kbps
     */
    private final static int NETWORK_TYPE_HSPA = TelephonyManager.NETWORK_TYPE_HSPA;
    /**
     * iDen
     * </br> Speed: 約 25 kbps
     */
    private final static int NETWORK_TYPE_IDEN = TelephonyManager.NETWORK_TYPE_IDEN;
    /**
     * EVDO revision B
     * </br> Speed: 約 5 Mbps
     */
    private final static int NETWORK_TYPE_EVDO_B = TelephonyManager.NETWORK_TYPE_EVDO_B;
    /**
     * LTE [4G]
     * </br> Speed: 約 10+ Mbps
     */
    private final static int NETWORK_TYPE_LTE = TelephonyManager.NETWORK_TYPE_LTE;
    /**
     * eHRPD
     * </br> Speed: 約 1-2 Mbps
     */
    private final static int NETWORK_TYPE_EHRPD = TelephonyManager.NETWORK_TYPE_EHRPD;
    /**
     * HSPA+
     * </br> Speed: 約 10-20 Mbps
     */
    private final static int NETWORK_TYPE_HSPAP = TelephonyManager.NETWORK_TYPE_HSPAP;
    /**
     * GSM {@hide} [2G]
     * </br> Speed: 約 100 kbps 以下
     */
    private final static int NETWORK_TYPE_GSM = 16;

    public static String getNetworkTypeName(int aNetworkType) {
        String typeName = null;
        switch (aNetworkType) {
            case NETWORK_TYPE_UNKNOWN:
                typeName = "NETWORK_TYPE_UNKNOWN";
                break;
            case NETWORK_TYPE_GPRS:
                typeName = "NETWORK_TYPE_GPRS";
                break;
            case NETWORK_TYPE_EDGE:
                typeName = "NETWORK_TYPE_EDGE";
                break;
            case NETWORK_TYPE_UMTS:
                typeName = "NETWORK_TYPE_UMTS";
                break;
            case NETWORK_TYPE_CDMA:
                typeName = "NETWORK_TYPE_CDMA";
                break;
            case NETWORK_TYPE_EVDO_0:
                typeName = "NETWORK_TYPE_EVDO_0";
                break;
            case NETWORK_TYPE_EVDO_A:
                typeName = "NETWORK_TYPE_EVDO_A";
                break;
            case NETWORK_TYPE_1xRTT:
                typeName = "NETWORK_TYPE_1xRTT";
                break;
            case NETWORK_TYPE_HSDPA:
                typeName = "NETWORK_TYPE_HSDPA";
                break;
            case NETWORK_TYPE_HSUPA:
                typeName = "NETWORK_TYPE_HSUPA";
                break;
            case NETWORK_TYPE_HSPA:
                typeName = "NETWORK_TYPE_HSPA";
                break;
            case NETWORK_TYPE_IDEN:
                typeName = "NETWORK_TYPE_IDEN";
                break;
            case NETWORK_TYPE_EVDO_B:
                typeName = "NETWORK_TYPE_EVDO_B";
                break;
            case NETWORK_TYPE_LTE:
                typeName = "NETWORK_TYPE_LTE";
                break;
            case NETWORK_TYPE_EHRPD:
                typeName = "NETWORK_TYPE_EHRPD";
                break;
            case NETWORK_TYPE_HSPAP:
                typeName = "NETWORK_TYPE_HSPAP";
                break;
            case NETWORK_TYPE_GSM:
                typeName = "NETWORK_TYPE_GSM";
                break;
        }
        return typeName;
    }

    public static boolean isNetworkSlow(int aNetworkType) {
        switch (aNetworkType) {
            case NETWORK_TYPE_GPRS:
            case NETWORK_TYPE_EDGE:
            case NETWORK_TYPE_CDMA:
            case NETWORK_TYPE_1xRTT:
            case NETWORK_TYPE_IDEN:
                // 2G
                return true;
            case NETWORK_TYPE_UMTS:
            case NETWORK_TYPE_EVDO_0:
            case NETWORK_TYPE_EVDO_A:
            case NETWORK_TYPE_EVDO_B:
            case NETWORK_TYPE_EHRPD:
                // 3G
                return false;
            case NETWORK_TYPE_HSPAP:
            case NETWORK_TYPE_HSDPA:
            case NETWORK_TYPE_HSUPA:
            case NETWORK_TYPE_HSPA:
            case NETWORK_TYPE_LTE:
                // 4G
                return false;
            default:
                return true;
        }
    }

}
