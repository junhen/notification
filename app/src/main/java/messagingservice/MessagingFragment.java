/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package messagingservice;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.leui.notification.test.R;

import xx.util.NewWindowUtil;

/**
 * The listener_notifications_main fragment that shows the buttons and the text xx.view containing the log.
 */
public class MessagingFragment extends Fragment implements View.OnClickListener, NewWindowUtil.Callback {

    private static final String TAG = MessagingFragment.class.getSimpleName();
    private String SEDN_MESSAGE_NOTIFICATION = "sendMessageNotification";

    private Button mSendSingleConversation;
    private Button mSendTwoConversations;
    private Button mSendConversationWithThreeMessages;
    private TextView mDataPortView;
    private Button mClearLogButton;

    private Messenger mService;
    private boolean mBound;

    private final ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mService = new Messenger(service);
            mBound = true;
            setButtonsState(true);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            mBound = false;
            setButtonsState(false);
        }
    };

    private final SharedPreferences.OnSharedPreferenceChangeListener listener =
            new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if (MessageLogger.LOG_KEY.equals(key)) {
                        mDataPortView.setText(MessageLogger.getAllMessages(getActivity()));
                    }
                }
            };

    public MessagingFragment() {
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.e(TAG, "onCreateView");
        View rootView = inflater.inflate(R.layout.fragment_message_me, container, false);

        mSendSingleConversation = (Button) rootView.findViewById(R.id.send_1_conversation);
        mSendSingleConversation.setOnClickListener(this);

        mSendTwoConversations = (Button) rootView.findViewById(R.id.send_2_conversations);
        mSendTwoConversations.setOnClickListener(this);

        mSendConversationWithThreeMessages = (Button) rootView.findViewById(R.id.send_1_conversation_3_messages);
        mSendConversationWithThreeMessages.setOnClickListener(this);

        mDataPortView = (TextView) rootView.findViewById(R.id.data_port);
        mDataPortView.setMovementMethod(new ScrollingMovementMethod());

        mClearLogButton = (Button) rootView.findViewById(R.id.clear);
        mClearLogButton.setOnClickListener(this);

        setButtonsState(false);
        //增加一个悬浮窗，可以在锁屏下和下拉状态栏的时候显示
        //HelperUtil.addFloatView(getContext(), mSendSingleConversation);
        NewWindowUtil.addOverlay(getActivity());
        NewWindowUtil.getInstance().addFloatView(getActivity(), SEDN_MESSAGE_NOTIFICATION, this);

        return rootView;
    }

    @Override
    public void onClick(View view) {
        if (view == mSendSingleConversation) {
            sendMsg(1, 1);
        } else if (view == mSendTwoConversations) {
            sendMsg(2, 1);
        } else if (view == mSendConversationWithThreeMessages) {
            sendMsg(1, 3);
        } else if (view == mClearLogButton) {
            MessageLogger.clear(getActivity());
            mDataPortView.setText(MessageLogger.getAllMessages(getActivity()));
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.e(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        getActivity().bindService(new Intent(getActivity(), MessagingService.class), mConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStart() {
        Log.e(TAG, "onStart");
        super.onStart();
    }

    @Override
    public void onPause() {
        Log.e(TAG, "onPause");
        super.onPause();
        MessageLogger.getPrefs(getActivity()).unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onResume() {
        Log.e(TAG, "onResume");
        super.onResume();
        mDataPortView.setText(MessageLogger.getAllMessages(getActivity()));
        MessageLogger.getPrefs(getActivity()).registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onStop() {
        Log.e(TAG, "onStop");
        super.onStop();
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
        //HelperUtil.removeFloatView();
        NewWindowUtil.getInstance().removeFloatView(SEDN_MESSAGE_NOTIFICATION);
        if (mBound) {
            getActivity().unbindService(mConnection);
            mBound = false;
        }
        super.onDestroy();
    }

    private void sendMsg(int howManyConversations, int messagesPerConversation) {
        if (mBound) {
            Message msg = Message.obtain(null, MessagingService.MSG_SEND_NOTIFICATION,
                    howManyConversations, messagesPerConversation);
            try {
                mService.send(msg);
            } catch (RemoteException e) {
                Log.e(TAG, "Error sending a message", e);
                MessageLogger.logMessage(getActivity(), "Error occurred while sending a message.");
            }
        }
    }

    private void setButtonsState(boolean enable) {
        mSendSingleConversation.setEnabled(enable);
        mSendTwoConversations.setEnabled(enable);
        mSendConversationWithThreeMessages.setEnabled(enable);
    }

    @Override
    public void onAddView() {
    }

    @Override
    public void onRemoveView() {
    }

    @Override
    public void onClickView() {
        mSendSingleConversation.performClick();
    }
}
