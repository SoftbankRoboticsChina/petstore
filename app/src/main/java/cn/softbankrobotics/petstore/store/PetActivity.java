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

package cn.softbankrobotics.petstore.store;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.QiChatbot;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.human.Human;

import java.util.List;

import cn.softbankrobotics.petstore.R;
import cn.softbankrobotics.petstore.common.CommonChat;
import cn.softbankrobotics.petstore.common.QrCodeDialog;
import cn.softbankrobotics.petstore.common.Constants;
import cn.softbankrobotics.petstore.common.PreferencesManager;
import cn.softbankrobotics.petstore.store.database.Pets;
import cn.softbankrobotics.petstore.store.database.PetsDao;
import cn.softbankrobotics.petstore.store.ui.StoreWheel;

/**
 * The activity for pet store. 把购物和支付抽取为独立的类
 */
public class PetActivity extends RobotActivity implements RobotLifecycleCallbacks {
    private static final String TAG = "PetActivity";
    private StoreWheel mWheel;
    private ImageView mImgPet;
    private TextView mLikeChoice;
    private Chat mChat;
    private int mImgPetIndex;
    private PetsDao mPetDao;
    private QiContext mQiContext;
    private boolean isSayNoLike = false;
    private GridView mPetPictureGridView;
    private Say mSay;
    private StorePicturesAdapter mPictureAdapter;
    private Future<Void> mChatFuture;
    private CommonChat mCommonChat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pet);
        // 注册QiSDK。
        QiSDK.register(this, this);

        // 初始化宠物本地数据存储。
        initPetDao();

        // 初始化view。
        initView();

        // 初始化chat。
        mCommonChat = new CommonChat();
    }

    private void initView() {
        // 绑定布局中的控件。
        mImgPet = findViewById(R.id.img_pet);
        mLikeChoice = findViewById(R.id.txt_like_choice);
        mPetPictureGridView = findViewById(R.id.gv_picture_list);

        // 设置一个Drawable为透明。
        mPetPictureGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));

        // 初始化宠物之家页宠物缩略图的适配器。
        mPictureAdapter = new StorePicturesAdapter(this);
        mPetPictureGridView.setAdapter(mPictureAdapter);
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        mQiContext = qiContext;
        handlerRobotGainEvent(qiContext);
    }

    @Override
    public void onRobotFocusLost() {
        mQiContext = null;
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.i(TAG, Constants.ROBOTIC_REFUSED + reason);
    }

    private void initPetDao() {
        mPetDao = new PetsDao(this);
        if (!mPetDao.isDataExist()) {
            mPetDao.initTable();
        }
    }

    // 根据用户年龄，性别条件推荐相应的宠物。
    private void recommendLikePets() {
        // 根据用户年龄，性别条件给出宠物图片引所。
        mImgPetIndex = getRecommendPets();

        mWheel = new StoreWheel(new StoreWheel.WheelListener() {
            @Override
            public void newImage(final int img, final int index) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (index < 5) {
                            // 轮转显示的宠物图片。
                            mImgPet.setImageResource(img);
                            isSayNoLike = true;
                        } else {
                            // 显示选中的宠物图片。
                            mImgPet.setImageResource(StoreWheel.PETS_IMG[mImgPetIndex]);
                            mLikeChoice.setVisibility(View.VISIBLE);
                            if (isSayNoLike) {
                                // 推荐结束时，给出的语音提示。
                                new Thread(() -> {
                                    // 重启人机对话以及监听事件处理。
                                    restartReaction();
                                }).start();
                                isSayNoLike = false;
                            }
                        }
                    }
                });
            }
        }, 200, 150, System.currentTimeMillis());
        // 开始宠物轮播。
        mWheel.start();
    }

    // 重启人机对话以及对听到事件的处理。
    private void restartReaction() {
        // 轮播结束后，paper要说的话。
        String words = getWords();

        mSay = SayBuilder.with(mQiContext)
                .withText(words)
                .build();
        mSay.async().run().andThenConsume(consume -> {
            // 创建chat。
            mChat = mCommonChat.createChat(mQiContext,R.raw.pet);

            // 添加chat的heard监听。
            mChat.addOnHeardListener(heardPhrase -> {
                reactionHeardListener(heardPhrase);
            });

            mChatFuture = mChat.async().run();

            mChatFuture.thenConsume(future -> {
                if (future.hasError()) {
                    String message = "finished with error.";
                    Log.e(TAG, message, future.getError());
                } else if (future.isSuccess()) {
                    Log.i(TAG, "run iflytekChatbot successful");
                } else if (future.isDone()) {
                    Log.i(TAG, "run iflytekChatbot isDone");
                }
            });
        });
    }


    // 人机活动pepper听到的监听。
    private void reactionHeardListener(Phrase heardPhrase) {
        String word = heardPhrase.getText();
        if (word.equals(getResources().getString(R.string.pet_like))) {
            handleHeardListener(0, 6000);
        } else if (word.equals(getResources().getString(R.string.pet_no_like))) {
            handleHeardListener(1, 5000);
        } else if (word.equals(getResources().getString(R.string.back))) {
            handleHeardListener(2, 4000);
        }
    }

    // 处理人机活动pepper听到的监听。
    private void handleHeardListener(final int type, long postTime) {
        Looper.prepare();
        new Handler().postDelayed(() -> {
            mChatFuture.cancel(true);
            runOnUiThread(() -> {
                if (type == 0) {
                    // 显示支付dialog。
                    QrCodeDialog.showPetPayDialog(this, mImgPetIndex);
                } else if (type == 1) {
                    // pepper听到不喜欢的时候，开始重新轮转。
                    mWheel.startWheel();
                    mLikeChoice.setVisibility(View.GONE);
                } else if (type == 2) {
                    // 退出当前页面，返回上一页面。
                    finish();
                }
            });
        }, postTime);
        Looper.loop();
    }

    // 根据本地存储pepper"看到"人的年龄与性别推荐其喜欢的宠物。
    private int getRecommendPets() {
        int resultIndex = 5;
        int age = PreferencesManager.getInstance().getUserAge();
        String userGender = PreferencesManager.getInstance().getUserGender();
        List<Pets> petsList = mPetDao.getAllData();

        if (petsList != null) {
            for (int i = 0; i < petsList.size(); i++) {

                int startAge = petsList.get(i).getUserAgeStart();
                int endAge = petsList.get(i).getUserAgeEnd();
                String gender = petsList.get(i).getUserGender();

                // 查询符合性别与年龄的宠物。
                if ((age > startAge) && (age < endAge)
                        && userGender.equals(gender)) {
                    resultIndex = i;
                    break;
                }
            }
        }
        // 得到推荐的宠物引索。
        return resultIndex;
    }

    // 欢迎、推荐宠物。
    private void handlerRobotGainEvent(QiContext qiContext) {
        mSay = SayBuilder.with(qiContext)
                .withText(getResources().getString(R.string.pet_guide))
                .build();

        mSay.async().run().andThenConsume((consume) -> {
            runOnUiThread(() -> {
                mImgPet.setVisibility(View.VISIBLE);
                mPetPictureGridView.setVisibility(View.GONE);
            });

            // 推荐给pepper"看到的人"可能喜欢的宠物。
            recommendLikePets();
        });

        // pepper"看到"的人的特征监听及其存储。
        qiContext.getHumanAwareness().addOnEngagedHumanChangedListener(engagedHuman -> {
            retrieveHumanParams(engagedHuman);
        });
    }

    // 读取Human中年龄，性别信息并保存。
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

            // 存储pepper"看到"人的性别跟年龄。
            PreferencesManager.getInstance().setUserGender(call);
            PreferencesManager.getInstance().setUserAge(age);
        }
    }

    // 轮播结束后，paper要说的话。
    private String getWords() {
        // 获取存储在本地的pepper"看到的"人的性别和年龄。
        String call = PreferencesManager.getInstance().getUserGender();
        int age = PreferencesManager.getInstance().getUserAge();
        String ageString = "";
        if (age > 90) {
            ageString = 90 + getResources().getString(R.string.pet_recommend_age);
        } else if (age > 80) {
            ageString = 80 + getResources().getString(R.string.pet_recommend_age);
        } else if (age > 70) {
            ageString = 70 + getResources().getString(R.string.pet_recommend_age);
        } else if (age > 60) {
            ageString = 60 + getResources().getString(R.string.pet_recommend_age);
        } else if (age > 50) {
            ageString = 50 + getResources().getString(R.string.pet_recommend_age);
        } else if (age > 20) {
            ageString = 40 + getResources().getString(R.string.pet_recommend_age);
        } else if (age > 20) {
            ageString = 30 + getResources().getString(R.string.pet_recommend_age);
        } else if (age > 20) {
            ageString = 20 + getResources().getString(R.string.pet_recommend_age);
        } else if (age > 10) {
            ageString = 10 + getResources().getString(R.string.pet_recommend_age);
            if (call.equals(getResources().getString(R.string.lady))) {
                call = getResources().getString(R.string.pet_recommend_girl);
            } else if (call.equals(getResources().getString(R.string.lady))) {
                call = getResources().getString(R.string.pet_recommend_boy);
            }
        } else if (age > 0) {
            ageString = getResources().getString(R.string.pet_recommend_age_low_ten);
            call = getResources().getString(R.string.pet_recommend_child);
        } else {
            ageString = getResources().getString(R.string.pet_recommend_u);
            call = "";
        }
        String words = getResources().getString(R.string.pet_recommend) + ageString + call
                + getResources().getString(R.string.pet_guide_like);
        return words;
    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
        if (mWheel != null) {
            // 停止宠物轮播。
            mWheel.stopWheel();
        }
    }
}
