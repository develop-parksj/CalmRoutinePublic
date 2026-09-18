package com.gyoheul.calm_routine.data

import android.os.LocaleList
import java.util.Locale

object LocaleModel {
    private val supportedLanguages: List<String>
        get() = listOf("en", "de", "es", "fr", "hi", "ja", "ko", "pt", "ru", "zh-Hans")

    val languageCode: String
        get() {
            val deviceLangTag = LocaleList.getDefault()[0].toLanguageTag()
            return supportedLanguages.find { deviceLangTag.startsWith(it) } ?: "en"
        }

    fun getLanguageNameWithLanguageCode(languageCode: String = "en", targetCode: String = LocaleModel.languageCode): String {
        return Locale.forLanguageTag(targetCode).getDisplayLanguage(Locale.forLanguageTag(languageCode))
    }
}