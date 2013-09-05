package com.example.singergames;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.app.Application;

public class singgamesApp extends Application
{
	private MediaPlayer myPlayer = null;
	@Override
	public void onCreate()
	{
		super.onCreate();
		System.out.println("singgamesApp init \n");
	}
}


	