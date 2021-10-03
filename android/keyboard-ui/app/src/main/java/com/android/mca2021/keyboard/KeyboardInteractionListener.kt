package com.android.mca2021.keyboard

interface KeyboardInteractionListener {
    enum class KeyboardType {
        ENGLISH,
        KOREAN,
        SYMBOLS,
        CAMERA,
    }
    fun changeMode(mode: KeyboardType)
}