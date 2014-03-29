package ca.taglab.vocabnomad.olm;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.Toast;

import ca.taglab.vocabnomad.R;
import ca.taglab.vocabnomad.auth.UserManager;
import ca.taglab.vocabnomad.db.DatabaseHelper;
import ca.taglab.vocabnomad.db.UserEvents;

public class LearnerModelActivity extends FragmentActivity {
    public static final String TAG = "LearnerModelActivity";

    ViewPager mViewPager;
    LearnerModelAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.olm_main);

        // TODO: Comment this out when you are not using this as first activity
        try {
            DatabaseHelper.getInstance(getApplicationContext()).open();
        } catch(Exception e) {
            Log.e(TAG, "An error occurred when opening the VocabNomad database");
        }
        UserEvents.init(this);
        UserManager.login(this);


        mAdapter = new LearnerModelAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                getActionBar().setSelectedNavigationItem(position);
                super.onPageSelected(position);
            }
        });
        mViewPager.setAdapter(mAdapter);

        createTabs();
    }


    private void createTabs() {
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create a tab listener that is called when the user changes tabs.
        ActionBar.TabListener tabListener = new ActionBar.TabListener() {
            @Override
            public void onTabSelected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {
                mViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

            }

            @Override
            public void onTabReselected(ActionBar.Tab tab, android.app.FragmentTransaction fragmentTransaction) {

            }
        };

        for (int i = 0; i < mAdapter.getCount(); i++) {
            actionBar.addTab(actionBar.newTab()
                            .setText(mAdapter.getPageTitle(i))
                            .setTabListener(tabListener)
            );
        }
    }


    public void selected() {
        Toast.makeText(this, "Helllloooo", Toast.LENGTH_SHORT).show();
    }


    class LearnerModelAdapter extends FragmentStatePagerAdapter {

        public LearnerModelAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;

            switch (position) {
                case 0:
                    fragment = new OverviewFragment();
                    break;

                case 1:
                    //fragment = new OverviewFragment();
                    break;

                case 2:
                    //fragment = new OverviewFragment();
                    break;
            }

            Log.i(TAG, "" + getPageTitle(position));

            return fragment;
        }

        @Override
        public int getCount() {
            return 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Overview";

                case 1:
                    return "Goals";

                case 2:
                    return "Completed";
            }

            return super.getPageTitle(position);
        }
    }

}
