package com.example.myapplication.ui.screens

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

enum class NewsTtsStatus {
    Initializing,
    Ready,
    Speaking,
    Paused,
    Error,
    Unsupported
}

data class NewsTtsUiState(
    val status: NewsTtsStatus = NewsTtsStatus.Initializing,
    val currentChunk: Int = 0,
    val totalChunks: Int = 0,
    val message: String? = null
) {
    val isSpeaking: Boolean get() = status == NewsTtsStatus.Speaking
    val isPaused: Boolean get() = status == NewsTtsStatus.Paused
    val canSpeak: Boolean get() = status == NewsTtsStatus.Ready || status == NewsTtsStatus.Paused || status == NewsTtsStatus.Speaking
}

class NewsTtsManager(
    context: Context,
    private val onStateChanged: (NewsTtsUiState) -> Unit
) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var tts: TextToSpeech? = null
    private var chunks: List<String> = emptyList()
    private var currentIndex: Int = 0
    private var paused: Boolean = false
    private var stoppedByUser: Boolean = false
    private var ready: Boolean = false

    init {
        updateState(NewsTtsUiState(status = NewsTtsStatus.Initializing, message = "Đang khởi tạo trình đọc..."))

        tts = TextToSpeech(context.applicationContext) { status ->
            val engine = tts
            if (status != TextToSpeech.SUCCESS || engine == null) {
                ready = false
                updateState(NewsTtsUiState(status = NewsTtsStatus.Error, message = "Không thể khởi tạo trình đọc."))
                return@TextToSpeech
            }

            val languageResult = engine.setLanguage(Locale("vi", "VN"))
            if (languageResult == TextToSpeech.LANG_MISSING_DATA || languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                ready = false
                updateState(NewsTtsUiState(status = NewsTtsStatus.Unsupported, message = "Thiết bị chưa hỗ trợ giọng đọc tiếng Việt."))
                return@TextToSpeech
            }

            engine.setSpeechRate(1.0f)
            engine.setPitch(1.0f)
            engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    updateState(
                        NewsTtsUiState(
                            status = NewsTtsStatus.Speaking,
                            currentChunk = currentIndex + 1,
                            totalChunks = chunks.size,
                            message = "Đang đọc bài viết"
                        )
                    )
                }

                override fun onDone(utteranceId: String?) {
                    if (paused || stoppedByUser) return

                    currentIndex += 1
                    if (currentIndex < chunks.size) {
                        speakCurrentChunk()
                    } else {
                        chunks = emptyList()
                        currentIndex = 0
                        updateState(NewsTtsUiState(status = NewsTtsStatus.Ready, message = "Đã đọc xong."))
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    updateState(NewsTtsUiState(status = NewsTtsStatus.Error, message = "Có lỗi khi đọc bài viết."))
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    updateState(NewsTtsUiState(status = NewsTtsStatus.Error, message = "Có lỗi khi đọc bài viết."))
                }
            })

            ready = true
            updateState(NewsTtsUiState(status = NewsTtsStatus.Ready, message = "Sẵn sàng đọc bài viết."))
        }
    }

    fun playOrPause(text: String) {
        when {
            !ready -> updateState(NewsTtsUiState(status = NewsTtsStatus.Error, message = "Trình đọc chưa sẵn sàng."))
            text.isBlank() -> updateState(NewsTtsUiState(status = NewsTtsStatus.Error, message = "Bài viết chưa có nội dung để đọc."))
            isSpeaking() -> pause()
            paused -> resume()
            else -> speak(text)
        }
    }

    fun speak(text: String) {
        val cleanedText = text
            .replace(Regex("\\s+"), " ")
            .trim()

        if (cleanedText.isBlank()) {
            updateState(NewsTtsUiState(status = NewsTtsStatus.Error, message = "Bài viết chưa có nội dung để đọc."))
            return
        }

        chunks = splitForTts(cleanedText)
        currentIndex = 0
        paused = false
        stoppedByUser = false
        speakCurrentChunk()
    }

    fun pause() {
        if (!isSpeaking()) return
        paused = true
        stoppedByUser = false
        tts?.stop()
        updateState(
            NewsTtsUiState(
                status = NewsTtsStatus.Paused,
                currentChunk = currentIndex + 1,
                totalChunks = chunks.size,
                message = "Đã tạm dừng."
            )
        )
    }

    fun resume() {
        if (!paused || chunks.isEmpty()) return
        paused = false
        stoppedByUser = false
        speakCurrentChunk()
    }

    fun stop() {
        stoppedByUser = true
        paused = false
        currentIndex = 0
        chunks = emptyList()
        tts?.stop()
        updateState(NewsTtsUiState(status = NewsTtsStatus.Ready, message = "Đã dừng đọc."))
    }

    fun release() {
        stoppedByUser = true
        paused = false
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }

    private fun speakCurrentChunk() {
        val text = chunks.getOrNull(currentIndex) ?: return
        val utteranceId = "news_tts_${currentIndex}_${System.currentTimeMillis()}"
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
    }

    private fun isSpeaking(): Boolean = tts?.isSpeaking == true

    private fun updateState(state: NewsTtsUiState) {
        mainHandler.post { onStateChanged(state) }
    }

    private fun splitForTts(text: String, maxLength: Int = 3200): List<String> {
        if (text.length <= maxLength) return listOf(text)

        val sentences = text.split(Regex("(?<=[.!?…。！？])\\s+"))
        val result = mutableListOf<String>()
        val current = StringBuilder()

        fun flushCurrent() {
            val value = current.toString().trim()
            if (value.isNotBlank()) result.add(value)
            current.clear()
        }

        sentences.forEach { sentence ->
            val cleanSentence = sentence.trim()
            if (cleanSentence.isBlank()) return@forEach

            if (cleanSentence.length > maxLength) {
                flushCurrent()
                cleanSentence.chunked(maxLength).forEach { result.add(it) }
                return@forEach
            }

            if (current.length + cleanSentence.length + 1 > maxLength) {
                flushCurrent()
            }

            if (current.isNotEmpty()) current.append(' ')
            current.append(cleanSentence)
        }

        flushCurrent()
        return result.ifEmpty { listOf(text.take(maxLength)) }
    }
}
