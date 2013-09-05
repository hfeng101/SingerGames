package com.example.singergames;

import com.example.singergames.singgamesApp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;


public class MainActivity extends Activity
{
	private ImageButton playButton;
	private ImageButton pauseButton;
	private ImageButton stopButton;
	private ImageButton nextButton;
	private ImageButton preButton;
	
	private EditText	textInfo;
	
	private String	filePath;
	
	private Cursor myCursor = null;
	private LocalService myService = null;
	
	// 找到拖动条和文本框  
    private SeekBar seekBar = null;

/*************************************************************************************************/
	// 设置拖动条改变监听器  
    private OnSeekBarChangeListener osbcl = new  OnSeekBarChangeListener() 
    { 
        @Override  
        public void onProgressChanged(SeekBar seekBar,  int  progress, 
                boolean  fromUser) { 
            Toast.makeText(getApplicationContext(), "onProgressChanged",Toast.LENGTH_SHORT).show(); 
            //myService.setPosition(seekBar.getProgress());
            //Log.i("MainActivity","current process is " + myService.getPosition());
        } 

        @Override  
        public void onStartTrackingTouch(SeekBar seekBar) { 
            Toast.makeText(getApplicationContext(), "onStartTrackingTouch",Toast.LENGTH_SHORT).show(); 
        } 

        @Override  
        public void onStopTrackingTouch(SeekBar seekBar) { 
            Toast.makeText(getApplicationContext(), "onStopTrackingTouch",Toast.LENGTH_SHORT).show(); 
            myService.setPosition(seekBar.getProgress());
            Log.i("MainActivity","current process is " + myService.getPosition());
            
            playButton.setVisibility(View.INVISIBLE);
			pauseButton.setVisibility(View.VISIBLE);
        } 
    }; 
    
    //进度条更新滚动同步操作，用在线程中
    private Handler mHandle = new Handler(){
    	@Override
		public void handleMessage(Message msg)
		{
    		//会导致举出,加括号也不好，容易得出０
			/*
			int position = ;
			int mMax = myService.getDuration();
			int sMax = seekBar.getMax();
			seekBar.setProgress(position*sMax/mMax); 
			*/
    		if(myService != null)
    			seekBar.setProgress(myService.getPosition());
		}
	};
    
	//进度条更新线程，用于设置进度条自动滚动同步歌曲
	public class DelayThread extends Thread
	{
		int milliseconds;
		public DelayThread(int i)
		{
			milliseconds = i;
		}

		public void run() 
		{
			while(true)
			{
				try 
				{
					sleep(milliseconds);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
			
				mHandle.sendEmptyMessage(0);
			}
		}
	}

    //拖动进度条更新
	public void startSeekbarUpdateThread()
	{
		//开辟Thread 用于定期刷新SeekBar
		DelayThread dThread = new DelayThread(100);
		dThread.start();
	}
	
	protected BroadcastReceiver myPlayerEvtReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if(action.equals(LocalService.PLAYER_PREPARE_END))
			{
				// 设置拖动条的初始值和文本框的初始值  
				Log.i("MainActivity","Recved play prepare end Event and start to set seekbar!");
				Log.i("MainAcitivy","seekbar max is " + seekBar.getMax());
				seekBar.setMax(myService.getDuration()); 
				
				Log.i("MainAcitivy","seekbar max is 2 + " + seekBar.getMax());
				seekBar.setProgress(myService.getPosition()); 
		        // 为拖动条绑定监听器  
		        seekBar.setOnSeekBarChangeListener(osbcl);
		        
		        //设置自动更新滚动条，与歌曲同步
		        startSeekbarUpdateThread();
			}
			else if(action.equals(LocalService.PLAY_COMPLETED))
			{
				Log.i("BackPlay","Play next song,will it be success !!!\n");
			}
		}
	};
	
	private ServiceConnection myPlayerBackConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName className, IBinder service)
		{
			Log.i("MainActivity", "Service connected succussed Before Getting the service handle! \n");
			myService = ((LocalService.LocalBinder)service).getService();
			if(myService == null)
				Log.e("MainActivity","Get service erro r !!!");
			else
				Log.i("MainActivity","Get service succuss! \n");
		}
		
		@Override
		public void onServiceDisconnected(ComponentName className)
		{
			Log.i("MainActivity", "Service connect failed ! \n");
			myService = null;
		}
	};
	
/***********************************************************************************************/	
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		playButton = (ImageButton) findViewById(R.id.play);
		pauseButton = (ImageButton) findViewById(R.id.pause);
		stopButton = (ImageButton) findViewById(R.id.stop);
		nextButton = (ImageButton) findViewById(R.id.next);
		preButton = (ImageButton) findViewById(R.id.pre);
		seekBar = (SeekBar) findViewById(R.id.seekBar1); 
		textInfo = (EditText) findViewById(R.id.editText1);
		
		//在没播放歌曲以前，让seekbar不可以移动
		seekBar.setMax(0);
		
		SharedPreferences settings = getSharedPreferences("Setting_file", 0);
		String CoverShow = settings.getString("LastRandomCover", "<null>");

		startService(new Intent(this, LocalService.class));
		bindService(new Intent(this, LocalService.class), myPlayerBackConnection, Context.BIND_AUTO_CREATE);
		
		if(myService == null)
			Log.w("abcdedf","MyService is null\n");
		else 
			Log.w("abcdedf","MyService started success!!\n");
		System.out.println("After start service \n"); 
		
		playButton.setOnClickListener(new ImageButton.OnClickListener() 
		{
			@Override
 			public void onClick(View v)
			{
				playButton.setVisibility(View.INVISIBLE);
				pauseButton.setVisibility(View.VISIBLE);
			
				/*	１、获取文件路径，如果没有就传空，三种方式，一是默认播放之前的文件，二是播放列表内的文件，三是播放指定的文件
				 *	２、四种模式，一是顺序，二是单循环，三是循环，四是随机
				 *	３、播放列表
				 */
		
				System.out.println("The file Path is " + filePath);
				myService.start(filePath, 0, myCursor);
				
				//清空filePath,避免暂停播放，出现播放的不是同一首歌。
				filePath = null;
			}
		
		});		
		
		pauseButton.setOnClickListener(new ImageButton.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				playButton.setVisibility(View.VISIBLE);
				pauseButton.setVisibility(View.INVISIBLE);
				
				myService.pause();
			}
		});
		
		stopButton.setOnClickListener(new ImageButton.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				playButton.setVisibility(View.VISIBLE);
				pauseButton.setVisibility(View.INVISIBLE);
				
				myService.stop();
			}
		});
		
		preButton.setOnClickListener(new ImageButton.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				playButton.setVisibility(View.INVISIBLE);
				pauseButton.setVisibility(View.VISIBLE);
				
				myService.pre();
			}
		});
		
		nextButton.setOnClickListener(new ImageButton.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				playButton.setVisibility(View.INVISIBLE);
				pauseButton.setVisibility(View.VISIBLE);
				
				myService.next();
			}		
		});
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(LocalService.PLAYER_PREPARE_END);
		filter.addAction(LocalService.PLAY_COMPLETED);
		registerReceiver(myPlayerEvtReceiver, filter);
	}
	
/*************************************************************************************************/
	//按键监听
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  
	{  
		
	    if (keyCode == KeyEvent.KEYCODE_BACK)  
	    {  	    	
	    	// 创建退出对话框  
	    	AlertDialog.Builder isExit = new AlertDialog.Builder(MainActivity.this);
	    	isExit.setTitle("系统提示");
	    	isExit.setIcon(R.drawable.ic_launcher);
	    	isExit.setMessage("确定要退出吗");
	    	
	    	isExit.setPositiveButton("确定",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        	/*
                        	 * 不行，无法关闭
                        	 */
                        	//dialog.cancel();
                        	//onPause();
                        	//onStop();
                        	//onDestroy();
                        	
                        	System.exit(0);
                        }
                    });
			
	    	isExit.setNeutralButton("后台运行",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                        	/*
                        	 * 不行，无法后台运行
        					dialog.cancel();
							onPause();
                        	onStop();
                        	*/
                        	Intent intent = new Intent(Intent.ACTION_MAIN);  
	                    	intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
	                        intent.addCategory(Intent.CATEGORY_HOME);  
	                        startActivity(intent); 
	                        
	                        //生成通知
	                        // 定义通知管理器 NOTIFICATION_SERVICE  
	                        NotificationManager notificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);  
	                        // 定义系统通知  
	                        Notification notification = new Notification(R.drawable.ic_launcher, "K歌之王",  
	                                System.currentTimeMillis()); 
	                        
	                        notification.flags |= Notification.FLAG_ONGOING_EVENT;  
	                        notification.flags |= Notification.FLAG_NO_CLEAR;  
	                        notification.flags |= Notification.FLAG_SHOW_LIGHTS;  
	                        notification.defaults = Notification.DEFAULT_LIGHTS;  
	                        notification.ledARGB = Color.BLUE;  
	                        notification.ledOnMS = 5000;  
	                  
	                        CharSequence contentTitle = "K歌之王";  
	                        CharSequence contentText = myService.getSongName();
	                        
	                        Intent notificationIntent = new Intent();  
	                        notificationIntent.setComponent(getComponentName());  
	                        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
	                        //notificationIntent.setClass(MainActivity.this, XBFActivity.class);  
	                  
	                        PendingIntent contentIntent = PendingIntent.getActivity(MainActivity.this, 0,  
	                                notificationIntent, 0);  
	                  
	                        notification.setLatestEventInfo(MainActivity.this, contentTitle, contentText,  
	                                contentIntent);  
	                  
	                        notificationManager.notify(0, notification); 
                        }
                    });
			
	    	isExit.setNegativeButton("取消",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
	    	isExit.show();
	    	
	    	return true;
	    	
	    }  
	    else if(keyCode == KeyEvent.KEYCODE_HOME)
	    {
	    	
	    }
	    else if(keyCode == KeyEvent.KEYCODE_MENU)
	    {
	    	
	    }
	    
	    return false; 
	}		    
	
/**********************************************************************************************/
	@Override
	protected void onResume()
	{
		super.onResume();
		
		//删除通知
		NotificationManager notificationManager = (NotificationManager) this   
                .getSystemService(NOTIFICATION_SERVICE);   
        notificationManager.cancel(0); 
        
		System.out.println("Resume is execed \n ");
//		myCursor = myMusicFileList.getMusicList();
//		ListAdapter myAdapter = new MusicFileListAdapter(this, android.R.layout.simple_expandable_list_item_2, myCursor, new String[]{}, new int[]{}, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
//		myMusicFileList.setListAdapter(myAdapter);
	}
	
	@Override
	protected void onPause()
	{
		Log.w("MainActivity","Activity will be pause!");
		
		super.onPause();
	}

	
	@Override
	protected void onStop()
	{
		String CoverShow = filePath;
		
		Log.w("MainActivity","Activity will be stop!");
		/*
		if(!CoverShow.equals("<null>"))
		{
			SharedPreferences settings = getSharedPreferences("Settingfile", 0);
			SharedPreferences.Editor setDefault = settings.edit();
			setDefault.putString("LastRandomCover", CoverShow);
			setDefault.commit();
		}
		*/
		
		Log.w("MainActivity","Activity is stoped ???!??");
		super.onStop();
	}

	
	@Override
	protected void onDestroy()
	{
		unregisterReceiver(myPlayerEvtReceiver);
		
		myService = null;
		
		Log.w("MainActivity","[onDestroy]:Activity is stoped ???!??");
		unbindService(myPlayerBackConnection);
		stopService(new Intent(this, LocalService.class));
		
		//System.exit(0);
		super.onDestroy();
		Log.w("MainActivity","[onDestroy]:Activity is stoped ?? YES!");
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
