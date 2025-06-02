package com.example.apel

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class DetectionActivity : AppCompatActivity() {

    companion object {
        private const val MODEL_NAME = "applegweh.tflite"
        private const val INPUT_SIZE = 224
        private const val GALLERY_REQUEST_CODE = 100
        private const val CAMERA_REQUEST_CODE = 101
        private const val TAG = "DetectionActivity"
    }

    private lateinit var interpreter: Interpreter
    private lateinit var imageView: ImageView
    private lateinit var resultText: TextView
    private lateinit var btnPickImage: Button
    private lateinit var btnTakePicture: Button
    private lateinit var detailButton: Button
    private lateinit var diseaseNameText: TextView
    private lateinit var predictionLabel: TextView // Tambahkan TextView untuk label hasil prediksi

    private val labels = listOf(
        "Apple___Scab",
        "Apple___Black_rot",
        "Apple___Cedar_apple_rust",
        "Apple___Healthy",
        "Unknown"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detection)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        imageView = findViewById(R.id.imageView)
        resultText = findViewById(R.id.resultText)
        btnPickImage = findViewById(R.id.btnPickImage)
        btnTakePicture = findViewById(R.id.btnTakePicture)
        detailButton = findViewById(R.id.detailButton)
        detailButton.visibility = View.GONE
        diseaseNameText = findViewById(R.id.diseaseNameText)
        diseaseNameText.visibility = View.GONE
        predictionLabel = findViewById(R.id.predictionLabel) // Inisialisasi TextView untuk label hasil prediksi
        predictionLabel.visibility = View.GONE // Sembunyikan awalnya

        setPlaceholderBackground()

        try {
            interpreter = Interpreter(loadModelFile())
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model", e)
        }

        btnPickImage.setOnClickListener {
            openGallery()
        }

        btnTakePicture.setOnClickListener {
            openCamera()
        }

        detailButton.setOnClickListener {
            val disease = it.tag as? String ?: return@setOnClickListener
            val intent = Intent(this, DiseaseDetailActivity::class.java)
            intent.putExtra("disease_name", disease)
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                GALLERY_REQUEST_CODE -> data?.data?.let { uri: Uri ->
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        setPlaceholderBackground()
                        imageView.setImageBitmap(bitmap)
                        classifyImage(bitmap)
                    }
                }
                CAMERA_REQUEST_CODE -> {
                    val bitmap = data?.extras?.get("data") as? Bitmap
                    bitmap?.let {
                        setPlaceholderBackground()
                        imageView.setImageBitmap(it)
                        classifyImage(it)
                    }
                }
            }
        }
    }

    private fun classifyImage(bitmap: Bitmap) {
        detailButton.visibility = View.GONE
        predictionLabel.visibility = View.GONE
        diseaseNameText.visibility = View.GONE

        val scaled = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false)
        val byteBuffer = convertBitmapToByteBuffer(scaled)

        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(byteBuffer, output)

        val confidences = output[0]
        val maxIndex = confidences.indices.maxByOrNull { confidences[it] } ?: -1
        if (maxIndex == -1) {
            resultText.text = "Gagal memproses gambar. Silakan coba ulang."
            return
        }

        val confidence = confidences[maxIndex]
        val threshold = 0.8f
        val labelKey = labels[maxIndex]

        if (labelKey == "Unknown" || confidence < threshold) {
            resultText.setTextColor(resources.getColor(android.R.color.holo_red_dark))
            resultText.text =
                "Sepertinya ini bukan gambar daun apel \uD83C\uDF4E. Silakan coba dengan gambar lain."
            return
        }

        resultText.setTextColor(resources.getColor(R.color.green))

        val displayLabel = when (labelKey) {
            "Apple___Scab" -> "Apple Scab"
            "Apple___Black_rot" -> "Black Rot"
            "Apple___Cedar_apple_rust" -> "Cedar Apple Rust"
            "Apple___Healthy" -> "Healthy Leaf"
            else -> labelKey
        }

        // Ambil detail penyakit dari DiseaseDetailActivity
        val diseaseInfo = DiseaseDetailActivity.getDiseaseDetails(displayLabel)

        // Tampilkan deskripsi penyakit saja di resultText
        if (diseaseInfo != null) {
            resultText.text = "${diseaseInfo.description}\n\nAkurasi deteksi: ${"%.2f".format(confidence * 100)}%"
        } else {
            resultText.text = "Deskripsi penyakit tidak ditemukan.\n\nAkurasi deteksi: ${"%.2f".format(confidence * 100)}%"
        }

        // Tampilkan label hasil prediksi dan nama penyakit
        predictionLabel.visibility = View.VISIBLE
        diseaseNameText.text = displayLabel
        diseaseNameText.visibility = View.VISIBLE

        if (labelKey == "Apple___Healthy") {
            detailButton.visibility = View.GONE
        } else {
            detailButton.apply {
                tag = displayLabel
                text = "Lihat Detail $displayLabel"
                visibility = View.VISIBLE
            }
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3)
        byteBuffer.order(ByteOrder.nativeOrder())
        val intValues = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(intValues, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)
        intValues.forEach { pixel ->
            byteBuffer.putFloat(((pixel shr 16) and 0xFF) / 255.0f)
            byteBuffer.putFloat(((pixel shr 8) and 0xFF) / 255.0f)
            byteBuffer.putFloat((pixel and 0xFF) / 255.0f)
        }
        return byteBuffer
    }

    @Throws(IOException::class)
    private fun loadModelFile(): MappedByteBuffer {
        val fileDescriptor = assets.openFd(MODEL_NAME)
        FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
            val fileChannel = inputStream.channel
            return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                fileDescriptor.startOffset,
                fileDescriptor.declaredLength
            )
        }
    }

    private fun setPlaceholderBackground() {
        imageView.setImageDrawable(null)
        imageView.setBackgroundResource(R.drawable.placeholder_background)
    }

    override fun onDestroy() {
        interpreter.close()
        super.onDestroy()
    }
}
