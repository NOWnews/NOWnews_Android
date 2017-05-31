/*******************************************************************************
 * Copyright (C) 2009-2010 eoeMobile.
 * All rights reserved.
 * http://www.eoeMobile.com/
 * <p/>
 * CHANGE LOG:
 * DATE			AUTHOR			COMMENTS
 * =============================================================================
 * 2010MAY11		Waznheng Ma		Refine for Constructor and error handler.
 *******************************************************************************/

package com.nownews.mobile.Utils;

import android.util.Log;

import com.nownews.mobile.Baselibs.Utility;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class MD5 {
    private static final String LOG_TAG = "MD5";
    private static final String ALGORITHM = "MD5";

    private static char sHexDigits[] = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static MessageDigest sDigest;

    static {
        try {
            sDigest = MessageDigest.getInstance(ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            Log.e(LOG_TAG, "Get MD5 Digest failed.");
        }
    }

    private MD5() {
    }

    final public static String encode(String source) {
        byte[] btyes = source.getBytes();
        byte[] encodedBytes = sDigest.digest(btyes);

        return Utility.hexString(encodedBytes);
    }
}
