package com.github.cesar1287.revisaofirebase

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.github.cesar1287.revisaofirebase.databinding.ActivityMainBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var imageCapture: ImageCapture? = null

    private lateinit var outputDirectory: File
    private lateinit var cameraExecutor: ExecutorService

    private val db by lazy {
        Firebase.firestore
    }

    private val auth by lazy {
        Firebase.auth
    }

    private val messaging by lazy {
        Firebase.messaging
    }

    private val analytics by lazy {
        Firebase.analytics
    }

    private val storageRef by lazy {
        Firebase.storage.reference
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        analytics.logEvent("main_screen", null)

        setupObservables()

        loadProfilePicture()

        messaging.token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("token", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            //val msg = getString(R.string.msg_token_fmt, token)
            Log.d("token", token ?: "não deu")
        })

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Set up the listener for take photo button
        binding.btLoginGallery.setOnClickListener { takePhoto() }

        outputDirectory = getOutputDirectory()

        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    private fun loadProfilePicture() {
        storageRef.child(
            "${(auth.currentUser?.uid ?: "")}/profile.jpg"
        )

        val localFile = File.createTempFile("profile", "jpg")

        storageRef.getFile(localFile).addOnSuccessListener {
            localFile
        }.addOnFailureListener {
            Toast.makeText(
                this@MainActivity,
                it.localizedMessage,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val photoFile = File(
            outputDirectory,
            "profile.jpg")

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = Uri.fromFile(photoFile)
                    val profilePhoto = storageRef.child(
                        "${(auth.currentUser?.uid ?: "")}/${savedUri.lastPathSegment}"
                    )
                    profilePhoto.putFile(savedUri).addOnSuccessListener {
                        Toast.makeText(
                            this@MainActivity,
                            "Foto salva com sucesso",
                            Toast.LENGTH_SHORT
                        ).show()
                    }.addOnFailureListener {
                        Toast.makeText(
                            this@MainActivity,
                            it.localizedMessage,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    val msg = "Photo capture succeeded: $savedUri"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            })
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
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture)

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() } }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this,
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupObservables() {
        binding.btLoginLogout.setOnClickListener {
            analytics.logEvent("logout_btn", null)
            Firebase.auth.signOut()
            signIn()
        }

        binding.btLoginSave.setOnClickListener {
            analytics.logEvent("save_btn", null)
            val userData = hashMapOf(
                "name" to binding.tieLoginName.text.toString(),
                "email" to binding.tieLoginEmail.text.toString(),
                "last_name" to binding.tieLoginLastName.text.toString(),
                "phone" to binding.tieLoginPhone.text.toString()
            )

            db.collection(FIREBASE_COLLECTION_USERS)
                .document(auth.currentUser?.uid ?: "")
                .set(userData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Dados salvos com sucesso", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onResume() {
        super.onResume()
        auth.currentUser?.let {
            val documentReference = db.collection(FIREBASE_COLLECTION_USERS).document(it.uid)
            documentReference.get()
                .addOnSuccessListener { snapshot ->
                    val userData = snapshot.data
                    binding.tieLoginEmail.text = (userData?.get("email") as String).getEditable()
                    //Cesar Rodrigues Nascimento
                    // name = [Cesar, Rodrigues, Nascimento]
                    val name = userData["name"] as String
                    val lastName = userData["last_name"] as String
                    binding.tieLoginName.text = name.getEditable()
                    binding.tieLoginLastName.text = lastName.getEditable()
                    binding.tieLoginPhone.text = (userData["phone"] as String).getEditable()
                }.addOnFailureListener {
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                }
        } ?: run {
            signIn()
        }
    }

    private fun signIn() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    companion object {
        private const val FIREBASE_COLLECTION_USERS = "users"
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}