package com.example.cyberfirewall;

import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FirewallVpnService extends VpnService {

    private static final String TAG = "FirewallVPN";

    private ParcelFileDescriptor vpnInterface;

    @Override
    public int onStartCommand(android.content.Intent intent,
                              int flags,
                              int startId) {

        establishVpn();

        return START_STICKY;
    }

    private void establishVpn() {

        try {

            Builder builder = new Builder();

            builder.setSession("CyberFirewall");

            builder.addAddress("10.0.0.2", 24);

            builder.addRoute("0.0.0.0", 0);

            builder.addDnsServer("8.8.8.8");
            builder.addDnsServer("1.1.1.1");

            List<String> blockedApps = getBlockedApps();

            for (String pkg : blockedApps) {
                try {
                    builder.addDisallowedApplication(pkg);
                } catch (Exception e) {
                    Log.e(TAG,
                            "Cannot block package: " + pkg,
                            e);
                }
            }

            vpnInterface = builder.establish();

            Log.d(TAG, "VPN established");

        } catch (Exception e) {
            Log.e(TAG, "VPN setup failed", e);
        }
    }

    private List<String> getBlockedApps() {

        List<String> apps = new ArrayList<>();

        apps.add("com.facebook.katana");
        apps.add("com.instagram.android");
        apps.add("com.twitter.android");

        return apps;
    }

    @Override
    public void onDestroy() {

        try {
            if (vpnInterface != null) {
                vpnInterface.close();
            }
        } catch (IOException ignored) {
        }

        super.onDestroy();
    }
}
