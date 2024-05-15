package com.example.ayg

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import kotlin.math.abs
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var isShaking = false
    private var isFlat = false
    private var lastIsShaking = false
    private var lastIsFlat = false
    private var mediaPlayer: MediaPlayer? = null
    private lateinit var textViewState: TextView
    private lateinit var buttonReset: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textViewState = findViewById(R.id.textViewState)
        buttonReset = findViewById(R.id.buttonReset)

        buttonReset.setOnClickListener {
            resetState()
        }

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        registerSensors()
    }

    private fun registerSensors() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            when (it.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val (x, y, z) = it.values
                    isFlat = abs(x) < 0.5 && abs(y) < 0.5 && abs(z - 9.81) < 0.5
                }
                Sensor.TYPE_GYROSCOPE -> {
                    val (x, y, z) = it.values
                    val rotationRate = sqrt((x * x + y * y + z * z).toDouble())
                    isShaking = rotationRate > 0.2
                }
            }

            updateState()
        }
    }

    private fun updateState() {
        if (isShaking != lastIsShaking || isFlat != lastIsFlat) {
            when {
                isShaking -> {
                    textViewState.text = "En Movimiento"
                    playSound(R.raw.sonido)
                }
                isFlat -> {
                    textViewState.text = "Estable"
                    playSound(R.raw.sound)
                }
                else -> {
                    textViewState.text = "Estable"
                    stopSound()
                }
            }
            lastIsShaking = isShaking
            lastIsFlat = isFlat
        }
    }

    private fun playSound(soundResource: Int) {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null
        }
        mediaPlayer = MediaPlayer.create(this, soundResource)
        mediaPlayer?.start()
    }

    private fun stopSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun resetState() {
        isShaking = false
        isFlat = false
        lastIsShaking = false
        lastIsFlat = false
        textViewState.text = "Estable"
        stopSound()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No se utiliza en este ejemplo
    }

    override fun onResume() {
        super.onResume()
        registerSensors()
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
        stopSound()
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        stopSound()
    }
}
