package com.ecci.taskmanager.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return TextView(context).apply {
            text = "⚙️ Configuracion\n\n(En desarrollo - Sprint 4)"
            textSize = 20f
            setPadding(32, 32, 32, 32)
            textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        }
    }
}