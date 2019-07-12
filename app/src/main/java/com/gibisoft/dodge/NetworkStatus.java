package com.gibisoft.dodge;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkStatus {
    public static boolean getConnectivityStatus(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();

        if(networkInfo != null) {
            int type = networkInfo.getType();
            if(type == ConnectivityManager.TYPE_MOBILE || type == ConnectivityManager.TYPE_WIFI) return true;
        }
        return false;
    }
}
