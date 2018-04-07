package com.fa.grubot;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.fa.grubot.fragments.BaseFragment;
import com.fa.grubot.fragments.ChatsListFragment;
import com.fa.grubot.fragments.DashboardFragment;
import com.fa.grubot.fragments.ProfileFragment;
import com.fa.grubot.fragments.SettingsFragment;
import com.fa.grubot.fragments.WorkInProgressFragment;
import com.fa.grubot.objects.users.VkUser;
import com.github.badoualy.telegram.tl.api.TLUser;
import com.ncapdevi.fragnav.FragNavController;
import com.ncapdevi.fragnav.tabhistory.FragNavTabHistoryController;
import com.roughike.bottombar.BottomBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import icepick.Icepick;
import io.reactivex.annotations.Nullable;

public class MainActivity extends AppCompatActivity implements BaseFragment.FragmentNavigation, FragNavController.TransactionListener, FragNavController.RootFragmentListener {

    @Nullable @BindView(R.id.bottom_navigation) BottomBar bottomNavigationView;

    private final int TAB_SEARCH = FragNavController.TAB1;
    private final int TAB_PROFILE = FragNavController.TAB2;
    private final int TAB_DASHBOARD = FragNavController.TAB3;
    private final int TAB_CHATS = FragNavController.TAB4;
    private final int TAB_SETTINGS = FragNavController.TAB5;

    private FragNavController navController;

    private TLUser currentUser;
    private VkUser currentVkUser;

    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Icepick.restoreInstanceState(this, savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setupViews(savedInstanceState);
        if (App.INSTANCE.getCurrentUser().hasTelegramUser()) {
            currentUser = App.INSTANCE.getCurrentUser().getTelegramUser();
        }
        if (App.INSTANCE.getCurrentUser().hasVkUser()) {
            currentVkUser = App.INSTANCE.getCurrentUser().getVkUser();
        }

        if (currentUser != null) {
            Toast.makeText(this, "Welcome back " + currentUser.getFirstName() + " " + currentUser.getLastName(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Icepick.saveInstanceState(this, outState);
        if (navController != null) {
            navController.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onDestroy() {
        App.INSTANCE.closeTelegramClient();
        super.onDestroy();
    }

    private void setupViews(Bundle savedInstanceState) {
        navController = FragNavController.newBuilder(savedInstanceState, getSupportFragmentManager(), R.id.content)
                .transactionListener(this)
                .rootFragmentListener(this, 5)
                .popStrategy(FragNavTabHistoryController.UNIQUE_TAB_HISTORY)
                .switchController((index, transactionOptions) -> bottomNavigationView.selectTabAtPosition(index))
                .build();

        bottomNavigationView.setOnTabSelectListener(tabId -> {
            if (!App.INSTANCE.isBackstackEnabled())
                navController.clearStack();

            switch (tabId) {
                case R.id.tab_search:
                    navController.switchTab(TAB_SEARCH);
                    break;
                case R.id.tab_profile:
                    navController.switchTab(TAB_PROFILE);
                    break;
                case R.id.tab_dashboard:
                    navController.switchTab(TAB_DASHBOARD);
                    break;
                case R.id.tab_chats:
                    navController.switchTab(TAB_CHATS);
                    break;
                case R.id.tab_settings:
                    navController.switchTab(TAB_SETTINGS);
                    break;
            }
        });

        bottomNavigationView.setOnTabReselectListener(tabId -> {
            navController.clearStack();
        });

        if (savedInstanceState == null) {
            bottomNavigationView.selectTabAtPosition(2);
        }
    }

    @Override
    public void onBackPressed() {
        if (navController.getCurrentStack().size() == 1 && navController.isRootFragment()) {
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                super.onBackPressed();
                return;
            } else {
                Toast.makeText(getBaseContext(), "Нажмите еще раз для выхода", Toast.LENGTH_SHORT).show();
            }
            mBackPressed = System.currentTimeMillis();
        } else {
            navController.popFragment();
        }
    }

    @Override
    public void pushFragment(Fragment fragment) {
        if (navController != null) {
            navController.pushFragment(fragment);
        }
    }

    @Override
    public void onTabTransaction(Fragment fragment, int index) {
        // If we have a backstack, show the back button
        if (getSupportActionBar() != null && navController != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!navController.isRootFragment());
        }
    }

    @Override
    public void onFragmentTransaction(Fragment fragment, FragNavController.TransactionType transactionType) {
        //do fragmentty stuff. Maybe change title, I'm not going to tell you how to live your life
        // If we have a backstack, show the back button
        if (getSupportActionBar() != null && navController != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(!navController.isRootFragment());
        }
    }

    @Override
    public Fragment getRootFragment(int index) {
        switch (index) {
            case TAB_SEARCH:
                return new WorkInProgressFragment();
            case TAB_PROFILE:
                return ProfileFragment.getInstanse();
            case TAB_DASHBOARD:
                return DashboardFragment.newInstance(0);
            case TAB_CHATS:
                return ChatsListFragment.newInstance(0);
            case TAB_SETTINGS:
                return new SettingsFragment();
        }
        throw new IllegalStateException("Need to send an index that we know");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                navController.popFragment();
                break;
        }
        return true;
    }
}
