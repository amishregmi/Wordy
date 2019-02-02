/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gms.samples.vision.ocrreader;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.StrictMode;
//import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.CameraSource;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.CameraSourcePreview;
import com.google.android.gms.samples.vision.ocrreader.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import com.ibm.watson.developer_cloud.language_translator.v3.LanguageTranslator;
import com.ibm.watson.developer_cloud.language_translator.v3.model.TranslateOptions;
import com.ibm.watson.developer_cloud.language_translator.v3.model.TranslationResult;
import com.ibm.watson.developer_cloud.service.security.IamOptions;

import com.microsoft.cognitiveservices.speech.ResultReason;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognitionResult;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;

import java.util.concurrent.Future;

import static android.Manifest.permission.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Future;

/**
 * Activity for the Ocr Detecting app.  This app detects text and displays the value with the
 * rear facing camera. During detection overlay graphics are drawn to indicate the position,
 * size, and contents of each TextBlock.
 */
public final class OcrCaptureActivity extends AppCompatActivity implements AsyncResponse {

    // Replace below with your own subscription key
    private static String speechSubscriptionKey = "9dbfa487c11744a0905058796c82b57c";
    // Replace below with your own service region (e.g., "westus").
    private static String serviceRegion = "westus";

    private String word;
    private String translatedWord;

    private static final String TAG = "OcrCaptureActivity";

    // Intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;

    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    // Constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";
    public static final String TextBlockObject = "String";

    private CameraSource cameraSource;
    private CameraSourcePreview preview;
    private GraphicOverlay<OcrGraphic> graphicOverlay;

    // Helper objects for detecting taps and pinches.
    private ScaleGestureDetector scaleGestureDetector;
    private GestureDetector gestureDetector;

    // A TextToSpeech engine for speaking a String value.
    private TextToSpeech tts;

    /**
     * Initializes the UI and creates the detector pipeline.
     */
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.ocr_capture);

        preview = (CameraSourcePreview) findViewById(R.id.preview);
        graphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.graphicOverlay);

        // Set good defaults for capturing text.
        boolean autoFocus = true;
        boolean useFlash = false;

        //Translation menu
        addDropDown();

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash);
        } else {
            requestCameraPermission();
        }

        gestureDetector = new GestureDetector(this, new CaptureGestureListener());
        scaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

        Snackbar.make(graphicOverlay, "Tap to Speak. Pinch/Stretch to zoom",
                Snackbar.LENGTH_LONG)
                .show();

        // Set up the Text To Speech engine.
        TextToSpeech.OnInitListener listener =
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(final int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            Log.d("OnInitListener", "Text to speech engine started successfully.");
                            tts.setLanguage(Locale.US);
                        } else {
                            Log.d("OnInitListener", "Error starting the text to speech engine.");
                        }
                    }
                };
        tts = new TextToSpeech(this.getApplicationContext(), listener);
        //FOR VOICE--------------------------------
        // Note: we need to request the permissions
        int requestCode = 5; // unique code for the permission request
        ActivityCompat.requestPermissions(OcrCaptureActivity.this, new String[]{RECORD_AUDIO, INTERNET}, requestCode);

        // Set up Swipe to change activity
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.topLayout);
        linearLayout.setOnTouchListener(new OnSwipeTouchListener(OcrCaptureActivity.this) {
            public void onSwipeTop() {
                Toast.makeText(OcrCaptureActivity.this, "top", Toast.LENGTH_SHORT).show();

            }
            public void onSwipeRight() {/
                Toast.makeText(OcrCaptureActivity.this, "right", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(OcrCaptureActivity.this, Saved.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            public void onSwipeLeft() {
                Toast.makeText(OcrCaptureActivity.this, "left", Toast.LENGTH_SHORT).show();
            }
            public void onSwipeBottom() {
                Toast.makeText(OcrCaptureActivity.this, "bottom", Toast.LENGTH_SHORT).show();
            }

        });

    }

    public void addDropDown() {

        System.out.println("Inside Main Activity2");
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.toTranslate, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
    }


    /**
     * Handles the requesting of the camera permission.  This includes
     * showing a "Snackbar" message of why the permission is needed then
     * sending the request.
     */
    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(graphicOverlay, R.string.permission_camera_rationale,
                Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.ok, listener)
                .show();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean b = scaleGestureDetector.onTouchEvent(e);

        boolean c = gestureDetector.onTouchEvent(e);

        return b || c || super.onTouchEvent(e);
    }

    /**
     * Creates and starts the camera.  Note that this uses a higher resolution in comparison
     * to other detection examples to enable the ocr detector to detect small text samples
     * at long distances.
     *
     * Suppressing InlinedApi since there is a check that the minimum version is met before using
     * the constant.
     */
    @SuppressLint("InlinedApi")
    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        // A text recognizer is created to find text.  An associated multi-processor instance
        // is set to receive the text recognition results, track the text, and maintain
        // graphics for each text block on screen.  The factory is used by the multi-processor to
        // create a separate tracker instance for each text block.
        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();
        textRecognizer.setProcessor(new OcrDetectorProcessor(graphicOverlay));

        if (!textRecognizer.isOperational()) {
            // Note: The first time that an app using a Vision API is installed on a
            // device, GMS will download a native libraries to the device in order to do detection.
            // Usually this completes before the app is run for the first time.  But if that
            // download has not yet completed, then the above call will not detect any text,
            // barcodes, or faces.
            //
            // isOperational() can be used to check if the required native libraries are currently
            // available.  The detectors will automatically become operational once the library
            // downloads complete on device.
            Log.w(TAG, "Detector dependencies are not yet available.");

            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, R.string.low_storage_error, Toast.LENGTH_LONG).show();
                Log.w(TAG, getString(R.string.low_storage_error));
            }
        }

        // Creates and starts the camera.  Note that this uses a higher resolution in comparison
        // to other detection examples to enable the text recognizer to detect small pieces of text.
        cameraSource =
                new CameraSource.Builder(getApplicationContext(), textRecognizer)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280, 1024)
                .setRequestedFps(2.0f)
                .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_TORCH : null)
                .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO : null)
                .build();
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (preview != null) {
            preview.stop();
        }
    }

    /**
     * Releases the resources associated with the camera source, the associated detectors, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (preview != null) {
            preview.release();
        }
    }

    /**
     * Callback for the result from requesting permissions. This method
     * is invoked for every call on {@link #requestPermissions(String[], int)}.
     * <p>
     * <strong>Note:</strong> It is possible that the permissions request interaction
     * with the user is interrupted. In this case you will receive empty permissions
     * and results arrays which should be treated as a cancellation.
     * </p>
     *
     * @param requestCode  The request code passed in {@link #requestPermissions(String[], int)}.
     * @param permissions  The requested permissions. Never null.
     * @param grantResults The grant results for the corresponding permissions
     *                     which is either {@link PackageManager#PERMISSION_GRANTED}
     *                     or {@link PackageManager#PERMISSION_DENIED}. Never null.
     * @see #requestPermissions(String[], int)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // we have permission, so create the camerasource
            boolean autoFocus = getIntent().getBooleanExtra(AutoFocus,true);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
            createCameraSource(autoFocus, useFlash);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Multitracker sample")
                .setMessage(R.string.no_camera_permission)
                .setPositiveButton(R.string.ok, listener)
                .show();
    }

    /**
     * Starts or restarts the camera source, if it exists.  If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() throws SecurityException {
        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (cameraSource != null) {
            try {
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    /**
     * onTap is called to speak the tapped TextBlock, if any, out loud.
     *
     * @param rawX - the raw position of the tap
     * @param rawY - the raw position of the tap.
     * @return true if the tap was on a TextBlock
     */
    private boolean onTap(float rawX, float rawY) {
        //store the raw x,y coordinates
        LocateWord.setCoordinates(rawX, rawY);

        //get the graphicOverlay view's location and store it
        LocateWord.setViewLocation(graphicOverlay.getLocOnScreen());

        OcrGraphic graphic = graphicOverlay.getGraphicAtLocation(rawX, rawY);
        TextBlock text = null;
        if (graphic != null) {
            text = graphic.getTextBlock();
            if (text != null && text.getValue() != null) {
                Log.d(TAG, "text data is being spoken! " + text.getValue());
                // Speak the string.
                //tts.speak(text.getValue(), TextToSpeech.QUEUE_ADD, null, "DEFAULT");
                ArrayList<String> uniqueWords = LocateWord.filterUniqueWords(LocateWord.findWord());
                displayWordOption(uniqueWords);
                //displayDialogBox(text.getValue());
            }
            else {
                Log.d(TAG, "text data is null");
            }
        }
        else {
            Log.d(TAG,"no text detected");
        }
        return text != null;
    }

    //making the app cover the entire screen
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if(hasFocus){
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE   //for transition to be stable
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY                      //swipe down, it will swipe up
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN                     //to remove any artifacts for the transition in the full screen
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN                            //have the full screen
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);                     //hide the navigation bar
        }
    }
/*
    private void displayDialogBox(String text){
        String tran = "Translated Coordinates: " + LocateWord.getCoordinates().first + " " + LocateWord.getCoordinates().second;
        String raw = "\nRaw Coordinates: " + LocateWord.getRawCoordinates().first + " " +LocateWord.getRawCoordinates().second;
        AlertDialog.Builder prompt = new AlertDialog.Builder(this);
        prompt.setTitle("Your Word");
        ArrayList<String> uniqueWords = LocateWord.filterUniqueWords(LocateWord.findWord());

        //prompt.setMessage(tran + "\n" + raw + "\nWord clicked: " + LocateWord.findWord() + LocateWord.getbounds()+ "\n" + text);// + "\n" + LocateWord.getbounds());//LocateWord.getMap().keySet().toString());
        prompt.setMessage("\n" + uniqueWords);
        prompt.show();
    }
*/

    private class CaptureGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return onTap(e.getRawX(), e.getRawY()) || super.onSingleTapConfirmed(e);
        }
    }

    private class ScaleListener implements ScaleGestureDetector.OnScaleGestureListener {

        /**
         * Responds to scaling events for a gesture in progress.
         * Reported by pointer motion.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should consider this event
         * as handled. If an event was not handled, the detector
         * will continue to accumulate movement until an event is
         * handled. This can be useful if an application, for example,
         * only wants to update scaling factors if the change is
         * greater than 0.01.
         */
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            return false;
        }

        /**
         * Responds to the beginning of a scaling gesture. Reported by
         * new pointers going down.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         * @return Whether or not the detector should continue recognizing
         * this gesture. For example, if a gesture is beginning
         * with a focal point outside of a region where it makes
         * sense, onScaleBegin() may return false to ignore the
         * rest of the gesture.
         */
        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        /**
         * Responds to the end of a scale gesture. Reported by existing
         * pointers going up.
         * <p/>
         * Once a scale has ended, {@link ScaleGestureDetector#getFocusX()}
         * and {@link ScaleGestureDetector#getFocusY()} will return focal point
         * of the pointers remaining on the screen.
         *
         * @param detector The detector reporting the event - use this to
         *                 retrieve extended info about event state.
         */
        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if (cameraSource != null) {
                cameraSource.doZoom(detector.getScaleFactor());
            }
        }
    }


    // On Click function to show intent of dictionary/database
    public void showAllWordAndMeaning(View view) {
        Log.d(OcrCaptureActivity.class.getName(), "showAllWordAndMeaning button clicked !");
        Intent intent = new Intent(this, Saved.class);
        startActivity(intent);
    }

    /*-----------------ANUJ--------------*/
    public void displayWordOption( ArrayList<String> unique){
        final String uniqueWords[] = new String[unique.size()];
        for(int i = 0; i < unique.size(); i++ ){
            uniqueWords[i] = unique.get(i);
        }
        System.out.println("The string array is " + uniqueWords.toString());
        final AlertDialog.Builder prompt = new AlertDialog.Builder(this);
        prompt.setItems(uniqueWords, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.out.println("Int which is " + uniqueWords[which]);
                String finalWord = uniqueWords[which];
                word = finalWord;
                findMeaning(finalWord);
            }
        });
        prompt.show();
    }

    public void findMeaning(String finalWord){
        CallbackTask callbackTask = new CallbackTask();
        callbackTask.delegate = this;
        callbackTask.execute(dictionaryEntries());
    }

    // -----------------------------------------------------------------------------------------------------------------
    // returns string

    private String dictionaryEntries() {
        final String language = "en";
        System.out.println("User input is " + word);
        final String word_id = word.toLowerCase(); //word id is case sensitive and lowercase is required
        return "https://od-api.oxforddictionaries.com:443/api/v1/entries/" + language + "/" + word_id;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // This function is called when the AsyncTask is finished
    // The Asynctask is called to get meaning of a word
    @Override
    public void processFinish(final String output) {
        doTranslation();
        System.out.println("The output in process finish is " + output);
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(word);
        alertDialog.setMessage(output + "\n\n" + "Translation: " + translatedWord);
        alertDialog.setPositiveButton("Save Word?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.out.println("Save Button Clicked");
                saveWordAndMeaning(word, output);
                // now pass word and its meaning to save function
            }
        });
        alertDialog.show();
    }

    public void doTranslation(){
        Spinner spinner = findViewById(R.id.spinner);

        String languageToTranslate = spinner.getItemAtPosition(spinner.getSelectedItemPosition()).toString();

        System.out.println("The language translated to selected is " + languageToTranslate);

        if ( !languageToTranslate.equals("None")){
            String langCode = "en-" +getLanguageCode(languageToTranslate);
            System.out.println("The lang code is " + langCode);
            translateLanguageNow(langCode);
        }
        else{
            translatedWord = "";
        }
    }

    public void saveWordAndMeaning(String word, String meaning) {
        if ("".equals(word)){
            Toast.makeText(OcrCaptureActivity.this, "word field empty", Toast.LENGTH_LONG).show();
            return;
        }
        if ("".equals(meaning)){
            Toast.makeText(OcrCaptureActivity.this, "meaning field empty", Toast.LENGTH_LONG).show();
            return;
        }
        Log.d(OcrCaptureActivity.class.getName(), "Rows Count bhanda agadi in main");
        DatabaseInitializer.populateAsync(AppDatabase.getAppDatabase(this), word, meaning);
    }

    // Api Call to translate the given word
    // takes language code as a parameter
    public void translateLanguageNow(String langCode){
        System.out.println("Inside Translate Language function");

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        IamOptions options = new IamOptions.Builder()
                .apiKey("eLS9MVEjVdtEK-MSXDYhgUnuE04zsPfYlGmLMX2ldVwk")
                .build();

        LanguageTranslator languageTranslator = new LanguageTranslator(
                "2018-05-01",
                options
        );

        languageTranslator.setEndPoint("https://gateway-wdc.watsonplatform.net/language-translator/api");

        TranslateOptions translateOptions = new TranslateOptions.Builder()
                .addText(word)
                .modelId(langCode)
                .build();

        TranslationResult result = languageTranslator.translate(translateOptions)
                .execute();

        System.out.print("The translated result is " + result);
        translatedWord = result.getTranslations().get(0).getTranslationOutput();
        System.out.println("Translate word is " + translatedWord);
    }

    // Returns universal language code
    // Each language code is related to specific languages
    // The language code is then used while calling IBM api to get translated word
    public String getLanguageCode(String name){
        String code = "de";
        if ( name.equals("German")){
            code = "de";
        }
        else if ( name.equals("Indian")){
            code = "hi";
        }
        else if ( name.equals("Japanese")){
            code = "ja";
        }
        else if ( name.equals("Russian")){
            code = "ru";
        }
        else if ( name.equals("Chinese")){
            code = "zh";
        }
        else if ( name.equals("Spanish")){
            code = "es";
        }
        else if ( name.equals("Italian")){
            code = "it";
        }

        return code;
    }

    // -----------------------------------------------------------------------------------------------------------------
    //AMISH
    public void performAudio(View v) {
        Toast.makeText(this, "Audio recording...", Toast.LENGTH_LONG);
        System.out.println("Reacheddddd");
        try {
            SpeechConfig config = SpeechConfig.fromSubscription(speechSubscriptionKey, serviceRegion);
            assert(config != null);

            SpeechRecognizer reco = new SpeechRecognizer(config);
            assert(reco != null);

            Future<SpeechRecognitionResult> task = reco.recognizeOnceAsync();
            assert(task != null);
            System.out.println("Reachedddd 2");


            SpeechRecognitionResult result = task.get();
            assert(result != null);

            if (result.getReason() == ResultReason.RecognizedSpeech) {
                String whole_string = result.toString();
                //txt.setText(result.toString());

                int last_openbrac_pos = whole_string.lastIndexOf("<");

                System.out.println("The position of < is: "+last_openbrac_pos);
                System.out.println("Reachedddd 2");
                int total_string_length = whole_string.length();

                //Stores the input word.
                String spokensentence = whole_string.substring(last_openbrac_pos+1, total_string_length-3);
                System.out.println("The spoken sentence is: "+spokensentence);
                System.out.println("Reachedddd 3 "+spokensentence);

                //txt.setText("The spoken sentence is: " +spokensentence);

                //Extracting the first word from the result.
                int firstspace = spokensentence.indexOf(' ');
                if (firstspace!=-1){

                    String first_spoken_word = spokensentence.substring(0,firstspace);
                    first_spoken_word=first_spoken_word.replace(",","");
                    spokensentence = first_spoken_word;
                    //spokensentence.substring(firstspace);

                    System.out.println("The first word spoken is: "+first_spoken_word);
                    //txt.setText("The first spoken word is: "+first_spoken_word);
                }
                word = spokensentence;
                findMeaning(word);
            }
            else {
                System.out.println("Error recognizing. Did you update the subscription info?" + System.lineSeparator() + result.toString());
                //txt.setText("Error recognizing. Did you update the subscription info?" + System.lineSeparator() + result.toString());
            }

            reco.close();
        } catch (Exception ex) {
            Log.e("SpeechSDKDemo", "unexpected " + ex.getMessage());
            assert(false);
            System.out.println("Reachedddd else");
        }

    }
}