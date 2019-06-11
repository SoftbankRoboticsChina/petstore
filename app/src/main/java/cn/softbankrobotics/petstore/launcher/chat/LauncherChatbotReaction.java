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

package cn.softbankrobotics.petstore.launcher.chat;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.aldebaran.qi.Consumer;
import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.object.conversation.BaseChatbotReaction;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.conversation.SpeechEngine;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import cn.softbankrobotics.petstore.R;
import cn.softbankrobotics.petstore.common.Constants;
import cn.softbankrobotics.petstore.launcher.app.AppsInfo;
import cn.softbankrobotics.petstore.launcher.app.LocalAppsHandle;
import cn.softbankrobotics.petstore.photo.TakePhotoActivity;
import cn.softbankrobotics.petstore.store.PetActivity;

/**
 *  ChatbotReaction of Launcher。https://qisdk.softbankrobotics.com/sdk/doc/pepper-sdk/ch4_api/conversation/reference/basechatbotreaction.html
 */
public class LauncherChatbotReaction extends BaseChatbotReaction {
    private static final String TAG = "LauncherChatbotReaction";
    private Future<Void> fSay;
    private QiContext qiContext;
    private Context context;
    private String voiceCommand;
    private int type;
    private LocalAppsHandle localAppsHandle;

    public LauncherChatbotReaction(QiContext qiContext, Context context, String answer) {
        super(qiContext);
        this.qiContext = qiContext;
        this.context = context;
        this.voiceCommand = answer;
    }

    @Override
    public void runWith(SpeechEngine speechEngine) {
        String words = "";
        String petHome = context.getResources().getString(R.string.pet_store_home);
        String dance = context.getResources().getString(R.string.dance);
        String takePhoto = context.getResources().getString(R.string.take_photo);

        // 处理answer。
        if (voiceCommand.contains(petHome)) {
            type = Constants.MAIN_START_TYPE_PET;
            words = context.getResources().getString(R.string.main_pet_home_start);
        } else if (voiceCommand.contains(dance)) {
            type = Constants.MAIN_START_TYPE_DANCE;
            words = context.getResources().getString(R.string.main_pet_dance_start);
        } else if (voiceCommand.contains(takePhoto)) {
            type = Constants.MAIN_START_TYPE_PHOTO;
            words = context.getResources().getString(R.string.main_pet_photo_start);
        }
        Say say = SayBuilder.with(speechEngine)
                .withText(words)
                .build();
        fSay = say.async().run().andThenConsume(new Consumer<Void>() {
            @Override
            public void consume(Void aVoid) throws Throwable {
                if (type == Constants.MAIN_START_TYPE_PET) {
                    // 启动宠物之家页面。
                    context.startActivity(new Intent(context, PetActivity.class));
                } else if (type == Constants.MAIN_START_TYPE_DANCE) {
                    localAppsHandle = new LocalAppsHandle(context);
                    AppsInfo appsInfo = localAppsHandle.getAppInfo(voiceCommand);
                    localAppsHandle.startApp(appsInfo);
                } else if (type == Constants.MAIN_START_TYPE_PHOTO) {
                    // 启动拍照页面。
                    context.startActivity(new Intent(context, TakePhotoActivity.class));
                }
            }
        });

        try {
            fSay.get();
        } catch (ExecutionException e) {
            Log.e(TAG, "Error during Say", e);
        } catch (CancellationException e) {
            Log.i(TAG, "Interruption during Say" + e);
        }
    }

    @Override
    public void stop() {
        if (fSay != null) {
            fSay.cancel(true);
        }
    }
}
