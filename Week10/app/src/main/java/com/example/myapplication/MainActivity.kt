
package com.example.myapplication

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var mSensorManager: SensorManager

    private var mLightSensor: Sensor? = null
    private var mProximitySensor: Sensor? = null

    private lateinit var tvLightValue: TextView
    private lateinit var tvProximityValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mSensorManager = getSystemService(
            Context.SENSOR_SERVICE
        ) as SensorManager

        // Get sensors
        mLightSensor = mSensorManager.getDefaultSensor(
            Sensor.TYPE_LIGHT
        )

        mProximitySensor = mSensorManager.getDefaultSensor(
            Sensor.TYPE_PROXIMITY
        )

        // Connect TextViews
        tvLightValue = findViewById(R.id.tvLightValue)
        tvProximityValue = findViewById(R.id.tvProximityValue)
    }

    override fun onResume() {
        super.onResume()

        mLightSensor?.let {
            mSensorManager.registerListener(
                this, it, SensorManager.SENSOR_DELAY_NORMAL
            )
        }

        mProximitySensor?.let {
            mSensorManager.registerListener(
                this, it, SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {

        if (event == null) return

        when (event.sensor.type) {

            Sensor.TYPE_LIGHT -> {
                val value = event.values[0]
                tvLightValue.text = "Light: $value lux"
            }

            Sensor.TYPE_PROXIMITY -> {
                val value = event.values[0]
                tvProximityValue.text = "Proximity: $value cm"
            }
        }
    }

    override fun onAccuracyChanged(
        sensor: Sensor?,
        accuracy: Int
    ) {
    }

    override fun onPause() {
        super.onPause()
        mSensorManager.unregisterListener(this)
    }
}

