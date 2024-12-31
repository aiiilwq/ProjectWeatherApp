package com.example.weatherapp

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.weatherapp.screens.MainScreen
import com.example.weatherapp.ui.theme.WeatherAppTheme
import com.example.weatherapp.model.NotificationHelper
import java.util.*

class MainActivity : ComponentActivity() {

    companion object {
        const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверка разрешения на отправку уведомлений
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                NOTIFICATION_PERMISSION_REQUEST_CODE
            )
        } else {
            // Создание канала уведомлений
            val notificationHelper = NotificationHelper(this) // Создаем экземпляр NotificationHelper
            notificationHelper.createNotificationChannel() // Вызываем метод создания канала
        }

        setContent {
            // Создаём состояние внутри @Composable
            var isDarkTheme by remember { mutableStateOf(false) }

            // Определение времени суток для выбора темы
            val isNight = Calendar.getInstance().get(Calendar.HOUR_OF_DAY) in 18..23
            if (isNight) {
                isDarkTheme = true
            }

            // Применение темы
            WeatherAppTheme(darkTheme = isDarkTheme) {
                Surface {
                    MainScreen(
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = { isDarkTheme = !isDarkTheme }
                    )
                }
            }
        }
    }

    // Обработка результата запроса разрешений
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Разрешение предоставлено, создаем канал уведомлений
                val notificationHelper = NotificationHelper(this)
                notificationHelper.createNotificationChannel()
            } else {
                Toast.makeText(this, "Notification permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
