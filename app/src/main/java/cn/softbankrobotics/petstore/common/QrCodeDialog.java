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
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.WriterException;

import cn.softbankrobotics.petstore.R;
import cn.softbankrobotics.petstore.util.QRCodeUtils;
import cn.softbankrobotics.petstore.util.TextShowUtils;

public class QrCodeDialog {
    private static final String TAG = "QrCodeDialog";

    // 支付对话框。
    public static void showPetPayDialog(Activity activity,int imgPetIndex) {
        final Dialog dialog = new Dialog(activity, R.style.Theme_Light_Dialog);
        dialog.setCanceledOnTouchOutside(false);
        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_pet_pay, null);

        // 创建window，设置window位置参数。
        Window window = dialog.getWindow();
        window.setGravity(Gravity.CENTER);
        window.setWindowAnimations(R.style.dialogStyle);
        window.getDecorView().setPadding(0, 0, 0, 0);

        // 设置dialog大小属性。
        android.view.WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = window.getWindowManager().getDefaultDisplay().getWidth() / 2;
        lp.height = window.getWindowManager().getDefaultDisplay().getWidth() / 2;
        window.setAttributes(lp);
        dialog.setContentView(dialogView);

        ImageView imgQrCode = dialog.findViewById(R.id.img_qr_code);
        TextView txtQrCodeGuide = dialog.findViewById(R.id.txt_qr_code_guide);

        dialog.show();

        // 以字符跳动效果显示"请扫描二维码支付定金，然后去后台挑选萌宠吧."
        new TextShowUtils(txtQrCodeGuide, activity.getResources().getString(R.string.pet_pay_guide), 100);

        // 生成二维码。
        createQrCode(imgQrCode,imgPetIndex);

        new Handler().postDelayed(() -> {
            dialog.dismiss();
            activity.finish();
        }, 5000);
    }

    // 创建二维码。
    private static void createQrCode(ImageView imgQrCode,int imgPetIndex) {
        try {
            //根据输入的文本生成对应的二维码并且显示出来。
            Bitmap bitmap = QRCodeUtils.createQRCode(String.valueOf(imgPetIndex), 500);
            if (bitmap != null) {
                // 显示二维码。
                imgQrCode.setImageBitmap(bitmap);
            }
        } catch (WriterException e) {
            Log.e(TAG, "Error during createQrCode WriterException " + e);
        }
    }
}
