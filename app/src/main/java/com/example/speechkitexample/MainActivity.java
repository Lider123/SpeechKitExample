package com.example.speechkitexample;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    Button voiceButton;
    RadioGroup voiceGroup;
    RadioGroup emotionGroup;

    SpeechKitVocalizer vocalizer;
    List<String> phraseQueue;

    private String voice = "alyss";
    private String emotion = "good";

    private void initPhrases() {
        phraseQueue = new ArrayList<>();
        phraseQueue.add("Сказка — это когда женился на лягушке, а она оказалась царевной. А быль — это когда наоборот.");
        phraseQueue.add("У тебя — своя сказка, а у меня — своя.");
        phraseQueue.add("Мы любим сказки, но не верим в них.");
        phraseQueue.add("Знаешь, как появились сказки? Никто не хочет верить в реальность.");
        phraseQueue.add("Взрослые иногда нуждаются в сказке даже больше, чем дети.");
        phraseQueue.add("Минус сказок в том, что они приводят к разочарованию. В реальной жизни принц остаётся не с той принцессой.");
        phraseQueue.add("Одни сказки читают, а другие в них живут.");
        phraseQueue.add("Сказки как старые друзья — их надо навещать время от времени.");
        phraseQueue.add("Что плохого в сказках? В них всегда счастливый конец.");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initPhrases();

        vocalizer = new SpeechKitVocalizer(getString(R.string.oauth_token), getString(R.string.folder_id));

        voiceGroup = findViewById(R.id.voice_radio_group);
        voiceGroup.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
                View radioButton = group.findViewById(checkedId);
                int radioId = group.indexOfChild(radioButton);
                RadioButton btn = (RadioButton) group.getChildAt(radioId);
                voice = (String) btn.getText();
                initPhrases();
                Log.d(TAG, "Selected voice: " + voice);
            });

        emotionGroup = findViewById(R.id.emotion_radio_group);
        emotionGroup.setOnCheckedChangeListener((RadioGroup group, int checkedId) -> {
                View radioButton = group.findViewById(checkedId);
                int radioId = group.indexOfChild(radioButton);
                RadioButton btn = (RadioButton) group.getChildAt(radioId);
                emotion = (String) btn.getText();
                initPhrases();
                Log.d(TAG, "Selected emotion: " + emotion);
            });

        voiceButton = findViewById(R.id.button_voice);
        voiceButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        String phrase = phraseQueue.remove(0);
        vocalizer.synthesize(phrase, voice, emotion);
        phraseQueue.add(phrase);
    }
}
