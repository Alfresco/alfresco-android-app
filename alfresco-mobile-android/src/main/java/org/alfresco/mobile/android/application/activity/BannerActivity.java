package org.alfresco.mobile.android.application.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.alfresco.mobile.android.application.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BannerActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_banner);
        Button btnRedirect = findViewById(R.id.btn_redirect);
        TextView labelDismiss = findViewById(R.id.label_dismiss);
        TextView contentDownloadApp = findViewById(R.id.tv_download_app);

        if (isAppExpired())
            contentDownloadApp.setText(getString(R.string.download_app_text_expire));
        else contentDownloadApp.setText(getString(R.string.download_app_text));


        btnRedirect.setOnClickListener(view -> {
            final String appPackageNameDebug = "com.alfresco.content.app.debug"; // package name of the app
            final String appPackageName = "com.alfresco.content.app"; // package name of the app
            PackageManager pm = getPackageManager();
            boolean isInstalled = isPackageInstalled(appPackageNameDebug, pm);
            if (isInstalled) {
                startActivity(getPackageManager().getLaunchIntentForPackage(appPackageNameDebug));
            } else {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (ActivityNotFoundException exception) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.alfresco.content.app")));
                }
            }
        });

        labelDismiss.setOnClickListener(view -> {
            finish();
        });
    }

    private boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean isAppExpired() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date strDate = null;
        try {
            strDate = sdf.parse("1/12/2022");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return !(new Date().before(strDate));
    }
}