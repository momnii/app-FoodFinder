package com.example.foodfinder

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.foodfinder.R

class FilterFragment : Fragment() {

    private lateinit var listener: OnFilterChangedListener
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var vegetarianCheckbox: CheckBox
    private lateinit var veganCheckbox: CheckBox
    private lateinit var resetButton: Button

    // Schlüssel für SharedPreferences
    private val PREFS_NAME = "FilterPrefs"
    private val VEGETARIAN_KEY = "vegetarian"
    private val VEGAN_KEY = "vegan"

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFilterChangedListener) {
            listener = context
        } else {
            throw RuntimeException("$context muss OnFilterChangedListener implementieren")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_filter, container, false)

        // SharedPreferences initialisieren
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Checkboxen finden
        vegetarianCheckbox = view.findViewById(R.id.vegetarian_checkbox)
        veganCheckbox = view.findViewById(R.id.vegan_checkbox)
        resetButton = view.findViewById(R.id.button_reset_filters)

        // Zustand der Checkboxen aus SharedPreferences laden
        loadCheckboxStates()

        // Listener für Checkboxen hinzufügen
        vegetarianCheckbox.setOnCheckedChangeListener { _, isChecked ->
            saveCheckboxState(VEGETARIAN_KEY, isChecked)
            listener.onFilterChanged(vegetarianCheckbox.isChecked, veganCheckbox.isChecked)
        }

        veganCheckbox.setOnCheckedChangeListener { _, isChecked ->
            saveCheckboxState(VEGAN_KEY, isChecked)
            listener.onFilterChanged(vegetarianCheckbox.isChecked, veganCheckbox.isChecked)
        }

        // Reset-Button-Listener einrichten
        resetButton.setOnClickListener {
            resetCheckboxes()
            listener.onFilterChanged(false, false) // Benachrichtige, dass Filter zurückgesetzt wurden
        }

        return view
    }

    // Zustand der Checkboxen speichern
    private fun saveCheckboxState(key: String, isChecked: Boolean) {
        with(sharedPreferences.edit()) {
            putBoolean(key, isChecked)
            apply()
        }
    }

    // Zustand der Checkboxen laden
    private fun loadCheckboxStates() {
        vegetarianCheckbox.isChecked = sharedPreferences.getBoolean(VEGETARIAN_KEY, false)
        veganCheckbox.isChecked = sharedPreferences.getBoolean(VEGAN_KEY, false)
    }

    // Checkboxen zurücksetzen
    private fun resetCheckboxes() {
        vegetarianCheckbox.isChecked = false
        veganCheckbox.isChecked = false
        saveCheckboxState(VEGETARIAN_KEY, false)
        saveCheckboxState(VEGAN_KEY, false)
    }

    interface OnFilterChangedListener {
        fun onFilterChanged(isVegetarian: Boolean, isVegan: Boolean)
    }
}