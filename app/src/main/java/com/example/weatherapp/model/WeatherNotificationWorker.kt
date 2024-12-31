package com.example.weatherapp.model

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.weatherapp.MainActivity
import com.example.weatherapp.R

class NotificationHelperWorker(private val context: Context) {

    // Создание канала уведомлений
    fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channelId = "default_channel"
            val channelName = "Default Channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = android.app.NotificationChannel(channelId, channelName, importance).apply {
                description = "This is the default notification channel"
            }

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Метод для отображения уведомления о погоде
    companion object {
        fun showWeatherNotification(context: Context, temperature: String, condition: String) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notificationId = 1
            val channelId = "default_channel"

            // Создаем Intent для открытия приложения при нажатии на уведомление
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Создаем уведомление
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_sun) // Укажите иконку (добавьте ic_weather в папку res/drawable)
                .setContentTitle("Current Weather")
                .setContentText("$condition, $temperature")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            // Отображаем уведомление
            notificationManager.notify(notificationId, notification)
        }
    }
}
