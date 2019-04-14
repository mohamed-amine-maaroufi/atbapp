package com.atb.appbankatb.Signin;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PageAdapter extends FragmentPagerAdapter {

    private int numOfTabs;

    PageAdapter(FragmentManager fm, int numOfTabs) {
        super(fm);
        this.numOfTabs = numOfTabs;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = new SigninEmailFragment();
        switch (position) {
            case 0:
                //return new SigninEmailFragment();
                fragment = new  SigninEmailFragment();
                break;

            case 1:
                //return new SigninFingerprintFragment();
                fragment =  new SigninFingerprintFragment();
                break;
            default:
                fragment =  null;
        }
        return fragment;
    }

    @Override
    public int getCount() {
        return numOfTabs;
    }
}