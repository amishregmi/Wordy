package com.google.android.gms.samples.vision.ocrreader;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.db.SupportSQLiteOpenHelper;
import android.arch.persistence.db.SupportSQLiteOpenHelper.Callback;
import android.arch.persistence.db.SupportSQLiteOpenHelper.Configuration;
import android.arch.persistence.room.DatabaseConfiguration;
import android.arch.persistence.room.InvalidationTracker;
import android.arch.persistence.room.RoomOpenHelper;
import android.arch.persistence.room.RoomOpenHelper.Delegate;
import android.arch.persistence.room.util.TableInfo;
import android.arch.persistence.room.util.TableInfo.Column;
import android.arch.persistence.room.util.TableInfo.ForeignKey;
import android.arch.persistence.room.util.TableInfo.Index;
import java.lang.IllegalStateException;
import java.lang.Override;
import java.lang.String;
import java.util.HashMap;
import java.util.HashSet;

public class AppDatabase_Impl extends AppDatabase {
  private volatile BibDataDao _bibDataDao;

  protected SupportSQLiteOpenHelper createOpenHelper(DatabaseConfiguration configuration) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(configuration, new RoomOpenHelper.Delegate(1) {
      public void createAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("CREATE TABLE IF NOT EXISTS `BibData` (`word` TEXT NOT NULL, `meaning` TEXT, PRIMARY KEY(`word`))");
        _db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        _db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, \"2ec7bc407a4ccc0a9c89568cf731d8a3\")");
      }

      public void dropAllTables(SupportSQLiteDatabase _db) {
        _db.execSQL("DROP TABLE IF EXISTS `BibData`");
      }

      protected void onCreate(SupportSQLiteDatabase _db) {
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onCreate(_db);
          }
        }
      }

      public void onOpen(SupportSQLiteDatabase _db) {
        mDatabase = _db;
        internalInitInvalidationTracker(_db);
        if (mCallbacks != null) {
          for (int _i = 0, _size = mCallbacks.size(); _i < _size; _i++) {
            mCallbacks.get(_i).onOpen(_db);
          }
        }
      }

      protected void validateMigration(SupportSQLiteDatabase _db) {
        final HashMap<String, TableInfo.Column> _columnsBibData = new HashMap<String, TableInfo.Column>(2);
        _columnsBibData.put("word", new TableInfo.Column("word", "TEXT", true, 1));
        _columnsBibData.put("meaning", new TableInfo.Column("meaning", "TEXT", false, 0));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBibData = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBibData = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBibData = new TableInfo("BibData", _columnsBibData, _foreignKeysBibData, _indicesBibData);
        final TableInfo _existingBibData = TableInfo.read(_db, "BibData");
        if (! _infoBibData.equals(_existingBibData)) {
          throw new IllegalStateException("Migration didn't properly handle BibData(com.google.android.gms.samples.vision.ocrreader.BibData).\n"
                  + " Expected:\n" + _infoBibData + "\n"
                  + " Found:\n" + _existingBibData);
        }
      }
    }, "2ec7bc407a4ccc0a9c89568cf731d8a3");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(configuration.context)
        .name(configuration.name)
        .callback(_openCallback)
        .build();
    final SupportSQLiteOpenHelper _helper = configuration.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  protected InvalidationTracker createInvalidationTracker() {
    return new InvalidationTracker(this, "BibData");
  }

  @Override
  public BibDataDao BibDataDao() {
    if (_bibDataDao != null) {
      return _bibDataDao;
    } else {
      synchronized(this) {
        if(_bibDataDao == null) {
          _bibDataDao = new BibDataDao_Impl(this);
        }
        return _bibDataDao;
      }
    }
  }
}
