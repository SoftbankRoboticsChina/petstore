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

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.softbankrobotics.petstore.R;
import cn.softbankrobotics.petstore.common.Constants;

/**
 * Dao of pets.
 */
public class PetsDao {

    private static final String TAG = "PetsDao";
    private final String[] PETS_COLUMNS = new String[]{"id", "petName", "petPicPath", "userGender", "userAgeStart", "userAgeEnd"};
    private Context context;
    private StoreSQLiteDbHelper mPetsDBHelper;

    public PetsDao(Context context) {
        this.context = context;
        mPetsDBHelper = new StoreSQLiteDbHelper(context);
    }

    // 判断表中是否有数据。
    public boolean isDataExist() {

        int count = 0;
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = mPetsDBHelper.getReadableDatabase();
            // select count(Id) from pets
            cursor = db.query(StoreSQLiteDbHelper.TABLE_PETS, new String[]{"COUNT(id)"}, null, null, null, null, null);

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            if (count > 0)
                return true;
        } catch (Exception e) {
            Log.e(TAG, "Error during isDataExist ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return false;
    }

    // 初始化数据。
    public void initTable() {

        SQLiteDatabase db = null;
        try {
            db = mPetsDBHelper.getWritableDatabase();
            db.beginTransaction();

            db.execSQL("insert into " + StoreSQLiteDbHelper.TABLE_PETS + " (id, petName, petPicPath, userGender, userAgeStart, userAgeEnd) values (1, 'pet1', 1, 'MALE', 0 , 10)");
            db.execSQL("insert into " + StoreSQLiteDbHelper.TABLE_PETS + " (id, petName, petPicPath, userGender, userAgeStart, userAgeEnd) values (2, 'pet2', 2, 'FEMALE', 11 , 20)");
            db.execSQL("insert into " + StoreSQLiteDbHelper.TABLE_PETS + " (id, petName, petPicPath, userGender, userAgeStart, userAgeEnd) values (3, 'pet3', 3, 'BOTH', 21 , 30)");
            db.execSQL("insert into " + StoreSQLiteDbHelper.TABLE_PETS + " (id, petName, petPicPath, userGender, userAgeStart, userAgeEnd) values (4, 'pet4', 4, 'MALE', 31 , 40)");
            db.execSQL("insert into " + StoreSQLiteDbHelper.TABLE_PETS + " (id, petName, petPicPath, userGender, userAgeStart, userAgeEnd) values (5, 'pet5', 5, 'FEMALE', 41 , 50)");
            db.execSQL("insert into " + StoreSQLiteDbHelper.TABLE_PETS + " (id, petName, petPicPath, userGender, userAgeStart, userAgeEnd) values (6, 'pet6', 6, 'BOTH', 0 , 60)");

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e(TAG, "Error during initTable ", e);
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }

    // 执行自定义SQL语句。
    public void execSQL(String sql) {

        SQLiteDatabase db = null;

        try {
            if (sql.contains("select")) {
                Toast.makeText(context, R.string.unableSql, Toast.LENGTH_SHORT).show();
            } else if (sql.contains("insert") || sql.contains("update") || sql.contains("delete")) {
                db = mPetsDBHelper.getWritableDatabase();
                db.beginTransaction();
                db.execSQL(sql);
                db.setTransactionSuccessful();
                Toast.makeText(context, R.string.successSql, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, R.string.errorSql, Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Error during execSQL ", e);
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }

    // 查询数据库中所有数据。
    public List<Pets> getAllData() {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            db = mPetsDBHelper.getReadableDatabase();
            // select * from pets
            cursor = db.query(StoreSQLiteDbHelper.TABLE_PETS, PETS_COLUMNS, null, null, null, null, null);

            if (cursor.getCount() > 0) {
                List<Pets> petsList = new ArrayList<Pets>(cursor.getCount());
                while (cursor.moveToNext()) {
                    petsList.add(parsePets(cursor));
                }
                return petsList;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error during getAllData ", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return null;
    }

    // 新增一条数据。
    public boolean insertDate() {
        SQLiteDatabase db = null;

        try {
            db = mPetsDBHelper.getWritableDatabase();
            db.beginTransaction();

            ContentValues contentValues = new ContentValues();
            contentValues.put(Constants.DB_PET_ID, 7);
            contentValues.put(Constants.DB_PET_NAME, "pet7");
            contentValues.put(Constants.DB_PET_PIC_PATH, R.drawable.pet6);
            contentValues.put(Constants.DB_PET_USER_GENDER, "Female");
            contentValues.put(Constants.DB_PET_USER_AGE_START, 60);
            contentValues.put(Constants.DB_PET_USER_AGE_END, 70);
            db.insertOrThrow(StoreSQLiteDbHelper.TABLE_PETS, null, contentValues);

            db.setTransactionSuccessful();
            return true;
        } catch (SQLiteConstraintException e) {
            Toast.makeText(context, context.getResources().getString(R.string.db_toast_repeat), Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "Error during insertDate ", e);
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
        return false;
    }

    // 删除一条数据  例如此处删除Id为7的数据。
    public boolean deletePet() {
        SQLiteDatabase db = null;

        try {
            db = mPetsDBHelper.getWritableDatabase();
            db.beginTransaction();

            // 删除id为7的宠物。
            db.delete(StoreSQLiteDbHelper.TABLE_PETS, "id = ?", new String[]{String.valueOf(7)});
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error during deletePet ", e);
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
        return false;
    }

    /**
     * 修改一条数据 。
     * 此处将id为6的数据的userGender修改了FEMALE。
     */
    public boolean updatePets() {
        SQLiteDatabase db = null;
        try {
            db = mPetsDBHelper.getWritableDatabase();
            db.beginTransaction();

            // update pets set userGender = FEMALE where id = 6
            ContentValues cv = new ContentValues();
            cv.put(Constants.DB_PET_USER_GENDER, Constants.HUMAN_FEMALE);
            db.update(StoreSQLiteDbHelper.TABLE_PETS,
                    cv,
                    "id = ?",
                    new String[]{String.valueOf(6)});
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error during updatePets ", e);
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }

        return false;
    }


    // 将查找到的数据转换成pet类。
    private Pets parsePets(Cursor cursor) {
        Pets pet = new Pets();
        pet.id = (cursor.getInt(cursor.getColumnIndex(Constants.DB_PET_ID)));
        pet.petName = (cursor.getString(cursor.getColumnIndex(Constants.DB_PET_NAME)));
        pet.userAgeStart = (cursor.getInt(cursor.getColumnIndex(Constants.DB_PET_USER_AGE_START)));
        pet.userAgeEnd = (cursor.getInt(cursor.getColumnIndex(Constants.DB_PET_USER_AGE_END)));
        pet.userGender = (cursor.getString(cursor.getColumnIndex(Constants.DB_PET_USER_GENDER)));
        return pet;
    }
}
