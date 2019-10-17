package de.danoeh.antennapod.config;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import de.danoeh.antennapod.activity.MainActivity;
import de.danoeh.antennapod.core.GpodnetCallbacks;
import de.danoeh.antennapod.core.InaudibleCallbacks;


public class InaudibleCallbacksImpl implements InaudibleCallbacks {

    @Override
    public PendingIntent getInaudibleSyncServiceErrorNotificationPendingIntent(Context context) {
        return PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
