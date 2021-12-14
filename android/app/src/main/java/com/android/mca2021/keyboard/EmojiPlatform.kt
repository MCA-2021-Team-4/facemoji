package com.android.mca2021.keyboard

enum class EmojiPlatform {
    APPLE,
    FACEBOOK,
    GOOGLE,
    SAMSUNG,
    TWITTER;

    companion object {
        fun from(name: String) = when(name) {
            "apple" -> APPLE
            "facebook" -> FACEBOOK
            "google" -> GOOGLE
            "samsung" -> SAMSUNG
            "twitter" -> TWITTER
            else -> GOOGLE
        }

        val all = listOf(
            APPLE,
            FACEBOOK,
            GOOGLE,
            SAMSUNG,
            TWITTER,
        )
    }
}