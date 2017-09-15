package xx.view.animation.utiles;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.view.Window;

import com.leui.notification.test.R;

public class ViewControlActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static final String TAG = "ViewControlActivity";
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private String[] mAdaptors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().requestFeature(Window.FEATURE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_add3_danimation);

        mNavigationDrawerFragment = (NavigationDrawerFragment)getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
        mAdaptors = getResources().getStringArray(R.array.drawerListAdapters);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Log.d(TAG, "xinx  onNavigationDrawerItemSelected    position = "+position);
        // update the main content by replacing fragments
        getFragmentManager().beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        /*switch (number) {
            case 1:
                mTitle = mAdaptors[0];
                break;
            case 2:
                mTitle = mAdaptors[1];
                break;
            case 3:
                mTitle = mAdaptors[2];
                break;
            default:
                mTitle = "大于3条的部分， "+number;
        }*/
        mTitle = mAdaptors[number - 1];
        restoreActionBar();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d(TAG,"XINX   dispatchTouchEvent   ev = "+ev);
        boolean result = super.dispatchTouchEvent(ev);
        Log.d(TAG,"XINX   dispatchTouchEvent   ev = "+ev+",   result = "+result);
        return result;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG,"XINX   onTouchEvent   ev = "+event);
        boolean result = super.onTouchEvent(event);
        Log.d(TAG,"XINX   onTouchEvent   ev = "+event+",   result = "+result);
        return result;
    }

    /**
     * A placeholder fragment containing a simple xx.view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        //the index of listview
        private static int mSectionNumber;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            mSectionNumber = sectionNumber;
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView;
            switch (mSectionNumber) {
                case 1 :
                    rootView= inflater.inflate(R.layout.view_animation_3d_ratote, container, false);
                    break;
                case 2 :
                    rootView= inflater.inflate(R.layout.view_control_activity_net, container, false);
                    break;
                case 3 :
                    rootView= inflater.inflate(R.layout.touch_view_test, container, false);
                    break;
                case 4 :
                    rootView= inflater.inflate(R.layout.multil_thread_view, container, false);
                    break;
                case 5 :
                    rootView= inflater.inflate(R.layout.test_view, container, false);
                    break;
                default:
                    rootView= inflater.inflate(R.layout.fragment_view_add3_danimation, container, false);
            }
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((ViewControlActivity) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }

    }

}
