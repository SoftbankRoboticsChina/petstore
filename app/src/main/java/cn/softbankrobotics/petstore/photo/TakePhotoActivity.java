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

package cn.softbankrobotics.petstore.photo;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aldebaran.qi.Future;
import com.aldebaran.qi.sdk.Qi;
import com.aldebaran.qi.sdk.QiContext;
import com.aldebaran.qi.sdk.QiSDK;
import com.aldebaran.qi.sdk.RobotLifecycleCallbacks;
import com.aldebaran.qi.sdk.builder.SayBuilder;
import com.aldebaran.qi.sdk.builder.TakePictureBuilder;
import com.aldebaran.qi.sdk.design.activity.RobotActivity;
import com.aldebaran.qi.sdk.object.camera.TakePicture;
import com.aldebaran.qi.sdk.object.conversation.Chat;
import com.aldebaran.qi.sdk.object.conversation.Phrase;
import com.aldebaran.qi.sdk.object.conversation.Say;
import com.aldebaran.qi.sdk.object.image.EncodedImage;
import com.aldebaran.qi.sdk.object.image.EncodedImageHandle;
import com.softbankrobotics.transam.video.preview.PreviewService;

import java.nio.ByteBuffer;

import cn.softbankrobotics.petstore.R;
import cn.softbankrobotics.petstore.common.CommonChat;
import cn.softbankrobotics.petstore.common.PrinterManager;
import cn.softbankrobotics.petstore.common.PreviewHelper;

/**
 * The activity for taking photo.
 */
public class TakePhotoActivity extends RobotActivity implements RobotLifecycleCallbacks {
    private static final String TAG = "TakePhotoActivity";
    private TextView mTxtTakePhoto;
    private QiContext mQiContext;
    private ImageView mImgPicture, mImgCancel, mIvPlayer;
    private TextView mTxtPrint;
    private Bitmap mPictureBitmap;
    private Say mSay;
    private PreviewService mPreviewService;
    private RelativeLayout mLayPicture;
    private ProgressBar mProgressBar;
    private Chat mChat;
    private Future<Void> mChatFuture;
    private boolean isPrintPicture = false;
    private CommonChat mCommonChat;
    private PrinterManager mPrinterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_take_photo);
        // 注册QiSDK。
        QiSDK.register(this, this);

        // 初始化view以及监听事件。
        initView();

        // 初始化Preview。
        mPreviewService = PreviewHelper.INSTANCE.getPreviewService(this, mProgressBar, mIvPlayer);

        // 初始化chat。
        mCommonChat = new CommonChat();

        // 初始化打印管理。
        mPrinterManager = new PrinterManager();
    }

    private void initView() {
        mLayPicture = findViewById(R.id.lay_picture);
        mImgPicture = findViewById(R.id.img_picture);
        mImgCancel = findViewById(R.id.img_cancel);
        mTxtPrint = findViewById(R.id.txt_print);
        mTxtTakePhoto = findViewById(R.id.txt_take_photo);
        mIvPlayer = findViewById(R.id.iv_player);
        mProgressBar = findViewById(R.id.progressBar);

        // 拍照点击监听。
        mTxtTakePhoto.setOnClickListener(v -> {
            takePic();
        });

        // 拍出的照片的点击监听，点击后显示预览。
        mImgPicture.setOnClickListener(v -> {
            mLayPicture.setVisibility(View.GONE);
            mIvPlayer.setVisibility(View.VISIBLE);
        });

        // 取消按钮点击监听，点击后显示预览。
        mImgCancel.setOnClickListener(v -> {
            cancelPicture();
        });

        // 打印按钮点击监听。
        mTxtPrint.setOnClickListener(v -> {
            showTakePicturePrintDialog();
        });
    }

    @Override
    public void onRobotFocusGained(QiContext qiContext) {
        mQiContext = qiContext;
        // 开始进行拍照。
        String welcomeWords = getResources().getString(R.string.take_photo_guide);
        doTakePicture(welcomeWords);
    }

    @Override
    public void onRobotFocusLost() {
        this.mQiContext = null;

        if (mChat != null) {
            mChat.removeAllOnHeardListeners();
        }
    }

    @Override
    public void onRobotFocusRefused(String reason) {
        Log.i(TAG, "onRobotFocusRefused: " + reason);
    }

    // Chat监听实现打印、拍照、返回和取消功能。
    private void doTakePicture(String welcomeWords) {
        //拍照欢迎语
        mSay = SayBuilder.with(mQiContext)
                .withText(welcomeWords)
                .build();
        mSay.async().run().andThenConsume(consume -> {

            // 创建chat。
            mChat = mCommonChat.createChat(mQiContext, R.raw.photo);

            // 添加听到监听。#此处可以用bookmark这个api也可以实现。
            mChat.addOnHeardListener(heardPhrase -> {
                doTakePictureHeardListener(heardPhrase);
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
    private void doTakePictureHeardListener(Phrase heardPhrase) {
        String word = heardPhrase.getText();
        if (word.equals(getResources().getString(R.string.take_photo))
                || word.equals(getResources().getString(R.string.take_photo_eggplant))) {
            // 拍照。
            handleTakePictureHeardListener(0, 4000);
        } else if (word.equals(getResources().getString(R.string.take_photo_print))) {
            // 打印图片。
            handleTakePictureHeardListener(1, 4000);
        } else if (word.equals(getResources().getString(R.string.back))) {
            // 离开拍照页面，返回上一页面。
            handleTakePictureHeardListener(2, 4000);
        } else if (word.equals(getResources().getString(R.string.store_cancel))) {
            // 显示preview。
            handleTakePictureHeardListener(3, 3000);
        }
    }

    // 处理人机活动pepper听到的监听。
    private void handleTakePictureHeardListener(final int type, long postTime) {
        Looper.prepare();
        new Handler().postDelayed(() -> {
            if (type == 0 || type == 1) {
                mChatFuture.cancel(true);
            }
            runOnUiThread(() -> {
                if (type == 0) {
                    // 拍照。
                    takePic();
                } else if (type == 1) {
                    // 打印图片。
                    mPrinterManager.doPhotoPrint(this, mPictureBitmap);
                    isPrintPicture = true;
                    cancelPicture();
                } else if (type == 2) {
                    // 离开拍照页面，返回上一页面。
                    finish();
                } else if (type == 3) {
                    // 显示preview。
                    cancelPicture();
                }
            });
        }, postTime);
        Looper.loop();
    }

    // 利用pepper相机进行拍照并显示。https://qisdk.softbankrobotics.com/sdk/doc/pepper-sdk/ch4_api/perception/reference/takepicture.html#takepicture
    private void takePic() {
        if (mQiContext == null) {
            return;
        }

        // 点击拍照后，显示加载画面。
        runOnUiThread(() -> {
            mLayPicture.setVisibility(View.VISIBLE);
            mIvPlayer.setVisibility(View.GONE);
            mProgressBar.setVisibility(View.VISIBLE);
        });

        if (mPictureBitmap != null) {
            mPictureBitmap.recycle();
            mPictureBitmap = null;
            mImgPicture.setImageBitmap(null);
        }

        Log.i(TAG, "build take picture");
        Future<TakePicture> takePictureFuture = TakePictureBuilder.with(mQiContext).buildAsync();

        takePictureFuture.andThenCompose(Qi.onUiThread(takePicture -> {
            Log.i(TAG, "take picture launched!");
            return takePicture.async().run();
        })).andThenConsume(timestampedImageHandle -> {

            Log.i(TAG, "Picture taken");
            EncodedImageHandle encodedImageHandle = timestampedImageHandle.getImage();

            EncodedImage encodedImage = encodedImageHandle.getValue();
            Log.i(TAG, "PICTURE RECEIVED!");

            ByteBuffer buffer = encodedImage.getData();
            buffer.rewind();
            final int pictureBufferSize = buffer.remaining();
            final byte[] pictureArray = new byte[pictureBufferSize];
            buffer.get(pictureArray);

            Log.i(TAG, "PICTURE RECEIVED! (" + pictureBufferSize + " Bytes)");
            mPictureBitmap = BitmapFactory.decodeByteArray(pictureArray, 0, pictureBufferSize);

            // 显示拍的照片。
            runOnUiThread(() -> {
                mTxtPrint.setVisibility(View.VISIBLE);
                mImgCancel.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                mImgPicture.setImageBitmap(mPictureBitmap);
            });

            // 重启语音拍照。
            new Thread(() -> {
                String welcomeWords = getResources().getString(R.string.take_photo_guide_print_reset);
                doTakePicture(welcomeWords);
            }).start();
        });
    }


    // 打印图片提示对话框。
    private void showTakePicturePrintDialog() {
        final Dialog dialog = new Dialog(this, R.style.Theme_Light_Dialog);
        dialog.setCanceledOnTouchOutside(false);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_photo_print, null);

        // 创建window。
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);
        window.setWindowAnimations(R.style.dialogStyle);
        window.getDecorView().setPadding(0, 0, 0, 0);

        // window大小属性设置。
        android.view.WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = window.getWindowManager().getDefaultDisplay().getWidth() / 2;
        lp.height = window.getWindowManager().getDefaultDisplay().getWidth() / 2;
        window.setAttributes(lp);
        dialog.setContentView(dialogView);

        Button sure = dialog.findViewById(R.id.btn_sure);
        Button cancel = dialog.findViewById(R.id.btn_cancel);

        dialog.show();

        sure.setOnClickListener(v -> {
            dialog.dismiss();
            // 打印图片。
            mPrinterManager.doPhotoPrint(this, mPictureBitmap);
            isPrintPicture = true;
            cancelPicture();
        });
        cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isPrintPicture) {
            isPrintPicture = false;
            cancelPicture();
        }
    }

    private void cancelPicture() {
        mLayPicture.setVisibility(View.GONE);
        mImgCancel.setVisibility(View.GONE);
        mTxtPrint.setVisibility(View.GONE);
        mIvPlayer.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPreview(mPreviewService);
    }

    @Override
    protected void onDestroy() {
        QiSDK.unregister(this, this);
        super.onDestroy();
        mQiContext = null;
    }

    // 停止preview。
    private void stopPreview(PreviewService previewService) {
        if (previewService != null) {
            try {
                previewService.stopPreview();
            } catch (Exception e) {
                Log.e(TAG, "Exception in stopPreview");
            }
        }
    }
}
