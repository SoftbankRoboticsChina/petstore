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
import android.util.Log;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.object.conversation.BaseChatbot;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.ReplyPriority;
import com.aldebaran.qi.sdk.object.conversation.StandardReplyReaction;
import com.aldebaran.qi.sdk.object.locale.Locale;

import cn.softbankrobotics.petstore.R;
import cn.softbankrobotics.petstore.launcher.chat.IFlytekNlpReaction;
import cn.softbankrobotics.petstore.common.DirectSayReaction;
import cn.softbankrobotics.petstore.launcher.chat.LauncherChatbotReaction;

/**
 * The chatbot of main.https://qisdk.softbankrobotics.com/sdk/doc/pepper-sdk/ch4_api/conversation/reference/basechatbot.html
 */
public class LauncherChatbot extends BaseChatbot {

    private static final String TAG = "LauncherChatbot";
    private QiContext qiContent;
    private Context mContext;

    public LauncherChatbot(QiContext qiContent, Context context) {
        super(qiContent);
        this.qiContent = qiContent;
        mContext = context;
    }

    @Override
    public StandardReplyReaction replyTo(Phrase phrase, Locale locale) {
        if (phrase != null) {
            String text = phrase.getText();
            if (text.isEmpty()) {
                Log.i(TAG, "answer empty: ");
                DirectSayReaction emptyReac = new DirectSayReaction(qiContent, this.qiContent.getResources().getString(R.string.pey_say_app));
                return new StandardReplyReaction(emptyReac, ReplyPriority.FALLBACK);
            } else if (text.equals(mContext.getResources().getString(R.string.take_photo))) {
                LauncherChatbotReaction mainReaction = new LauncherChatbotReaction(qiContent, mContext, text);
                return new StandardReplyReaction(mainReaction, ReplyPriority.FALLBACK);
            } else if (text.contains(mContext.getResources().getString(R.string.dance))) {
                LauncherChatbotReaction mainReaction = new LauncherChatbotReaction(qiContent, mContext, text);
                return new StandardReplyReaction(mainReaction, ReplyPriority.FALLBACK);
            } else if (text.contains(mContext.getResources().getString(R.string.pet_store_home))) {
                LauncherChatbotReaction mainReaction = new LauncherChatbotReaction(qiContent, mContext, text);
                return new StandardReplyReaction(mainReaction, ReplyPriority.FALLBACK);
            } else {
                Log.i(TAG, "nuance could asr string is :" + text);
                IFlytekNlpReaction iFlytekNlpReaction = new IFlytekNlpReaction(qiContent, text);
                return new StandardReplyReaction(iFlytekNlpReaction, ReplyPriority.NORMAL);
            }
        } else {
            Log.w(TAG, "answer empty: ");
            DirectSayReaction emptyReac = new DirectSayReaction(qiContent, this.qiContent.getResources().getString(R.string.pey_say_app));
            return new StandardReplyReaction(emptyReac, ReplyPriority.FALLBACK);
        }
    }

    @Override
    public void acknowledgeHeard(Phrase phrase, Locale locale) {
        Log.i(TAG, "Last phrase heard by the robot and whose chosen answer is not mine: " + phrase.getText());
    }

    @Override
    public void acknowledgeSaid(Phrase phrase, Locale locale) {
        Log.i(TAG, "Another chatbot answered: " + phrase.getText());
    }

}
