package cloud.valetudo.companion.screens.provisioning.selection.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import cloud.valetudo.companion.services.WifiService

class ProvisioningModeSelectionViewModel(application: Application) : AndroidViewModel(application) {
    private val wifiService = WifiService
        .fromContext(application.applicationContext)

    val connectedToRobotLiveData = wifiService.connectedToRobot.asLiveData()
}