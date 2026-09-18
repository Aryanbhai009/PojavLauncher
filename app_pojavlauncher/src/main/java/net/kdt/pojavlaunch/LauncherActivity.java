package net.kdt.pojavlaunch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;

import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.fragments.MainMenuFragment;
import net.kdt.pojavlaunch.prefs.LauncherPreferences;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.ProgressLayout;

public class LauncherActivity extends BaseActivity {

    private ActivityResultLauncher<String> mRequestNotificationPermissionLauncher;
    private Runnable mRequestNotificationPermissionRunnable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pojav_launcher);

        if (getSupportFragmentManager().getBackStackEntryCount() < 1) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .addToBackStack("ROOT")
                    .add(R.id.container_fragment, MainMenuFragment.class, null, "ROOT")
                    .commit();
        }

        mRequestNotificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (mRequestNotificationPermissionRunnable != null && isGranted) {
                        mRequestNotificationPermissionRunnable.run();
                        mRequestNotificationPermissionRunnable = null;
                    }
                }
        );
    }

    public void checkForNotificationPermission(Runnable onGranted) {
        this.mRequestNotificationPermissionRunnable = onGranted;
        if (mRequestNotificationPermissionLauncher != null) {
            mRequestNotificationPermissionLauncher.launch("android.permission.POST_NOTIFICATIONS");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
