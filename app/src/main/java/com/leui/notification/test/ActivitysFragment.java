package com.leui.notification.test;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import xx.activity.RecorderVideoActivity;
import xx.activity.RecorderVideoActivityTwo;
import xx.activity.SocketBindActivity;
import xx.activity.SurfaceViewActivity;
import xx.activity.TestActivityLife;
import xx.activity.TextReaderActivity;
import xx.activity.VectorDrawableActivity;
import xx.view.animation.utiles.ViewControlActivity;
import javacodetest.TestSyncFullscreenActivity;
import notificationlistener.NotificationListenerActivity;
import service.ServerSocketService;
import sockettest.SocketTestActivity;
import xx.game.lunar.LunarGameActivity;
import xx.json.parser.JsonParserActivity;
import xx.widget.WidgetActivity;
import xx.xml.parser.XmlParserActivity;

/**
 * A fragment representing a list of Items.
 */
public class ActivitysFragment extends Fragment implements View.OnClickListener {

    // TODO: Customize parameter argument names
    private static final String ARG_COLUMN_COUNT = "column-count";
    // TODO: Customize parameters
    private int mColumnCount = 1;
    private Button mListenerNotificationButton;
    private Button mTestSyncButton;
    private Button mViewAnimationButton;
    private Button mSocketTestButton;
    private Button mTestActivityLife;
    private Button mServerTestServiceStart;
    private Button mServerTestServiceStop;
    private Button mServerTestServiceBind;
    private Button mWidgetActivityBn;
    private Button mVectorDrawableActivityBn;
    private Button mSurfaceViewActivityBn;
    private Button mXmlParserActivityBn;
    private Button json_parser_activity_bn;
    private Button mJunarGameActivityBn;
    private Button mTextReaderActivityBn;
    //private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ActivitysFragment() {
    }

    // TODO: Customize parameter initialization
    @SuppressWarnings("unused")
    public static ActivitysFragment newInstance(int columnCount) {
        ActivitysFragment fragment = new ActivitysFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_listener_notifications, container, false);

        mListenerNotificationButton = (Button) view.findViewById(R.id.listener_notification);
        mListenerNotificationButton.setOnClickListener(this);
        mTestSyncButton = (Button) view.findViewById(R.id.test_sync);
        mTestSyncButton.setOnClickListener(this);
        mViewAnimationButton = (Button) view.findViewById(R.id.view_animation);
        mViewAnimationButton.setOnClickListener(this);
        mSocketTestButton = (Button) view.findViewById(R.id.socket_test);
        mSocketTestButton.setOnClickListener(this);
        mTestActivityLife = (Button) view.findViewById(R.id.test_activity_file);
        mTestActivityLife.setOnClickListener(this);
        mServerTestServiceStart = (Button) view.findViewById(R.id.server_test_service_start);
        mServerTestServiceStart.setOnClickListener(this);
        mServerTestServiceStop = (Button) view.findViewById(R.id.server_test_service_stop);
        mServerTestServiceStop.setOnClickListener(this);
        mServerTestServiceBind = (Button) view.findViewById(R.id.server_test_service_bind);
        mServerTestServiceBind.setOnClickListener(this);
        mWidgetActivityBn = (Button) view.findViewById(R.id.widget_activity_bn);
        mWidgetActivityBn.setOnClickListener(this);
        mVectorDrawableActivityBn = (Button) view.findViewById(R.id.vector_drawable_activity_bn);
        mVectorDrawableActivityBn.setOnClickListener(this);
        mSurfaceViewActivityBn = (Button) view.findViewById(R.id.surfaceview_activity_bn);
        mSurfaceViewActivityBn.setOnClickListener(this);
        mXmlParserActivityBn = (Button) view.findViewById(R.id.xml_parser_activity_bn);
        mXmlParserActivityBn.setOnClickListener(this);
        json_parser_activity_bn = (Button) view.findViewById(R.id.json_parser_activity_bn);
        json_parser_activity_bn.setOnClickListener(this);
        mJunarGameActivityBn = (Button) view.findViewById(R.id.lunar_game_activity);
        mJunarGameActivityBn.setOnClickListener(this);
        mTextReaderActivityBn = (Button) view.findViewById(R.id.text_reader_activity);
        mTextReaderActivityBn.setOnClickListener(this);
        view.findViewById(R.id.recorder_video_activity).setOnClickListener(this);
        view.findViewById(R.id.recorder_video_activity_two).setOnClickListener(this);

        return view;
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.listener_notification :
                Intent notificationlistenerIntent = new Intent(getActivity(), NotificationListenerActivity.class);
                getActivity().startActivity(notificationlistenerIntent);
                break;
            case R.id.test_sync :
                Intent testsyncIntent = new Intent(getActivity(), TestSyncFullscreenActivity.class);
                testsyncIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                getActivity().startActivity(testsyncIntent);
                break;
            case R.id.view_animation :
                Intent viewanimationIntent = new Intent(getActivity(), ViewControlActivity.class);
                getActivity().startActivity(viewanimationIntent);
                break;
            case R.id.socket_test :
                Intent socketTestIntent = new Intent(getActivity(), SocketTestActivity.class);
                getActivity().startActivity(socketTestIntent);
                break;
            case R.id.test_activity_file :
                Intent testActivityLife = new Intent(getActivity(), TestActivityLife.class);
                getActivity().startActivity(testActivityLife);
                break;
            case R.id.server_test_service_start :
                Intent serverTestServiceStart = new Intent(getActivity(), ServerSocketService.class);
                getActivity().startService(serverTestServiceStart);
                break;
            case R.id.server_test_service_stop :
                Intent serverTestServiceStop = new Intent(getActivity(), ServerSocketService.class);
                getActivity().stopService(serverTestServiceStop);
                break;
            case R.id.server_test_service_bind :
                Intent serverTestServiceBind = new Intent(getActivity(), SocketBindActivity.class);
                getActivity().startActivity(serverTestServiceBind);
                break;
            case R.id.widget_activity_bn :
                Intent widgetactivitybn = new Intent(getActivity(), WidgetActivity.class);
                getActivity().startActivity(widgetactivitybn);
                break;
            case R.id.vector_drawable_activity_bn :
                Intent vectorDrawableActivity = new Intent(getActivity(), VectorDrawableActivity.class);
                getActivity().startActivity(vectorDrawableActivity);
                break;
            case R.id.surfaceview_activity_bn :
                Intent surfaceViewActivity = new Intent(getActivity(), SurfaceViewActivity.class);
                getActivity().startActivity(surfaceViewActivity);
                break;
            case R.id.xml_parser_activity_bn :
                Intent xmlParserActivity = new Intent(getActivity(), XmlParserActivity.class);
                getActivity().startActivity(xmlParserActivity);
                break;
            case R.id.json_parser_activity_bn :
                Intent jsonParserActivity = new Intent(getActivity(), JsonParserActivity.class);
                getActivity().startActivity(jsonParserActivity);
                break;
            case R.id.lunar_game_activity :
                Intent lunarGameActivity = new Intent(getActivity(), LunarGameActivity.class);
                getActivity().startActivity(lunarGameActivity);
                break;
            case R.id.text_reader_activity :
                Intent textReaderActivity = new Intent(getActivity(), TextReaderActivity.class);
                getActivity().startActivity(textReaderActivity);
                break;
            case R.id.recorder_video_activity :
                Intent recorderVideoActivity = new Intent(getActivity(), RecorderVideoActivity.class);
                getActivity().startActivity(recorderVideoActivity);
                break;
            case R.id.recorder_video_activity_two :
                Intent recorderVideoTwoActivity = new Intent(getActivity(), RecorderVideoActivityTwo.class);
                getActivity().startActivity(recorderVideoTwoActivity);
                break;



        }
    }
}
