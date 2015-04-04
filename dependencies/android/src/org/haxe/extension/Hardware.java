package org.haxe.extension;

import org.haxe.lime.HaxeObject;

import android.os.Vibrator;
import android.graphics.Point;
import android.view.Display;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

//WakeUp
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

//Timer
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.util.Log;


/* 
   You can use the Android Extension class in order to hook
   into the Android activity lifecycle. This is not required
   for standard Java code, this is designed for when you need
   deeper integration.

   You can access additional references from the Extension class,
   depending on your needs:

   - Extension.assetManager (android.content.res.AssetManager)
   - Extension.callbackHandler (android.os.Handler)
   - Extension.mainActivity (android.app.Activity)
   - Extension.mainContext (android.content.Context)
   - Extension.mainView (android.view.View)

   You can also make references to static or instance methods
   and properties on Java classes. These classes can be included 
   as single files using <java path="to/File.java" /> within your
   project, or use the full Android Library Project format (such
   as this example) in order to include your own AndroidManifest
   data, additional dependencies, etc.

   These are also optional, though this example shows a static
   function for performing a single task, like returning a value
   back to Haxe from Java.
   */
public class Hardware extends Extension 
{
    public static Point size;

    private static KeyguardLock keyguardLock=null;
    private static HaxeObject onTimerCallback=null;
    private static boolean timerInited=false;
    private static PendingIntent pendingIntent=null;
    private static AlarmManager alarmManager=null;
    private static BroadcastReceiver mReceiver=null;
    private static int timerRepeatCount=1;

    public static void vibrate(int duration)
    {
        ((Vibrator) mainContext.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(duration);
    }

    public static void wakeUp()
    {
        PowerManager pm = (PowerManager) mainContext.getSystemService(Context.POWER_SERVICE);
        WakeLock wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
        PowerManager.ACQUIRE_CAUSES_WAKEUP, "Hardware.class");
        wakeLock.acquire();
        wakeLock.release();
        wakeLock = null;

        KeyguardManager keyguardManager = (KeyguardManager) mainActivity.getSystemService(Activity.KEYGUARD_SERVICE); 
        if(keyguardLock == null)
        {
            keyguardLock = keyguardManager.newKeyguardLock(Activity.KEYGUARD_SERVICE); 
        }
        keyguardLock.disableKeyguard();
    }

    public static int getScreenHeight()
    {
        return size.y;
    }

    public static int getScreenWidth()
    {
        return size.x;
    }

    public static void initTimer()
    {
        timerInited=true;
        mReceiver = new BroadcastReceiver() 
        {
            @Override public void onReceive(Context context, Intent intent)
            {
                timeHandler();
            }
        };
        String tag="com.alagatar.quidytime.timer";
        mainActivity.registerReceiver(mReceiver, new IntentFilter(tag));
        pendingIntent = PendingIntent.getBroadcast(mainActivity, 0, new Intent(tag), 0);
        alarmManager = (AlarmManager) mainContext.getSystemService(Context.ALARM_SERVICE);
    }

    public static void unregisterAlarm()
    {
        stopTimer();
        mainContext.unregisterReceiver(mReceiver);

        alarmManager=null;
        pendingIntent=null;
        mReceiver=null;
    }

    public static void startTimer(int time, HaxeObject callback)
    {
        onTimerCallback=callback;
        timerRepeatCount=0;

        if(timerInited == false)
        {
            initTimer();
        }
        stopTimer();
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + time, pendingIntent);
    }

    public static void startRepeatTimer(int time, int repeatCount, HaxeObject callback)
    {
        onTimerCallback=callback;
        timerRepeatCount=repeatCount;

        if(timerInited == false)
        {
            initTimer();
        }
        stopTimer();
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + time, time, pendingIntent);
    }

    public static void timeHandler()
    {
        if(timerRepeatCount > 0)
        {
            timerRepeatCount--;
        }
        
        if(timerRepeatCount == 0)
        {
            stopTimer();
        }

        if(timerRepeatCount == 0 || timerRepeatCount == -1)
        {
            completeTimer();
        }
    }

    public static void completeTimer()
    {
        if(onTimerCallback == null)
        {
            return;
        }

        Extension.callbackHandler.post(new Runnable ()
        {
            @Override public void run()
            {
                onTimerCallback.call0("completeTimer");
            }
        });
    }

    public static void stopTimer()
    {
        if(alarmManager != null)
        {
            alarmManager.cancel(pendingIntent);
        }
    }

    /**
    * Called when an activity you launched exits, giving you the requestCode 
    * you started it with, the resultCode it returned, and any additional data 
    * from it.
    */
    public boolean onActivityResult (int requestCode, int resultCode, Intent data) 
    {
        return true;
    }

    /**
    * Called when the activity is starting.
    */
    public void onCreate (Bundle savedInstanceState) 
    {
    }

    /**
    * Perform any final cleanup before an activity is destroyed.
    */
    public void onDestroy () 
    {
        if(keyguardLock != null)
        {
            keyguardLock.reenableKeyguard();
            keyguardLock = null;
        }

        if(timerInited == true)
        {
            unregisterAlarm();
        }

        if(onTimerCallback != null)
        {
            onTimerCallback=null;
        }
    }

    /**
    * Called as part of the activity lifecycle when an activity is going into
    * the background, but has not (yet) been killed.
    */
    public void onPause () 
    {
        if(keyguardLock != null)
        {
            keyguardLock.reenableKeyguard();
        }
    }

    /**
    * Called after {@link #onStop} when the current activity is being 
    * re-displayed to the user (the user has navigated back to it).
    */
    public void onRestart () 
    {
    }

    /**
    * Called after {@link #onRestart}, or {@link #onPause}, for your activity 
    * to start interacting with the user.
    */
    public void onResume () 
    {
    }

    /**
    * Called after {@link #onCreate} &mdash; or after {@link #onRestart} when  
    * the activity had been stopped, but is now again being displayed to the 
    * user.
    */
    public void onStart () 
    {
        Display display = mainActivity.getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
    }

    /**
    * Called when the activity is no longer visible to the user, because 
    * another activity has been resumed and is covering this one. 
    */
    public void onStop () 
    {
    }
}
