package com.example.weatherapp.screens

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.weatherapp.R
import com.example.weatherapp.model.NotificationHelper
import com.example.weatherapp.model.WeatherResponse
import com.example.weatherapp.network.ApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainScreen(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    var weatherInfo by remember { mutableStateOf("Loading...") }
    var cityName by remember { mutableStateOf("Aqtobe") }
    var searchQuery by remember { mutableStateOf("") }
    var weatherIconUrl by remember { mutableStateOf("") }
    var forecastData by remember { mutableStateOf<List<Triple<String, String, String>>>(emptyList()) }
    var selectedTab by remember { mutableStateOf("Hours") }
    var isCelsius by remember { mutableStateOf(true) }
    var autoRefreshEnabled by remember { mutableStateOf(true) }

    // State for current location and notifications
    var currentLocationEnabled by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val coroutineScope = rememberCoroutineScope()

    // Convert Celsius to Fahrenheit
    fun convertToFahrenheit(celsius: Double): Double {
        return (celsius * 9 / 5) + 32
    }

    // Function to fetch weather information based on city name
    fun fetchWeatherAndForecast() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.getCurrentWeather(
                    apiKey = "ff4af17811ae4f2680a152755241010",
                    location = cityName
                )
                withContext(Dispatchers.Main) {
                    val temperature = if (isCelsius) response.current.temp_c else convertToFahrenheit(response.current.temp_c)
                    weatherInfo = """
                        ${response.location.name}
                        ${"%.1f".format(temperature)}°${if (isCelsius) "C" else "F" }
                        ${response.current.condition.text}
                    """.trimIndent()
                    weatherIconUrl = "https:${response.current.condition.icon}"

                    forecastData = if (selectedTab == "Hours") {
                        generateHourlyForecast(response)
                    } else {
                        generateDailyForecast(response)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    weatherInfo = "Error: ${e.message}"
                }
            }
        }
    }

    LaunchedEffect(notificationsEnabled) {
        // Listen for changes to notificationEnabled
        if (notificationsEnabled) {
            // Trigger notification
            fetchWeatherAndForecast()
        }
    }

    // Function to send notifications
    fun sendNotification(context: Context, temperature: String, condition: String) {
        // Create a NotificationHelper instance
        val notificationHelper = NotificationHelper(context)

        // Call the public showNotification method
        notificationHelper.showNotification(
            "Weather Update",
            "Current Temperature: $temperature°C, Condition: $condition"
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.55f)
                    .background(Color.White.copy(alpha = 0.8f))
                    .padding(16.dp)
            ) {
                NavigationItem(
                    label = "Home",
                    currentScreen = currentScreen,
                    targetScreen = Screen.Home,
                    coroutineScope = coroutineScope,
                    drawerState = drawerState,
                    onClick = { currentScreen = it },
                    buttonColor = Color.Green
                )
                Spacer(modifier = Modifier.height(16.dp))
                NavigationItem(
                    label = "Manage Locations",
                    currentScreen = currentScreen,
                    targetScreen = Screen.Locations,
                    coroutineScope = coroutineScope,
                    drawerState = drawerState,
                    onClick = { currentScreen = it },
                    buttonColor = Color.Green
                )
                Spacer(modifier = Modifier.height(16.dp))
                NavigationItem(
                    label = "Settings",
                    currentScreen = currentScreen,
                    targetScreen = Screen.Settings,
                    coroutineScope = coroutineScope,
                    drawerState = drawerState,
                    onClick = { currentScreen = it },
                    buttonColor = Color.Green
                )
                Spacer(modifier = Modifier.height(16.dp))
                NavigationItem(
                    label = "Help",
                    currentScreen = currentScreen,
                    targetScreen = Screen.Help,
                    coroutineScope = coroutineScope,
                    drawerState = drawerState,
                    onClick = { currentScreen = it },
                    buttonColor = Color.Green
                )
            }
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDarkTheme) Color(0xFF121212) else Color(0xFFF5F5F5))
                .padding(16.dp)
        ) {
            when (currentScreen) {
                Screen.Home -> {
                    // Your home screen content here
                    Column {
                        WeatherCard(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { searchQuery = it },
                            onSearch = {
                                cityName = searchQuery.trim()
                                fetchWeatherAndForecast()
                            },
                            weatherInfo = weatherInfo,
                            weatherIconUrl = weatherIconUrl,
                            isDarkTheme = isDarkTheme,
                            onThemeToggle = onThemeToggle,
                            onRefreshWeather = { fetchWeatherAndForecast() },
                            onMenuToggle = {
                                coroutineScope.launch {
                                    if (drawerState.isClosed) {
                                        drawerState.open()
                                    } else {
                                        drawerState.close()
                                    }
                                }
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        TabSwitcher(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ForecastList(forecastData = forecastData)
                    }
                }
                Screen.Locations -> {
                    // Add your ManageLocationScreen content here
                    Column {
                        ManageLocationScreen()
                        Button { currentScreen = Screen.Home }
                    }
                }
                Screen.Settings -> {
                    // Settings content with notifications toggle
                    SettingsScreen(
                        isCelsius = isCelsius,
                        onUnitToggle = { isCelsius = !isCelsius },
                        autoRefreshEnabled = autoRefreshEnabled,
                        onAutoRefreshToggle = { autoRefreshEnabled = !autoRefreshEnabled },
                        currentLocationEnabled = currentLocationEnabled,
                        onCurrentLocationToggle = {
                            currentLocationEnabled = !currentLocationEnabled
                        },
                        notificationsEnabled = notificationsEnabled,
                        onNotificationsToggle = { notificationsEnabled = it },
                        onBackPressed = {
                            currentScreen = Screen.Home
                        }
                    )
                    Button { currentScreen = Screen.Home }
                }
                Screen.Help -> {
                    // Add your HelpScreen content here
                    Column {
                        HelpScreen(
                            onBackPressed = {
                                currentScreen = Screen.Home
                            }
                        )
                        Button { currentScreen = Screen.Home }
                    }
                }
                else -> {
                    // Optional fallback case for unexpected scenarios
                    Text("Unknown screen selected")
                }
            }
        }
    }
}

@Composable
fun NavigationItem(
    label: String,
    currentScreen: Screen,
    targetScreen: Screen,
    coroutineScope: CoroutineScope,
    drawerState: DrawerState,
    onClick: (Screen) -> Unit,
    buttonColor: Color
) {
    Button(
        onClick = {
            coroutineScope.launch { drawerState.close() }
            onClick(targetScreen)
        },
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = label, color = Color.White)
    }
}

@Composable
fun ManageLocationScreen() {
    var city by remember { mutableStateOf("") }
    var cities = remember { mutableStateListOf<String>() }
    var favoriteCities = remember { mutableStateListOf<String>() }
    var weatherData by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    // Function to add city to the list
    fun addCity() {
        if (city.isNotBlank()) {
            cities.add(city.trim())
            city = "" // Clear the input field after adding
        }
    }

    // Function to remove city from the list
    fun removeCity(cityToRemove: String) {
        cities.remove(cityToRemove)
    }

    // Function to add city to favorites
    fun addToFavorites(city: String) {
        if (!favoriteCities.contains(city)) {
            favoriteCities.add(city)
        }
    }

    // Function to remove city from favorites
    fun removeFromFavorites(city: String) {
        favoriteCities.remove(city)
    }

    // Function to fetch weather for a city
    fun fetchWeather(city: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiService.getCurrentWeather(
                    apiKey = "ff4af17811ae4f2680a152755241010", // Replace with your actual API key
                    location = city
                )
                val temperature = "${response.current.temp_c}°C"
                weatherData = weatherData + (city to temperature)
            } catch (e: Exception) {
                weatherData = weatherData + (city to "Error: ${e.message}")
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        // Input field to enter city
        TextField(
            value = city,
            onValueChange = { city = it },
            label = { Text("Enter city") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Button to add city
        Button(onClick = { addCity() }, modifier = Modifier.fillMaxWidth()) {
            Text("Add City")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display the list of cities using LazyColumn
        Text("Cities", style = MaterialTheme.typography.bodyLarge)
        LazyColumn {
            items(cities) { cityItem ->
                ManageLocationCard(
                    title = cityItem,
                    content = {
                        CityItem(
                            city = cityItem,
                            onRemove = { removeCity(cityItem) },
                            onViewWeather = { fetchWeather(cityItem) },
                            weatherData = weatherData,
                            onAddToFavorites = { addToFavorites(cityItem) },
                            isFavorite = favoriteCities.contains(cityItem)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display the list of favorite cities
        Text("Favorite Cities", style = MaterialTheme.typography.bodyLarge)
        LazyColumn {
            items(favoriteCities) { cityItem ->
                ManageLocationCard(
                    title = cityItem,
                    content = {
                        CityItem(
                            city = cityItem,
                            onRemove = { removeCity(cityItem) },
                            onViewWeather = { fetchWeather(cityItem) },
                            weatherData = weatherData,
                            onAddToFavorites = { removeFromFavorites(cityItem) },
                            isFavorite = true
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun CityItem(
    city: String,
    onRemove: () -> Unit,
    onViewWeather: () -> Unit,
    weatherData: Map<String, String>,
    onAddToFavorites: () -> Unit,
    isFavorite: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        weatherData[city]?.let { weatherInfo ->
            Text(
                text = weatherInfo,
                modifier = Modifier.padding(bottom = 8.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Button to view weather for the city
        Button(
            onClick = onViewWeather,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text("View Weather")
        }

        // Button to add/remove city from favorites
        Button(
            onClick = onAddToFavorites,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(if (isFavorite) "Remove from Favorites" else "Add to Favorites")
        }

        // Button to remove city
        Button(
            onClick = onRemove
        ) {
            Text("Remove City")
        }
    }
}

@Composable
fun ManageLocationCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
fun SettingsScreen(
    isCelsius: Boolean,
    onUnitToggle: () -> Unit,
    autoRefreshEnabled: Boolean,
    onAutoRefreshToggle: () -> Unit,
    currentLocationEnabled: Boolean,
    onCurrentLocationToggle: () -> Unit,
    notificationsEnabled: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
    onBackPressed: () -> Unit // Добавляем обработчик для кнопки назад
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Notifications Section
        SettingCard(
            title = "Notifications",
            content = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Enable Notifications")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { onNotificationsToggle(it) }
                    )
                }
            }
        )

        // Temperature Unit Section
        SettingCard(
            title = "Unit",
            content = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Celsius/Fahrenheit")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = isCelsius,
                        onCheckedChange = { onUnitToggle() }
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Auto-refresh Section
        SettingCard(
            title = "Auto-refresh",
            content = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Enable Auto-refresh")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = autoRefreshEnabled,
                        onCheckedChange = { onAutoRefreshToggle() }
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Current Location Section
        SettingCard(
            title = "Current Location",
            content = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Use current location")
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(
                        checked = currentLocationEnabled,
                        onCheckedChange = { onCurrentLocationToggle() }
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // App Version Section
        SettingCard(
            title = "About the App",
            content = {
                Column {
                    Text(text = "Version: 1.0.0")
                    Text(text = "Latest version is already installed")
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Кнопка "Назад" удалена
    }
}


@Composable
fun SettingCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            content()
        }
    }
}

@Composable
fun HelpScreen(onBackPressed: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Вертикальный список для текста
        LazyColumn(
            modifier = Modifier
                .weight(1f, fill = true)
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            item {
                Text(
                    text = """
                        Приветствуем в WeatherApp!
                        Ваш надежный спутник в мире погоды, который всегда подскажет, когда брать зонт, а когда можно наконец-таки оставить его дома.

                        Как использовать приложение:

                        - Открытие приложения: Запустите WeatherApp и получите мгновенный доступ к самой актуальной погоде в вашем регионе.
                        - Выбор города: Просто введите название города в строку поиска, и вы увидите точные данные о температуре, влажности и других погодных условиях.
                        - Прогноз на несколько дней: Не переживайте, если сегодня дождливо! У нас есть прогноз на несколько дней вперед, чтобы вы всегда были готовы к изменениям погоды.
                        - Уведомления: Мы всегда будем держать вас в курсе. Настройте уведомления, чтобы получать предупреждения о плохой погоде.

                        Мы постарались сделать интерфейс простым и удобным, чтобы даже ваш питомец мог с ним справиться! Но в случае непредвиденных ситуаций всегда можно обратиться к нашей справке.

                        Примечание: Приложение было разработано двумя дружными создателями: Алибеком Есенжоловым и Карабурой Айтжановым. Мы надеемся, что WeatherApp будет полезен и сделает вашу жизнь немного удобнее и веселее!

                        Не забывайте, что мы всегда рядом, чтобы сделать ваш день солнечнее, даже если на улице дождь!
                    """.trimIndent(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Кнопка "Назад"
        Button(onBackPressed = onBackPressed)
    }
}

@Composable
fun Button(onBackPressed: () -> Unit) {
    Button(
        onClick = onBackPressed,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
    ) {
        Text("Back", color = Color.White)
    }
}

@Composable
fun WeatherCard(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    weatherInfo: String,
    weatherIconUrl: String,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    onRefreshWeather: () -> Unit,
    onMenuToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ряд с кнопками
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMenuToggle) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_menu),
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Row {
                    ThemeToggleButton(isDarkTheme = isDarkTheme, onThemeToggle = onThemeToggle)
                    IconButton(onClick = onRefreshWeather) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sync),
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Поиск города
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .background(Color.LightGray, RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onSearch() })
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onSearch) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        contentDescription = "Search",
                        tint = Color.White
                    )
                }
            }

            // Иконка погоды и информация
            if (weatherIconUrl.isNotEmpty()) {
                Image(
                    painter = rememberAsyncImagePainter(weatherIconUrl),
                    contentDescription = "Weather Icon",
                    modifier = Modifier.size(64.dp)
                )
            }
            Text(text = weatherInfo, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

sealed class Screen {
    object Home : Screen()
    object Locations : Screen()
    object Settings : Screen()
    object Help : Screen()
}

@Composable
fun TabSwitcher(
    selectedTab: String,
    onTabSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        IconButton(
            onClick = { onTabSelected("Hours") },
            modifier = Modifier
                .background(
                    if (selectedTab == "Hours") MaterialTheme.colorScheme.primary else Color.Gray,
                    shape = RoundedCornerShape(8.dp)
                )
                .size(60.dp)  // Устанавливаем размер кнопки
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_hours),
                contentDescription = "Hours",
                tint = Color.White
            )
        }

        IconButton(
            onClick = { onTabSelected("Days") },
            modifier = Modifier
                .background(
                    if (selectedTab == "Days") MaterialTheme.colorScheme.primary else Color.Gray,
                    shape = RoundedCornerShape(8.dp)
                )
                .size(60.dp)  // Устанавливаем размер кнопки
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_days),
                contentDescription = "Days",
                tint = Color.White
            )
        }
    }
}

@Composable
fun ForecastList(forecastData: List<Triple<String, String, String>>) {
    LazyColumn {
        items(forecastData) { forecastItem ->
            val (time, weatherDescription, temp) = forecastItem
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = time,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    Text(
                        text = weatherDescription,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )

                    Text(
                        text = temp,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun ThemeToggleButton(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit
) {
    IconButton(
        onClick = onThemeToggle
    ) {
        Icon(
            painter = painterResource(id = if (isDarkTheme) R.drawable.ic_sun else R.drawable.ic_moon),
            contentDescription = "Theme Toggle",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

private fun generateHourlyForecast(response: WeatherResponse): List<Triple<String, String, String>> {
    val calendar = Calendar.getInstance()
    val currentTime = calendar.get(Calendar.HOUR_OF_DAY)

    // Text descriptions for weather based on time of day
    val nightWeatherDescriptions = listOf(
        "Clear Night",
        "Partly Cloudy Night",
        "Overcast Night",
        "Rainy Night",
        "Snowfall Night",
        "Windy Night"
    )

    val morningWeatherDescriptions = listOf(
        "Clear Morning",
        "Foggy Morning",
        "Partly Cloudy Morning",
        "Rainy Morning"
    )

    val dayWeatherDescriptions = listOf(
        "Sunny",
        "Partly Cloudy",
        "Cloudy",
        "Rainy",
        "Snowfall",
        "Windy"
    )

    val eveningWeatherDescriptions = listOf(
        "Clear Evening",
        "Partly Cloudy Evening",
        "Overcast Evening",
        "Rainy Evening",
        "Windy Evening"
    )

    return List(24) { i ->
        calendar.add(Calendar.HOUR_OF_DAY, if (i == 0) 0 else 1)
        val hour = SimpleDateFormat("HH:00", Locale.getDefault()).format(calendar.time)

        // Determine the weather description based on time of day
        val weatherDescription = when {
            i in 0..8 -> nightWeatherDescriptions.random() // Night (0:00 - 9:00)
            i in 9..11 -> morningWeatherDescriptions.random() // Morning (9:00 - 12:00)
            i in 12..17 -> dayWeatherDescriptions.random() // Day (12:00 - 18:00)
            else -> eveningWeatherDescriptions.random() // Evening (18:00 - 24:00)
        }

        // Format temperature
        val temp = "%.1f°C".format(response.current.temp_c + (i % 5))

        Triple(hour, weatherDescription, temp)
    }
}

private fun generateDailyForecast(response: WeatherResponse): List<Triple<String, String, String>> {
    val today = Calendar.getInstance()
    val dateFormat = SimpleDateFormat("EEE, dd MMM", Locale.getDefault())

    // Weather descriptions for each day
    val weatherDescriptions = listOf(
        "Sunny",
        "Partly Cloudy",
        "Cloudy",
        "Overcast",
        "Rainy",
        "Snowfall",
        "Windy"
    )

    return List(7) { i ->
        if (i != 0) today.add(Calendar.DAY_OF_YEAR, 1)
        val formattedDate = dateFormat.format(today.time)

        // Pick a weather description for each day
        val weatherDescription = weatherDescriptions.random()

        // Format temperature
        val temp = "%.1f°C".format(response.current.temp_c + (i % 10))

        Triple(formattedDate, weatherDescription, temp)
    }
}