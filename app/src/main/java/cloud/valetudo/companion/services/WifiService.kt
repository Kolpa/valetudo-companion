package cloud.valetudo.companion.services

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow


class WifiService(private val connectivityManager: ConnectivityManager, private val wifiManager: WifiManager) {
    private val networkRequest by lazy {
        NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build()
    }

    /*
     *  Will provide true/false indicating if the user is connected to a valetudo powered robot via wifi
     */
    val connectedToRobot = callbackFlow {
        val networkCallback = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> PostSNetworkCallback(this)
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> PostQNetworkCallback(this)
            else -> PreQNetworkCallback(this, wifiManager)
        }

        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    // Yes we really need 3 different ways of acquiring the wifi ssid depending on the android version running on the device

    // Same as the PostQ Version but with the addition of the FLAG_INCLUDE_LOCATION_INFO parameter which is required post android S
    @RequiresApi(Build.VERSION_CODES.S)
    internal class PostSNetworkCallback(private val producer: ProducerScope<Boolean>) : NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            // If the cast to WifiInfo fails or if the network name does not match a known valetudo id we set false, else true
            val wifiInfo = networkCapabilities.transportInfo as? WifiInfo
            producer.trySendBlocking(wifiInfo?.isValetudoDevice() == true)
        }

        override fun onUnavailable() {
            super.onUnavailable()
            producer.trySendBlocking(false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    internal class PostQNetworkCallback(private val producer: ProducerScope<Boolean>) : NetworkCallback() {
        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            // If the cast to WifiInfo fails or if the network name does not match a known valetudo id we set false, else true
            val wifiInfo = networkCapabilities.transportInfo as? WifiInfo
            producer.trySendBlocking(wifiInfo?.isValetudoDevice() == true)
        }

        override fun onUnavailable() {
            super.onUnavailable()
            producer.trySendBlocking(false)
        }
    }

    // Uses the old deprecated wifiManager.connectionInfo property
    @Suppress("DEPRECATION")
    internal class PreQNetworkCallback(
        private val producer: ProducerScope<Boolean>, private val wifiManager: WifiManager
    ) : NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            producer.trySendBlocking(wifiManager.connectionInfo?.isValetudoDevice() == true)
        }

        override fun onUnavailable() {
            super.onUnavailable()
            producer.trySendBlocking(false)
        }
    }

    companion object {
        private fun WifiInfo.isValetudoDevice(): Boolean =
            this.ssid.startsWith("roborock-vacuum-") || this.ssid.startsWith("rockrobo-vacuum-") || this.ssid.startsWith("viomi-vacuum-") || this.ssid.startsWith("dreame-vacuum-")

        fun fromContext(context: Context): WifiService {
            val connectivityManager = ContextCompat.getSystemService(context, ConnectivityManager::class.java) ?: throw Exception("Failed to get ConnectivityManager")
            val wifiManager = ContextCompat.getSystemService(context, WifiManager::class.java) ?: throw Exception("Failed to get WifiManager")
            return WifiService(connectivityManager, wifiManager)
        }
    }
}