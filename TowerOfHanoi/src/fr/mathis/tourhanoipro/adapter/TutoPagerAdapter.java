package fr.mathis.tourhanoipro.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import fr.mathis.tourhanoipro.fragment.TutoStepFragment;

public class TutoPagerAdapter extends FragmentStatePagerAdapter {

    public static int NB_STEPS = 3;
    
    public TutoPagerAdapter(FragmentManager fm) {
            super(fm);
    }

    @Override
    public Fragment getItem(int position) {
            return TutoStepFragment.newInstance(position);
    }

    @Override
    public int getCount() {
            return NB_STEPS;
    }
    
    @Override
    public int getItemPosition(Object object) {
            return -1;
    }

}