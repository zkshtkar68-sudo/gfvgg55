package com.homelander.assistant

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {
    private lateinit var status: TextView
    private lateinit var heardText: TextView
    private lateinit var replyText: TextView
    private lateinit var listenButton: Button
    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private val requestAudio = 42

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(9, 13, 31)
        window.navigationBarColor = android.graphics.Color.rgb(9, 13, 31)
        window.decorView.systemUiVisibility = 0

        tts = TextToSpeech(this, this)
        buildUi()
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), requestAudio)
        }
        if (SpeechRecognizer.isRecognitionAvailable(this)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    status.text = "گوش می‌دهم…"
                    listenButton.text = "در حال شنیدن…"
                }
                override fun onBeginningOfSpeech() { status.text = "صدایت را دریافت می‌کنم" }
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() { status.text = "در حال پردازش گفتار…" }
                override fun onError(error: Int) {
                    status.text = "گفتار دریافت نشد؛ دوباره تلاش کن."
                    listenButton.text = "🎙 شروع مکالمه"
                }
                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val phrase = matches?.firstOrNull().orEmpty()
                    heardText.text = if (phrase.isBlank()) "چیزی تشخیص داده نشد." else "تو گفتی: $phrase"
                    listenButton.text = "🎙 شروع مکالمه"
                    if (phrase.isNotBlank()) handleCommand(phrase)
                }
                override fun onPartialResults(partialResults: Bundle?) {}
                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        } else {
            status.text = "سرویس تشخیص گفتار روی این گوشی در دسترس نیست."
        }
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(28), dp(24), dp(24))
            setBackgroundColor(android.graphics.Color.rgb(9, 13, 31))
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
        }
        val brand = TextView(this).apply {
            text = "HOMELANDER"
            textSize = 13f
            letterSpacing = 0.25f
            setTextColor(android.graphics.Color.rgb(105, 224, 255))
            gravity = Gravity.CENTER
        }
        content.addView(brand, matchWrap())
        val title = TextView(this).apply {
            text = "هوم‌لندر"
            textSize = 34f
            setTextColor(android.graphics.Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(0, dp(8), 0, dp(4))
        }
        content.addView(title, matchWrap())
        val subtitle = TextView(this).apply {
            text = "دستیار صوتی فارسی تو"
            textSize = 16f
            setTextColor(android.graphics.Color.rgb(184, 195, 225))
            gravity = Gravity.CENTER
        }
        content.addView(subtitle, matchWrap())
        val orb = TextView(this).apply {
            text = "◉"
            textSize = 86f
            gravity = Gravity.CENTER
            setTextColor(android.graphics.Color.rgb(111, 126, 255))
            setPadding(0, dp(16), 0, dp(10))
        }
        content.addView(orb, matchWrap())
        status = TextView(this).apply {
            text = "آماده‌ام؛ روی میکروفون بزن و صحبت کن."
            textSize = 14f
            gravity = Gravity.CENTER
            setTextColor(android.graphics.Color.rgb(105, 224, 255))
            setPadding(0, 0, 0, dp(16))
        }
        content.addView(status, matchWrap())
        listenButton = Button(this).apply {
            text = "🎙 شروع مکالمه"
            textSize = 17f
            isAllCaps = false
            setOnClickListener { startListening() }
        }
        content.addView(listenButton, LinearLayout.LayoutParams(-1, dp(56)))
        heardText = makeInfoText("گفتار شما اینجا نمایش داده می‌شود.")
        content.addView(heardText, matchWrap())
        replyText = makeInfoText("نمونه فرمان‌ها:\n• مرورگر را باز کن\n• یوتیوب را باز کن\n• ساعت چنده؟\n• با شماره 09… تماس بگیر\n• به شماره 09… پیام بده سلام")
        content.addView(replyText, matchWrap())
        val note = TextView(this).apply {
            text = "نسخه آزمایشی محلی • برای مکالمه آزاد با هوش مصنوعی، اتصال API لازم است."
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(android.graphics.Color.rgb(135, 146, 176))
            setPadding(0, dp(18), 0, 0)
        }
        content.addView(note, matchWrap())
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun makeInfoText(value: String) = TextView(this).apply {
        text = value
        textSize = 15f
        setTextColor(android.graphics.Color.WHITE)
        gravity = Gravity.CENTER
        setPadding(dp(12), dp(18), dp(12), dp(18))
    }

    private fun startListening() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO), requestAudio)
            return
        }
        val recognizer = speechRecognizer
        if (recognizer == null) {
            say("سرویس تشخیص گفتار روی این گوشی فعال نیست. سرویس گفتار را نصب یا فعال کن.")
            return
        }
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa-IR")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "fa-IR")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "با هوم‌لندر فارسی صحبت کن")
            }
            recognizer.startListening(intent)
        } catch (e: Exception) {
            say("شروع تشخیص گفتار ممکن نشد.")
        }
    }

    private fun handleCommand(raw: String) {
        val command = raw.lowercase(Locale("fa", "IR")).replace("‌", " ").trim()
        when {
            command.contains("مرورگر") || command.contains("اینترنت") || command.contains("کروم") ->
                openApp("com.android.chrome", "مرورگر")
            command.contains("یوتیوب") || command.contains("youtube") ->
                openApp("com.google.android.youtube", "یوتیوب")
            command.contains("ساعت") && (command.contains("چنده") || command.contains("الان") || command.contains("بگو")) -> {
                val now = java.text.SimpleDateFormat("HH:mm", Locale("fa", "IR")).format(java.util.Date())
                say("الان ساعت $now است.")
            }
            command.contains("تماس") || command.contains("زنگ") -> {
                val number = extractPhone(raw)
                if (number.isBlank()) say("شماره تلفن را هم بگو؛ مثلاً تماس با شماره 09123456789")
                else {
                    say("صفحه تماس را باز می‌کنم. برای برقراری تماس، دکمه تماس را بزن.")
                    try { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))) }
                    catch (_: Exception) { say("برنامه تماس در دسترس نیست.") }
                }
            }
            command.contains("پیامک") || command.contains("پیام بده") || command.contains("اس ام اس") -> {
                val number = extractPhone(raw)
                if (number.isBlank()) say("برای پیامک، شماره تلفن را هم بگو.")
                else {
                    val body = raw.substringAfter(number, "").replace(Regex("^[،,:\\s]+"), "").trim()
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("smsto:$number")
                        putExtra("sms_body", body.ifBlank { "سلام" })
                    }
                    try {
                        startActivity(intent)
                        say("پیام آماده شد؛ آن را بررسی کن و خودت ارسال کن.")
                    } catch (_: Exception) { say("برنامه پیامک در دسترس نیست.") }
                }
            }
            command.contains("سلام") -> say("سلام! من هوم‌لندر هستم. چه کمکی از دستم برمی‌آید؟")
            command.contains("چه کار") || command.contains("کمک") -> say("می‌توانم مرورگر یا یوتیوب را باز کنم، ساعت را بگویم و صفحه تماس یا پیامک را آماده کنم.")
            else -> say("فعلاً این فرمان را بلد نیستم. در این نسخه می‌توانی بگویی مرورگر را باز کن، یوتیوب را باز کن، ساعت چنده، یا تماس با شماره…")
        }
    }

    private fun extractPhone(text: String): String {
        val normalized = text.map { ch ->
            when (ch) {
                in '۰'..'۹' -> ('0'.code + (ch.code - '۰'.code)).toChar()
                in '٠'..'٩' -> ('0'.code + (ch.code - '٠'.code)).toChar()
                else -> ch
            }
        }
        return Regex("(\\+?\\d[\\d\\s-]{7,}\\d)").find(normalized)?.value?.replace(Regex("[\\s-]"), "") ?: ""
    }

    private fun openApp(packageName: String, appName: String) {
        val launch = packageManager.getLaunchIntentForPackage(packageName)
        if (launch != null) {
            say("$appName را باز می‌کنم.")
            try { startActivity(launch) } catch (_: ActivityNotFoundException) { say("باز کردن $appName ممکن نشد.") }
        } else if (appName == "مرورگر") {
            try { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com"))) }
            catch (_: Exception) { say("مرورگری روی گوشی پیدا نکردم.") }
        } else {
            say("$appName روی گوشی نصب نیست.")
        }
    }

    private fun say(message: String) {
        replyText.text = message
        status.text = "پاسخ هوم‌لندر"
        if (ttsReady) tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "homelander_reply")
        else Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onInit(statusCode: Int) {
        if (statusCode == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale("fa", "IR"))
            ttsReady = result != TextToSpeech.LANG_MISSING_DATA &&
                    result != TextToSpeech.LANG_NOT_SUPPORTED
            if (!ttsReady) {
                runOnUiThread {
                    status.text = "صدای فارسی در موتور گفتار گوشی موجود نیست؛ از تنظیمات TTS، صدای فارسی را نصب کن."
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == requestAudio && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) {
            status.text = "مجوز میکروفون فعال شد؛ آماده‌ام."
        } else if (requestCode == requestAudio) {
            status.text = "برای مکالمه صوتی باید مجوز میکروفون را بدهی."
        }
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
    private fun matchWrap() = LinearLayout.LayoutParams(-1, -2)
}
