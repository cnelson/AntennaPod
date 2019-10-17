package de.danoeh.antennapod.core;

import android.app.PendingIntent;
import android.content.Context;

/**
 * Callbacks related to the Inaudible integration
 */
public interface InaudibleCallbacks {

    PendingIntent getInaudibleSyncServiceErrorNotificationPendingIntent(Context context);
}
