package com.pocketlawbook.alaska

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pocketlawbook.alaska.di.AppContainer
import com.pocketlawbook.alaska.ui.navigation.PocketLawbookApp
import com.pocketlawbook.alaska.ui.theme.PocketLawbookTheme

class MainActivity : ComponentActivity() {

    private val container by lazy { AppContainer(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // LegalContentRefreshScheduler is not wired up here: there is no
        // LegalContentSyncRepository implementation yet, so the scheduled
        // worker would do nothing every 24 hours forever. Re-enable this
        // once a real sync backend exists (see LegalContentSyncRepository).

        setContent {
            PocketLawbookTheme {
                PocketLawbookApp(container = container)
            }
        }
    }
}
