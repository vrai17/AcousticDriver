package com.vraidev.acousticdriver.audio.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import com.vraidev.acousticdriver.AcousticDriverApp

class AudioSessionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, AudioEffect.ERROR_BAD_VALUE)
        if (sessionId == AudioEffect.ERROR_BAD_VALUE || sessionId == 0) return

        val app = context.applicationContext as? AcousticDriverApp ?: return
        val audioEngine = app.audioEngine

        when (action) {
            AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION -> {
                audioEngine.bindAudioSession(sessionId)
            }
            AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION -> {
                audioEngine.bindAudioSession(0)
            }
        }
    }
}
