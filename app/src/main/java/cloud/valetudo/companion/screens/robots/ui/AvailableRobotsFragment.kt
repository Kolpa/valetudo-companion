package cloud.valetudo.companion.screens.robots.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import cloud.valetudo.companion.R
import cloud.valetudo.companion.databinding.FragmentAvailableRobotsBinding
import cloud.valetudo.companion.screens.robots.data.DiscoveredValetudoInstance
import cloud.valetudo.companion.utils.setVisibility

class AvailableRobotsFragment : Fragment(R.layout.fragment_available_robots) {
    private lateinit var binding: FragmentAvailableRobotsBinding
    private val viewModel: AvailableRobotsViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAvailableRobotsBinding.bind(view)

        binding.enterProvisioningActivityButton.setOnClickListener(::startProvisioning)

        val instancesAdapter = DiscoveredValetudoInstancesAdapter(
            ::launchInAppBrowser,
            ::launchExternalBrowser
        )

        binding.discoveredList.adapter = instancesAdapter

        viewModel.devicesLiveData.observe(viewLifecycleOwner) {
            instancesAdapter.instances = it

            binding.helpText.setVisibility(it.isEmpty())

            binding.mainText.setText(
                if (it.isEmpty()) {
                    R.string.discovering_valetudo_instances
                } else {
                    R.string.found_devices
                }
            )
        }

        enableEgg()
    }

    private val customTabsIntent by lazy {
        val defaultColors = CustomTabColorSchemeParams.Builder().setToolbarColor(
            ResourcesCompat.getColor(resources, R.color.valetudo_main, null)
        )
            .build()

        CustomTabsIntent.Builder()
            .setDefaultColorSchemeParams(defaultColors)
            .setUrlBarHidingEnabled(false)
            .build()
    }


    private fun launchInAppBrowser(instance: DiscoveredValetudoInstance) {
        try {
            customTabsIntent.launchUrl(
                requireContext(),
                instance.hostUri
            )
        } catch (e: ActivityNotFoundException) {
                Toast.makeText(
                    requireContext(),
                    "No http:// intent handler installed.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private fun launchExternalBrowser(instance: DiscoveredValetudoInstance): Boolean {
        val browserIntent = Intent(
            Intent.ACTION_VIEW,
            instance.hostUri
        )
        return try {
            startActivity(browserIntent)
            true
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                requireContext(),
                "No http:// intent handler installed.",
                Toast.LENGTH_LONG
            ).show()
            false
        }
    }

    @Suppress("UNUSED_PARAMETER")
    private fun startProvisioning(view: View) {
        findNavController().navigate(R.id.action_availableRobotsFragment_to_provisioningModeSelectionFragment)
    }

    private fun enableEgg() {
        val icon = binding.valetudoLogo
        var iconClicks = 0

        if ((1..100).random() == 42) {
            icon.setImageResource(R.drawable.ic_valetudog)
        }

        icon.setOnClickListener {
            if (iconClicks == 9) {
                icon.setImageResource(R.drawable.ic_valetudog)
            } else {
                iconClicks++
            }
        }
    }
}