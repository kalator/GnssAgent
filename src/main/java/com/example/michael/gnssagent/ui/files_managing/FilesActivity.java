package com.example.michael.gnssagent.ui.files_managing;

import android.support.design.widget.TabLayout;

import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.View;

import com.example.michael.gnssagent.R;
import com.example.michael.gnssagent.ui.BaseActivity;

//https://camposha.info/source/android-viewpager-sliding-tabs-listviews-source

public class FilesActivity extends BaseActivity
        implements TabLayout.OnTabSelectedListener{

    ViewPager vp;
    TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.acitivity_files_tabs);

        // disable back arrow on the action bar
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        } catch (NullPointerException e) {
            // do nothing
        }

        vp = findViewById(R.id.mViewpager_ID);
        this.addPages();

        tabLayout = findViewById(R.id.mTab_ID);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(vp);
        tabLayout.addOnTabSelectedListener(this);

    }

    private void addPages() {
        FilesPagerAdapter filesPagerAdapter = new FilesPagerAdapter(this.getSupportFragmentManager());
        filesPagerAdapter.addFragment(new AllFilesFragment());
        filesPagerAdapter.addFragment(new ObsFilesFragment());
        filesPagerAdapter.addFragment(new NavFilesFragment());
        filesPagerAdapter.addFragment(new PosFilesFragment());

        vp.setAdapter(filesPagerAdapter);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        vp.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onBackPressed() {
        finish();
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public void goToMain(View view) {
        onBackPressed();
    }
}
