package com.example.singergames;

import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.content.ContentResolver;
import android.content.Context;

public class MusicFileList
{
	public singgamesApp playerApp = null;
	
	public MusicFileList(singgamesApp myapp)
	{
		if(myapp == null)
		{
			Log.i("MusicFileList","Input param is error!\n");
		}
		else
		{
			Log.i("MusicFileList","Init playerApp!\n");
			playerApp = myapp;
		}
		
		if(playerApp == null)
			Log.w("MusicFileList","Init playerApp failed!\n");
	}
	
	public void setapp(singgamesApp myapp)
	{
		if(playerApp == null)
		{
			playerApp = myapp;
		}
	}
	
	private Cursor query(Uri uri, String[] prjs, String selections,
			String[] selectArgs, String order)
	{
		if(playerApp == null)
			Log.e("MusicFileList","playerApp is null!\n");
		else
			Log.i("MusicFileList","playerApp is right!\n");
		System.out.println("yes,you are right111!\n");
		ContentResolver resolver = playerApp.getContentResolver();
		//ContentResolver resolver = getContentResolver();
		if(resolver == null)
		{
			return null;
		}
		else
		{
			System.out.println("Get Content Resolver succuss!\n");
		}
		return resolver.query(uri, prjs, selections, selectArgs, order);
	}
	
	public Cursor getMusicList()
	{
		return query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null, MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
	}
}

/**********************************
 * 用于音乐列表,创建数据库到listview的映射
 *********************************/
class MusicFileListAdapter extends SimpleCursorAdapter {
	
	public MusicFileListAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) 
	{
		super(context, layout, c, from, to, flags);
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {	
		
		super.bindView(view, context, cursor);
		
		TextView titleView = (TextView) view.findViewById(android.R.id.text1);
		TextView artistView = (TextView) view.findViewById(android.R.id.text2);

		titleView.setText(cursor.getString(cursor.getColumnIndexOrThrow(MediaColumns.TITLE)));

		artistView.setText(cursor.getString(cursor.getColumnIndexOrThrow(AudioColumns.ARTIST)));

		//int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));		
	}
	
	public static String makeTimeString(long milliSecs) {
        StringBuffer sb = new StringBuffer();
        long m = milliSecs / (60 * 1000);
        sb.append(m < 10 ? "0" + m : m);
        sb.append(":");
        long s = (milliSecs % (60 * 1000)) / 1000;
        sb.append(s < 10 ? "0" + s : s);
        return sb.toString();
    }
}