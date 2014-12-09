package hu.uniobuda.nik.thisnameistoolon;

import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;

import java.util.List;

public class RestartAppLockerService extends Service {

    public static final String TAG = "hu.uniobuda.nik.thisnameistoolon.RestartAppLockerService";
    private final Handler handler = new Handler();

    public RestartAppLockerService() {
    }

    //This service starts, when the selected app starts, and runs until the user goes back to the home screen.
    //When the user leaves the app, it restarts the applocker service
    public int onStartCommand(Intent intent, int flags, int startId) {
        Runnable runnable;
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                if(ForegroundApp() == 1)
                {
                    handler.postDelayed(this, 100);
                }
            }
        }, 100);
        return 1;

    }

    public int ForegroundApp() {
        ActivityManager mActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> RunningTask = mActivityManager.getRunningTasks(1);
        ActivityManager.RunningTaskInfo ar = RunningTask.get(0);
        String activityOnTop = ar.topActivity.getPackageName();
        if (activityOnTop.contains("home")) {
            startService(new Intent(RestartAppLockerService.this, AppLockerService.class));
            return 0;
        }
        if (activityOnTop.matches("com.example.tomi.applock"))
        {
            return 0;
        }
        return 1;
    }
    public IBinder onBind(Intent intent) {
        return null;
    }
}

