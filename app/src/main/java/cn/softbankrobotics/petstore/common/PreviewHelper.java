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

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.softbankrobotics.transam.connection.api.RobotConnectionConfigration;
import com.softbankrobotics.transam.video.preview.PreviewService;
import com.softbankrobotics.transam.video.preview.PreviewServiceBuild;

/**
 * The public of various.
 */
public enum PreviewHelper {
    INSTANCE;

    private PreviewService previewService;

    // 设置preview服务。
    public void setPreviewService(PreviewService previewService) {
        this.previewService = previewService;
    }

    // 获取preview服务。
    public PreviewService getPreviewService(Activity mActivity, ProgressBar mProgressBar, ImageView mIvPlayer) {
        initPreviewService(mActivity, mProgressBar, mIvPlayer);
        return previewService;
    }

    // 初始化preview服务。
    private synchronized void initPreviewService(Activity mActivity, ProgressBar mProgressBar, ImageView mIvPlayer) {
        boolean isStart = false;
        if (previewService == null) {
            // 此处账号密码为机器人的SSH账号和密码(使用本demon时要输入对应的机器人的SSH账号和密码)。
            RobotConnectionConfigration robotConnectionConfigration = new RobotConnectionConfigration("foo","bar");
            previewService = new PreviewServiceBuild().setConnection(robotConnectionConfigration).build();
            isStart = true;
        }
        startListener(mActivity, mProgressBar, mIvPlayer, isStart);
    }

    // 监听preview。
    private void startListener(Activity mActivity, ProgressBar mProgressBar, ImageView mIvPlayer, boolean isStart) {
        if (!isStart) {
            mActivity.runOnUiThread(() -> mProgressBar.setVisibility(View.GONE));
            previewService.startPreview(mActivity, mIvPlayer);
        } else {
            previewService.init(mActivity);
            previewService.addListener(() -> {
                mActivity.runOnUiThread(() -> mProgressBar.setVisibility(View.GONE));
                previewService.startPreview(mActivity, mIvPlayer);
            });
        }
    }

}
