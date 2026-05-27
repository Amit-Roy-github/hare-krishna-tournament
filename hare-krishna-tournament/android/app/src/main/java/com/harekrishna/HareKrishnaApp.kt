package com.harekrishna

import android.app.Application
import com.harekrishna.di.ServiceLocator

class HareKrishnaApp : Application() {

    lateinit var services: ServiceLocator
        private set

    override fun onCreate() {
        super.onCreate()
        services = ServiceLocator(this)
    }
}
