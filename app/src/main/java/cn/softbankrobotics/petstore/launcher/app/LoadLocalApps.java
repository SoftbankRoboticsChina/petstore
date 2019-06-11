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

package cn.softbankrobotics.petstore.launcher.app;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.softbankrobotics.petstore.common.Constants;
import cn.softbankrobotics.petstore.common.PreferencesManager;

public class LoadLocalApps {
    private static final String TAG = "LoadLocalApps";
    private Context context;
    public LoadLocalApps(Context context) {
        this.context = context;
    }

    // 加载本地安装应用。
    public List<AppsInfo> loadLocalApps() {
        List<AppsInfo> apps = new ArrayList<AppsInfo>();

        // 获取本地应用信息。
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> appsOfLauncher = context.getPackageManager().queryIntentActivities(mainIntent, 0);

        // 遍历获取本地应用获得需要的信息。
        for (ResolveInfo appInfo : appsOfLauncher) {
            String packageName = appInfo.activityInfo.packageName;
            AppsInfo tmpInfos = new AppsInfo();
            tmpInfos.packageName = packageName;
            tmpInfos.mainActivity = appInfo.activityInfo.name;
            Log.i(TAG, tmpInfos.packageName);

            // 过滤android原生应用与google应用。
            if (tmpInfos.packageName.contains(Constants.MAIN_GOOGLE)
                    || tmpInfos.packageName.contains(Constants.MAIN_ANDROID))
                continue;

            // 过滤没有mainActivity的本地应用。
            if ((tmpInfos.mainActivity != null)
                    && (!tmpInfos.mainActivity.contains(Constants.MAIN_ACTIVITY)))
                continue;

            Log.i(TAG, tmpInfos.packageName);
            apps.add(tmpInfos);
        }

        // 存储本地app信息。#这里存储icon会有问题，会导致存储失败。
        PreferencesManager.getInstance().setDataList(Constants.LOCAL_APPS, apps);
        return apps;
    }
}
