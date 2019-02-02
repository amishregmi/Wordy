package com.google.android.gms.samples.vision.ocrreader;

import android.app.DownloadManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.util.BitSet;
import java.util.List;

public class DatabaseInitializer {
    private static final String TAG = DatabaseInitializer.class.getName();

    public static void populateAsync(@NonNull final AppDatabase db, String word, String meaning) {
        PopulateDbAsync task = new PopulateDbAsync(db, word, meaning);
        task.execute();
    }


    public static void populateSync(@NonNull final AppDatabase db, String word, String meaning) {
        populateWithTestData(db, word, meaning);
    }

    private static BibData addWordMeaning(final AppDatabase db, BibData bibData) {
        db.BibDataDao().insertAll(bibData);
        return bibData;
    }

    private static void populateWithTestData(AppDatabase db, String word, String meaning) {
        BibData bibData = new BibData();
        bibData.setWord(word);
        bibData.setMeaning(meaning);
//        bibData.setAge(25);
        addWordMeaning(db, bibData);

        List<BibData> bibDataList = db.BibDataDao().getAll();
        Log.d(DatabaseInitializer.TAG, "Rows Count: " + bibDataList.size());

    }

    private static class PopulateDbAsync extends AsyncTask<Void, Void, Void> {

        private final AppDatabase mDb;
        String word;
        String meaning;
        PopulateDbAsync(AppDatabase db, String word, String meaning)
        {
            mDb = db;
            this.word = word;
            this.meaning = meaning;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            populateWithTestData(mDb, word, meaning);
            return null;
        }

    }




 }
