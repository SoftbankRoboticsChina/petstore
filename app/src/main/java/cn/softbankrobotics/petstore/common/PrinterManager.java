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
import android.graphics.Bitmap;
import android.support.v4.print.PrintHelper;

import cn.softbankrobotics.petstore.R;

public class PrinterManager {
    /*
     * 打印图片。
     * 打印网页，打印常用文件请参照 https://developer.android.com/training/printing 。
     * 具体步骤如下：
     * 1）在google官网下https://developer.android.com/training/printing，根据官方文档接入接入您需要的官方打印服务，例如打印文档，图片等。
     * 2）在应用市场，搜索对应打印机的打印服务插件安装。推荐使用“Mopria PrintService”打印服务插件；“Mopria Print Service”为Mopria联盟推出的一款Android设备打印服务应用，官网为http://mopria.org/zh-cn；可以支持大部分的打印设备。
     * 3）安装完成后，系统设置 -> 打印–> 打印服务，可以看到“Mopria Print Service”，点击进入，选择打开，会自动搜索网络中的打印机。
     * */
    public void doPhotoPrint(Context context, Bitmap pictureBitmap) {
        PrintHelper photoPrinter = new PrintHelper(context);
        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FILL);
        String jobName = context.getString(R.string.app_name) + Constants.TAKE_PICTURE_JOB_NAME;
        photoPrinter.printBitmap(jobName, pictureBitmap);
    }
}
