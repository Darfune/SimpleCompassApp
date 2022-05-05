package com.example.compassapp

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class MainActivity : AppCompatActivity(), SensorEventListener{


    private var pointerImageView: ImageView? = null
    private var degreeTextView: TextView? = null

    private var sensorManager: SensorManager? = null
    private var sensorAccelerometer: Sensor? = null
    private var sensorMagneticField: Sensor? = null

    private var gravity: FloatArray = FloatArray(3)
    private var lastGravityDataCopied: Boolean = false
    private var geoMagnetic: FloatArray = FloatArray(3)
    private var lastGeoMagneticDataCopied: Boolean = false

    private var orientation: FloatArray = FloatArray(3)
    private var rotationMatrix: FloatArray = FloatArray(9)

    private var lastUpdatedTime: Long = 0
    private var currentDegree: Float = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pointerImageView = findViewById(R.id.pointerImageView)
        degreeTextView = findViewById(R.id.degreeTextView)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager?
        sensorAccelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorMagneticField = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    }

    @SuppressLint("SetTextI18n")
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            when(event.sensor.type){
                Sensor.TYPE_ACCELEROMETER -> {
                    System.arraycopy(event.values, 0, gravity, 0, event.values.size)
                    lastGravityDataCopied = true
                }
                Sensor.TYPE_MAGNETIC_FIELD -> {
                    System.arraycopy(event.values, 0, geoMagnetic, 0, event.values.size)
                    lastGeoMagneticDataCopied = true
                }
            }

            if (lastGravityDataCopied && lastGeoMagneticDataCopied && System.currentTimeMillis() - lastUpdatedTime>250){
                SensorManager.getRotationMatrix(rotationMatrix, null, gravity, geoMagnetic)
                SensorManager.getOrientation(rotationMatrix, orientation)

                var azimuthInRadians: Float = orientation[0]
                var azimuthInDegrees: Double = Math.toDegrees(azimuthInRadians.toDouble())

                var rotationAnimation = RotateAnimation(currentDegree,
                    (-azimuthInDegrees).toFloat(), Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
                rotationAnimation.duration = 250
                rotationAnimation.fillAfter = true
                pointerImageView?.startAnimation(rotationAnimation)

                currentDegree = (- azimuthInDegrees).toFloat()
                lastUpdatedTime = System.currentTimeMillis()

                var x: Int = azimuthInDegrees.toInt()
                degreeTextView?.text = "$x°"
            }

        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    override fun onResume() {
        super.onResume()
        sensorManager?.registerListener(this, sensorAccelerometer,SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager?.registerListener(this, sensorMagneticField,SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager?.unregisterListener(this, sensorAccelerometer)
        sensorManager?.unregisterListener(this, sensorMagneticField)
    }
}