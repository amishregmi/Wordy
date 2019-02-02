package com.google.android.gms.samples.vision.ocrreader;

import android.arch.persistence.db.SupportSQLiteStatement;
import android.arch.persistence.room.EntityDeletionOrUpdateAdapter;
import android.arch.persistence.room.EntityInsertionAdapter;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.RoomSQLiteQuery;
import android.database.Cursor;
import java.lang.Override;
import java.lang.String;
import java.util.ArrayList;
import java.util.List;

public class BibDataDao_Impl implements BibDataDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter __insertionAdapterOfBibData;

  private final EntityDeletionOrUpdateAdapter __deletionAdapterOfBibData;

  public BibDataDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBibData = new EntityInsertionAdapter<BibData>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR ABORT INTO `BibData`(`uid`,`word`,`meaning`) VALUES (nullif(?, 0),?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, BibData value) {
        stmt.bindLong(1, value.getUid());
        if (value.getWord() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getWord());
        }
        if (value.getMeaning() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getMeaning());
        }
      }
    };
    this.__deletionAdapterOfBibData = new EntityDeletionOrUpdateAdapter<BibData>(__db) {
      @Override
      public String createQuery() {
        return "DELETE FROM `BibData` WHERE `uid` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, BibData value) {
        stmt.bindLong(1, value.getUid());
      }
    };
  }

  @Override
  public void insertAll(BibData... bibData) {
    __db.beginTransaction();
    try {
      __insertionAdapterOfBibData.insert(bibData);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void delete(BibData bibData) {
    __db.beginTransaction();
    try {
      __deletionAdapterOfBibData.handle(bibData);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public List<BibData> getAll() {
    final String _sql = "SELECT * FROM BibData";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final Cursor _cursor = __db.query(_statement);
    try {
      final int _cursorIndexOfUid = _cursor.getColumnIndexOrThrow("uid");
      final int _cursorIndexOfWord = _cursor.getColumnIndexOrThrow("word");
      final int _cursorIndexOfMeaning = _cursor.getColumnIndexOrThrow("meaning");
      final List<BibData> _result = new ArrayList<BibData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final BibData _item;
        _item = new BibData();
        final int _tmpUid;
        _tmpUid = _cursor.getInt(_cursorIndexOfUid);
        _item.setUid(_tmpUid);
        final String _tmpWord;
        _tmpWord = _cursor.getString(_cursorIndexOfWord);
        _item.setWord(_tmpWord);
        final String _tmpMeaning;
        _tmpMeaning = _cursor.getString(_cursorIndexOfMeaning);
        _item.setMeaning(_tmpMeaning);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }
}
