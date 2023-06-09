package com.udacity.location_reminders.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.udacity.location_reminders.BuildConfig
import com.udacity.location_reminders.R
import com.udacity.location_reminders.view.ReminderDescriptionActivity
import com.udacity.location_reminders.view.reminders_list.ReminderDataItem
import timber.log.Timber

private const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID + ".channel"

fun sendNotification(context: Context, reminderDataItem: ReminderDataItem) {
    val notificationManager = context
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    // We need to create a NotificationChannel associated with our CHANNEL_ID before sending a notification.
    createNotificationChannel(notificationManager, context)

    val intent = ReminderDescriptionActivity.newIntent(context.applicationContext, reminderDataItem)

    //create a pending intent that opens ReminderDescriptionActivity when the user clicks on the notification
    val stackBuilder = TaskStackBuilder.create(context)
        .addParentStack(ReminderDescriptionActivity::class.java)
        .addNextIntent(intent)

    val pendingIntentNotificationId =
        "${reminderDataItem.getGlobalyUniqueId()}#pending-intent".hashCode()
    Timber.i(
        "NotId :: Reminder title \"${reminderDataItem.title}\" :: " +
                "Notification id $pendingIntentNotificationId"
    )
    val notificationPendingIntent = stackBuilder
        .getPendingIntent(
            pendingIntentNotificationId,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

//    build the notification object with the data to be shown
    val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(reminderDataItem.title)
        .setContentText(reminderDataItem.location)
        .setContentIntent(notificationPendingIntent)
        .setAutoCancel(true)
        .build()

    Timber.i(
        "NotId :: Reminder title \"${reminderDataItem.title}\" :: " +
                "Notification id ${reminderDataItem.getGlobalyUniqueId().hashCode()}"
    )
    notificationManager.notify(reminderDataItem.getGlobalyUniqueId().hashCode(), notification)
}

private fun createNotificationChannel(
    notificationManager: NotificationManager,
    context: Context
) {
    if (notificationManager.getNotificationChannel(NOTIFICATION_CHANNEL_ID) == null) {
        val name = context.getString(R.string.app_name)
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }
}
