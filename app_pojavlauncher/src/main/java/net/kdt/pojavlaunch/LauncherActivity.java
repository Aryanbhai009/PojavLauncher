package net.kdt.pojavlaunch;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.fragments.MainMenuFragment;
import net.kdt.pojavlaunch.modloaders.ModloaderInstallTracker;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.ProgressLayout;
import net.kdt.pojavlaunch.tasks.AsyncVersionList;
import net.kdt.pojavlaunch.utils.IconCacheJanitor;

public class LauncherActivity extends BaseActivity {

    public ActivityResultLauncher<String> mRequestNotificationPermissionLauncher;
    public Runnable mRequestNotificationPermissionRunnable;

    private NotificationManager mNotificationManager;
    private ModloaderInstallTracker mInstallTracker;
    private ProgressLayout mProgressLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pojav_launcher);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() < 1) {
            fragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .addToBackStack("ROOT")
                    .add(R.id.container_fragment, MainMenuFragment.class, null, "ROOT")
                    .commit();
        }

        try { IconCacheJanitor.runJanitor(); } catch (Throwable ignored) {}

        mRequestNotificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isAllowed -> {
                    if (isAllowed && mRequestNotificationPermissionRunnable != null) {
                        mRequestNotificationPermissionRunnable.run();
                        mRequestNotificationPermissionRunnable = null;
                    }
                }
        );

        getWindow().setBackgroundDrawable(null);
        bindViews();
        
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        try {
            new AsyncVersionList().getVersionList(versions -> ExtraCore.setValue(ExtraConstants.RELEASE_TABLE, versions), false);
        } catch (Throwable ignored) {}

        try { mInstallTracker = new ModloaderInstallTracker(this); } catch (Throwable ignored) {}
    }

    private void bindViews() {
        mProgressLayout = findViewById(R.id.progress_layout);
    }

    public void checkForNotificationPermission(Runnable onGranted) {
        this.mRequestNotificationPermissionRunnable = onGranted;
        if (mRequestNotificationPermissionLauncher != null) {
            try {
                mRequestNotificationPermissionLauncher.launch("android.permission.POST_NOTIFICATIONS");
            } catch (Throwable e) {
                if (onGranted != null) onGranted.run();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mInstallTracker != null) try { mInstallTracker.attach(); } catch (Throwable ignored) {}
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mInstallTracker != null) try { mInstallTracker.detach(); } catch (Throwable ignored) {}
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProgressLayout != null) {
            try { ProgressKeeper.removeTaskCountListener(mProgressLayout); } catch (Throwable ignored) {}
        }
    }
}
