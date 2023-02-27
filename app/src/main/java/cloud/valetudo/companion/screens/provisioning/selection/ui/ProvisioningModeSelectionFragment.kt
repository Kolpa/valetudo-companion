package cloud.valetudo.companion.screens.provisioning.selection.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import cloud.valetudo.companion.R
import cloud.valetudo.companion.databinding.FragmentProvisioningModeSelectionBinding


class ProvisioningModeSelectionFragment : Fragment(R.layout.fragment_provisioning_mode_selection) {
    private lateinit var binding: FragmentProvisioningModeSelectionBinding
    private val viewModel: ProvisioningModeSelectionViewModel by viewModels()

    private fun subscribeToConnectionState() {
        viewModel.connectedToRobotLiveData.observe(viewLifecycleOwner) {
            binding.wizardPage1SkipButton.isEnabled = it
        }
    }

    private val locationPermissionContract by lazy {
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            if (it[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                subscribeToConnectionState()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Location Permission is required for wifi based provisioning",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun subscribeToConnectionStatePerms() {
        if (hasLocationPermission(requireContext())) {
            subscribeToConnectionState()
        } else {
            locationPermissionContract.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProvisioningModeSelectionBinding.bind(view)

        subscribeToConnectionStatePerms()
    }

    companion object {
        private fun hasLocationPermission(context: Context): Boolean =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
}