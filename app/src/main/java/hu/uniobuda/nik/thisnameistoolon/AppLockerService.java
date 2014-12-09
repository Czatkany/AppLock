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

public class AppLockerService extends Service {
    public static final String TAG = "hu.uniobuda.nik.thisnameistoolon.AppLockerService";
    private final Handler handler = new Handler();
    private ArrayList<String> packageNames = new ArrayList<String>();
   // private int firstRun = 0;
    public AppLockerService() {
    }

    //In the main function there is a handle.postDelayer loop which restarts in every 100 millisec by recalling itself
    //The loop stops only, when the ForegroundApp finds a selected app.
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.postDelayed(new Runnable() {
            public void run() {
                if (ForegroundApp() == 1) {
                    handler.postDelayed(this, 100);
                }
            }
        }, 100);
        return 1;
    }

    //This function checks the actual foreground app.
    //If the foreground app matches one of the selected apps, the function
    //starts the code activity and stops the handler loop, by returning 0.
    //The applocker app is selected by default.
    //When the user restarts the beforehand locked app after imputing the right code, the background service is terminated.
    public int ForegroundApp() {
        loadArray();
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
                    Intent i = new Intent();
                    i.setClass(this, CodeWindowActivity.class);
                    i.putExtra("foundApp",s);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(i);
                    return 0;
                }
            }
        }
        return 1;
    }

    //This function refreshes applist by reloading it from the shared preferences file.
    public void loadArray() {
        SharedPreferences settings = getSharedPreferences("SavedAppList", 0);
        packageNames.clear();
        int size = settings.getInt("Status_size", 0);
        for(int i=0;i<size;i++)
        {
            packageNames.add(settings.getString("Status_" + i, null));
        }
    }
    public IBinder onBind(Intent intent) {
        return null;
    }}
