package com.example.moodup.ui.home

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.moodup.database.ChatDatabase
import com.example.moodup.database.RealtimeDatabase

class HomeViewModel : ViewModel() {

    private val realtimeDatabase = RealtimeDatabase()
    private val chatDatabase = ChatDatabase()

    val isDeviceConnected = MutableLiveData(false)
    val userDeviceCode = MutableLiveData("")
    val roomTemperature = MutableLiveData("")
    val roomHumidity = MutableLiveData("")
    val roomLight = MutableLiveData("")

    private var isRoomMotion = MutableLiveData(true)
    var onAddButtonClicked = MutableLiveData(false)
    var onColourPickerButtonClicked = MutableLiveData(false)
    var onTipsButtonClicked = MutableLiveData(false)
    var onDisconnectButtonClicked = MutableLiveData(false)
    var isTipsButtonClicked = MutableLiveData(false)
    val currentDeviceColour = MutableLiveData<Int>()
    val currentColour: MutableLiveData<Int> = MutableLiveData(Color.WHITE)


    fun onClickAddButton() {
        onAddButtonClicked.value = true
    }

    fun onClickColourPickerButton() {
        onColourPickerButtonClicked.value = true
    }

    fun onClickTipsButton() {
        onTipsButtonClicked.value = true
    }

    fun tipsButtonCancel() {
        isTipsButtonClicked.value = false
    }

    fun onClickDisconnectButton() {
        onDisconnectButtonClicked.value = true
    }

    fun connectDevice(deviceCode: String) {
        realtimeDatabase.readDeviceCodes(
            deviceCode,
            onDeviceFound = { deviceName ->
                if (deviceName == deviceCode) {
                    isDeviceConnected.postValue(true)
                }
            },
            onDeviceNotFound = {
                isDeviceConnected.postValue(false)
            })
    }

    fun readSensorsData(deviceCode: String) {
        realtimeDatabase.readSensorsData(
            deviceCode,
            onDataReceived = { temperature, humidity, light, motion ->
                roomTemperature.postValue(temperature)
                roomHumidity.postValue(humidity)
                roomLight.postValue(light)
                if (motion == 1) {
                    isRoomMotion.postValue(true)
                } else {
                    isRoomMotion.postValue(false)
                }

            },
            onError = { error ->
                Log.e("Firebase", "Failed to read value: $error")
            }
        )
    }

    fun sendColourToDB(deviceCode: String, colour: Int) {
        realtimeDatabase.sendColourToDB(deviceCode, colour)
    }

    fun sendUserOfDevice(deviceCode: String) {
        realtimeDatabase.sendUserOfDevice(deviceCode, chatDatabase.getCurrentUserId())
    }

    fun sendDisconnectedUserFromDevice(deviceCode: String) {
        realtimeDatabase.sendDisconnectedUserFromDevice(deviceCode)
    }

    fun readCurrentColour(deviceCode: String) {
        realtimeDatabase.readCurrentColour(
            deviceCode,
            onColourFetched = { colour ->
                currentDeviceColour.postValue(colour)
            },
            onError = { error ->
                Log.e("Firebase", "Failed to fetch color: $error")
            }
        )
    }
}
