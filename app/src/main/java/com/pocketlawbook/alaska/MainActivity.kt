package com.pocketlawbook.alaska

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pocketlawbook.alaska.di.AppContainer
import com.pocketlawbook.alaska.ui.navigation.PocketLawbookApp
import com.pocketlawbook.alaska.ui.theme.PocketLawbookTheme

class MainActivity : ComponentActivity() {

    private val container = AppContainer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PocketLawbookTheme {
                PocketLawbookApp(container = container)
            }
        }
    }
}
