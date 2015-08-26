package com.example.demomusic.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;

import java.util.List;

import com.example.demomusic.activity.HomeActivity;
import com.example.demomusic.model.Mp3Info;
import com.example.demomusic.util.AppConstant;
import com.example.demomusic.util.MediaUtil;


public class PlayService extends Service {

    private MyReceiver myReceiver;    //自定义广播接收器
    private MediaPlayer mediaPlayer;
    private String path;            // 音乐文件路径
    private int msg;                //播放信息
    private boolean isPause;        // 暂停状态
    private int current = 0;        // 记录当前正在播放的音乐
    private List<Mp3Info> mp3Infos;    //存放Mp3Info对象的集合
    private int status = 3;        //播放状态，默认为顺序播放
    private int currentTime;        //当前播放进度
    private int duration;            //播放长度

//    private LrcProcess mLrcProcess;	//歌词处理
//    private List<LrcContent> lrcList = new ArrayList<LrcContent>(); //存放歌词列表对象


    //服务要发送的一些Action
    public static final String UPDATE_ACTION = "com.ruanyun.action.UPDATE_ACTION";    //更新动作
    public static final String CTL_ACTION = "com.ruanyun.action.CTL_ACTION";        //控制动作
    public static final String MUSIC_CURRENT = "com.ruanyun.action.MUSIC_CURRENT";    //当前音乐播放时间更新动作
    public static final String MUSIC_DURATION = "com.ruanyun.action.MUSIC_DURATION";//新音乐长度更新动作
    public static final String SHOW_LRC = "com.ruanyun.action.SHOW_LRC";            //通知显示歌词

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaPlayer = new MediaPlayer();
        mp3Infos = MediaUtil.getMp3Infos(PlayService.this);

        /**
         * 设置音乐播放完成时的监听器
         */
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (status == 1) {
                    mediaPlayer.start();
                } else if (status == 2) {
                    current++;
                    if (current > mp3Infos.size() - 1) {
                        current = 0;
                    }
                    Intent sendIntent = new Intent(UPDATE_ACTION);
                    sendIntent.putExtra("current", current);
                    // 发送广播，将被Activity组件中的BroadcastReceiver接收到
                    sendBroadcast(sendIntent);
                    path = mp3Infos.get(current).getUrl();
                    play(0);
                } else if (status == 3) { // 顺序播放
                    current++;    //下一首位置
                    if (current <= mp3Infos.size() - 1) {
                        Intent sendIntent = new Intent(UPDATE_ACTION);
                        sendIntent.putExtra("current", current);
                        // 发送广播，将被Activity组件中的BroadcastReceiver接收到
                        sendBroadcast(sendIntent);
                        path = mp3Infos.get(current).getUrl();
                        play(0);
                    } else {
                        mediaPlayer.seekTo(0);
                        current = 0;
                        Intent sendIntent = new Intent(UPDATE_ACTION);
                        sendIntent.putExtra("current", current);
                        // 发送广播，将被Activity组件中的BroadcastReceiver接收到
                        sendBroadcast(sendIntent);
                    }
                } else if (status == 4) {    //随机播放
                    current = getRandomIndex(mp3Infos.size() - 1);
                    System.out.println("currentIndex ->" + current);
                    Intent sendIntent = new Intent(UPDATE_ACTION);
                    sendIntent.putExtra("current", current);
                    // 发送广播，将被Activity组件中的BroadcastReceiver接收到
                    sendBroadcast(sendIntent);
                    path = mp3Infos.get(current).getUrl();
                    play(0);
                }
            }
        });

        myReceiver = new MyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(CTL_ACTION);
        filter.addAction(SHOW_LRC);
        registerReceiver(myReceiver, filter);
    }


    /**
     * 自定义广播接收器
     */
    public class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(SHOW_LRC)) {
//                current = intent.getIntExtra("listPosition", -1);
//                initLrc();
            }
            if (action.equals(CTL_ACTION)) {
                int control = intent.getIntExtra(AppConstant.CONTROL_CYCLIC, -1);

                switch (control) {
                    case 1:
                        status = 1; // 将播放状态置为1表示：单曲循环
                        break;
                    case 2:
                        status = 2;    //将播放状态置为2表示：全部循环
                        break;
                    case 3:
                        status = 3;    //将播放状态置为3表示：顺序播放
                        break;
                    case 4:
                        status = 4;    //将播放状态置为4表示：随机播放
                        break;
                }
            }
        }
    }

    /**
     * handler用来接收消息，来发送广播更新播放时间
     */
    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            if (msg.what == 1) {
                if (mediaPlayer != null) {
                    currentTime = mediaPlayer.getCurrentPosition(); // 获取当前音乐播放的位置
                    Intent intent = new Intent();
                    intent.setAction(MUSIC_CURRENT);
                    intent.putExtra("currentTime", currentTime);
                    sendBroadcast(intent); // 给PlayerActivity发送广播
                    handler.sendEmptyMessageDelayed(1, 1000);
                }
            }
        }
    };

    /**
     * 获取随机位置
     *
     * @param end
     * @return
     */
    protected int getRandomIndex(int end) {
        int index = (int) (Math.random() * end);
        return index;
    }

    /**
     * 播放音乐   (时间)
     *
     * @param
     */
    private void play(int currentTime) {
        try {
//            initLrc();
            mediaPlayer.reset();// 把各项参数恢复到初始状态
            mediaPlayer.setDataSource(path);
            mediaPlayer.prepare(); // 进行缓冲
            mediaPlayer.setOnPreparedListener(new PreparedListener(currentTime));// 注册一个监听器
            handler.sendEmptyMessage(1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 暂停音乐
     */
    private void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPause = true;
        }
    }

    private void resume() {
        if (isPause) {
            mediaPlayer.start();
            isPause = false;
        }
    }

    /**
     * 上一首
     */
    private void previous() {
        Intent sendIntent = new Intent(UPDATE_ACTION);
        sendIntent.putExtra("current", current);
        // 发送广播，将被Activity组件中的BroadcastReceiver接收到
        sendBroadcast(sendIntent);
        play(0);

    }


    /**
     * 下一首
     */
    private void next() {
        Intent sendIntent = new Intent(UPDATE_ACTION);
        sendIntent.putExtra("current", current);
        // 发送广播，将被Activity组件中的BroadcastReceiver接收到
        sendBroadcast(sendIntent);
        play(0);
    }

    /**
     * 实现一个OnPrepareLister接口,当音乐准备好的时候开始播放
     */
    private final class PreparedListener implements MediaPlayer.OnPreparedListener {
        private int currentTime;

        public PreparedListener(int currentTime) {
            this.currentTime = currentTime;
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            mediaPlayer.start(); // 开始播放
            if (currentTime > 0) { // 如果音乐不是从头播放
                mediaPlayer.seekTo(currentTime);
            }
            Intent intent = new Intent();
            intent.setAction(MUSIC_DURATION);
            duration = mediaPlayer.getDuration();
            intent.putExtra("duration", duration);    //通过Intent来传递歌曲的总长度
            sendBroadcast(intent);

        }
    }


    /**
     * @param intent
     * @param startId
     */
    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {
        path = intent.getStringExtra("url");        //音乐路径
        current = intent.getIntExtra("listPosition", -1);//音乐位置
        msg = intent.getIntExtra("MSG", 0);

        switch (msg) {

            case AppConstant.PRIVIOUS_MSG: // 上一曲

                previous();

                break;

            case AppConstant.CONTINUE_MSG:// 继续

                resume();

                break;

            case AppConstant.NEXT_MSG:// 下一曲

                next();

                break;

            case AppConstant.PAUSE_MSG:// 暂停

                pause();

                break;

            case AppConstant.PLAY_MSG:// 播放

                play(0);

                break;

            default:
                break;

        }

        super.onStart(intent, startId);
    }

    /**
     * onDestroy()
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
