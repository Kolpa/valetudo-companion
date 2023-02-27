package cloud.valetudo.companion.screens.robots.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import cloud.valetudo.companion.repositories.ValetudoInstancesRepository

class AvailableRobotsViewModel(application: Application) : AndroidViewModel(application) {
    private val valetudoInstancesRepository = ValetudoInstancesRepository
        .fromContext(application.applicationContext)

    val devicesLiveData = valetudoInstancesRepository.valetudoInstances.asLiveData()
}