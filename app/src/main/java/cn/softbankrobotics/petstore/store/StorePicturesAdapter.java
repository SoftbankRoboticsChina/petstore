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

package cn.softbankrobotics.petstore.store;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cn.softbankrobotics.petstore.R;


public class StorePicturesAdapter extends BaseAdapter {
    private Context mContext;
    private static int[] PETS_IMG = {
            R.drawable.pet1, R.drawable.pet2, R.drawable.pet3, R.drawable.pet4,
            R.drawable.pet5, R.drawable.pet6};

    public StorePicturesAdapter(Context context){
        this.mContext = context;
    }

    @Override
    public int getCount() {

        return PETS_IMG.length;
    }

    @Override
    public Object getItem(int position) {
        return PETS_IMG[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.pet_picture_item, null);
            holder.imgIcon = (ImageView) convertView.findViewById(R.id.img_picture);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.imgIcon.setImageResource(PETS_IMG[position]);

        return convertView;
    }

    static class ViewHolder {
        ImageView imgIcon;
    }
}
