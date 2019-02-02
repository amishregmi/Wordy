package com.google.android.gms.samples.vision.ocrreader;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.List;

public class DatabaseGetter {

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
