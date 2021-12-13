package com.android.mca2021.keyboard

interface KeyboardInteractionManager {
    enum class KeyboardType {
        ENGLISH,
        KOREAN,
        SYMBOL,
        EMOJI,
        CAMERA,
    }
    fun changeMode(mode: KeyboardType)
}