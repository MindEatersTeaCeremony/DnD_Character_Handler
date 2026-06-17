package com.dndcharacterhandler

import android.app.Application

class DndApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
