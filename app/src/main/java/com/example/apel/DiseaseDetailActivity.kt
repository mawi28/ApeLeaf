package com.example.apel

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class DiseaseDetailActivity : AppCompatActivity() {

    private lateinit var diseaseViewPager: ViewPager2
    private lateinit var indicatorLayout: LinearLayout

    private val handler = Handler(Looper.getMainLooper())
    private var currentPage = 0
    private val delay: Long = 3000 // 3 detik
    private var imageList: List<Int> = emptyList()

    private val autoSlideRunnable = object : Runnable {
        override fun run() {
            currentPage = (currentPage + 1) % imageList.size
            diseaseViewPager.setCurrentItem(currentPage, true)
            handler.postDelayed(this, delay)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disease_detail)

        val btnBack = findViewById<ImageView>(R.id.btn_back)
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val diseaseName = intent.getStringExtra("disease_name")

        diseaseViewPager = findViewById(R.id.iv_disease)
        indicatorLayout = findViewById(R.id.indicator_layout)

        val diseaseTitleTextView: TextView = findViewById(R.id.tv_title)
        val diseaseDescriptionTextView: TextView = findViewById(R.id.tv_description)
        val diseaseCauseTextView: TextView = findViewById(R.id.tv_cause)
        val diseasePreventionTextView: TextView = findViewById(R.id.tv_prevention)
        val diseaseTreatmentTextView: TextView = findViewById(R.id.tv_treatment)

        // Set gravity center untuk semua TextView
        listOf(
            diseaseTitleTextView,
            diseaseDescriptionTextView,
            diseaseCauseTextView,
            diseasePreventionTextView,
            diseaseTreatmentTextView
        ).forEach { it.gravity = Gravity.CENTER }

        // Ambil data penyakit dari fungsi statis getDiseaseDetails
        val diseaseInfo = diseaseName?.let { getDiseaseDetails(it) }

        if (diseaseInfo != null) {
            diseaseTitleTextView.text = diseaseInfo.name
            diseaseDescriptionTextView.text = diseaseInfo.description
            diseaseCauseTextView.text = diseaseInfo.symptoms ?: "-"
            diseasePreventionTextView.text = diseaseInfo.prevention ?: "-"
            diseaseTreatmentTextView.text = diseaseInfo.treatment ?: "-"

            // Set gambar sesuai nama penyakit
            imageList = when (diseaseInfo.name) {
                "Apple Scab" -> listOf(R.drawable.scab, R.drawable.scab2, R.drawable.scab3)
                "Black Rot" -> listOf(R.drawable.blackrot, R.drawable.blackrot2, R.drawable.blackrot3)
                "Cedar Apple Rust" -> listOf(R.drawable.rust, R.drawable.rust2, R.drawable.rust3)
                else -> emptyList()
            }
        } else {
            // Jika penyakit tidak ditemukan, isi dengan default atau kosong
            diseaseTitleTextView.text = "Unknown Disease"
            diseaseDescriptionTextView.text = "-"
            diseaseCauseTextView.text = "-"
            diseasePreventionTextView.text = "-"
            diseaseTreatmentTextView.text = "-"
            imageList = emptyList()
        }

        diseaseViewPager.adapter = CardPagerAdapter(imageList)

        if (imageList.size > 1) {
            setupIndicators(imageList.size)
            setCurrentIndicator(0)

            diseaseViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    currentPage = position
                    setCurrentIndicator(position)
                }
            })

            handler.postDelayed(autoSlideRunnable, delay)
        }
    }

    private fun setupIndicators(count: Int) {
        indicatorLayout.removeAllViews()
        for (i in 0 until count) {
            val dot = ImageView(this)
            dot.setImageResource(R.drawable.indicator_inactive)
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.setMargins(8, 0, 8, 0)
            dot.layoutParams = params
            indicatorLayout.addView(dot)
        }
    }

    private fun setCurrentIndicator(index: Int) {
        for (i in 0 until indicatorLayout.childCount) {
            val imageView = indicatorLayout.getChildAt(i) as ImageView
            imageView.setImageResource(
                if (i == index) R.drawable.indicator_active
                else R.drawable.indicator_inactive
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(autoSlideRunnable)
    }

    data class DiseaseInfo(
        val name: String,
        val description: String,
        val symptoms: String? = null,
        val prevention: String? = null,
        val treatment: String? = null
    )

    companion object {
        fun getDiseaseDetails(diseaseName: String): DiseaseInfo? {
            val diseases = listOf(
                DiseaseInfo(
                    name = "Apple Scab",
                    description = "Apple Scab adalah penyakit jamur yang menyerang daun dan buah apel, menyebabkan bercak hitam dan deformasi pada daun dan buah.",
                    symptoms = "Penyakit ini disebabkan oleh jamur Venturia inaequalis yang berkembang biak melalui spora di lingkungan lembap, terutama saat musim hujan.",
                    prevention = "Pemangkasan cabang untuk meningkatkan sirkulasi udara, menjaga kebersihan kebun dengan mengumpulkan dan membuang daun dan buah yang jatuh, serta memilih varietas apel yang tahan penyakit.",
                    treatment = "Mengelola sanitasi kebun secara rutin dan menggunakan teknik budidaya yang baik seperti rotasi tanaman dan pemangkasan, tanpa menggunakan fungisida kimia."
                ),
                DiseaseInfo(
                    name = "Black Rot",
                    description = "Black Rot adalah penyakit jamur yang menyerang daun, buah, dan cabang apel, menyebabkan pembusukan hitam dan kematian jaringan tanaman.",
                    symptoms = "Disebabkan oleh jamur Botryosphaeria obtusa yang berkembang di jaringan tanaman yang terluka atau stres.",
                    prevention = "Pemangkasan cabang yang sakit dan penghilangan buah yang terinfeksi dari pohon dan tanah, menjaga kebersihan kebun, serta memperkuat tanaman dengan pemupukan yang tepat.",
                    treatment = "Meningkatkan kondisi tanaman dengan manajemen air dan nutrisi yang baik serta sanitasi kebun, tanpa menggunakan fungisida kimia."
                ),
                DiseaseInfo(
                    name = "Cedar Apple Rust",
                    description = "Cedar Apple Rust adalah penyakit jamur yang membutuhkan dua inang, yaitu pohon cedar dan apel, menyebabkan bercak oranye pada daun apel.",
                    symptoms = "Disebabkan oleh jamur Gymnosporangium juniperi-virginianae yang menginfeksi daun apel dan pohon cedar sebagai bagian siklus hidupnya.",
                    prevention = "Menjaga jarak tanam antara pohon apel dan cedar, pemangkasan cabang yang terinfeksi, serta menghilangkan pohon cedar di sekitar kebun.",
                    treatment = "Sanitasi kebun dengan menghilangkan daun dan cabang yang terinfeksi serta menerapkan teknik budidaya yang baik, tanpa penggunaan fungisida kimia."
                )
            )
            return diseases.find { it.name.equals(diseaseName, ignoreCase = true) }
        }
    }
}
