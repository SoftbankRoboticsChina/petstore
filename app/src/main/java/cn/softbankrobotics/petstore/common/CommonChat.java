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

package cn.softbankrobotics.petstore.common;

import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.builder.ChatBuilder;
import com.aldebaran.qi.sdk.builder.QiChatbotBuilder;
import com.aldebaran.qi.sdk.builder.TopicBuilder;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Topic;

import cn.softbankrobotics.petstore.R;

public class CommonChat {
    private static final String TAG = "CommonChat";

    // 创建chat。https://qisdk.softbankrobotics.com/sdk/doc/pepper-sdk/ch4_api/conversation/reference/chat.html
    public Chat createChat(QiContext qiContent,int resource) {
        // 创建topic。https://qisdk.softbankrobotics.com/sdk/doc/pepper-sdk/ch4_api/conversation/reference/topic_topicstatus.html
        Topic topic = TopicBuilder.with(qiContent)
                .withResource(resource)
                .build();

        // 创建QiChatbot。#这里可以根据com.aldebaran.qi.sdk.object.conversation.BaseChatbot自定义QiChatbot。
        QiChatbot qiChatbot = QiChatbotBuilder.with(qiContent)
                .withTopic(topic)
                .build();

        // Chat.withChatbot()中使用QiChatbot加载本地对话规则实现特定对话功能。
        Chat chat = ChatBuilder.with(qiContent)
                .withChatbot(qiChatbot)
                .build();
        return chat;
    }
}
