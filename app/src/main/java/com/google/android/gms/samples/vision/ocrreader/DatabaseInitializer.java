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
    public static void deleteAsync(@NonNull final AppDatabase db, String word){
        DeleteAsync task = new DeleteAsync(db, word);
        task.execute();
    }

//    public static void populateSync(@NonNull final AppDatabase db, String word, String meaning) {
//        populateWithTestData(db, word, meaning);
//    }

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

    private static class DeleteAsync extends AsyncTask<Void, Void, Void>{

        AppDatabase mDb;
        String word;
        DeleteAsync(AppDatabase db, String word){
            this.mDb = db;
            this.word = word;
        }
        @Override
        protected Void doInBackground(final Void... params) {
            mDb.BibDataDao().delete(word);
            return null;
        }
    }


    public static List<BibData> getDatabase( @NonNull final AppDatabase db){
        List<BibData> testBibData = null;
        MyTask myTask = new MyTask();
        try{
            testBibData = myTask.execute(db).get();
        } catch (Exception error){
            Log.d(Saved.class.getName(), "Getting database failed");
        }
        return testBibData;
    }
    private static class MyTask extends AsyncTask<AppDatabase, Integer, List<BibData> >{


        @Override
        protected List<BibData> doInBackground(AppDatabase... appDatabases) {
            AppDatabase myAppDataBase  = appDatabases[0];
            return myAppDataBase.BibDataDao().getAll();

        }

        @Override
        protected void onProgressUpdate(Integer... values){

        }

        @Override
        protected void onPostExecute(List<BibData> resultListBibData){
            super.onPostExecute(resultListBibData);
        }
    }


 }
