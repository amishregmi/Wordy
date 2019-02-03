package com.google.android.gms.samples.vision.ocrreader;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class CallbackTask extends AsyncTask<String, Integer, String> {

    public com.google.android.gms.samples.vision.ocrreader.AsyncResponse delegate = null;
    String mainForm;

    @Override
    protected String doInBackground(String... params) {

        final String app_id = "16bc3ce6";
        final String app_key = "5c018eabc65b814a58b76f55d5b1386e";
        try {
            URL url = new URL(params[0]);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Accept","application/json");
            urlConnection.setRequestProperty("app_id",app_id);
            urlConnection.setRequestProperty("app_key",app_key);

            // read the output from the server
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
            return stringBuilder.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        System.out.println("The first result is " + result);
        mainForm = "";
        try{
            JSONObject js = new JSONObject(result);
            JSONArray results = js.getJSONArray("results");
            System.out.println("Results is " + results);

            JSONObject lEntries = results.getJSONObject(0);
            JSONArray laArray = lEntries.getJSONArray("lexicalEntries");
            System.out.println("laArray is " + laArray);

            JSONObject inflectionof = laArray.getJSONObject(0);
            System.out.println("");

            JSONArray inflec = inflectionof.getJSONArray("inflectionOf");
            System.out.println("inflec is " + inflec);

            JSONObject id = inflec.getJSONObject(0);
            mainForm = id.getString("id");

        } catch (JSONException e){
            e.printStackTrace();
        }
        System.out.println("The main form at last is " + mainForm);
        delegate.processFinish(mainForm);
    }
}