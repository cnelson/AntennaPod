package de.danoeh.antennapod.core.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.collection.ArrayMap;
import androidx.core.app.NotificationCompat;
import androidx.core.app.SafeJobIntentService;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import de.danoeh.antennapod.core.ClientConfig;
import de.danoeh.antennapod.core.R;
import de.danoeh.antennapod.core.export.opml.OpmlElement;
import de.danoeh.antennapod.core.export.opml.OpmlReader;
import de.danoeh.antennapod.core.feed.Feed;
import de.danoeh.antennapod.core.feed.FeedItem;
import de.danoeh.antennapod.core.feed.FeedMedia;
import de.danoeh.antennapod.core.preferences.InaudiblePreferences;
import de.danoeh.antennapod.core.preferences.UserPreferences;
import de.danoeh.antennapod.core.service.download.AntennapodHttpClient;
import de.danoeh.antennapod.core.storage.DBReader;
import de.danoeh.antennapod.core.storage.DBTasks;
import de.danoeh.antennapod.core.storage.DBWriter;
import de.danoeh.antennapod.core.storage.DownloadRequestException;
import de.danoeh.antennapod.core.storage.DownloadRequester;
import de.danoeh.antennapod.core.util.LangUtils;
import de.danoeh.antennapod.core.util.NetworkUtils;
import de.danoeh.antennapod.core.util.gui.NotificationUtils;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.Request;

public class InaudibleSyncService extends SafeJobIntentService {

    private static final String TAG = "InaudibleSyncService";

    private static final long WAIT_INTERVAL = 1000L;

    private static final String ARG_ACTION = "action";

    private static final String ACTION_SYNC = "de.danoeh.antennapod.intent.action.sync";

    private static final AtomicInteger syncActionCount = new AtomicInteger(0);

    private static final int JOB_ID = -91600;

    private static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, InaudibleSyncService.class, JOB_ID, intent);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        final String action = intent.getStringExtra(ARG_ACTION);
        if (action != null) {
            switch(action) {
                case ACTION_SYNC:
                    Log.d(TAG, String.format("Waiting %d milliseconds before syncing", WAIT_INTERVAL));
                    int syncActionId = syncActionCount.incrementAndGet();
                    try {
                        Thread.sleep(WAIT_INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (syncActionId == syncActionCount.get()) {
                        // onHandleWork was not called again in the meantime
                        sync();
                    }
                    return;
                default:
                    Log.e(TAG, "Received invalid intent: action argument is invalid");
            }
        } else {
            Log.e(TAG, "Received invalid intent: action argument is null");
        }
    }


    private synchronized void sync() {
        if (!NetworkUtils.networkAvailable()) {
            stopForeground(true);
            stopSelf();
            return;
        }

        Log.d(TAG, "Syncing from: " + InaudiblePreferences.getURL());
        OkHttpClient client = AntennapodHttpClient.getHttpClient();
        Response response = null;

        try {
            Request request = new Request.Builder()
                    .url(InaudiblePreferences.getURL())
                    .build();
            response = client.newCall(request).execute();
            OpmlReader opmlReader = new OpmlReader();
            final List<String> localSubscriptions = DBReader.getFeedListDownloadUrls();

            for (OpmlElement element : opmlReader.readDocument(response.body().charStream())) {
                Log.d(TAG, "Got: "+element.getText());
                if (!localSubscriptions.contains(element.getXmlUrl())) {
                    Log.d(TAG, "Adding new feed: " + element.getXmlUrl());
                    Feed feed = new Feed(element.getXmlUrl(), null,
                            element.getText());
                    DownloadRequester.getInstance().downloadFeed(this, feed);
                }

            }
        } catch (Exception e) {
            // TODO: quiet this once we see whats up
            Log.d(TAG, Log.getStackTraceString(e));
            updateErrorNotification(e);
            return;
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    private void clearErrorNotifications() {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        nm.cancel(R.id.notification_inaudible_sync_error);
    }

    private void updateErrorNotification(Exception exception) {
        Log.d(TAG, "Posting error notification");

        if (!InaudiblePreferences.notificationsEnabled()) {
            return;
        }

        final String title;
        final String description;
        final int id;
        title = getString(R.string.inaudible_error_title);
        description = getString(R.string.inaudible_error_descr) + exception.getMessage();
        id = R.id.notification_inaudible_sync_error;

        PendingIntent activityIntent = ClientConfig.inaudibleCallbacks.getInaudibleSyncServiceErrorNotificationPendingIntent(this);
        Notification notification = new NotificationCompat.Builder(this, NotificationUtils.CHANNEL_ID_ERROR)
                .setContentTitle(title)
                .setContentText(description)
                .setContentIntent(activityIntent)
                .setSmallIcon(R.drawable.stat_notify_sync_error)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build();
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(id, notification);
    }

    public static void sendSyncIntent(Context context) {
            Intent intent = new Intent(context, InaudibleSyncService.class);
            intent.putExtra(ARG_ACTION, ACTION_SYNC);
            enqueueWork(context, intent);
    }
}
