package com.example.moodup.ui.home

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.moodup.databinding.FragmentHomeBinding
import yuku.ambilwarna.AmbilWarnaDialog
import android.content.Context
import android.content.SharedPreferences
import android.view.inputmethod.InputMethodManager
import androidx.activity.OnBackPressedCallback
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val homeViewModel: HomeViewModel by viewModels()
    private var selectedColor = Color.WHITE
    private var deviceCodeCopy: String = ""

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.homeViewModel = homeViewModel

        sharedPreferences =
            requireActivity().getSharedPreferences("deviceConnected", Context.MODE_PRIVATE)

        moodTipsObserver()
        setupObservers()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showQuitAppDialog()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun showQuitAppDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Leaving so Soon?")
            .setMessage("Are you sure you want to exit the app?")
            .setPositiveButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Yes") { _, _ ->
                activity?.finish()
            }
            .show()
    }

    private fun showDisconnectDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Disconnect Device")
            .setMessage("Are you sure you want to disconnect your device?")
            .setPositiveButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Yes") { _, _ ->
                homeViewModel.isDeviceConnected.value = false
                saveConnectionState(false)
                showDisconnectedViews()
            }
            .show()
    }

    private fun moodTipsObserver() {
        homeViewModel.onTipsButtonClicked.observe(viewLifecycleOwner) { isClicked ->
            if (isClicked) {
                homeViewModel.isTipsButtonClicked.value = true
                homeViewModel.onTipsButtonClicked.value = false
            }

        }
    }

    private fun setupObservers() {
        homeViewModel.isDeviceConnected.observe(viewLifecycleOwner) { isConnected ->
            if (isConnected) {
                if (getConnectionState()) {
                    showConnectedViews()
                }
            } else {
                showDisconnectedViews()
            }
        }
        addButtonObserver()
        colourPickerButtonObserver()
        disconnectButtonObserver()
    }

    private fun showConnectedViews() {
        binding.textHomeConnected.visibility = View.VISIBLE
        binding.roomTemperatureConnected.visibility = View.VISIBLE
        binding.roomHumidityConnected.visibility = View.VISIBLE
        binding.roomLightConnected.visibility = View.VISIBLE
        binding.roomColourConnected.visibility = View.VISIBLE
        binding.disconnectDeviceButtonConnected.visibility = View.VISIBLE
        binding.textHomeSubtitleConnected.visibility = View.VISIBLE
        binding.colourButtonConnected.visibility = View.VISIBLE
        binding.roomTemperatureValueConnected.visibility = View.VISIBLE
        binding.roomHumidityValueConnected.visibility = View.VISIBLE
        binding.roomLightValueConnected.visibility = View.VISIBLE
        binding.currentRoomColourConnected.visibility = View.GONE

        binding.textHomeDisconnected.visibility = View.GONE
        binding.deviceCodeDisconnected.visibility = View.GONE
        binding.sendCodeButtonDisconnected.visibility = View.GONE
    }

    private fun showDisconnectedViews() {
        binding.textHomeConnected.visibility = View.GONE
        binding.roomTemperatureConnected.visibility = View.GONE
        binding.roomHumidityConnected.visibility = View.GONE
        binding.roomLightConnected.visibility = View.GONE
        binding.roomColourConnected.visibility = View.GONE
        binding.disconnectDeviceButtonConnected.visibility = View.GONE
        binding.textHomeSubtitleConnected.visibility = View.GONE
        binding.colourButtonConnected.visibility = View.GONE
        binding.roomTemperatureValueConnected.visibility = View.GONE
        binding.roomHumidityValueConnected.visibility = View.GONE
        binding.roomLightValueConnected.visibility = View.GONE
        binding.currentRoomColourConnected.visibility = View.GONE

        binding.textHomeDisconnected.visibility = View.VISIBLE
        binding.deviceCodeDisconnected.visibility = View.VISIBLE
        binding.sendCodeButtonDisconnected.visibility = View.VISIBLE
    }

    private fun addButtonObserver() {
        homeViewModel.onAddButtonClicked.observe(viewLifecycleOwner) { isClicked ->
            if (isClicked) {
                val deviceCode: String = homeViewModel.userDeviceCode.value!!.trim()
                deviceCodeCopy = deviceCode
                if (deviceCode.isNotEmpty()) {
                    homeViewModel.connectDevice(deviceCode)
                    saveConnectionState(true)
                    homeViewModel.readSensorsData(deviceCode)
                    homeViewModel.readCurrentColour(deviceCode)
                    //binding.currentRoomColourConnected.setBackgroundColor(homeViewModel.currentDeviceColour.value)
                    //binding.currentRoomColourConnected.setBackgroundColor(homeViewModel.currentColour)
                    homeViewModel.sendUserOfDevice(deviceCode)
                    hideKeyboard()
                } else {
                    Toast.makeText(context, "Please enter device code!", Toast.LENGTH_LONG).show()
                }
                homeViewModel.onAddButtonClicked.value = false
            }
        }
    }

    private fun colourPickerButtonObserver() {
        homeViewModel.onColourPickerButtonClicked.observe(viewLifecycleOwner) { isClicked ->
            if (isClicked) {
                openColourPicker()
                homeViewModel.onColourPickerButtonClicked.value = false
            }
        }
    }

    private fun disconnectButtonObserver() {
        homeViewModel.onDisconnectButtonClicked.observe(viewLifecycleOwner) { isClicked ->
            if (isClicked) {
                showDisconnectDialog()
                //homeViewModel.sendDisconnectedUserFromDevice(deviceCodeCopy)
                homeViewModel.onDisconnectButtonClicked.value = false
            }
        }
    }

    private fun hideKeyboard() {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }

    private fun openColourPicker() {
        val colorPicker = AmbilWarnaDialog(
            requireContext(),
            selectedColor,
            object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onCancel(dialog: AmbilWarnaDialog) {

                }

                override fun onOk(dialog: AmbilWarnaDialog, colour: Int) {
                    selectedColor = colour
                    homeViewModel.sendColourToDB(deviceCodeCopy, selectedColor)
                    // binding.colourButtonConnected.setBackgroundColor(colour)
                }
            })
        colorPicker.show()
    }

    private fun saveConnectionState(isConnected: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("isDeviceConnected", isConnected)
        editor.apply()
    }

    private fun getConnectionState(): Boolean {
        return sharedPreferences.getBoolean("isDeviceConnected", false)
    }
}