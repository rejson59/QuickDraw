package com.example.model

enum class ActionType(val label: String) {
    APP("Aplikacja"),
    SYSTEM("Akcja systemowa"),
    WEB_URL("Strona WWW"),
    FLOW("Sekwencja (Flow)")
}

enum class SystemAction(
    val code: String,
    val title: String,
    val description: String,
    val category: String,
    val iconName: String
) {
    // Kontrola i ekran
    FLASHLIGHT_TOGGLE("FLASHLIGHT_TOGGLE", "Włącz / Wyłącz latarkę", "Przełącza diodę LED aparatu", "Kontrola", "Flashlight"),
    HOME_SCREEN("HOME_SCREEN", "Ekran główny", "Powrót do pulpitu Androida", "Kontrola", "Home"),
    NOTIFICATION_SHADE("NOTIFICATION_SHADE", "Pasek powiadomień", "Rozwija górną kurtynę powiadomień", "Kontrola", "NotificationsActive"),
    QUICK_SETTINGS("QUICK_SETTINGS", "Szybkie przełączniki", "Rozwija panel szybkich ustawień systemu", "Kontrola", "Tune"),
    OPEN_CAMERA("OPEN_CAMERA", "Aparat fotograficzny", "Błyskawiczne otwarcie kamery", "Kontrola", "Camera"),
    OPEN_VIDEO_CAMERA("OPEN_VIDEO_CAMERA", "Kamera wideo", "Uruchamia nagrywanie wideo", "Kontrola", "Videocam"),

    // Dźwięk i multimedia
    VOLUME_DIALOG("VOLUME_DIALOG", "Panel głośności", "Wyświetla systemowy suwak głośności", "Dźwięk", "VolumeUp"),
    VOLUME_UP("VOLUME_UP", "Głośniej", "Zwiększa głośność multimediów o jeden krok", "Dźwięk", "VolumeUp"),
    VOLUME_DOWN("VOLUME_DOWN", "Ciszej", "Zmniejsza głośność multimediów o jeden krok", "Dźwięk", "VolumeDown"),
    VOLUME_MUTE("VOLUME_MUTE", "Wycisz multimedia", "Wycisza dźwięk multimediów do zera", "Dźwięk", "VolumeMute"),
    TOGGLE_RINGER("TOGGLE_RINGER", "Tryb dzwonka", "Przełącza tryb: Normalny -> Wibracje -> Cichy", "Dźwięk", "Notifications"),
    MEDIA_PLAY_PAUSE("MEDIA_PLAY_PAUSE", "Muzyka: Play / Pauza", "Wznawia lub zatrzymuje odtwarzacz muzyki", "Dźwięk", "PlayCircle"),
    MEDIA_NEXT("MEDIA_NEXT", "Muzyka: Następny utwór", "Przeskakuje do kolejnego utworu", "Dźwięk", "SkipNext"),
    MEDIA_PREVIOUS("MEDIA_PREVIOUS", "Muzyka: Poprzedni utwór", "Wznawia lub cofa do poprzedniego utworu", "Dźwięk", "SkipPrevious"),

    // Narzędzia
    OPEN_DIALER("OPEN_DIALER", "Telefon / Klawiatura", "Otwiera wybieranie numeru telefonu", "Narzędzia", "Phone"),
    OPEN_CALCULATOR("OPEN_CALCULATOR", "Kalkulator", "Uruchamia systemowy kalkulator", "Narzędzia", "Calculate"),
    OPEN_ALARM_CLOCK("OPEN_ALARM_CLOCK", "Zegar i budzik", "Otwiera alarmy i zegar", "Narzędzia", "Alarm"),
    OPEN_CALENDAR("OPEN_CALENDAR", "Kalendarz", "Otwiera kalendarz systemowy", "Narzędzia", "CalendarMonth"),
    VOICE_SEARCH("VOICE_SEARCH", "Asystent głosowy / Szukaj", "Wywołuje wyszukiwanie głosowe Google", "Narzędzia", "Mic"),

    // Ustawienia systemu
    OPEN_SETTINGS("OPEN_SETTINGS", "Główne ustawienia", "Otwiera główne ustawienia telefonu", "Ustawienia", "Settings"),
    OPEN_WIFI_SETTINGS("OPEN_WIFI_SETTINGS", "Ustawienia Wi-Fi", "Bezpośredni panel konfiguracji Wi-Fi", "Ustawienia", "Wifi"),
    OPEN_BLUETOOTH_SETTINGS("OPEN_BLUETOOTH_SETTINGS", "Ustawienia Bluetooth", "Zarządzanie urządzeniami Bluetooth", "Ustawienia", "Bluetooth"),
    OPEN_DISPLAY_SETTINGS("OPEN_DISPLAY_SETTINGS", "Ekran i jasność", "Ustawienia wyświetlacza, tapety i wygaszacza", "Ustawienia", "Brightness"),
    OPEN_SOUND_SETTINGS("OPEN_SOUND_SETTINGS", "Ustawienia dźwięku", "Konfiguracja dzwonków i poziomów głośności", "Ustawienia", "Audiotrack"),
    OPEN_BATTERY_SETTINGS("OPEN_BATTERY_SETTINGS", "Bateria i zasilanie", "Statystyki zużycia i tryb oszczędzania", "Ustawienia", "Battery"),
    OPEN_APPLICATION_SETTINGS("OPEN_APPLICATION_SETTINGS", "Zarządzanie aplikacjami", "Lista zainstalowanych aplikacji i pamięć", "Ustawienia", "Apps"),
    OPEN_ACCESSIBILITY_SETTINGS("OPEN_ACCESSIBILITY_SETTINGS", "Ułatwienia dostępu", "Funkcje dostępności i wsparcia ekranowego", "Ustawienia", "Accessibility"),
    OPEN_LOCATION_SETTINGS("OPEN_LOCATION_SETTINGS", "Lokalizacja GPS", "Włączanie i zarządzanie lokalizacją", "Ustawienia", "Location"),
    OPEN_AIRPLANE_MODE("OPEN_AIRPLANE_MODE", "Tryb samolotowy", "Przełącznik trybu offline / samolotowego", "Ustawienia", "Airplanemode"),
    OPEN_STORAGE_SETTINGS("OPEN_STORAGE_SETTINGS", "Pamięć telefonu", "Menedżer pamięci wewnętrznej i dysku", "Ustawienia", "Storage"),
    OPEN_SECURITY_SETTINGS("OPEN_SECURITY_SETTINGS", "Bezpieczeństwo i blokada", "Blokada ekranu, biometria i ochrona", "Ustawienia", "Security"),
    OPEN_PRIVACY_SETTINGS("OPEN_PRIVACY_SETTINGS", "Prywatność", "Panel uprawnień aplikacji i ochrona danych", "Ustawienia", "Privacy"),
    OPEN_DATE_SETTINGS("OPEN_DATE_SETTINGS", "Data i godzina", "Ustawienia zegara systemowego i strefy czasowej", "Ustawienia", "Schedule"),
    OPEN_DEVICE_INFO("OPEN_DEVICE_INFO", "O telefonie", "Informacje o telefonie i wersji systemu", "Ustawienia", "Info");

    companion object {
        fun fromCode(code: String): SystemAction {
            return entries.find { it.code == code } ?: HOME_SCREEN
        }
    }
}

data class FlowActionItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val type: ActionType,
    val target: String,
    val label: String,
    val delayMs: Long = 200L
)

data class InstalledApp(
    val packageName: String,
    val label: String,
    val isSystemApp: Boolean = false
)
