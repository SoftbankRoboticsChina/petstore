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

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cn.softbankrobotics.petstore.R;
import cn.softbankrobotics.petstore.common.Constants;
import cn.softbankrobotics.petstore.common.PreferencesManager;

/**
 * handle local apps.
 */
public class LocalAppsHandle {
    private static final String TAG = "LocalAppsHandle";
    private Context context;
    private List<AppsInfo> apps = new ArrayList<AppsInfo>();
    private Map<String, String> categoryMap = new HashMap<String, String>();
    private final String dance = "category_dance";
    private final String photo = "category_take_photo";
    private final String game = "category_game";
    private final String chat = "category_dance";
    private final String music = "category_music";

    public LocalAppsHandle(Context context) {
        if (apps != null && apps.size() > 0) {
            apps.clear();
        }
        this.context = context;
        this.apps = PreferencesManager.getInstance().getDataList(Constants.LOCAL_APPS, AppsInfo[].class);
        initCategoryMap();
    }

    private void initCategoryMap() {
        categoryMap.put(context.getResources().getString(R.string.category_dance), dance);
        categoryMap.put(context.getResources().getString(R.string.category_take_photo), photo);
        categoryMap.put(context.getResources().getString(R.string.category_game), game);
        categoryMap.put(context.getResources().getString(R.string.category_chat), chat);
        categoryMap.put(context.getResources().getString(R.string.category_music), music);
    }

    // 语音启动应用。
    public void startApp(AppsInfo appsInfo) {
        String packageName = appsInfo.packageName;
        String mainActivityName = appsInfo.mainActivity;

        if (!TextUtils.isEmpty(packageName)) {
            Intent intent = new Intent();
            if (mainActivityName != null) {
                ComponentName name = new ComponentName(packageName
                        , mainActivityName);
                intent.setComponent(name);
                context.startActivity(intent);
            } else {
                Log.i(TAG, "mainActivityName: " + mainActivityName);
            }
        } else {
            Log.i(TAG, "packageName: " + packageName);
        }
    }

    // 根据关键词检查本地是否含有某应用。
    public boolean hasCategoryApps(String words) {
        boolean hasCategoryName = false;
        for (String key : categoryMap.keySet()) {
            if (words.contains(key)) {
                hasCategoryName = true;
                Log.i(TAG, words + " hasCategoryName is: true ---" + key);
                break;
            } else {
                hasCategoryName = false;
                Log.i(TAG, words + " hasCategoryName is: false ---" + key);
            }
        }
        Log.i(TAG, words + " hasCategoryName result ==== " + hasCategoryName);
        return hasCategoryName;
    }

    // 获取应用分类名称。
    public String getCategory(String words) {
        String categoryName = "";
        for (String key : categoryMap.keySet()) {
            if (words.contains(key)) {
                categoryName = key;
            }
        }
        return categoryName;
    }

    // 获取本地安装的根据meta_data属性分类的应用引索。
    public int getCategoryAppIndex(String voiceCommand) {
        int appsCurrentIndex = 0;
        List<Integer> relevantList = new ArrayList<Integer>();
        try {
            if (apps != null && apps.size() > 0) {
                for (int i = 0; i < apps.size(); i++) {
                    String tempPkgName = apps.get(i).packageName;
                    ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(tempPkgName,
                            PackageManager.GET_META_DATA);
                    if (appInfo != null && null != appInfo.metaData) {
                        String temp = appInfo.metaData.getString(categoryMap.get(getCategory(voiceCommand)));
                        if (temp != null && temp.equals(tempPkgName)) {
                            relevantList.add(i);
                            continue;
                        }
                    } else {
                        continue;
                    }
                }

                Random random = new Random();
                int index = random.nextInt(relevantList.size());
                appsCurrentIndex = relevantList.get(index);
            }
            Log.i(TAG, "appsCurrentIndex: " + appsCurrentIndex);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Error during getCategoryAppIndex" + e);
        }
        return appsCurrentIndex;
    }

    // 根据语音获取按分类获取应用的信息。
    public AppsInfo getCategoryApp(String voiceCommand) {
        int appIndex = getCategoryAppIndex(voiceCommand);
        if (apps != null && apps.size() > appIndex) {
            AppsInfo appsInfo = apps.get(appIndex);
            return appsInfo;
        }
        return null;
    }


    // 根据语音获取相应分类的app信息。
    public AppsInfo getAppInfo(String voiceCommand) {
        AppsInfo appInfo = null;
        if (hasCategoryApps(voiceCommand)) {
            appInfo = getCategoryApp(voiceCommand);
            Log.i(TAG, voiceCommand + " hasCategoryAppName  ---" + voiceCommand);
        }
        return appInfo;
    }
}
