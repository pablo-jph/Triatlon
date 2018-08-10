package com.cardmovil.cardmovil.ayudas.mapa;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;


public class DetectedActivitiesIntentService extends IntentService {

    private static final String TAG = DetectedActivitiesIntentService.class.getSimpleName();

    public DetectedActivitiesIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

        DetectedActivity detectedActivity = result.getMostProbableActivity();

        int type = detectedActivity.getType();

        if (DetectedActivity.ON_FOOT == type) {
            type = walkingOrRunning(result.getProbableActivities());
        }

        Intent localIntent = new Intent(Constants.BROADCAST_ACTION);

        localIntent.putExtra(Constants.ACTIVITY_KEY, type);

        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }

    private int walkingOrRunning(List<DetectedActivity> probableActivities) {
        int walkActivity = 0, runningActivity = 0;

        for (DetectedActivity probableActivity : probableActivities) {
            if (probableActivity.getType() == DetectedActivity.WALKING) {
                walkActivity = probableActivity.getConfidence();
            } else if (probableActivity.getType() == DetectedActivity.RUNNING) {
                runningActivity = probableActivity.getConfidence();
            }
        }

        return (walkActivity >= runningActivity) ? DetectedActivity.WALKING : DetectedActivity.RUNNING;
    }
}