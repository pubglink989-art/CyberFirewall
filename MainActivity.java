package com.example.cyberfirewall;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int VPN_REQUEST_CODE = 1001;
    private static final int OVERLAY_REQUEST_CODE = 1002;

    private TextView statusText;
    private Button startVpnBtn;
    private Button overlayBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.txtStatus);
        startVpnBtn = findViewById(R.id.btnStartVpn);
        overlayBtn = findViewById(R.id.btnOverlay);

        overlayBtn.setOnClickListener(v -> checkOverlayPermission());

        startVpnBtn.setOnClickListener(v -> startVpnFlow());

        updateStatus();
    }

    private void checkOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (!Settings.canDrawOverlays(this)) {

                Intent intent = new Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName())
                );

                startActivityForResult(intent, OVERLAY_REQUEST_CODE);

            } else {
                statusText.setText("[+] Overlay permission granted");
            }
        }
    }

    private void startVpnFlow() {

        Intent intent = VpnService.prepare(this);

        if (intent != null) {
            startActivityForResult(intent, VPN_REQUEST_CODE);
        } else {
            onActivityResult(VPN_REQUEST_CODE, Activity.RESULT_OK, null);
        }
    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VPN_REQUEST_CODE &&
                resultCode == RESULT_OK) {

            Intent vpnIntent =
                    new Intent(this, FirewallVpnService.class);

            startService(vpnIntent);

            statusText.setText("[+] Firewall VPN Started");
        }

        if (requestCode == OVERLAY_REQUEST_CODE) {
            updateStatus();
        }
    }

    private void updateStatus() {

        boolean overlayGranted =
                Build.VERSION.SDK_INT < Build.VERSION_CODES.M
                        || Settings.canDrawOverlays(this);

        statusText.setText(
                overlayGranted
                        ? "[+] Overlay Permission Granted"
                        : "[-] Overlay Permission Missing"
        );
    }
}
