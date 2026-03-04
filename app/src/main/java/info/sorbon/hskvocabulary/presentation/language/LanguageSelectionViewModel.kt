package info.sorbon.hskvocabulary.presentation.language

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import info.sorbon.hskvocabulary.core.datastore.UserPreferences
import info.sorbon.hskvocabulary.data.repository.LanguagePackRepositoryImpl
import info.sorbon.hskvocabulary.domain.model.Language
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LanguageItem(val language: Language, val isActive: Boolean, val isDownloaded: Boolean, val isDownloading: Boolean)

@HiltViewModel
class LanguageSelectionViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val languagePackRepository: LanguagePackRepositoryImpl
) : ViewModel() {

    private val _languages = MutableStateFlow<List<LanguageItem>>(emptyList())
    val languages: StateFlow<List<LanguageItem>> = _languages.asStateFlow()

    private val downloadingCodes = mutableSetOf<String>()

    init { observeLanguages() }

    private fun observeLanguages() {
        viewModelScope.launch {
            combine(userPreferences.activeLanguageCode, userPreferences.downloadedLanguages) { active, downloaded ->
                Language.entries.map { lang ->
                    LanguageItem(
                        language = lang,
                        isActive = lang.code == active,
                        isDownloaded = lang.code in downloaded || lang.code == "en",
                        isDownloading = lang.code in downloadingCodes
                    )
                }
            }.collect { _languages.value = it }
        }
    }

    fun selectLanguage(lang: Language) {
        viewModelScope.launch {
            if (lang.code == "en" || _languages.value.find { it.language == lang }?.isDownloaded == true) {
                userPreferences.setActiveLanguage(lang.code)
            } else {
                downloadingCodes.add(lang.code)
                observeLanguages()
                try {
                    languagePackRepository.downloadLanguagePack(lang.code)
                    userPreferences.setActiveLanguage(lang.code)
                } catch (_: Exception) { }
                downloadingCodes.remove(lang.code)
                observeLanguages()
            }
        }
    }
}
