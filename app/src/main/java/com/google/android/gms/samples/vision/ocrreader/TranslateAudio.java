package com.google.android.gms.samples.vision.ocrreader;

import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.ibm.watson.developer_cloud.language_translator.v3.LanguageTranslator;
import com.ibm.watson.developer_cloud.language_translator.v3.model.TranslateOptions;
import com.ibm.watson.developer_cloud.language_translator.v3.model.TranslationResult;
import com.ibm.watson.developer_cloud.service.security.IamOptions;
import com.microsoft.cognitiveservices.speech.audio.AudioConfig;
import com.microsoft.cognitiveservices.speech.SpeechConfig;
import com.microsoft.cognitiveservices.speech.SpeechRecognizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.RECORD_AUDIO;

public class TranslateAudio extends AppCompatActivity {

    private static final String SpeechSubscriptionKey = "f2a689a79c8840a18e4f8acaa75eacfb";
    // Replace below with your own service region (e.g., "westus").
    private static final String SpeechRegion = "westus";

    private TextView recognizedTextView;

    // Translated sentence
    private  String translatedWord;

    // A TextToSpeech engine for speaking a String value.
    private TextToSpeech tts;

    // Sentence to be translated
    private String sentenceToTranslate;

    private Button recognizeContinuousButton;
    private String languageToTranslate;
    private MicrophoneStream microphoneStream;

    private MicrophoneStream createMicrophoneStream() {
        if (microphoneStream != null) {
            microphoneStream.close();
            microphoneStream = null;
        }

        microphoneStream = new MicrophoneStream();
        return microphoneStream;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate_audio);

        recognizedTextView = findViewById(R.id.recognizedText);

        recognizeContinuousButton = findViewById(R.id.buttonRecognizeContinuous);

        initializeSpinner();

        try {
            int permissionRequestId = 5;

            ActivityCompat.requestPermissions(TranslateAudio.this, new String[]{RECORD_AUDIO, INTERNET}, permissionRequestId);
        }
        catch(Exception ex) {
            Log.e("SpeechSDK", "could not init sdk, " + ex.toString());
            recognizedTextView.setText("Could not initialize: " + ex.toString());
        }

        // create config
        final SpeechConfig speechConfig;
        try {
            speechConfig = SpeechConfig.fromSubscription(SpeechSubscriptionKey, SpeechRegion);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
            displayException(ex);
            return;
        }

        recognizeContinuousButton.setOnClickListener(new View.OnClickListener() {
            private static final String logTag = "reco 3";
            private boolean continuousListeningStarted = false;
            private SpeechRecognizer reco = null;
            private AudioConfig audioInput = null;
            private String buttonText = "";
            private ArrayList<String> content = new ArrayList<>();

            @Override
            public void onClick(final View view) {
                final Button clickedButton = (Button) view;
                disableButtons();
                if (continuousListeningStarted) {
                    if (reco != null) {
                        final Future<Void> task = reco.stopContinuousRecognitionAsync();
                        setOnTaskCompletedListener(task, result -> {
                            Log.i(logTag, "Continuous recognition stopped.");
                            TranslateAudio.this.runOnUiThread(() -> {
                                clickedButton.setText(buttonText);
                            });
                            enableButtons();
                            continuousListeningStarted = false;
                        });
                    } else {
                        continuousListeningStarted = false;
                    }

                    return;
                }

                clearTextBox();

                try {
                    content.clear();

                    // audioInput = AudioConfig.fromDefaultMicrophoneInput();
                    audioInput = AudioConfig.fromStreamInput(createMicrophoneStream());
                    reco = new SpeechRecognizer(speechConfig, audioInput);

                    reco.recognizing.addEventListener((o, speechRecognitionResultEventArgs) -> {
                        final String s = speechRecognitionResultEventArgs.getResult().getText();
                        Log.i(logTag, "Intermediate result received: " + s);
                        content.add(s);
                        setRecognizedText(TextUtils.join(" ", content));
                        content.remove(content.size() - 1);
                    });

                    reco.recognized.addEventListener((o, speechRecognitionResultEventArgs) -> {
                        final String s = speechRecognitionResultEventArgs.getResult().getText();
                        Log.i(logTag, "Final result received:: " + s);
                        sentenceToTranslate = s;
                        doTranslation();
                        System.out.println("Translated word is " + translatedWord);
                        content.add(translatedWord);
                        setRecognizedText(TextUtils.join(" ", content));
                        tts.speak(translatedWord, TextToSpeech.QUEUE_ADD, null, "DEFAULT");
                    });

                    final Future<Void> task = reco.startContinuousRecognitionAsync();
                    setOnTaskCompletedListener(task, result -> {
                        continuousListeningStarted = true;
                        TranslateAudio.this.runOnUiThread(() -> {
                            buttonText = clickedButton.getText().toString();
                            clickedButton.setText("Stop");
                            clickedButton.setEnabled(true);
                        });
                    });
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                    displayException(ex);
                }
            }
        });

        final HashMap<String, String> intentIdMap = new HashMap<>();
        intentIdMap.put("1", "play music");
        intentIdMap.put("2", "stop");

        ///////////////////////////////////////////////////
        // text recognizer
        ///////////////////////////////////////////////////
        // Set up the Text To Speech engine.
        TextToSpeech.OnInitListener listener =
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(final int status) {
                        if (status == TextToSpeech.SUCCESS) {
                            Log.d("OnInitListener", "Text to speech engine started successfully.");
                        } else {
                            Log.d("OnInitListener", "Error starting the text to speech engine.");
                        }
                    }
                };
        tts = new TextToSpeech(this.getApplicationContext(), listener);
    }

    private void initializeSpinner(){
        Spinner spinner = (Spinner) findViewById(R.id.translateTo);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.Translate, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    public void doTranslation(){
        System.out.println("Inside doTranslation");
        Spinner spinner = findViewById(R.id.translateTo);
        languageToTranslate = spinner.getItemAtPosition(spinner.getSelectedItemPosition()).toString();
        System.out.println("Language to translate is ::: " + languageToTranslate);
        if ( !languageToTranslate.equals("None")){
            System.out.println("The language selected is " + languageToTranslate);
            String langCode = "en-" +getLanguageCode(languageToTranslate);
            translateLanguageNow(langCode);
            tts.setLanguage(Locale.forLanguageTag(getLanguageCode(languageToTranslate)));
        }
        else{
            System.out.println("ELSE NONE");;
            translatedWord = "";
        }

    }
    /**
     * Api Call to translate the given word
     * @param langCode Language Code
     */
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
                .addText(sentenceToTranslate)
                .modelId(langCode)
                .build();

        TranslationResult result = languageTranslator.translate(translateOptions)
                .execute();

        translatedWord = result.getTranslations().get(0).getTranslationOutput();
        System.out.println("The translated word after done is " + translatedWord);
    }

    /**
     * Establishes Language name to language code
     * @param name Language name
     * @return code language code
     */
    public String getLanguageCode(String name){
        String code = "de";
        if ( name.equals("German")){
            code = "de";
        }
        else if (name.equals("Arabic")){
            code = "ar";
        }
        else if (name.equals("Czech")){
            code = "cs";
        }
        else if (name.equals("Danish")){
            code = "da";
        }
        else if (name.equals("Dutch")){
            code = "nl";
        }
        else if (name.equals("English")){
            code = "en";
        }
        else if (name.equals("Finnish")){
            code = "fi";
        }
        else if (name.equals("French")){
            code = "fr";
        }
        else if ( name.equals("Hindi")){
            code = "hi";
        }
        else if (name.equals("Hungarian")){
            code = "hu";
        }
        else if (name.equals("Italian")){
            code = "it";
        }
        else if ( name.equals("Japanese")){
            code = "ja";
        }
        else if (name.equals("Korean")){
            code = "ko";
        }
        else if (name.equals("Norwegian")){
            code = "no";
        }
        else if (name.equals("Polish")){
            code = "pl";
        }
        else if (name.equals("Portuguese")){
            code = "pt";
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
        else if ( name.equals("Swedish")){
            code = "sv";
        }
        else if ( name.equals("Turkish")){
            code = "tr";
        }

        return code;
    }

    private void displayException(Exception ex) {
        recognizedTextView.setText(ex.getMessage() + System.lineSeparator() + TextUtils.join(System.lineSeparator(), ex.getStackTrace()));
    }

    private void clearTextBox() {
        AppendTextLine("", true);
    }

    private void setRecognizedText(final String s) {
        System.out.println("The final string recorded is " + s);
        AppendTextLine(s, true);
    }

    private void AppendTextLine(final String s, final Boolean erase) {
        TranslateAudio.this.runOnUiThread(() -> {
            if (erase) {
                recognizedTextView.setText(s);
            } else {
                String txt = recognizedTextView.getText().toString();
                recognizedTextView.setText(txt + System.lineSeparator() + s);
            }
        });
    }

    private void disableButtons() {
        TranslateAudio.this.runOnUiThread(() -> {

            recognizeContinuousButton.setEnabled(false);

        });
    }

    private void enableButtons() {
        TranslateAudio.this.runOnUiThread(() -> {

            recognizeContinuousButton.setEnabled(true);

        });
    }

    private <T> void setOnTaskCompletedListener(Future<T> task, OnTaskCompletedListener<T> listener) {
        s_executorService.submit(() -> {
            T result = task.get();
            listener.onCompleted(result);
            return null;
        });
    }

    private interface OnTaskCompletedListener<T> {
        void onCompleted(T taskResult);
    }

    private static ExecutorService s_executorService;
    static {
        s_executorService = Executors.newCachedThreadPool();
    }
}