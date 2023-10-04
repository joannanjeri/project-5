package com.example.project5

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.Task
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentCont, TranslationFragment())
            .commit()
    }
}
class TranslationViewModel : ViewModel() {
    private val translator: Translator

    init {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.SPANISH)
            .build()
        translator = Translation.getClient(options)

        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()

        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener { }
            .addOnFailureListener { e ->
                throw e
            }
    }

    fun translateText(inputText: String): Task<String> {
        return translator
            .translate(inputText)
            .addOnSuccessListener { translatedText ->
                translatedText
            }
            .addOnFailureListener{ e ->
                throw e
            }
    }
}

    class TranslationFragment :  Fragment() {
        private lateinit var viewModel: TranslationViewModel

        private lateinit var editText: EditText
        private lateinit var translateButton: Button
        private lateinit var translationView: TextView

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            val view = inflater.inflate(R.layout.fragment_translation, container, false)
            editText = view.findViewById(R.id.editedText)
            translateButton = view.findViewById(R.id.translation)
            translationView = view.findViewById(R.id.translationView)
            return view
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            viewModel = ViewModelProvider(this).get(TranslationViewModel::class.java)
            translateButton.setOnClickListener {
                val inputText = editText.text.toString()

                viewModel.translateText(inputText)
                    .addOnSuccessListener { translatedText ->
                        translationView.text = "Translation: $translatedText"
                    }
                    .addOnFailureListener { e ->
                        e.printStackTrace()
                    }
            }
        }


    }





