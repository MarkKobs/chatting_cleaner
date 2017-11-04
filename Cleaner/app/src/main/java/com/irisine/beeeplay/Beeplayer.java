package com.irisine.beeeplay;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;

import com.irisine.mediatest.R;
import com.irisine.wordsfilter.FilteredRange;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yuwen on 2017/11/4 0004.
 */
public enum  Beeplayer {
    Instance;

    MediaPlayer m_mainPlayer = null;
    MediaPlayer m_beePlayer = null;

    Timer m_timer = null;

    long m_wholeTime = 0;
    long m_currentTime = 0;
    int m_currentRangeIndex = 0;

    long m_maxNum = 0;

    AudioManager m_audioManager = null;

    ArrayList<FilteredRange> m_filteredRanges = null;

    public static final String BEE_FILE_PATH = "";

    public void initialize(Context context) {
        m_beePlayer = MediaPlayer.create(context, R.raw.beee);
        m_beePlayer.setLooping(true);
    }

    long wordsNum2SoundTime(long wordsNum){
        return wordsNum * (m_wholeTime / m_maxNum);
    }

    /*
    * 如何调用：
    * FilterResult filterResult = SimpleWordsFilter.Instance.filter(...)
    * BeepPlayer.Instance.play(录音文件地址, filterResult.getResultRanges(), filterResult.getResultString.lenght(), AudioManager's instance of Activity)
    * */
    public void play(String orgSoundPath, ArrayList<FilteredRange> filteredRanges, int maxNum, AudioManager audioManager) throws IOException {

        if(m_mainPlayer != null && m_mainPlayer.isPlaying()) {
            return;
        }

        m_filteredRanges = filteredRanges;
        m_maxNum = maxNum;
        m_audioManager = audioManager;

        m_mainPlayer = new MediaPlayer();
        m_mainPlayer.setDataSource(orgSoundPath);
        m_mainPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        m_mainPlayer.prepareAsync();
        m_mainPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mediaPlayer.setVolume(1, 1);
                mediaPlayer.start();

                m_wholeTime = mediaPlayer.getDuration();
                m_currentRangeIndex = 0;
                m_currentTime = 0;

                m_timer = new Timer();
                m_timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        if(m_currentRangeIndex >= m_filteredRanges.size()) {
                            m_timer.cancel();
                            return;
                        }

                        long from = wordsNum2SoundTime(m_filteredRanges.get(m_currentRangeIndex).getFrom());
                        long to = wordsNum2SoundTime(m_filteredRanges.get(m_currentRangeIndex).getTo());
                        if(m_currentTime >= from
                                && m_currentTime < to) {
                            // do bee
                            if(!m_beePlayer.isPlaying()) {
//                              m_beePlayer.prepare();
                                m_beePlayer.start();
                                m_mainPlayer.setVolume(0, 0);
                            }
                        }
                        else if(m_currentTime >= to) {
                            if(m_beePlayer.isPlaying()) {
                                m_beePlayer.pause();
                                m_mainPlayer.setVolume(1, 1);
                                ++m_currentRangeIndex;
                            }
                        }

                        m_currentTime += 50;
                    }
                }, 0, 50);

            }
        });

        m_mainPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.stop();
                mediaPlayer.release();
                m_beePlayer.pause();
                m_mainPlayer = null;
                m_timer.cancel();
            }
        });
    }

    void release() {
        if(m_timer != null) {
            m_timer.cancel();;
            m_timer = null;
        }

        if(m_mainPlayer != null) {
            m_mainPlayer.release();
            m_mainPlayer = null;
        }
        if(m_beePlayer != null) {
            m_beePlayer.release();
            m_mainPlayer = null;
        }
    }

}
