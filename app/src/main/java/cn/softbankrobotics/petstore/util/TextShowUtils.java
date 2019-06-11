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

package cn.softbankrobotics.petstore.util;

import android.util.Log;
import android.widget.TextView;
/**
 * Byte run.
 */
public class TextShowUtils {
    private static final String TAG = "TextShowUtils";
    private static TextView tv;
    private static String words;
    private static int length;
    private static long showPeriod;
    private static int startIndex = 0;
    private static int mCurrentShowWordsLength;

    public TextShowUtils(TextView tv, String words, long showPeriod) {
        this.tv = tv;
        this.words = words;
        this.showPeriod = showPeriod;
        this.length = words.length();
        startTv(startIndex);
    }


    public static void startTv(final int startIndex) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // 截取要填充的字符串
                    final String stv = words.substring(0, startIndex);
                    tv.post(new Runnable() {
                        @Override
                        public void run() {
                            tv.setText(stv);
                        }
                    });
                    Thread.sleep(showPeriod);

                    mCurrentShowWordsLength = startIndex + 1;
                    if (mCurrentShowWordsLength <= length) {
                        startTv(mCurrentShowWordsLength);
                    }

                } catch (InterruptedException e) {
                    Log.e(TAG,"Error during startTv " + e);
                }
            }
        }
        ).start();
    }
}
