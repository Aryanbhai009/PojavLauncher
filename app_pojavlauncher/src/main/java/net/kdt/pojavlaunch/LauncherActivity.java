package net.kdt.pojavlaunch;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import net.kdt.pojavlaunch.extra.ExtraConstants;
import net.kdt.pojavlaunch.extra.ExtraCore;
import net.kdt.pojavlaunch.progresskeeper.ProgressKeeper;
import net.kdt.pojavlaunch.progresskeeper.ProgressLayout;
import net.kdt.pojavlaunch.tasks.AsyncVersionList;
import net.kdt.pojavlaunch.utils.IconCacheJanitor;
import net.kdt.pojavlaunch.modloaders.ModloaderInstallTracker;
import net.kdt.pojavlaunch.value.LauncherPreferences;

public class LauncherActivity extends BaseActivity {

    private ActivityResultLauncher<String> mRequestNotificationPermissionLauncher;
    private Runnable mRequestNotificationPermissionRunnable;
    private NotificationManager mNotificationManager;
    private ModloaderInstallTracker mInstallTracker;

    private View mSettingsButton;
    private View.OnClickListener mSettingButtonListener;
    private ExtraCore.ExtraListener mBackPressedListener;
    private ExtraCore.ExtraListener mSelectAuthMethodListener;
    private ExtraCore.ExtraListener mLaunchGameListener;
    private ProgressLayout mProgressLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pojav_launcher);

        FragmentManager fragmentManager = getSupportFragmentManager();
        if(fragmentManager.getBackStackEntryCount() < 1) {
            fragmentManager.beginTransaction()
                    .setReorderingAllowed(true)
                    .addToBackStack("ROOT")
                    .add(R.id.container_fragment, MainMenuFragment.class, null, "ROOT").commit();
        }

        IconCacheJanitor.runJanitor();
        mRequestNotificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isAllowed -> {
                    if(!isAllowed) handleNoNotificationPermission();
                    else {
                        Runnable runnable = Tools.getWeakReference(mRequestNotificationPermissionRunnable);
                        if(runnable != null) runnable.run();
                    }
                }
        );

        getWindow().setBackgroundDrawable(null);
        bindViews();
        checkNotificationPermission();
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Bypassed UI Listeners safely
        // if (mSettingsButton != null && mSettingButtonListener != null) {
        //     mSettingsButton.setOnClickListener(mSettingButtonListener);
        // }

        ExtraCore.addExtraListener(ExtraConstants.BACK_PREFERENCE, mBackPressedListener);
        ExtraCore.addExtraListener(ExtraConstants.SELECT_AUTH_METHOD, mSelectAuthMethodListener);
        ExtraCore.addExtraListener(ExtraConstants.LAUNCH_GAME, mLaunchGameListener);

        new AsyncVersionList().getVersionList(versions -> ExtraCore.setValue(ExtraConstants.RELEASE_TABLE, versions), false);

        mInstallTracker = new ModloaderInstallTracker(this);

        // Bypassed progress layout safely
        // if (mProgressLayout != null) {
        //     mProgressLayout.observe(ProgressLayout.DOWNLOAD_MINECRAFT);
        //     mProgressLayout.observe(ProgressLayout.UNPACK_RUNTIME);
        //     mProgressLayout.observe(ProgressLayout.INSTALL_MODPACK);
        //     mProgressLayout.observe(ProgressLayout.AUTHENTICATE_MICROSOFT);
        //     mProgressLayout.observe(ProgressLayout.DOWNLOAD_VERSION_LIST);
        // }
    }

    private void bindViews() {
        mSettingsButton = findViewById(R.id.setting_button);
        mProgressLayout = findViewById(R.id.progress_layout);
    }

    private void checkNotificationPermission() {
    }

    private void handleNoNotificationPermission() {
    }

    @Override
    protected void onResume() {
        super.onResume();
        ContextExecutor.setActivity(this);
        if(mInstallTracker != null) mInstallTracker.attach();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ContextExecutor.clearActivity();
        if(mInstallTracker != null) mInstallTracker.detach();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mProgressLayout != null) {
            ProgressKeeper.removeTaskCountListener(mProgressLayout);
        }
    }
}
