package dev.playground.codexcompanion

import android.app.Application
import android.content.Context
import dev.playground.codexcompanion.data.CompanionStore

class CompanionApplication : Application() {
    val appContainer: AppContainer by lazy { AppContainer(this) }
}

class AppContainer(context: Context) {
    val store: CompanionStore = CompanionStore(context)
}
