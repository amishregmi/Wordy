package com.google.android.gms.samples.vision.ocrreader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class Saved extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);

        List<BibData> resultListBibData;
        resultListBibData = DatabaseInitializer.getDatabase(AppDatabase.getAppDatabase(this));

        LinearLayout linearLayout = findViewById(R.id.verticalLinearLayout);

        if (resultListBibData.isEmpty()){
            Log.d(Saved.class.getName(), "Null case ma pugyo");
            TextView textView = new TextView(this);
            textView.setText("Nothing to show on database");
            linearLayout.addView(textView);
            Toast.makeText(this,"Empty Dictionary", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < resultListBibData.size(); i++) {
            BibData bibData = resultListBibData.get(i);

            LinearLayout subLayout = new LinearLayout(this);
            subLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            subLayout.setOrientation(LinearLayout.VERTICAL);
            subLayout.setBackground(getResources().getDrawable(R.drawable.border));
            subLayout.setPadding(40,25,10,10);
            linearLayout.addView(subLayout);

            TextView wordView = new TextView(this);
            wordView.setWidth(50);
            wordView.setTextSize(20);
            wordView.setText(bibData.getWord().toUpperCase());
            wordView.setTextColor(Color.parseColor("#BE3416"));

            TextView meaningView = new TextView(this);
            meaningView.setText(bibData.getMeaning());

            //Delete view
            final ImageView imageView = new ImageView(this);
            final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(100, 100);
            params.setMargins(5,5,5,5);
            imageView.setPadding(5,5,5,5);
            params.gravity = Gravity.RIGHT;
            imageView.setLayoutParams(params);
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.deleteicon));
            imageView.setTag(bibData.getWord());
            imageView.setClickable(true);
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ViewGroup)v.getParent().getParent()).removeView((ViewGroup)v.getParent());
                    String wordName = v.getTag().toString();
                    System.out.println("asfasdfa"+wordName);
                    deleteWordAndMeaning(wordName);                }
            });

            subLayout.addView(wordView);
            subLayout.addView(meaningView);
            subLayout.addView(imageView);
        }
    }

    public void deleteWordAndMeaning(String word){
        DatabaseInitializer.deleteAsync(AppDatabase.getAppDatabase(this), word);
        Toast.makeText(this,"DELETED", Toast.LENGTH_SHORT).show();
    }
}