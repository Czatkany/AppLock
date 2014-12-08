package hu.uniobuda.nik.thisnameistoolon;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tomi on 2014.12.06..
 */
public class AppFinderService extends Service{
    private ArrayList<String> packageNames = new ArrayList<String>();
    private Handler handler = new Handler();
    private Runnable runnable;

    public AppFinderService() {

    }
    //This is the background service witch is watching the launched applications.
    //The onStartCommand function reads the selected application list,
    // and runs, until closed by the exit button in the main window.
    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        loadArray(this);

        //This handler is used as a scheduler.
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                AppBlocker();
                handler.postDelayed(this, 100);
            }
        }, 100);
        return 1;
    }

    public void AppBlocker()
    {
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> RunningTask = mActivityManager.getRunningTasks(1);
        ActivityManager.RunningTaskInfo ar = RunningTask.get(0);
        String activityOnTop = ar.topActivity.getPackageName();
        if (activityOnTop.length() > 0) {
            for (String s : packageNames) {
                if (s.matches(activityOnTop)) {
                    Intent startHomeScreen=new Intent(Intent.ACTION_MAIN);
                    startHomeScreen.addCategory(Intent.CATEGORY_HOME);
                    startHomeScreen.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(startHomeScreen);
                }
            }
        }
    }
    //This function gets the selected apps list.
    public void loadArray(Context mContext)
    {
        SharedPreferences settings = getSharedPreferences("MyPrefFile", 0);
        packageNames.clear();
        int size = settings.getInt("Status_size", 0);

        for(int i=0;i<size;i++)
        {
            packageNames.add(settings.getString("Status_" + i, null));
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
