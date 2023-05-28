package com.suraj854.trimmodule

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.suraj854.trimmodule.widget.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener
import com.suraj854.trimmodule.widget.crystalrangeseekbar.interfaces.OnRangeSeekbarFinalValueListener
import com.suraj854.trimmodule.widget.crystalrangeseekbar.widgets.CrystalRangeSeekbar


class crystal : AppCompatActivity() {
    lateinit var rangetext: TextView
    lateinit var range: CrystalRangeSeekbar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crystal)
        rangetext = findViewById(R.id.crystalRnage)
        range = findViewById(R.id.tag_accessibility_actions)
        range.setMinValue(0f)
        range.setMaxValue(100f)
        range.setBarHeight(140f)
        range.setMaxStartValue(100f)
        range.apply()

        range.setOnRangeSeekbarChangeListener(OnRangeSeekbarChangeListener { minValue, maxValue ->
            rangetext.setText("${minValue.toString()} // $maxValue")

        })

// set final value listener

// set final value listener
        range.setOnRangeSeekbarFinalValueListener(OnRangeSeekbarFinalValueListener { minValue, maxValue ->
            Log.d(
                "CRS=>",
                "$minValue : $maxValue"
            )
        })

    }
}