package com.example.project5

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

/**
 * Main activity that has the TranslationFragment
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentCont, TranslationFragment())
            .commit()
    }
}
/**
 * Data class that has all the language options for translation
 */
data class LanguageOption(
    val sourceLanguage: String,
    val targetLanguage: String,
    val translatorOptions: TranslatorOptions
)

/**
 * ViewModel that manages all of the translation operations
 */
class TranslationViewModel : ViewModel() {
    private val translators: Map<String, Translator>

    init {
        val optionsList = listOf(
            LanguageOption(TranslateLanguage.ENGLISH, TranslateLanguage.SPANISH,
                TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                    .setTargetLanguage(TranslateLanguage.SPANISH)
                    .build()),
            LanguageOption(TranslateLanguage.ENGLISH, TranslateLanguage.GERMAN,
                TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.ENGLISH)
                    .setTargetLanguage(TranslateLanguage.GERMAN)
                    .build()),
            LanguageOption(TranslateLanguage.SPANISH, TranslateLanguage.ENGLISH,
                TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.SPANISH)
                    .setTargetLanguage(TranslateLanguage.ENGLISH)
                    .build()),
            LanguageOption(TranslateLanguage.GERMAN, TranslateLanguage.ENGLISH,
            TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.GERMAN)
                .setTargetLanguage(TranslateLanguage.ENGLISH)
                .build()),
            LanguageOption(TranslateLanguage.SPANISH, TranslateLanguage.GERMAN,
                TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.SPANISH)
                    .setTargetLanguage(TranslateLanguage.GERMAN)
                    .build()),
            LanguageOption(TranslateLanguage.GERMAN, TranslateLanguage.SPANISH,
                TranslatorOptions.Builder()
                    .setSourceLanguage(TranslateLanguage.GERMAN)
                    .setTargetLanguage(TranslateLanguage.SPANISH)
                    .build())
        )

        translators = optionsList.associateBy({ "${it.sourceLanguage}_${it.targetLanguage}" }) {
            Translation.getClient(it.translatorOptions).apply {
                val conditions = DownloadConditions.Builder().requireWifi().build()
                downloadModelIfNeeded(conditions)
            }
        }
    }

    /**
     * This translates the provided text from source language to the target language
     *
     * @param inputText The text to translate
     * @param sourceLang The source language
     * @param targetLang The target language
     * @return A task with the translated string
     */
    fun translateText(inputText: String, sourceLang: String, targetLang: String): Task<String> {
        val key = "${sourceLang}_$targetLang"
        if (translators.containsKey(key).not()) {
            Log.e("TranslationViewModel", "No translator is found for: $key")
        }
        return translators[key]?.translate(inputText) ?: throw Exception("Translator not found")
    }

}

/**
 * The fragment handles the translation UI for operations
 */
class TranslationFragment : Fragment() {
    private lateinit var viewModel: TranslationViewModel
    private lateinit var editText: EditText
    private lateinit var translateButton: Button
    private lateinit var translationView: TextView
    private lateinit var sourceLanguageButton: Button
    private lateinit var targetLanguageButton: Button
    private var sourceLanguage = TranslateLanguage.ENGLISH
    private var targetLanguage = TranslateLanguage.SPANISH

    /**
     * This inflates the fragment layout and initializes UI components
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_translation, container, false)
        editText = view.findViewById(R.id.editedText)
        translateButton = view.findViewById(R.id.translation)
        translationView = view.findViewById(R.id.translationView)
        sourceLanguageButton = view.findViewById(R.id.sourceLanguageButton)
        targetLanguageButton = view.findViewById(R.id.targetLanguageButton)
        return view
    }

    /**
     * This sets up the listeners and binds all the data
     */
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    viewModel = ViewModelProvider(this).get(TranslationViewModel::class.java)

    sourceLanguageButton.text = sourceLanguage
    targetLanguageButton.text = targetLanguage

    sourceLanguageButton.setOnClickListener {
        sourceLanguage = when (sourceLanguage) {
            TranslateLanguage.ENGLISH -> TranslateLanguage.SPANISH
            TranslateLanguage.SPANISH -> TranslateLanguage.GERMAN
            else -> TranslateLanguage.ENGLISH
        }
        sourceLanguageButton.text = sourceLanguage
    }

    targetLanguageButton.setOnClickListener {
        targetLanguage = when (targetLanguage) {
            TranslateLanguage.ENGLISH -> TranslateLanguage.SPANISH
            TranslateLanguage.SPANISH -> TranslateLanguage.GERMAN
            else -> TranslateLanguage.ENGLISH
        }
        targetLanguageButton.text = targetLanguage
    }

    translateButton.setOnClickListener {
        val inputText = editText.text.toString()

        viewModel.translateText(inputText, sourceLanguage, targetLanguage)
            .addOnSuccessListener { translatedText ->
                translationView.text = "Translation: $translatedText"
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }
}

}
