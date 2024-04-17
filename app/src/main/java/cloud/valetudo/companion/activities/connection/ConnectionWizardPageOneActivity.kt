package cloud.valetudo.companion.activities.connection

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import cloud.valetudo.companion.activities.provisioning.ProvisioningActivity
import cloud.valetudo.companion.databinding.ActivityConnectionWizardPage1Binding
import cloud.valetudo.companion.utils.ValetudoProvisioningHelper


class ConnectionWizardPageOneActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConnectionWizardPage1Binding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConnectionWizardPage1Binding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val wifiManager: WifiManager? = getSystemService(WifiManager::class.java)
        val connectivityManager: ConnectivityManager? =
            getSystemService(ConnectivityManager::class.java)
        val provisioningHelper: ValetudoProvisioningHelper

        if (wifiManager != null && connectivityManager != null) {
            provisioningHelper = ValetudoProvisioningHelper(
                wifiManager,
                connectivityManager
            )
        } else {
            Log.e(
                "ConnectionWizardPageOneActivity",
                "Unable to create new provisioningHelper due to missing wifi- or connectivityManager"
            )

            runOnUiThread {
                this.finish()
            }

            return
        }

        binding.wizardPage1NextButton.setOnClickListener {
            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PERMISSION_GRANTED
            ) {
                val wizardPageTwoIntent =
                    Intent(this, ConnectionWizardPageTwoActivity::class.java)

                startActivity(wizardPageTwoIntent)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_REQUIRED,
                    PERMISSION_REQUEST_CODE
                )
            }
        }

        binding.wizardPage1SkipButton.setOnClickListener {
            if (provisioningHelper.getRobotWifiNetwork() != null) {
                val provisioningIntent = Intent(this, ProvisioningActivity::class.java)

                startActivity(provisioningIntent)
            } else {
                runOnUiThread {
                    Toast.makeText(
                        this@ConnectionWizardPageOneActivity,
                        "No you're not",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (
                permissions contentEquals PERMISSIONS_REQUIRED &&
                grantResults.all { it == PERMISSION_GRANTED }
            ) {
                val wizardPageTwoIntent =
                    Intent(this, ConnectionWizardPageTwoActivity::class.java)

                startActivity(wizardPageTwoIntent)
            } else {
                runOnUiThread {
                    Toast.makeText(
                        this@ConnectionWizardPageOneActivity,
                        "Wi-Fi SSID scanning requires the ACCESS_FINE_LOCATION permission",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1234

        private val PERMISSIONS_REQUIRED = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}