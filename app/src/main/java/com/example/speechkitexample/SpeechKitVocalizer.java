package com.example.speechkitexample;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.annotation.NonNull;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.internal.annotations.EverythingIsNonNull;

/**
 * @author babaetskv on 19.04.19
 */
public class SpeechKitVocalizer {
    private static final String TAG = SpeechKitVocalizer.class.getSimpleName();
    private static final int TIMEOUT = 10;
    private static final String URL_SYNTESISE = "https://tts.api.cloud.yandex.net/speech/v1/tts:synthesize";
    private static final String URL_TOKENS = "https://iam.api.cloud.yandex.net/iam/v1/tokens";
    private static int SAMPLE_RATE = 48000;

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private String iamToken;
    private String folderId;
    private OkHttpClient client;

    public SpeechKitVocalizer(String oauthToken, String folderId) {
        client = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .build();

        getIamToken(oauthToken);
        this.folderId = folderId;
    }

    private void getIamToken(String oauthToken) {
        String json = String.format("{'yandexPassportOauthToken': '%s'}", oauthToken);
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .url(URL_TOKENS)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @EverythingIsNonNull
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Token request failed");
                e.printStackTrace();
            }

            @EverythingIsNonNull
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.d(TAG, "Unable to get IAM Token");
                    Log.d(TAG, "Response code: " + response.code());
                    Log.d(TAG, "Response message: " + response.message());
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        iamToken = jsonObject.getString("iamToken");
                        Log.i(TAG, "Got iamToken");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void synthesize(String text, String voice, String emotion) {
        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("text", text)
                .addFormDataPart("lang", "ru-RU")
                .addFormDataPart("voice", voice)
                .addFormDataPart("format", "lpcm")
                .addFormDataPart("sampleRateHertz", "" + SAMPLE_RATE)
                .addFormDataPart("emotion", emotion)
                .addFormDataPart("folderId", folderId)
                .build();
        Request request = new Request.Builder()
                .url(URL_SYNTESISE)
                .addHeader("Authorization", "Bearer " + iamToken)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Synthesize request failed");
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.body() != null) {
                    byte[] soundBytes = response.body().bytes();
                    play(soundBytes);
                }
                else {
                    Log.e(TAG, "Synthesize response body is null");
                }
            }
        });
    }

    private void play(byte[] byteArray) {
        int i;
        byte[] music;
        InputStream is = new ByteArrayInputStream(byteArray);
        AudioTrack at = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE,
                AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT,
                512, AudioTrack.MODE_STREAM);

        try{
            music = new byte[512];
            at.play();

            while((i = is.read(music)) != -1)
                at.write(music, 0, i);

        } catch (IOException e) {
            e.printStackTrace();
        }

        at.stop();
        at.release();
    }
}
