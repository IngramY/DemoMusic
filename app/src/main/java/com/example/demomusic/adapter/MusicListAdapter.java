package com.example.demomusic.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.example.demomusic.R;
import com.example.demomusic.model.Mp3Info;
import com.example.demomusic.util.MediaUtil;

/**
 * Created by Administrator on 2015/6/26.
 */
public class MusicListAdapter extends BaseAdapter {

    private Context context;
    private List<Mp3Info> mp3Infos;
    private Mp3Info mp3Info;        //Mp3Info对象引用
    private int pos = -1;            //列表位置

    public MusicListAdapter(Context context, List<Mp3Info> mp3Infos) {
        this.context = context;
        this.mp3Infos = mp3Infos;
    }


    @Override
    public int getCount() {
        return mp3Infos.size();
    }


    @Override
    public Object getItem(int position) {
        return position;
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();

            convertView = LayoutInflater.from(context).inflate(R.layout.music_list_item, null);

            viewHolder.albumImage = (ImageView) convertView.findViewById(R.id.albumImage);
            viewHolder.musicTitle = (TextView) convertView.findViewById(R.id.music_title);
            viewHolder.musicArtist = (TextView) convertView.findViewById(R.id.music_Artist);
            viewHolder.musicDuration = (TextView) convertView.findViewById(R.id.music_duration);
            convertView.setTag(viewHolder);    //表示给View添加一个格外的数据，

        } else {
            viewHolder = (ViewHolder) convertView.getTag(); //通过getTag的方法将数据取出来
        }

        mp3Info = mp3Infos.get(position);
//           if(position == pos) {
//                viewHolder.albumImage.setImageResource(R.drawable.item);
//            } else {
//                Bitmap bitmap = getArtwork(context, mp3Info.getId(), mp3Info.getAlbumId(), true, true);
//                if(bitmap == null) {
//                    viewHolder.albumImage.setImageResource(R.drawable.music5);
//                } else {
//                    viewHolder.albumImage.setImageBitmap(bitmap);
//                }
//            }
        viewHolder.albumImage.setImageResource(R.drawable.music5);
        viewHolder.musicTitle.setText(mp3Info.getTitle());            //显示标题
        viewHolder.musicArtist.setText(mp3Info.getArtist());        //显示艺术家
        viewHolder.musicDuration.setText(MediaUtil.formatTime(mp3Info.getDuration()));//显示时长

        return convertView;
    }

    /**
     * 定义一个内部类
     * 声明相应的控件引用
     *
     * @author wwj
     */
    public class ViewHolder {
        //所有控件对象引用
        public ImageView albumImage;    //专辑图片
        public TextView musicTitle;        //音乐标题
        public TextView musicDuration;    //音乐时长
        public TextView musicArtist;    //音乐艺术家
    }

}
