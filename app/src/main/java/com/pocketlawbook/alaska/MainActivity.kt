package com.pocketlawbook.alaska

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.pocketlawbook.alaska.data.legal.LegalContentRefreshScheduler
import com.pocketlawbook.alaska.di.AppContainer
import com.pocketlawbook.alaska.ui.navigation.PocketLawbookApp
import com.pocketlawbook.alaska.ui.theme.PocketLawbookTheme

class MainActivity : ComponentActivity() {

    private val container by lazy { AppContainer(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Android decides the exact execution time for periodic work. This
        // schedules a best-effort daily network check without blocking startup.
        LegalContentRefreshScheduler.schedule(applicationContext)

        setContent {
            PocketLawbookTheme {
                PocketLawbookApp(container = container)
            }
        }
    }
}
