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

package cn.softbankrobotics.petstore.store.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLiteHelper of store.
 */
public class StoreSQLiteDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "StoreSQLiteDbHelper";

    public static final String DB_NAME = "petstore.db";
    public static final int DB_VERSION = 1;
    public static final String TABLE_PETS = "pets";

    private static final String PETS_CREATE_TABLE_SQL = "create table " + TABLE_PETS + "("
            + "id integer primary key autoincrement,"
            + "petName varchar(20) not null,"
            + "petPicPath varchar(30) not null,"
            + "userGender varchar(20) not null,"
            + "userAgeStart integer not null,"
            + "userAgeEnd integer not null"
            + ");";

    public StoreSQLiteDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(PETS_CREATE_TABLE_SQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        String sql = "DROP TABLE IF EXISTS " + TABLE_PETS;
        db.execSQL(sql);
        onCreate(db);
    }
}
