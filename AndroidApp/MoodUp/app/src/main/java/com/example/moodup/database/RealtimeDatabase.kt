package com.example.moodup.database

import android.graphics.Color
import android.util.Log
import com.example.moodup.utils.REALTIME_DATABASE
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RealtimeDatabase {
    private val realtimeDB =
        FirebaseDatabase.getInstance(REALTIME_DATABASE)
    private val realtimeRef = realtimeDB.getReference("devices")

    fun readDeviceCodes(
        userDeviceCode: String,
        onDeviceFound: (String) -> Unit,
        onDeviceNotFound: () -> Unit
    ) {
        realtimeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                var deviceFound = false
                for (deviceSnapshot in snapshot.children) {
                    val deviceName: String = deviceSnapshot.key ?: ""
                    if (deviceName == userDeviceCode) {
                        onDeviceFound(deviceName)
                        deviceFound = true
                        break
                    }
                }
                if (!deviceFound) {
                    onDeviceNotFound()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Firebase", "Failed to read value: ${error.toException()}")
            }
        })
    }


    fun readSensorsData(
        deviceCode: String,
        onDataReceived: (String, String, String, Int) -> Unit,
        onError: (String) -> Unit
    ) {
        val sensorsRef = realtimeRef.child(deviceCode).child("sensors")
        sensorsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val temperature = snapshot.child("temperature").getValue(Float::class.java) ?: 0.0f
                val humidity = snapshot.child("humidity").getValue(Float::class.java) ?: 0.0f
                val light = snapshot.child("light").getValue(Int::class.java) ?: 0
                val motion = snapshot.child("motion").getValue(Int::class.java) ?: 0

                onDataReceived(
                    temperature.toString(),
                    humidity.toString(),
                    light.toString(),
                    motion
                )
            }

            override fun onCancelled(error: DatabaseError) {
                onError("Failed to read sensor data: ${error.message}")
            }
        })
    }

    fun readCurrentColour(
        deviceCode: String,
        onColourFetched: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        val colourRef = realtimeRef.child(deviceCode).child("colour")
        colourRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val red = snapshot.child("R").getValue(Int::class.java) ?: 0
                val green = snapshot.child("G").getValue(Int::class.java) ?: 0
                val blue = snapshot.child("B").getValue(Int::class.java) ?: 0

                val colour = Color.rgb(red, green, blue)
                onColourFetched(colour)
                Log.d("Firebase", "Read colour red: $red")
                Log.d("Firebase", "Read colour green: $green")
                Log.d("Firebase", "Read colour blue: $blue")
                Log.d("Firebase", "Read colour: $colour")
            }

            override fun onCancelled(error: DatabaseError) {
                onError("Failed to fetch color: ${error.message}")
            }
        })
    }

    fun sendColourToDB(deviceCode: String, colour: Int) {
        val colourRef = realtimeRef.child("esp32wroomDA9826").child("colour")

        val red = Color.red(colour)
        val green = Color.green(colour)
        val blue = Color.blue(colour)

        val colourMap = mapOf(
            "R" to red,
            "G" to green,
            "B" to blue
        )

        colourRef.updateChildren(colourMap)
            .addOnSuccessListener {
                Log.d("Firebase", "Colour sent successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error sending colour: $e")
            }
    }

    fun sendUserOfDevice(deviceCode: String, userId: String) {
        val deviceRef = realtimeRef.child(deviceCode).child("connectedUser")
        val userMap = mapOf("userId" to userId)
        deviceRef.updateChildren(userMap)
            .addOnSuccessListener {
                Log.d("Firebase", "User sent successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error sending user: $e")
            }
    }

    fun sendDisconnectedUserFromDevice(deviceCode: String) {
        val deviceRef = realtimeRef.child(deviceCode).child("connectedUser")
        deviceRef.setValue(null)
            .addOnSuccessListener {
                Log.d("Firebase", "User disconnected successfully")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase", "Error sending disconnected: $e")
            }
    }

}
