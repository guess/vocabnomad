package ca.taglab.vocabnomad.olm;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import ca.taglab.vocabnomad.BaseActivity;
import ca.taglab.vocabnomad.R;

public class LearnerModelActivity extends BaseActivity {
    public static final String TAG = "LearnerModelActivity";

    ViewPager mViewPager;
    LearnerModelAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.olm_main);

        mAdapter = new LearnerModelAdapter(getSupportFragmentManager());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (getActionBar() != null) getActionBar().setSelectedNavigationItem(position);
                super.onPageSelected(position);
            }
        });
        mViewPager.setAdapter(mAdapter);

        createTabs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.olm_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {

            case R.id.menu_search:
                startActivity(new Intent(this, SearchGoalActivity.class));
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void createTabs() {
        final ActionBar actionBar = getActionBar();
        if (actionBar == null) return;
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.search_bg));
        actionBar.setTitle("Statistics");
        Resources resources = Resources.getSystem();
        if (resources != null) {
            int titleId = resources.getIdentifier("action_bar_title", "id", "android");
            TextView title = (TextView)findViewById(titleId);
            title.setTextColor(Color.parseColor("#99FFFFFF"));
        }

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

    @Override
    public void onBackPressed() {
        if (mViewPager.getCurrentItem() != 0) {
            mViewPager.setCurrentItem(0, true);
        } else {
            super.onBackPressed();
        }
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
                    fragment = new GoalsListFragment();
                    break;

                case 2:
                    fragment = new CompleteGoalsFragment();
                    break;
            }

            Log.i(TAG, "" + getPageTitle(position));

            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Overview";

                case 1:
                    return "Goals";

                case 2:
                    return "Memory";
            }

            return super.getPageTitle(position);
        }
    }

}
