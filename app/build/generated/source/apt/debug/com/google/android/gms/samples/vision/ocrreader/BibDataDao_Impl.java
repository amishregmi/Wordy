package com.google.android.gms.samples.vision.ocrreader;

import android.arch.persistence.db.SupportSQLiteStatement;
import android.arch.persistence.room.EntityInsertionAdapter;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.RoomSQLiteQuery;
import android.arch.persistence.room.SharedSQLiteStatement;
import android.database.Cursor;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class BibDataDao_Impl implements BibDataDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter __insertionAdapterOfBibData;

  private final SharedSQLiteStatement __preparedStmtOfDelete;

  public BibDataDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfBibData = new EntityInsertionAdapter<BibData>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `BibData`(`word`,`meaning`) VALUES (?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, BibData value) {
        if (value.getWord() == null) {
          stmt.bindNull(1);
        } else {
          stmt.bindString(1, value.getWord());
        }
        if (value.getMeaning() == null) {
          stmt.bindNull(2);
        } else {
          stmt.bindString(2, value.getMeaning());
        }
      }
    };
    this.__preparedStmtOfDelete = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "DELETE FROM bibdata WHERE word= ?";
        return _query;
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
  public void delete(String word) {
    final SupportSQLiteStatement _stmt = __preparedStmtOfDelete.acquire();
    __db.beginTransaction();
    try {
      int _argIndex = 1;
      if (word == null) {
        _stmt.bindNull(_argIndex);
      } else {
        _stmt.bindString(_argIndex, word);
      }
      _stmt.executeUpdateDelete();
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
      __preparedStmtOfDelete.release(_stmt);
    }
  }

  @Override
  public List<BibData> getAll() {
    final String _sql = "SELECT * FROM BibData";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final Cursor _cursor = __db.query(_statement);
    try {
      final int _cursorIndexOfWord = _cursor.getColumnIndexOrThrow("word");
      final int _cursorIndexOfMeaning = _cursor.getColumnIndexOrThrow("meaning");
      final List<BibData> _result = new ArrayList<BibData>(_cursor.getCount());
      while(_cursor.moveToNext()) {
        final BibData _item;
        _item = new BibData();
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
