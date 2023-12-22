package com.example.handsight

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.handsight.databinding.ActivityCameraxBinding
import com.google.common.util.concurrent.ListenableFuture
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.util.Locale
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityCameraxBinding
    private lateinit var uid: String
    private var tts: TextToSpeech? = null
    private var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
    private lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>
    private lateinit var imageClassifierHelper: ImageClassifierHelper
    private val db: FirebaseFirestore = Firebase.firestore
    private var previousObject: String = ""
    private val alphabetMapping = ('A'..'Z').toList()


    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                Toast.makeText(this, "Camera permission rejected", Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraxBinding.inflate(layoutInflater)
        setContentView(binding.root)
        uid = intent.extras!!.getString("uid").toString()
        val id = intent.extras!!.getString("uid").toString()
        requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        binding.switchCamera.setOnClickListener {
            cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) CameraSelector.DEFAULT_FRONT_CAMERA
            else CameraSelector.DEFAULT_BACK_CAMERA
            startCamera()
        }
        binding.btnMenu.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            val bundle = Bundle()
            bundle.putString("uid", id)
            intent.putExtras(bundle)
            startActivity(intent)
        }
        soundAction()
    }
    private fun startCamera() {
        imageClassifierHelper = ImageClassifierHelper(
            context = this,
            imageClassifierListener = object : ImageClassifierHelper.ClassifierListener {
                override fun onError(error: String) {
                    runOnUiThread {
                        Toast.makeText(this@CameraActivity, error, Toast.LENGTH_SHORT).show()
                    }
                }

                @SuppressLint("SetTextI18n")
                override fun onResults(results: List<Classifications>?, inferenceTime: Long) {
                    runOnUiThread {
                        results?.let { res ->
                            Log.d("xxx", res.toString())
                            if (res.isNotEmpty() && res[0].categories.isNotEmpty()) {
                                val sortedCategories = res[0].categories.sortedByDescending { it?.score }
                                val resultIndex = sortedCategories[0]?.label?.toIntOrNull() ?: -1

                                if (resultIndex in alphabetMapping.indices) {
                                    val result = alphabetMapping[resultIndex]

                                    // Convert previousObject to Char if it's not already
                                    val previousObjectChar = if (previousObject.length == 1) previousObject[0] else '\u0000'

                                    if (result != previousObjectChar) {
                                        saveToHistory(result.toString())
                                        if (tts != null) {
                                            tts?.speak(result.toString(), TextToSpeech.QUEUE_FLUSH, null, "")
                                        }
                                        binding.tvResult.text = result.toString()
                                        previousObject = result.toString()
                                    }
                                } else {
                                    Toast.makeText(
                                        this@CameraActivity,
                                        "Invalid classification result index",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                // If there are no results, you can set a default value or handle it accordingly.
                                binding.tvResult.text = "No result"
                                previousObject = "" // Clear previousObject if no result is available
                            }
                        }
                    }
                }
            }
        )

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(binding.viewFinder.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build()
                .also {
                    it.setAnalyzer(Executors.newSingleThreadExecutor()) { image ->
                        imageClassifierHelper.classify(image)
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this as LifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Gagal memunculkan kamera.", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun saveToHistory(name: String) {
        val data = hashMapOf(
            "uid" to uid,
            "name" to name,
            "created_at" to FieldValue.serverTimestamp()
        )
        db.collection("history")
            .add(data)
            .addOnSuccessListener {}
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed save to history: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun soundAction() {
        binding.btnSound.setOnClickListener {
            binding.btnSound.isSelected = !binding.btnSound.isSelected
            if (binding.btnSound.isSelected) {
                tts = TextToSpeech(this,this)
                tts?.speak(binding.tvResult.text, TextToSpeech.QUEUE_FLUSH, null, "")
            }
            else {
                tts?.stop()
                tts?.shutdown()
                tts = null
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS","The Language not supported!")
            }
        }
    }
}