package com.terramaster;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.terramaster.fragment.BaseFragment;

import java.util.Stack;

public class BaseFragmentActivity extends BaseActivity {
    private Stack<BaseFragment> mStacks = new Stack<>();

    public void pushFragment(BaseFragment fragment, boolean shouldAnimate, boolean shouldAdd) {
        if (shouldAdd)
            mStacks.push(fragment);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        if (shouldAnimate)
            ft.setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left);
        ft.replace(R.id.flContent, fragment);
        ft.commit();
    }

    public BaseFragment lastElement() {
        return mStacks.lastElement();
    }

    public void popFragments() {
    /*
	 * Select the second last fragment in current tab's stack.. which will
	 * be shown after the fragment transaction given below
	 */
        BaseFragment fragment = mStacks.elementAt(mStacks.size() - 2);

	/* pop current fragment from stack.. */
        mStacks.pop();

	/*
	 * We have the target fragment in hand.. Just show it.. Show a standard
	 * navigation animation
	 */
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        ft.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        ft.replace(R.id.flContent, fragment);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        if (((BaseFragment) mStacks.lastElement()).onBackPressed()) {
            // onBackPressed is managed by fragment
        } else {
	    /* Goto previous fragment in navigation stack of this tab */
            if (mStacks.size() <= 1) {
                // We are already showing first fragment of current tab, so when
                // back pressed, we will finish this activity..
                startActivity(new Intent(this, HomeActivity.class));
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
                return;
            }
            popFragments();
        }

    }
}
