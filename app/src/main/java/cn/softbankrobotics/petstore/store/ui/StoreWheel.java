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

package cn.softbankrobotics.petstore.store.ui;


import android.util.Log;

import cn.softbankrobotics.petstore.R;

/**
 * The wheel of petActivity.
 */
public class StoreWheel extends Thread {
    private static final String TAG = "StoreWheel";

    public interface WheelListener {
        void newImage(int img, int index);
    }

    public static int[] PETS_IMG = {
            R.drawable.pet1, R.drawable.pet2, R.drawable.pet3, R.drawable.pet4,
            R.drawable.pet5, R.drawable.pet6};
    private int mCurrentIndex;
    private WheelListener mWheelListener;
    private long mFrameDuration;
    private long mStartIn;
    private boolean isStarted;
    private long mStartWheelTime;
    private float mWheelStillTime = 4;
    private int mCycle = 0;

    public StoreWheel(WheelListener wheelListener, long frameDuration, long startIn, long startWheelTime) {
        this.mWheelListener = wheelListener;
        mFrameDuration = frameDuration;
        mStartIn = startIn;
        mCurrentIndex = 0;
        isStarted = true;
        mStartWheelTime = startWheelTime;
    }

    public void nextImg() {
        if (mCurrentIndex < PETS_IMG.length)
            mCurrentIndex++;

        if ((mCurrentIndex == PETS_IMG.length)) {
            mCurrentIndex = 0;
            if (mCycle < 8)
                mCycle++;
        }
    }

    @Override
    public void run() {
        try {
            Thread.sleep(mStartIn);
        } catch (InterruptedException e) {
            Log.e(TAG, "Error during wheel thread start " + e);
        }

        long timePeriod = System.currentTimeMillis() - mStartWheelTime;
        float timePeriodSecond = ((float) timePeriod / 1000);

        while (isStarted && (timePeriodSecond < mWheelStillTime)) {
            try {
                Thread.sleep(mFrameDuration);
            } catch (InterruptedException e) {
                Log.e(TAG, "Error during wheel circle " + e);
            }

            // 获取当前图片引索。
            nextImg();

            if (mWheelListener != null) {
                try {
                    Thread.sleep(mFrameDuration);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Error during picture circle " + e);
                }

                // 暴露当前显示的图片，以及图片循环的次数。
                mWheelListener.newImage(PETS_IMG[mCurrentIndex], mCycle);
            }
        }
    }

    public void stopWheel() {
        isStarted = false;
    }

    public void startWheel() {
        isStarted = true;
        mCycle = 0;
        mStartWheelTime = System.currentTimeMillis();
    }
}
