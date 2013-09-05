package com.example.singergames;

import java.io.IOException;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.os.IBinder;
import android.os.Binder;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.util.Log;


public class LocalService extends Service
{
	private final IBinder myBinder = new LocalBinder();
	
	private MediaPlayer myPlayer = new MediaPlayer();
	
	private singgamesApp myapp =  null;
	
	private MusicFileList myMusicFileList = null;
	
	public static final String	PLAYER_PREPARE_END	= "com.example.singergames.prepared";
	public static final String	PLAY_COMPLETED		= "com.example.singergames.playcompleted";
	
	//·����ģʽ���б�
	private static String FilePath = null;
	private static int playMode = 0;
	private static Cursor myCursor = null;
	
	//���ڻָ���ͣ����������
	private static boolean isPausing = false;
	
	//�б�����������������
	private static int listNum = 0;
	
	//myMusicFileList = new MusicFileList(myapp);
    MediaPlayer.OnPreparedListener mPrepareListener = new MediaPlayer.OnPreparedListener() 
    {
        @Override
		public void onPrepared(MediaPlayer mp) 
        {   
            broadcastEvent(PLAYER_PREPARE_END);
        }
    };
	    
    MediaPlayer.OnCompletionListener mCompleteListener = new MediaPlayer.OnCompletionListener() 
    {
        @Override
		public void onCompletion(MediaPlayer mp) 
        {
        	Log.v("test","Play over !!!!!!  Play over !");
            broadcastEvent(PLAY_COMPLETED);
            
            next();
        }
    };

    private void broadcastEvent(String what)
	{
		Intent i = new Intent(what);
		sendBroadcast(i);
	}
	
	@Override
	public IBinder onBind(Intent arg0)
	{
		return myBinder;
	}
	
	public class LocalBinder extends Binder
	{
		public LocalService getService()
		{
			return LocalService.this;
		}
	}

	@Override
	public void onCreate()
	{
		super.onCreate();		
		Log.i("LocalService","Create Service !! \n");
		
		
		/*��ʼ��Ĭ�ϲ����б�*/
		//�����治���г��ڲ������������������
		myapp = (singgamesApp)getApplication();
		if(myapp == null)
			Log.e("Error","myapp is null!");
		else
			System.out.println("myapp is OK!");
		
		myMusicFileList = new MusicFileList(myapp);	
		myCursor = myMusicFileList.getMusicList();

		myPlayer.setOnPreparedListener(mPrepareListener);
		myPlayer.setOnCompletionListener(mCompleteListener);
	}
	
	//��������      
	/*public void startservice(Context c)
	{                  
		Intent iService=new Intent(c,LocalService.class);          
		iService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);         
		c.startService(iService);     
	}      
	
	//�رշ���      
	public void stopservice(Context c)
	{          
		Intent iService=new Intent(c,LocalService.class);          
		iService.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);         
		c.stopService(iService); 
	}
	*/

	public void setPrepareAndPlay(String path)
	{
		try
		{
			if(myPlayer == null )
				Log.e("LocalService", "[setPrepareAndPlay]:myPlayer is null! please check the initial of palyer!");
			
			if(path == null)
				Log.e("LocalService", "[setPrepareAndPlay]:Input file path is null!");
			
			try{
				myPlayer.reset();
				myPlayer.setDataSource(path);
				myPlayer.prepare();
				myPlayer.start();
				isPausing = false;
			}
			catch (IOException e)
			{
				Log.e("LocalService","[setPrepareAndPlay]:setPrepareAndPlay failed\n");
				return;
			}
		}
		catch (IllegalArgumentException e)
		{
			Log.e("LocalService","[setPrepareAndPlay]:setPrepareAndPlay failed\n");
			return;
		}
	}

	//�����������ļ�·��������ģʽ�������б������б������cursor����
	public void start(String Path, int Mode, Cursor MediaList)
	{
		Log.i("LocalService", "Before Startting to play!\n ");
				
		if(isPausing == true)
		{
			Log.e("haha","haha");
			myPlayer.start();
			isPausing = false;
		}
		else
		{
			//����д��벥���б����ô���ģ�����ʹ��Ĭ����������
			if(MediaList != null)
			{
				myCursor = MediaList;
			}
			myCursor.moveToFirst();
			playMode = Mode;
			
			if(Path == null)
			{
				FilePath = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaColumns.DATA));
				System.out.println("The file Path is " + FilePath);
			}
			else
			{
				FilePath = Path;
			}
			
			//�õ��б�����Ŀ��������������pre/next����
			listNum = myCursor.getColumnCount();
			setPrepareAndPlay(FilePath);
		
/*		myPlayer.setOnCompletionListener(new OnCompletionListener)
		{
			
		}
*/
		}
	}

	public void stop()
	{
		if(myPlayer != null)
			myPlayer.stop();
	}

	public void pause()
	{
		if(myPlayer.isPlaying() == true)
		{
			myPlayer.pause();
			isPausing = true;
		}
	}
	
	public void pre()
	{
		//����ǰһ�ף����֮ǰû���Ź����Ͳ��ŵ�һ��
		if(myCursor.moveToPrevious() == false)
			myCursor.moveToFirst();
		
		if(myCursor != null)
			FilePath = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaColumns.DATA));
		
		if(FilePath != null)
		{
			Log.i("LocalService","music file is" + FilePath);
			setPrepareAndPlay(FilePath);
		}
		else
			Log.e("LocalService","Get FilePath failed at pre playing !");
	}
	
	public void next()
	{
		if(playMode == 0)
		{
			//˳�򲥷�
			if(myCursor.moveToNext())
			{
				Log.i("LocalService","Move to the next Playing!");
			}
			else
			{
				myCursor.moveToFirst();
				Log.w("LocalService","Play in the end of the list!");
			}
		}
		else if(playMode == 1)
		{
			//ѭ������
			if(myCursor.moveToNext())
			{
				Log.i("LocalService","Move to the next Playing!");
			}
			else
			{
				Log.w("LocalService","Play in the end of the list, will it Playing at first!");
				myCursor.moveToFirst();
			}
		}
		else if(playMode == 2)
		{
			//����ѭ��
			myCursor.moveToPrevious();
		}
		else if(playMode == 3)
		{
			//�������
			int readomNum = (int)(Math.random() * listNum);
			myCursor.moveToPosition(readomNum - 1);
		}
		
		if(myCursor != null)
		{
			Log.w("LocalService","[next]:start to Get filepath!\n");
			FilePath = myCursor.getString(myCursor.getColumnIndexOrThrow(MediaColumns.DATA));
			Log.i("LocalService","music file is" + FilePath);
			if(FilePath != null)
				setPrepareAndPlay(FilePath);
			else
				Log.e("LocalService","[next]:filepath is null!\n");
		}
		else
			Log.w("LocalService","[next]:myCursor is null!\n");
	}

	public boolean isPlaying()
	{
		if(myPlayer != null)
			return myPlayer.isPlaying();
		else
			return false;
	}
	
	public int getDuration()
	{
		if(myPlayer != null)
			return myPlayer.getDuration();
		else
			return -1;
	}

	public int getPosition()
	{
		if(myPlayer != null)
			return myPlayer.getCurrentPosition();
		else
			return -1;
	}
	
	public void setPosition(int Position)
	{
		myPlayer.seekTo(Position);
		if(myPlayer.isPlaying() == false)
		{
			myPlayer.start();
			isPausing = false;
		}
	}
	
	public String getSongName()
	{
		String name = "K��֮��";
		if(myCursor != null)
		{
			//myCursor.get
		}
		
		return name;
	}

	@Override
	public void onDestroy()
	{
		myPlayer.stop();
		myPlayer.release();
		Log.w("LocalService","[onDestroy]:onDestroy is excute!\n");
		super.onDestroy();
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
		Log.w("LocalService","[onUnbind]:onUnbind is excute!\n");
		return super.onUnbind(intent);
	}

	public long seek(long whereto)
	{
		myPlayer.seekTo((int) whereto);
		return whereto;
	}
}
