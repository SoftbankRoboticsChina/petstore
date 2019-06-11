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

/**
 * Bean of pet.
 */
public class Pets {
    public int id;
    public String petName;
    public String petPicPath;
    public String userGender;
    public int userAgeStart;
    public int userAgeEnd;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }


    public void setPetName(String petName) {
        this.petName = petName;
    }

    public String getPetName() {
        return petName;
    }

    public void setPetPicPath(String petPicPath) {
        this.petPicPath = petPicPath;
    }

    public String getPetPicPath() {
        return petPicPath;
    }

    public void setUserGender(String userGender) {
        this.userGender = userGender;
    }

    public String getUserGender() {
        return userGender;
    }

    public void setUserAgeStart(int userAgeStart) {
        this.userAgeStart = userAgeStart;
    }

    public int getUserAgeStart() {
        return userAgeStart;
    }

    public void setUserAgeEnd(int userAgeEnd) {
        this.userAgeEnd = userAgeEnd;
    }

    public int getUserAgeEnd() {
        return userAgeEnd;
    }
}
