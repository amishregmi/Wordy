package com.google.android.gms.samples.vision.ocrreader;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface BibDataDao {
    @Query("SELECT * FROM BibData")
//    LiveData<List<BibData>> getAll();
    List<BibData> getAll();

    @Insert
    void insertAll(BibData... bibData);

    @Delete
    void delete(BibData bibData);


}
