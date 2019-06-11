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

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The manager of preference.
 */
public class PreferencesManager {
    private static final String TAG = "PreferencesManager";
    private static PreferencesManager mInstance;
    private final SharedPreferences mPref;
    private final String USER_GENDER_SAVE = "userGender";
    private final String USER_AGE_SAVE = "userAge";

    private PreferencesManager(Context context) {
        mPref = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }

    public static synchronized void initializeInstance(Context context) {
        if (mInstance == null) {
            mInstance = new PreferencesManager(context);
        }
    }

    public static synchronized PreferencesManager getInstance() {
        if (mInstance == null) {
            throw new IllegalStateException(PreferencesManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return mInstance;
    }

    // User gender.
    public String getUserGender() {
        return mPref.getString(USER_GENDER_SAVE, "");
    }

    public void setUserGender(String userId) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(USER_GENDER_SAVE, userId);
        editor.commit();
    }


    // User age.
    public int getUserAge() {
        return mPref.getInt(USER_AGE_SAVE, 0);
    }

    public void setUserAge(int userAge) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putInt(USER_AGE_SAVE, userAge);
        editor.commit();
    }

    /**
     * 保存List。
     *
     * @param tag
     * @param dataList
     */
    public <T> void setDataList(String tag, List<T> dataList) {
        SharedPreferences.Editor editor = mPref.edit();
        if (null == dataList || dataList.size() <= 0)
            return;

        Gson gson = new Gson();
        // 转换成json数据，再保存
        String strJson = gson.toJson(dataList);
        editor.clear();
        editor.putString(tag, strJson);
        editor.commit();

    }

    /**
     * 获取List。
     *
     * @param tag
     * @return
     */
    public <T> List<T> getDataList(String tag, Class<T[]> clazz) {
        List<T> dataList = new ArrayList<T>();
        String strJson = mPref.getString(tag, null);
        if (null == strJson) {
            return dataList;
        }

        T[] arr = new Gson().fromJson(strJson, clazz);
        return Arrays.asList(arr);

    }
}
