package com.example.dosecalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.dosecalc.ui.DoseCalcScreen
import com.example.dosecalc.ui.theme.DoseCalcTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoseCalcTheme {
                DoseCalcScreen()
            }
        }
    }
}
