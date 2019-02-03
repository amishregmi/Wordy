package com.google.android.gms.samples.vision.ocrreader;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class MainWordCallbackTask extends AsyncTask<String, Integer, String> {

    public com.google.android.gms.samples.vision.ocrreader.AsyncResponseRealWord delegate = null;
    String meaning;

    @Override
    protected String doInBackground(String... params) {

        //replace with your own app id and app key
        final String app_id = "16bc3ce6";
        final String app_key = "5c018eabc65b814a58b76f55d5b1386e";
        try {
            URL url = new URL(params[0]);
            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Accept", "application/json");
            urlConnection.setRequestProperty("app_id", app_id);
            urlConnection.setRequestProperty("app_key", app_key);

            // read the output from the server
            BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();

            String line = null;
            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line + "\n");
            }
            return stringBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return e.toString();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        System.out.println("The meaning is ");
        System.out.println(result);
        meaning = "";
        try {
            JSONObject js = new JSONObject(result);
            JSONArray results = js.getJSONArray("results");

            JSONObject lEntries = results.getJSONObject(0);
            JSONArray laArray = lEntries.getJSONArray("lexicalEntries");

            JSONObject entries = laArray.getJSONObject(0);
            JSONArray e = entries.getJSONArray("entries");

            JSONObject jsonObject = e.getJSONObject(0);
            JSONArray senseArray = jsonObject.getJSONArray("senses");

            JSONObject d = senseArray.getJSONObject(0);
            JSONArray de = d.getJSONArray("definitions");
            meaning = de.getString(0);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.out.println("-----------------------------------------------------");
        System.out.println("This is meaning --> " + meaning);
        delegate.processFinishForReal(meaning);
    }
}