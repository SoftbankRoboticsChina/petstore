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

package cn.softbankrobotics.petstore;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.aldebaran.qi.sdk.util.IOUtils;

import org.json.JSONException;
import org.json.JSONObject;

import cn.softbankrobotics.petstore.common.PreferencesManager;

/**
 * The application of the store.
 */
public class StoreApplication extends Application {
    private static final String TAG = "StoreApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        // 初始化SharedPreferences manager。
        PreferencesManager.initializeInstance(this);

        // 检查在aiui_phone是否配置appid。
        checkAppIdAndKey();
    }

    /*
    *  检查在assets/cfg/aiui_phone文件是否配置appid。
    * */
    private void checkAppIdAndKey() {
        String params = IOUtils.fromAsset(this, "cfg/aiui_phone.cfg");
        params = params.replace("\n", "").replace("\t", "").replace(" ", "");
        Log.i(TAG, "NlpApplication  params " + params);
        try {
            JSONObject paramsJSonObject = new JSONObject(params);
            String appId = paramsJSonObject.getJSONObject("login").getString("appid");
            if (TextUtils.isEmpty(appId)) {
                Toast.makeText(this, getResources().getString(R.string.check_appid_key), Toast.LENGTH_SHORT).show();
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error during check appid " + e);
        }
    }
}
