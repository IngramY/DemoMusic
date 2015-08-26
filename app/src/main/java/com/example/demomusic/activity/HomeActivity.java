package com.example.demomusic.activity;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.demomusic.R;
import com.example.demomusic.adapter.MusicListAdapter;
import com.example.demomusic.model.Mp3Info;
import com.example.demomusic.util.AppConstant;
import com.example.demomusic.util.MediaUtil;

public class HomeActivity extends BaseActivity implements View.OnClickListener {

    private Button previous_music;// 上一首
    private Button repeat_music;// 重复（单曲循环、全部循环）
    private Button play_music;// 播放（播放、暂停）
    private Button shuffle_music;// 随机播放
    private Button next_music;// 下一首
    private Button playing;// 正在播放列表
    private ListView listView;// 音乐列表
    //	private ImageView music_album;
    private TextView music_title; //歌曲名
    private TextView music_duration; //音乐时长

    private int currentTime; // 当前时间
    private int duration; // 歌曲时长

    private MusicListAdapter listAdapter;// 列表适配器
    private HomeReceiver homeReceiver;// 广播接受器

    private List<Mp3Info> mp3Infos;
    private int repeatState = 3; // 循环标识
    private final int isCurrentRepeat = 1; // 单曲循环
    private final int isAllRepeat = 2;    // 全部循环
    private final int isNoneRepeat = 3;    // 无重复播放

    // 一系列动作
    public static final String UPDATE_ACTION = "com.ruanyun.action.UPDATE_ACTION"; // 更新动作
    public static final String CTL_ACTION = "com.ruanyun.action.CTL_ACTION"; // 控制动作
    public static final String MUSIC_CURRENT = "com.ruanyun.action.MUSIC_CURRENT"; // 当前音乐改变动作
    public static final String MUSIC_DURATION = "com.ruanyun.action.MUSIC_DURATION"; // 音乐时长改变动作
    public static final String REPEAT_ACTION = "com.ruanyun.action.REPEAT_ACTION"; // 音乐重复改变动作
    public static final String SHUFFLE_ACTION = "com.ruanyun.action.SHUFFLE_ACTION"; // 音乐随机播放动作

    private boolean isFirstTime = true;    //第一次播放
    private boolean isPlaying = false;    // 正在播放
    private boolean isPause;        // 暂停

    //	private boolean isNoneShuffle = true; // 顺序播放
    private boolean isShuffle = false; // 随机播放

    private int listPosition = 0; // 播放歌曲在mp3Infos的位置  初始化0
    private String url; // 歌曲路径

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        initView();// 初始化视图
        initViewListener();// 视图监听

        mp3Infos = MediaUtil.getMp3Infos(HomeActivity.this);    //获取媒体库音乐文件列表
        music_title.setText(mp3Infos.get(listPosition).getTitle());
        music_duration.setText(MediaUtil.formatTime(mp3Infos.get(listPosition).getDuration()));
        listAdapter = new MusicListAdapter(this, mp3Infos);
        listView.setAdapter(listAdapter);

        homeReceiver = new HomeReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UPDATE_ACTION);
        intentFilter.addAction(MUSIC_CURRENT);
        intentFilter.addAction(MUSIC_DURATION);
        registerReceiver(homeReceiver, intentFilter);
    }

    /**
     * 广播接受器， 接受来自 playService 的
     * // 自定义的BroadcastReceiver，负责监听从Service传回来的广播
     *
     * @author Administrator
     */
    public class HomeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();//接受动作指令
            if (action.equals(MUSIC_CURRENT)) {
                // currentTime代表当前播放的时间
                currentTime = intent.getIntExtra("currentTime", -1);
                music_duration.setText(MediaUtil.formatTime(currentTime) + "/" +
                        MediaUtil.formatTime(duration));
            } else if (action.equals(MUSIC_DURATION)) {
                duration = intent.getIntExtra("duration", -1);
            } else if (action.equals(UPDATE_ACTION)) {

                // 获取Intent中的current消息，current代表当前正在播放的歌曲
                listPosition = intent.getIntExtra("current", -1);
                if (listPosition >= 0) {
                    music_title.setText(mp3Infos.get(listPosition).getTitle());
                }

                //将播放按钮改变为正在播放状态
                play_music.setBackgroundResource(R.drawable.pause_selector);

            } else if (action.equals(REPEAT_ACTION)) {//循环模式

                repeatState = intent.getIntExtra("repeatState", -1);
                switch (repeatState) {
                    case isCurrentRepeat: // 单曲循环
                        repeat_music
                                .setBackgroundResource(R.drawable.repeat_current_selector);
                        shuffle_music.setClickable(false);
                        break;
                    case isAllRepeat: // 全部循环
                        repeat_music
                                .setBackgroundResource(R.drawable.repeat_all_selector);
                        shuffle_music.setClickable(false);
                        break;
                    case isNoneRepeat: // 无重复
                        repeat_music
                                .setBackgroundResource(R.drawable.repeat_none_selector);
                        shuffle_music.setClickable(true);
                        break;
                }

            } else if (action.equals(SHUFFLE_ACTION)) {//是否随机

                isShuffle = intent.getBooleanExtra("shuffleState", false);
                if (isShuffle) {

                    shuffle_music
                            .setBackgroundResource(R.drawable.shuffle_selector);
                    repeat_music.setClickable(false);
                } else {
                    isShuffle = true;
                    shuffle_music
                            .setBackgroundResource(R.drawable.shuffle_none_selector);
                    repeat_music.setClickable(true);

                }
            }

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.previous_music://上一曲
                previous_music();
                break;
            case R.id.repeat_music://重复模式
                repeat_music();
                break;
            case R.id.play_music:// 点击播放按钮
                play();
                break;
            case R.id.shuffle_music://播放顺序模式
                shuffle_music();
                break;
            case R.id.next_music://下一曲
                next_music();
                break;
            case R.id.playing://跳转到正在播放界面
                Intent intent = new Intent();
                intent.setClass(HomeActivity.this, PlayerActivity.class);
                startActivity(intent);
                break;
        }
    }

    /**
     * 是否随机播放
     */
    private void shuffle_music() {
        if (isShuffle) {
            shuffle_music.setBackgroundResource(R.drawable.shuffle_none_selector);
            isShuffle = false;
        } else {
            shuffle_music.setBackgroundResource(R.drawable.shuffle_selector);
            isShuffle = true;
        }
    }

    /**
     * 循环播放
     */
    private void repeat_music() {
        if (repeatState == isNoneRepeat) {        //点击当前不循环
            Intent intent = new Intent();
            intent.putExtra(AppConstant.CONTROL_CYCLIC, isCurrentRepeat);
            sendBroadcast(intent);
            shuffle_music.setClickable(false);
            repeatState = isCurrentRepeat;
            repeat_music.setBackgroundResource(R.drawable.repeat_current_selector);
            Toast.makeText(HomeActivity.this, R.string.repeat_current,
                    Toast.LENGTH_SHORT).show();
        } else if (repeatState == isCurrentRepeat) {    //点击当前单曲循环
            Intent intent = new Intent();
            intent.putExtra(AppConstant.CONTROL_CYCLIC, isAllRepeat);
            sendBroadcast(intent);
            shuffle_music.setClickable(true);
            repeatState = isAllRepeat;
            repeat_music.setBackgroundResource(R.drawable.repeat_all_selector);
            Toast.makeText(HomeActivity.this, R.string.repeat_all,
                    Toast.LENGTH_SHORT).show();

        } else if (repeatState == isAllRepeat) {    //点击当前全部循环
            Intent intent = new Intent();
            intent.putExtra(AppConstant.CONTROL_CYCLIC, isNoneRepeat);
            sendBroadcast(intent);
            shuffle_music.setClickable(true);
            repeatState = isNoneRepeat;
            repeat_music.setBackgroundResource(R.drawable.repeat_none_selector);
            Toast.makeText(HomeActivity.this, R.string.repeat_none,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 播放音乐
     */
    private void play() {
        Intent intent = new Intent();
        intent.setAction("com.ruanyun.media.MUSIC_SERVICE");//目标服务
        if (isFirstTime) {
            play_music.setBackgroundResource(R.drawable.pause_selector);
            Mp3Info mp3Info = mp3Infos.get(listPosition);//获取当前位置Mp3Info
            music_title.setText(mp3Info.getTitle());
            intent.putExtra("listPosition", 0);
            intent.putExtra("url", mp3Info.getUrl());//mp3Info
            intent.putExtra("MSG", AppConstant.PLAY_MSG);//
            startService(intent);//开启服务
            isFirstTime = false;
            isPlaying = true;
            isPause = false;
        } else {
            if (isPlaying) {
                play_music.setBackgroundResource(R.drawable.play_selector);
                intent.putExtra("MSG", AppConstant.PAUSE_MSG);
                startService(intent);
                isPlaying = false;
                isPause = true;
            } else if (isPause) {
                play_music.setBackgroundResource(R.drawable.pause_selector);
                intent.putExtra("MSG", AppConstant.CONTINUE_MSG);
                startService(intent);
                isPause = false;
                isPlaying = true;
            }
        }
    }

    /**
     * 下一首
     */
    private void next_music() {
        isFirstTime = false;
        isPlaying = true;
        isPause = false;
        play_music.setBackgroundResource(R.drawable.play_selector);
        listPosition = listPosition + 1;
        if (listPosition <= mp3Infos.size() - 1) {
            Mp3Info mp3Info = mp3Infos.get(listPosition);
            // showArtwork(mp3Info); //显示专辑封面
            url = mp3Info.getUrl();
            music_title.setText(mp3Info.getTitle());
            music_duration.setText(MediaUtil.formatTime(mp3Infos.get(listPosition).getDuration()));
            // musicArtist.setText(mp3Info.getArtist());
            Intent intent = new Intent();
            intent.setAction("com.ruanyun.media.MUSIC_SERVICE");
            intent.putExtra("url", mp3Info.getUrl());
            intent.putExtra("listPosition", listPosition);
            intent.putExtra("MSG", AppConstant.NEXT_MSG);
            startService(intent);
        } else {
            listPosition = mp3Infos.size() - 1;
            Toast.makeText(HomeActivity.this, "没有下一首了", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    /**
     * 点击   上一首
     */
    private void previous_music() {
        isFirstTime = false;
        isPlaying = true;
        isPause = false;
        //播放按钮由正在播放 （图标是点击暂停）   变为暂停（点击播放）
        play_music.setBackgroundResource(R.drawable.play_selector);
        listPosition = listPosition - 1;
        if (listPosition >= 0) {
            Mp3Info mp3Info = mp3Infos.get(listPosition); // 上一首MP3
            url = mp3Info.getUrl();
            music_title.setText(mp3Info.getTitle());
            music_duration.setText(MediaUtil.formatTime(mp3Infos.get(listPosition).getDuration()));
            Intent intent = new Intent();
            intent.setAction("com.ruanyun.media.MUSIC_SERVICE");
            intent.putExtra("url", mp3Info.getUrl());
            intent.putExtra("listPosition", listPosition);
            intent.putExtra("MSG", AppConstant.PRIVIOUS_MSG);
            startService(intent);
        } else {
            listPosition = 0;
            Toast.makeText(HomeActivity.this, "没有上一首了", Toast.LENGTH_SHORT)
                    .show();
        }
    }


    /**
     * 视图监听
     */
    private void initViewListener() {
        previous_music.setOnClickListener(this);
        repeat_music.setOnClickListener(this);
        play_music.setOnClickListener(this);
        shuffle_music.setOnClickListener(this);
        next_music.setOnClickListener(this);
        playing.setOnClickListener(this);
    }

    /**
     * 初始化视图
     */
    private void initView() {
        previous_music = (Button) findViewById(R.id.previous_music);// 上一曲
        repeat_music = (Button) findViewById(R.id.repeat_music);// 循环模式
        play_music = (Button) findViewById(R.id.play_music);// 播放音乐
        shuffle_music = (Button) findViewById(R.id.shuffle_music);// 播放模式
        next_music = (Button) findViewById(R.id.next_music);// 下一首
        playing = (Button) findViewById(R.id.playing);//正在播放
        listView = (ListView) findViewById(R.id.music_list);// 歌曲列表
//		music_album = (ImageView) findViewById(R.id.music_album);// 专辑图片
        music_title = (TextView) findViewById(R.id.music_song);// 歌曲名 - 歌手
        music_duration = (TextView) findViewById(R.id.music_time);//歌曲时间
    }


    /**
     * 对图片进行合适的缩放
     *
     * @param options
     * @param target
     * @return
     */
    /*
     * public static int computeSampleSize(BitmapFactory.Options options, int
	 * target) { int w = options.outWidth; int h = options.outHeight; int
	 * candidateW = w / target; int candidateH = h / target; int candidate =
	 * Math.max(candidateW, candidateH); if(candidate == 0) { return 1; }
	 * if(candidate > 1) { if((w > target) && (w / candidate) < target) {
	 * candidate -= 1; } } if(candidate > 1) { if((h > target) && (h /
	 * candidate) < target) { candidate -= 1; } } return candidate; }
	 *//**
     * 获取默认专辑图片
     *
     * @param context
     * @return
     */
    /*
     * public static Bitmap getDefaultArtwork(Context context,boolean small) {
	 * BitmapFactory.Options opts = new BitmapFactory.Options();
	 * opts.inPreferredConfig = Bitmap.Config.RGB_565; if(small){ //返回小图片 return
	 * BitmapFactory
	 * .decodeStream(context.getResources().openRawResource(R.mipmap.music5),
	 * null, opts); } return
	 * BitmapFactory.decodeStream(context.getResources().openRawResource
	 * (R.mipmap.defaultalbum), null, opts); }
	 *//**
     * 从文件当中获取专辑封面位图
     *
     * @param context
     * @param songid
     * @param albumid
     * @return
     */
	/*
	 * private static Bitmap getArtworkFromFile(Context context, long songid,
	 * long albumid){ Bitmap bm = null; if(albumid < 0 && songid < 0) { throw
	 * new IllegalArgumentException("Must specify an album or a song id"); } try
	 * { BitmapFactory.Options options = new BitmapFactory.Options();
	 * FileDescriptor fd = null; if(albumid < 0){ Uri uri =
	 * Uri.parse("content://media/external/audio/media/" + songid +
	 * "/albumart"); ParcelFileDescriptor pfd =
	 * context.getContentResolver().openFileDescriptor(uri, "r"); if(pfd !=
	 * null) { fd = pfd.getFileDescriptor(); } } else { Uri uri =
	 * ContentUris.withAppendedId(albumArtUri, albumid); ParcelFileDescriptor
	 * pfd = context.getContentResolver().openFileDescriptor(uri, "r"); if(pfd
	 * != null) { fd = pfd.getFileDescriptor(); } } options.inSampleSize = 1; //
	 * 只进行大小判断 options.inJustDecodeBounds = true; // 调用此方法得到options得到图片大小
	 * BitmapFactory.decodeFileDescriptor(fd, null, options); //
	 * 我们的目标是在800pixel的画面上显示 // 所以需要调用computeSampleSize得到图片缩放的比例
	 * options.inSampleSize = 100; // 我们得到了缩放的比例，现在开始正式读入Bitmap数据
	 * options.inJustDecodeBounds = false; options.inDither = false;
	 * options.inPreferredConfig = Bitmap.Config.ARGB_8888;
	 * 
	 * //根据options参数，减少所需要的内存 bm = BitmapFactory.decodeFileDescriptor(fd, null,
	 * options); } catch (FileNotFoundException e) { e.printStackTrace(); }
	 * return bm; }
	 *//**
     * 获取专辑封面位图对象
     *
     * @param context
     * @param song_id
     * @param album_id
     * @param allowdefalut
     * @return
     */
	/*
	 * public static Bitmap getArtwork(Context context, long song_id, long
	 * album_id, boolean allowdefalut, boolean small){ if(album_id < 0) {
	 * if(song_id < 0) { Bitmap bm = getArtworkFromFile(context, song_id, -1);
	 * if(bm != null) { return bm; } } if(allowdefalut) { return
	 * getDefaultArtwork(context, small); } return null; } ContentResolver res =
	 * context.getContentResolver(); Uri uri =
	 * ContentUris.withAppendedId(albumArtUri, album_id); if(uri != null) {
	 * InputStream in = null; try { in = res.openInputStream(uri);
	 * BitmapFactory.Options options = new BitmapFactory.Options(); //先制定原始大小
	 * options.inSampleSize = 1; //只进行大小判断 options.inJustDecodeBounds = true;
	 * //调用此方法得到options得到图片的大小 BitmapFactory.decodeStream(in, null, options);
	 *//** 我们的目标是在你N pixel的画面上显示。 所以需要调用computeSampleSize得到图片缩放的比例 **/
	/*
                *//** 这里的target为800是根据默认专辑图片大小决定的，800只是测试数字但是试验后发现完美的结合 **/
	/*
	 * if(small){ options.inSampleSize = computeSampleSize(options, 40); } else{
	 * options.inSampleSize = computeSampleSize(options, 600); } //
	 * 我们得到了缩放比例，现在开始正式读入Bitmap数据 options.inJustDecodeBounds = false;
	 * options.inDither = false; options.inPreferredConfig =
	 * Bitmap.Config.ARGB_8888; in = res.openInputStream(uri); return
	 * BitmapFactory.decodeStream(in, null, options); } catch
	 * (FileNotFoundException e) { Bitmap bm = getArtworkFromFile(context,
	 * song_id, album_id); if(bm != null) { if(bm.getConfig() == null) { bm =
	 * bm.copy(Bitmap.Config.RGB_565, false); if(bm == null && allowdefalut) {
	 * return getDefaultArtwork(context, small); } } } else if(allowdefalut) {
	 * bm = getDefaultArtwork(context, small); } return bm; } finally { try {
	 * if(in != null) { in.close(); } } catch (IOException e) {
	 * e.printStackTrace(); } } } return null; }
	 */

}
