/*
 * Copyright [2019] [SoftBank Robotics China Corp]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.softbankrobotics.petstore.launcher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.human.Human;

import java.util.ArrayList;
import java.util.List;

import cn.softbankrobotics.petstore.R;
import cn.softbankrobotics.petstore.common.Constants;
import cn.softbankrobotics.petstore.common.PreferencesManager;
import cn.softbankrobotics.petstore.launcher.app.AppsInfo;
import cn.softbankrobotics.petstore.launcher.app.LoadLocalApps;
import cn.softbankrobotics.petstore.launcher.app.LocalAppsHandle;
import cn.softbankrobotics.petstore.launcher.chat.LauncherChatbot;
import cn.softbankrobotics.petstore.photo.TakePhotoActivity;
import cn.softbankrobotics.petstore.store.PetActivity;

/**
 * The main activity.
 */
public class LauncherActivity extends RobotActivity implements View.OnClickListener, RobotLifecycleCallbacks {
    private static final String TAG = "LauncherActivity";
    private TextView mTxtPets, mTxtDance, mTxtTakePhoto;
    private Chat mChat;
    private QiContext mQiContext;
    private Say mSay;
    private LocalAppsHandle mHandlerLocalApps;
    private LoadLocalApps mLoadLocalApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // QiSDK注册。
        QiSDK.register(this, this);

        //初始化view。
        initView();

        // 加载本地安装应用。
        loadLocalApps();
    }

    private void initView() {
        // 绑定宠物、跳舞和拍照TextView控件。
        mTxtPets = findViewById(R.id.txt_pets);
        mTxtDance = findViewById(R.id.txt_dance);
        mTxtTakePhoto = findViewById(R.id.txt_take_photo);

        // 设置宠物、跳舞和拍照点击监听。
        mTxtPets.setOnClickListener(this);
        mTxtDance.setOnClickListener(this);
        mTxtTakePhoto.setOnClickListener(this);
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        mQiContext = qiContext;
        // 获取pepper焦点，实现对话等人机交互。
        handleRobotGainEvent(qiContext);
    }

    @Override
    public void onRobotFocusLost() {
        mQiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.i(TAG, Constants.ROBOTIC_REFUSED + reason);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.txt_pets:
                // 启动宠物之家界面。
                startActivity(new Intent(LauncherActivity.this, PetActivity.class));
                break;
            case R.id.txt_dance:
                // 启动舞蹈。
                startDanceApp();
                break;
            case R.id.txt_take_photo:
                // 启动拍照。
                startActivity(new Intent(LauncherActivity.this, TakePhotoActivity.class));
                break;
        }
    }

    // 加载本地安装应用。
    private void loadLocalApps() {
        // 加载本地应用。
        mLoadLocalApps = new LoadLocalApps(this);
        mLoadLocalApps.loadLocalApps();

        // 初始化本地应用处理。
        mHandlerLocalApps = new LocalAppsHandle(this);
    }

    // 读取Human中年龄，性别信息并保存。https://qisdk.softbankrobotics.com/sdk/doc/pepper-sdk/ch4_api/perception/reference/engage.html#engagehuman
    private void retrieveHumanParams(Human engagedHuman) {
        String call = "";
        int age = 0;
        if (engagedHuman != null) {
            if (engagedHuman.getEstimatedGender() != null)
                call = engagedHuman.getEstimatedGender().toString();
            age = engagedHuman.getEstimatedAge().getYears();

            // 转换称呼。
            if (call.equals(Constants.HUMAN_MALE)) {
                call = getResources().getString(R.string.sir);
            } else if (call.equals(Constants.HUMAN_FEMALE)) {
                call = getResources().getString(R.string.lady);
            }

            // 存储称呼与年龄。
            PreferencesManager.getInstance().setUserGender(call);
            PreferencesManager.getInstance().setUserAge(age);
        }
    }

    // 点击启动舞动应用。
    private void startDanceApp() {
        if (mHandlerLocalApps != null) {
            AppsInfo appsInfo = mHandlerLocalApps.getAppInfo(getResources().getString(R.string.category_dance));
            mHandlerLocalApps.startApp(appsInfo);
        } else {
            Toast.makeText(this, getResources().getString(R.string.category_apps_not_install), Toast.LENGTH_SHORT).show();
        }
    }

    // 基于BaseChatbot的Chat实现的人机交互。
    private void handleRobotGainEvent(QiContext qiContext) {

        mSay = SayBuilder.with(qiContext)
                .withText(getResources().getString(R.string.main_guide))
                .build();
        mSay.async().run().andThenConsume(consume -> {
            // 创建讯飞Chatbot。#除了接入第三方语义理解，还可以用com.aldebaran.qi.sdk.object.conversation.QiChatbot。
            LauncherChatbot iFlytekChatbot = new LauncherChatbot(qiContext, this);

            // 创建chat。
            mChat = ChatBuilder.with(qiContext)
                    .withChatbot(iFlytekChatbot)
                    .build();
            mChat.async().run();
        });

        // welcomeActivity页面时间较短pepper"看到"的人信息不一定获取的到，此处持续获取看到人的信息并存储。
        qiContext.getHumanAwareness().addOnEngagedHumanChangedListener(engagedHuman -> {
            retrieveHumanParams(engagedHuman);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // QiSDK注销。
        QiSDK.unregister(this, this);
        mQiContext = null;
    }
}
