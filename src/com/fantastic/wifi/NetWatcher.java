/*package com.fantastic.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.fantastic.makcal.MyService;

public class NetWatcher extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //here, check that the network connection is available. If yes, start your service. If not, stop your service.
       ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
       NetworkInfo info = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
       
       if (info != null) {
           if (info.isConnected()) {
               //start service
               Intent startService = new Intent(context, MyService.class);
               context.startService(startService);
           }
           else {
               //stop service
               Intent stopService = new Intent(context, MyService.class);
               context.stopService(stopService);
           }
       }
    }
}*/