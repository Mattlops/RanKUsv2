package com.example.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import java.util.Calendar

class MidnightReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = context.getSharedPreferences(MidnightReminderScheduler.PREFS_NAME, Context.MODE_PRIVATE)
        val isEnabled = prefs.getBoolean(MidnightReminderScheduler.KEY_REMINDER_ENABLED, true)

        if (!isEnabled && intent.action != Intent.ACTION_BOOT_COMPLETED) {
            return
        }

        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            if (isEnabled) {
                MidnightReminderScheduler.scheduleMidnightReminder(context)
            }
            return
        }

        // Send Push Notification
        MidnightReminderScheduler.createNotificationChannel(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, MidnightReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🌙 Jogos atualizados no RanKUs!")
            .setContentText("A virada da meia-noite chegou! Termo Quarteto, Globle e SizeItUp já têm novos desafios. Registre seu resultado!")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "A virada da meia-noite chegou! Termo Quarteto, Globle e SizeItUp já têm novos desafios disponíveis. Venha registrar sua pontuação e manter sua sequência ativa!"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(MidnightReminderScheduler.NOTIFICATION_ID_MIDNIGHT, notification)

        // Reschedule for next midnight
        MidnightReminderScheduler.scheduleMidnightReminder(context)
    }
}

object MidnightReminderScheduler {
    const val PREFS_NAME = "rankus_reminders"
    const val KEY_REMINDER_ENABLED = "midnight_reminder_enabled"
    const val CHANNEL_ID = "rankus_midnight_reminder"
    const val NOTIFICATION_ID_MIDNIGHT = 1001
    const val NOTIFICATION_ID_CONFIRMATION = 1002
    private const val ALARM_REQUEST_CODE = 2001

    fun isReminderEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_REMINDER_ENABLED, true)
    }

    fun setReminderEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_REMINDER_ENABLED, enabled).apply()

        if (enabled) {
            scheduleMidnightReminder(context)
            sendConfirmationNotification(context)
        } else {
            cancelMidnightReminder(context)
        }
    }

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Lembretes da Meia-Noite"
            val descriptionText = "Notificações diárias às 00:00 da atualização dos jogos diários."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun scheduleMidnightReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val intent = Intent(context, MidnightReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Next midnight: 00:00:00 of tomorrow
        val calendar = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (_: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelMidnightReminder(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, MidnightReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    fun sendConfirmationNotification(context: Context) {
        createNotificationChannel(context)

        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            1,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🔔 Lembrete da Meia-Noite Ativado!")
            .setContentText("Você receberá uma notificação pontualmente às 00:00 quando os novos desafios diários virarem.")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "Tudo pronto! O RanKUs avisará você diariamente às 00h00 assim que os novos jogos do Termo Quarteto, Globle e SizeItUp estiverem disponíveis para pontuar."
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID_CONFIRMATION, notification)
    }
}
