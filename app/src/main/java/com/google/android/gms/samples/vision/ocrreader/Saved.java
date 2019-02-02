package com.google.android.gms.samples.vision.ocrreader;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;

public class Saved extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);
        List<BibData> resultListBibData;
        resultListBibData = DatabaseInitializer.getDatabase(AppDatabase.getAppDatabase(this));

        Log.d(Saved.class.getName(), "Null case bhanda agadi");
        RelativeLayout rl=(RelativeLayout)findViewById(R.id.rl);
        if (resultListBibData.isEmpty()){
            Log.d(Saved.class.getName(), "Null case ma pugyo");
            TextView textView = new TextView(this);
            textView.setText("Nothing to show on database");
            rl.addView(textView);
            return;
        }
        Log.d(Saved.class.getName(), "Null case paxi not null bhandaagadi");
        if (resultListBibData!=null){
            Log.d(Saved.class.getName(), "Null not bhitra");
            for (BibData bd: resultListBibData){
                Log.d(Saved.class.getName(), "pass vayo ta ? " + bd.getWord() + " : " + bd.getMeaning());
            }
        }


        ScrollView sv = new ScrollView(this);
        sv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        LinearLayout ll = new LinearLayout(this);
        ll.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ll.setOrientation(LinearLayout.VERTICAL);
        sv.addView(ll);
        for(BibData i: resultListBibData)
        {

            TextView textView = new TextView(this);
            textView.setText(i.getWord() + " : " + i.getMeaning());
            ll.addView(textView);
        }

        rl.addView(sv);
    }




}
