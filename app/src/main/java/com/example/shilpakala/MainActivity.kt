package com.example.shilpakala

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.File
import kotlin.math.abs

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var imageCapture: ImageCapture
    private var lastCapturedFile: File? = null

    private var isKannada = false

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private lateinit var overlayView: OverlayView

    // ---------- String tables ----------

    private val en = mapOf(
        "app_title"        to "Shilpa-Kala",
        "capture"          to "Capture",
        "lang_toggle"      to "ಕನ್ನಡ",
        "dialog_title"     to "Brand Your Product",
        "name_hint"        to "Artisan name",
        "wood_label"       to "Wood type",
        "wood_other_hint"  to "Specify wood type",
        "price_label"      to "Price (₹)",
        "price_hint"       to "e.g. 1200",
        "save_btn"         to "Save & Brand",
        "cancel"           to "Cancel",
        "share_title"      to "Share your product?",
        "share_msg"        to "Your branded photo is saved. Share it now?",
        "share_yes"        to "Share",
        "later"            to "Later",
        "saved_ok"         to "Branded photo saved!",
        "save_fail"        to "Failed to save image.",
        "capture_fail"     to "Capture failed. Try again.",
        "load_fail"        to "Could not load image.",
        "share_via"        to "Share via",
        "tilt_left"        to "Tilt left ←",
        "tilt_right"       to "Tilt right →",
        "level_ok"         to "Level ✓"
    )

    private val kn = mapOf(
        "app_title"        to "ಶಿಲ್ಪ-ಕಲಾ",
        "capture"          to "ತೆಗೆ",
        "lang_toggle"      to "English",
        "dialog_title"     to "ನಿಮ್ಮ ಉತ್ಪನ್ನಕ್ಕೆ ಬ್ರ್ಯಾಂಡ್ ಮಾಡಿ",
        "name_hint"        to "ಕುಶಲಕರ್ಮಿ ಹೆಸರು",
        "wood_label"       to "ಮರದ ವಿಧ",
        "wood_other_hint"  to "ಮರದ ವಿಧ ನಮೂದಿಸಿ",
        "price_label"      to "ಬೆಲೆ (₹)",
        "price_hint"       to "ಉದಾ: ೧೨೦೦",
        "save_btn"         to "ಉಳಿಸಿ ಮತ್ತು ಬ್ರ್ಯಾಂಡ್ ಮಾಡಿ",
        "cancel"           to "ರದ್ದುಮಾಡಿ",
        "share_title"      to "ಉತ್ಪನ್ನ ಹಂಚಿಕೊಳ್ಳುವಿರಾ?",
        "share_msg"        to "ಫೋಟೋ ಉಳಿಸಲಾಗಿದೆ. ಈಗ ಹಂಚಿಕೊಳ್ಳುವಿರಾ?",
        "share_yes"        to "ಹಂಚಿಕೊಳ್ಳಿ",
        "later"            to "ನಂತರ",
        "saved_ok"         to "ಬ್ರ್ಯಾಂಡ್ ಫೋಟೋ ಉಳಿಸಲಾಗಿದೆ!",
        "save_fail"        to "ಚಿತ್ರ ಉಳಿಸಲು ವಿಫಲವಾಗಿದೆ.",
        "capture_fail"     to "ಕ್ಯಾಪ್ಚರ್ ವಿಫಲ. ಮತ್ತೆ ಪ್ರಯತ್ನಿಸಿ.",
        "load_fail"        to "ಚಿತ್ರ ಲೋಡ್ ಆಗಲಿಲ್ಲ.",
        "share_via"        to "ಮೂಲಕ ಹಂಚಿಕೊಳ್ಳಿ",
        "tilt_left"        to "ಎಡಕ್ಕೆ ವಾಲಿಸಿ ←",
        "tilt_right"       to "ಬಲಕ್ಕೆ ವಾಲಿಸಿ →",
        "level_ok"         to "ಸಮತಟ್ಟು ✓"
    )

    // English names always used for the branding text on the final photo
    private val woodOptions_en = arrayOf("Rosewood", "Sandalwood", "Teak", "Mango Wood", "Bamboo", "Other")
    private val woodOptions_kn = arrayOf("ರೋಸ್‌ವುಡ್", "ಶ್ರೀಗಂಧ", "ತೇಗ", "ಮಾವಿನ ಮರ", "ಬಿದಿರು", "ಇತರ")

    private fun s(key: String) = (if (isKannada) kn[key] else en[key]) ?: key
    private fun woodOptions()  = if (isKannada) woodOptions_kn else woodOptions_en

    // ---------- Lifecycle ----------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        overlayView   = findViewById(R.id.overlayView)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        applyLanguage()

        val previewView = findViewById<PreviewView>(R.id.previewView)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera(previewView)
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        }

        findViewById<Button>(R.id.captureBtn).setOnClickListener { takePhoto() }

        findViewById<Button>(R.id.langToggleBtn).setOnClickListener {
            isKannada = !isKannada
            applyLanguage()
        }
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun applyLanguage() {
        findViewById<TextView>(R.id.appTitle).text    = s("app_title")
        findViewById<Button>(R.id.captureBtn).text    = s("capture")
        findViewById<Button>(R.id.langToggleBtn).text = s("lang_toggle")
        // Update overlay hint text language too
        overlayView.setPlaceholderText(
            if (isKannada) "ಉತ್ಪನ್ನವನ್ನು ಇಲ್ಲಿ ಇಡಿ" else "Place product here"
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera(findViewById(R.id.previewView))
        }
    }

    // ---------- Sensor ----------

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_ACCELEROMETER) return
        val x = event.values[0]  // roll (left-right tilt)
        val y = event.values[1]  // pitch (forward-back tilt)

        // Phone is level in EITHER portrait or landscape if one axis is near 0
        val portraitLevel   = Math.abs(x) <= 1.5f   // normal upright hold
        val landscapeLevel  = Math.abs(y) <= 1.5f   // rotated 90 degrees

        val isLevel = portraitLevel || landscapeLevel

        val tiltNorm = (-x / 9.8f).coerceIn(-1f, 1f)
        val hint = when {
            isLevel            -> s("level_ok")
            x >  1.5f          -> s("tilt_right")
            else               -> s("tilt_left")
        }
        overlayView.updateTilt(tiltNorm, hint, isLevel)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // ---------- Camera ----------

    private fun startCamera(previewView: PreviewView) {
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            val provider = future.get()
            val preview  = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)
            imageCapture = ImageCapture.Builder().build()
            provider.unbindAll()
            provider.bindToLifecycle(
                this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture
            )
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val file = File(externalMediaDirs.first(), "${System.currentTimeMillis()}.jpg")
        imageCapture.takePicture(
            ImageCapture.OutputFileOptions.Builder(file).build(),
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    lastCapturedFile = file
                    runOnUiThread { showInputDialog() }
                }
                override fun onError(e: ImageCaptureException) {
                    e.printStackTrace()
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, s("capture_fail"), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    // ---------- Input dialog — fully localised ----------

    private fun showInputDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(56, 32, 56, 16)
        }

        // Artisan name
        val nameField = EditText(this).apply {
            hint = s("name_hint")
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS
        }

        // Wood type label
        val woodLabel = TextView(this).apply {
            text = s("wood_label")
            setPadding(0, 20, 0, 4)
            textSize = 14f
        }

        // Wood spinner — uses current language's option list
        val woodSpinner = Spinner(this).apply {
            adapter = ArrayAdapter(
                this@MainActivity,
                android.R.layout.simple_spinner_dropdown_item,
                woodOptions()          // ← picks kn or en based on isKannada
            )
        }

        // "Other / ಇತರ" free-text field — shown only for last option
        val otherField = EditText(this).apply {
            hint = s("wood_other_hint")
            inputType = android.text.InputType.TYPE_CLASS_TEXT or
                    android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS
            visibility = View.GONE
        }

        woodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                otherField.visibility =
                    if (pos == woodOptions().size - 1) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Price label + field
        val priceLabel = TextView(this).apply {
            text = s("price_label")
            setPadding(0, 20, 0, 4)
            textSize = 14f
        }
        val priceField = EditText(this).apply {
            hint = s("price_hint")
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }

        layout.addView(nameField)
        layout.addView(woodLabel)
        layout.addView(woodSpinner)
        layout.addView(otherField)
        layout.addView(priceLabel)
        layout.addView(priceField)

        android.app.AlertDialog.Builder(this)
            .setTitle(s("dialog_title"))
            .setView(layout)
            .setPositiveButton(s("save_btn")) { _, _ ->
                val name = nameField.text.toString().trim().ifEmpty { "Artisan" }
                val lastIdx = woodOptions().size - 1
                // Branding text on photo always uses English wood name
                val wood = if (woodSpinner.selectedItemPosition == lastIdx) {
                    otherField.text.toString().trim().ifEmpty { "Wood" }
                } else {
                    woodOptions_en[woodSpinner.selectedItemPosition]
                }
                val price = priceField.text.toString().trim()
                lastCapturedFile?.let { addBranding(it, name, wood, price) }
            }
            .setNegativeButton(s("cancel"), null)
            .show()
    }

    // ---------- Catalog branding ----------

    private fun addBranding(file: File, name: String, wood: String, price: String) {
        val src = BitmapFactory.decodeFile(file.absolutePath) ?: run {
            Toast.makeText(this, s("load_fail"), Toast.LENGTH_SHORT).show()
            return
        }

        val srcW    = src.width
        val srcH    = src.height
        val footerH = (srcH * 0.30f).toInt()
        val totalH  = srcH + footerH

        val output = Bitmap.createBitmap(srcW, totalH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // 1 — Original photo
        canvas.drawBitmap(src, 0f, 0f, null)

        // 2 — Cream footer
        canvas.drawRect(
            0f, srcH.toFloat(), srcW.toFloat(), totalH.toFloat(),
            Paint().apply { color = Color.parseColor("#F5F0E8") }
        )

        val gold = Color.parseColor("#7A5C1E")
        val pad  = srcW * 0.05f

        // 3 — Double rule
        Paint().apply { color = gold; strokeWidth = 4f; style = Paint.Style.STROKE }.also {
            canvas.drawLine(0f, srcH + 4f, srcW.toFloat(), srcH + 4f, it)
        }
        Paint().apply { color = gold; strokeWidth = 1f; style = Paint.Style.STROKE }.also {
            canvas.drawLine(0f, srcH + 11f, srcW.toFloat(), srcH + 11f, it)
        }

        // 4 — Heritage label
        canvas.drawText(
            "HANDMADE IN KARNATAKA",
            pad, srcH + footerH * 0.30f,
            Paint().apply {
                color         = gold
                textSize      = srcW * 0.028f
                isAntiAlias   = true
                typeface      = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                letterSpacing = 0.10f
            }
        )

        // 5 — Decorative diamonds
        canvas.drawText(
            "◆  ◆  ◆",
            pad, srcH + footerH * 0.48f,
            Paint().apply {
                color       = gold
                textSize    = srcW * 0.020f
                isAntiAlias = true
            }
        )

        // 6 — Artisan name, centred serif
        canvas.drawText(
            name,
            srcW / 2f, srcH + footerH * 0.72f,
            Paint().apply {
                color       = Color.parseColor("#1A1209")
                textSize    = srcW * 0.062f
                isAntiAlias = true
                typeface    = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                textAlign   = Paint.Align.CENTER
            }
        )

        val detailY = srcH + footerH * 0.92f

        // 7 — Wood type, left italic
        canvas.drawText(
            wood, pad, detailY,
            Paint().apply {
                color       = Color.parseColor("#5C4A2A")
                textSize    = srcW * 0.034f
                isAntiAlias = true
                typeface    = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            }
        )

        // 8 — Price, right bold gold
        if (price.isNotEmpty()) {
            canvas.drawText(
                "₹ $price", srcW - pad, detailY,
                Paint().apply {
                    color       = gold
                    textSize    = srcW * 0.050f
                    isAntiAlias = true
                    typeface    = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    textAlign   = Paint.Align.RIGHT
                }
            )
        }

        // 9 — Bottom rule
        Paint().apply { color = gold; strokeWidth = 1f; style = Paint.Style.STROKE }.also {
            canvas.drawLine(pad, totalH - 14f, srcW - pad, totalH - 14f, it)
        }

        // --- Save ---
        val uri: Uri = contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "ShilpaKala_${System.currentTimeMillis()}.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/ShilpaKala")
            }
        ) ?: run {
            Toast.makeText(this, s("save_fail"), Toast.LENGTH_SHORT).show()
            return
        }

        contentResolver.openOutputStream(uri)?.use { stream ->
            output.compress(Bitmap.CompressFormat.JPEG, 95, stream)
        }

        runOnUiThread {
            Toast.makeText(this, s("saved_ok"), Toast.LENGTH_SHORT).show()
            showSharePrompt(uri)
        }
    }

    // ---------- Share ----------

    private fun showSharePrompt(uri: Uri) {
        android.app.AlertDialog.Builder(this)
            .setTitle(s("share_title"))
            .setMessage(s("share_msg"))
            .setPositiveButton(s("share_yes")) { _, _ ->
                startActivity(
                    Intent.createChooser(
                        Intent(Intent.ACTION_SEND).apply {
                            type = "image/jpeg"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        },
                        s("share_via")
                    )
                )
            }
            .setNegativeButton(s("later"), null)
            .show()
    }
}