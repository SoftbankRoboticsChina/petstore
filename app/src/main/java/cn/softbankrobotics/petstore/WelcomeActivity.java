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

package cn.softbankrobotics.petstore;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.design.activity.conversationstatus.SpeechBarDisplayStrategy;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.human.Human;
import com.aldebaran.qi.sdk.object.humanawareness.HumanAwareness;

import java.util.List;

import cn.softbankrobotics.petstore.common.Constants;
import cn.softbankrobotics.petstore.common.PreferencesManager;
import cn.softbankrobotics.petstore.launcher.LauncherActivity;

/**
 * The activity for the welcome.
 */
public class WelcomeActivity extends RobotActivity implements RobotLifecycleCallbacks {
    private static final String TAG = "WelcomeActivity";
    private RelativeLayout mLayWelcome;
    private Say mSay;
    private HumanAwareness mHumanAwareness;
    String call = "";
    private QiContext mQiContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // 注册QiSDK。
        QiSDK.register(this, this);
        // 设置SpeechBar的状态为OVERLAY。
        setSpeechBarDisplayStrategy(SpeechBarDisplayStrategy.OVERLAY);
        // 初始化view。
        initView();
    }

    @Override
    public void onRobotFocusGained(final QiContext qiContext) {
        mQiContent = qiContext;
        mHumanAwareness = qiContext.getHumanAwareness();

        // 获取pepper"看到"人的特征参数。
        retrieveHumanAroundParams(qiContext);

        // 对获取"看到"人打招呼问候。
        toGreet();
    }

    @Override
    public void onRobotFocusLost() {
        mSay = null;
        mQiContent = null;
        // 在当前activity失去pepper的focus，去除AllOnHumansAroundChanged的监听。
        if (mHumanAwareness != null) {
            mHumanAwareness.removeAllOnHumansAroundChangedListeners();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.i(TAG, Constants.ROBOTIC_REFUSED + reason);
    }

    private void initView() {
        // 绑定欢迎控件。
        mLayWelcome = findViewById(R.id.lay_welcome);

        // 如果用户想要跳过加载欢迎页，可以点击屏幕跳过。
        mLayWelcome.setOnClickListener(v -> {
            startLauncherActivity();
        });
    }

    // 读取Human中年龄，性别信息并保存。 https://qisdk.softbankrobotics.com/sdk/doc/pepper-sdk/ch4_api/perception/reference/human.html#human
    private void retrieveHumanAroundParams(QiContext qiContext) {
        // 获取pepper感知到周围人的信息。还可以用getEngagedHuman去获取human信息，但是获取human信息只有一个人。
        List<Human> humans = qiContext.getHumanAwareness().getHumansAround();
        if (humans != null && humans.size() > 0) {
            Human human = humans.get(0);
            Integer age = human.getEstimatedAge().getYears();
            call = human.getEstimatedGender().toString();

            // 存储年龄。 补充：getHumansAround可以获取人的表情，面部图片等信息，这里可以根据自己的实际需求做一些存储或者其他处理。
            PreferencesManager.getInstance().setUserAge(age);
        }

        // 转换称呼。
        if (call.equals(Constants.HUMAN_MALE)) {
            call = getResources().getString(R.string.sir);
        } else if (call.equals(Constants.HUMAN_FEMALE)) {
            call = getResources().getString(R.string.lady);
        }
        // 存储性别。
        PreferencesManager.getInstance().setUserGender(call);
    }

    // 问候pepper"看到"的人。https://qisdk.softbankrobotics.com/sdk/doc/pepper-sdk/ch4_api/conversation/reference/say.html#say
    private void toGreet(){
        Log.i(TAG, call + getResources().getString(R.string.say_hello));
        mSay = SayBuilder.with(mQiContent)
                .withText(call + getResources().getString(R.string.say_hello))
                .build();

        mSay.async().run().andThenConsume(aVoid -> {
            // 问候结束后，跳过欢迎页面。
            startLauncherActivity();
        });
    }

    // 启动LauncherActivity。
    private void startLauncherActivity(){
        Intent intent = new Intent(WelcomeActivity.this, LauncherActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mQiContent = null;
        // QiSDK注销。
        QiSDK.unregister(this, this);
    }
}
