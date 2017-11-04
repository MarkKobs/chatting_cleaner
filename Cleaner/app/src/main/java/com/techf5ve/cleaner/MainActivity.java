package com.techf5ve.cleaner;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;

import com.irisine.beeeplay.Beeplayer;
import com.irisine.wordsfilter.FilterResult;
import com.irisine.wordsfilter.SimpleWordsFilter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import omrecorder.AudioChunk;
import omrecorder.AudioRecordConfig;
import omrecorder.OmRecorder;
import omrecorder.PullTransport;
import omrecorder.PullableSource;
import omrecorder.Recorder;

public class MainActivity extends AppCompatActivity {
    private Recorder recorder;
    private boolean isRecordering = false;
    private boolean isRecognizing = false;
    private OkHttpClient client;
    private String file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/audio.pcm";
    private String file_new = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath() + "/audio.wav";
//    private String jsonPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/data.json";

    private ArrayList<String> dict = new ArrayList<>();

    private AudioManager audioManager = null;

    @BindView(R.id.recordBtn)
    AppCompatButton recordBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        client = new OkHttpClient();
        initJson();
        Beeplayer.Instance.initialize(this);

        this.audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    private void initRecorder() {
        recorder = OmRecorder.pcm(
                new PullTransport.Default(mic(), new PullTransport.OnAudioChunkPulledListener() {
                    @Override
                    public void onAudioChunkPulled(AudioChunk audioChunk) {
                    }
                }), file());
    }

    private void initJson() {
        Request request = new Request.Builder()
//                .url("http://121.201.68.28/data.json")
                .url("http://iris2d.irisine.com/data.json")
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("TAG", e.toString());
                Log.d("TAG", "下载失败！");
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String result = response.body().string();
                    JSONObject obj = new JSONObject(result);
                    JSONArray array = obj.getJSONArray("words");
                    for (int i = 0; i < array.length(); ++i) {
                        String elem = array.getString(i);
                        dict.add(elem);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                InputStream is = null;
//                byte[] buf = new byte[1024];
//                int len = 0;
//                FileOutputStream fos = null;
//                try {
//                    long total = response.body().contentLength();
//                    Log.e("TAG", "total------>" + total);
//                    long current = 0;
//                    is = response.body().byteStream();
//                    fos = new FileOutputStream(new File(jsonPath));
//                    while ((len = is.read(buf)) != -1) {
//                        current += len;
//                        fos.write(buf, 0, len);
//                        Log.e("TAG", "current------>" + current);
//                    }
//                    fos.flush();
//                } catch (IOException e) {
//                    Log.e("TAG", e.toString());
//                } finally {
//                    try {
//                        if (is != null) {
//                            is.close();
//                        }
//                        if (fos != null) {
//                            fos.close();
//                        }
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
//                }
            }
        });
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            if (message.what == 200) {
                isRecognizing = false;
                Log.d("TAG", "分析结束");
            }
            return false;
        }
    });

    //FIXME
    @OnClick(R.id.recordBtn)
    public void onClick() {
        if (isRecognizing)
            return;
        if (isRecordering) {
            recordBtn.setText("Start");
            try {
                recorder.stopRecording();
                recorder = null;
                isRecognizing = true;
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String str = getUnisound(file);
                            Log.e("TAG", str);
                            Util.startPcmToWav(file, file_new);
                            ArrayList<String> list = getJson();
                            FilterResult filterResult = SimpleWordsFilter.Instance.filter(str, list, true);
                            //FIXME
                            Beeplayer.Instance.play(file_new, filterResult.getResultRanges(), filterResult.getResultString().length(), audioManager);
                            Message msg = new Message();
                            msg.what = 200;
                            mHandler.dispatchMessage(msg);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            initRecorder();
            recorder.startRecording();
            recordBtn.setText("STOP");
        }
        isRecordering = !isRecordering;
    }

    private String getUnisound(String path) throws IOException {
        RequestBody requestBody = RequestBody.create(null, new File(path));
        Request request = new Request.Builder()
                .url("http://api.hivoice.cn/USCService/WebApi?appkey=balfpjl3e2oz52l6z7nlg7i7e5gz65xglbx2fyqt&userid=YZS15097540632448380&id="
                        + UUID.randomUUID())
                .headers(new Headers.Builder()
                        .add("Content-Type", "audio/x-wav;codec=pcm;bit=16;rate=16000")
                        .add("Accept", "text/plain")
//                        .add("Transfer-Encoding", "chunked")
                        .add("Content-Length", String.valueOf(new File(path).length()))
                        .build())
                .post(requestBody)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private ArrayList<String> getJson() {
        if (dict == null) {
            //FIXME
            initJson();
        }
        return dict;
    }

    private PullableSource mic() {
        return new PullableSource.Default(
                new AudioRecordConfig.Default(
                        MediaRecorder.AudioSource.MIC, AudioFormat.ENCODING_PCM_16BIT,
                        AudioFormat.CHANNEL_IN_MONO, 16000
                )
        );
    }

    @NonNull
    private File file() {
        return new File(file);
    }
}
