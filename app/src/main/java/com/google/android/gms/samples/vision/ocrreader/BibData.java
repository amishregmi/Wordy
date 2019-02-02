package com.google.android.gms.samples.vision.ocrreader;

import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "BibData")
public class BibData {
////    @PrimaryKey(autoGenerate = true)
////    private int uid;
//    @PrimaryKey
//    @NonNull
////    @ColumnInfo(name = "word")
//    private String word;
//
//    @ColumnInfo(name = "meaning")
//    private String meaning;
//
////    @ColumnInfo(name = "age")
////    private int age;
//
////    public int getUid() {
////        return uid;
////    }
////
////    public void setUid(int uid) {
////        this.uid = uid;
////    }
//
//    public String getWord() {
//        return word;
//    }
//
//    public void setWord(String word) {
//        this.word = word;
//    }
//
//    public String getMeaning() {
//        return meaning;
//    }
//
//    public void setMeaning(String meaning) {
//        this.meaning = meaning;
//    }
//
//
////    public int getAge() {
////        return age;
////    }
//
////    public void setAge(int age) {
////        this.age = age;
////    }
//



    //    @PrimaryKey(autoGenerate = true)
//    private int uid;

    @PrimaryKey
    @NonNull
//    @ColumnInfo(name = "word")
    private String word;

    //    @ColumnInfo(name = "meaning")
    private String meaning;

//    @ColumnInfo(name = "age")
//    private int age;

//    public int getUid() {
//        return uid;
//    }
//
//    public void setUid(int uid) {
//        this.uid = uid;
//    }

    public String getWord() {
        return this.word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaning() {
        return this.meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }


//    public int getAge() {
//        return age;
//    }

//    public void setAge(int age) {
//        this.age = age;
//    }


}
