package com.coventry.kennyapplication

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.coventry.kennyapplication.databinding.ActivitySubmitRecordBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Logger
import com.google.firebase.ktx.Firebase
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class SubmitRecordActivity : AppCompatActivity() {

    private lateinit var viewBinding: ActivitySubmitRecordBinding
    private lateinit var cameraExecutor: ExecutorService
    lateinit var sensorStatusTV: TextView
    lateinit var proximitySensor: Sensor
    lateinit var sensorManager: SensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewBinding = ActivitySubmitRecordBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Initialize the cameraExecutor property
        cameraExecutor = Executors.newSingleThreadExecutor()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val database = FirebaseDatabase.getInstance()
        val leaderboardRef = database.getReference("leaderboard")

        val user = Firebase.auth.currentUser
        val optionFrom = intent?.getStringExtra("option")

        Log.d(TAG, "Screen: ${optionFrom}")

        // Assuming you have EditText fields for username and score in your layout
        val editTextUsername = findViewById<EditText>(R.id.edittext_username)
        val editTextScore = findViewById<EditText>(R.id.edittext_score)
        val buttonSubmitRecord = findViewById<Button>(R.id.button_submit_record)

        buttonSubmitRecord.setOnClickListener {
            if (NetworkUtils.isNetworkAvailable(this)) {
                // Retrieve the values from the input fields
                if (editTextUsername.text.isNotEmpty() || editTextScore.text.isNotEmpty()) {
                    val username = editTextUsername.text.toString()
                    val score = editTextScore.text.toString().toInt()

                    Log.d(TAG, "user id: ${user?.uid} | username: $username | score: $score")

                    // Create an instance of your leaderboard entry using the input values
                    val leaderboardEntry = LeaderboardEntry(username, score)

                    user?.let {
                        leaderboardRef.child(it.uid).setValue(leaderboardEntry)
                            .addOnSuccessListener {
                                // Record submitted successfully
                                // Handle any further actions or UI updates here
                                Log.d(TAG, "Success")
                                Toast.makeText(this, "Submitted your record.", Toast.LENGTH_LONG)
                                    .show()
                            }
                            .addOnFailureListener { error ->
                                // An error occurred while submitting the record
                                // Handle the error appropriately
                                Log.d(TAG, "Fail: $error")
                                Toast.makeText(
                                    this,
                                    "Fail to submit your record.",
                                    Toast.LENGTH_LONG
                                )
                                    .show()
                            }
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Success
                                } else {
                                    // Failure
                                    val exception = task.exception
                                    // Log or handle the exception as needed
                                    Log.d(TAG, "Exception: $exception")
                                }
                            }
                    }
                } else {
                    // Empty input, display an error message
                    Toast.makeText(
                        this,
                        "Please enter a username and score before submit record",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // No internet connection, show an error message or perform appropriate action
                Toast.makeText(this, "No internet connection", Toast.LENGTH_LONG)
                    .show()
            }
        }

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        //proximity-sensor
        sensorStatusTV = findViewById(R.id.idTVSensorStatus)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)

        //check if the proximity sensor is null
        if (proximitySensor == null) {
            //display a toast if no sensor is available
            Toast.makeText(this, "No proximity sensor found in device..", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            // register sensor with sensor manager
            sensorManager.registerListener(
                proximitySensorEventListener,
                proximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }


    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        )
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && it.value == false)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(
                    baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                startCamera()
            }
        }

    var proximitySensorEventListener: SensorEventListener? = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            // method to check accuracy changed in sensor.
        }

        override fun onSensorChanged(event: SensorEvent) {
            // check if the sensor type is proximity sensor.
            if (event.sensor.type == Sensor.TYPE_PROXIMITY) {
                if (event.values[0] == 0f) {
                    sensorStatusTV.text = "Too close"
                    sensorStatusTV.setTextColor(Color.RED)
                } else {
                    sensorStatusTV.text = "Can do exercise now"
                    sensorStatusTV.setTextColor(Color.GREEN)
                }
            }
        }
    }

    fun checkProximitySensorAvailability() {
        if (proximitySensor == null) {
            // No proximity sensor available
            val toast = Toast.makeText(this, "No proximity sensor found in device..", Toast.LENGTH_SHORT)
            toast.show()
            finish()
        } else {
            // Proximity sensor is available
            sensorManager.registerListener(
                proximitySensorEventListener,
                proximitySensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }
}