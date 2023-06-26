package com.coventry.kennyapplication

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.widget.TextView
import android.widget.Toast
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class ProximitySensorTest {
    private lateinit var context: Context
    private lateinit var sensorManager: SensorManager
    private lateinit var proximitySensor: Sensor
    private lateinit var sensorStatusTV: TextView

    @Before
    fun setup() {
        context = mock(Context::class.java)
        sensorManager = mock(SensorManager::class.java)
        proximitySensor = mock(Sensor::class.java)
        sensorStatusTV = mock(TextView::class.java)

        // Set up mocked behavior for getDefaultSensor() and registerListener()
        `when`(sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)).thenReturn(proximitySensor)
        `when`(context.getSystemService(Context.SENSOR_SERVICE)).thenReturn(sensorManager)
    }

    @Test
    fun testProximitySensorAvailability() {
        // Create an instance of the class under test
        val classUnderTest = SubmitRecordActivity()

        // Mock the Toast for no sensor available
        val toast = mock(Toast::class.java)
        `when`(Toast.makeText(context, "No proximity sensor found in device..", Toast.LENGTH_SHORT)).thenReturn(toast)

        // Call the method that checks the proximity sensor availability
        classUnderTest.checkProximitySensorAvailability()

        // Verify that the expected methods were called
        verify(sensorManager).getDefaultSensor(Sensor.TYPE_PROXIMITY)
        verify(sensorManager).registerListener(any(), eq(proximitySensor), eq(SensorManager.SENSOR_DELAY_NORMAL))
        verify(toast).show()
        verify(classUnderTest).finish()

        // Verify that the TextView was not used when the sensor is available
        verifyZeroInteractions(sensorStatusTV)
    }

}