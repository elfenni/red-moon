/*
 * Copyright (c) 2016 Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */
package com.jmstudios.redmoon.ui.preference

import android.content.Context
import android.content.res.TypedArray
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.preference.Preference
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView

import com.jmstudios.redmoon.R

import com.jmstudios.redmoon.filter.Command
import com.jmstudios.redmoon.util.Logger

abstract class SeekBarPreference(context: Context, attrs: AttributeSet) : Preference(context, attrs), SeekBar.OnSeekBarChangeListener {

    private lateinit var mSeekBar: SeekBar
    protected var mProgress: Int = 0
    private lateinit var mView: View

    open val DEFAULT_VALUE: Int = 20
    abstract val color: Int
    abstract val progress: Int
    abstract val suffix: String

    private val colorFilter: PorterDuffColorFilter
        get() = PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY)

    init {
        layoutResource = R.layout.preference_seekbar
    }

    fun setProgress(progress: Int) { mSeekBar.progress = progress }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getInteger(index, DEFAULT_VALUE)
    }

    override fun onSetInitialValue(restorePersistedValue: Boolean, defaultValue: Any?) {
        if (restorePersistedValue) {
            mProgress = getPersistedInt(DEFAULT_VALUE)
        } else {
            mProgress = (defaultValue as Int?) ?: DEFAULT_VALUE
            persistInt(mProgress)
        }
    }

    override fun onBindView(view: View) {
        Log.i("onBindView")
        super.onBindView(view)
        mView = view
        mSeekBar = view.findViewById(R.id.seekbar) as SeekBar
        setProgress(mProgress)
        mSeekBar.setOnSeekBarChangeListener(this)
        updateView()
    }

    //region OnSeekBarChangedListener
    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        Log.i("onSeekbarProgressChanged, $title to $progress")
        mProgress = progress
        persistInt(mProgress)
        updateView()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {
        pressesActive++
        Log.i("Touch down on a seek bar, $pressesActive presses active")
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        pressesActive--
        Log.i("Released a seek bar, $pressesActive presses active")
    }
    //end region

    private fun updateView() {
        if (isEnabled) {
            val moonIcon = mView.findViewById(R.id.moon_icon) as ImageView
            moonIcon.colorFilter = colorFilter
        }

        val progressView = mView.findViewById(R.id.seekbar_value) as TextView
        progressView.text = "$progress$suffix"
    }

    companion object : Logger() {
        private var pressesActive: Int = 0
            set(value) {
                Command.preview(value != 0)
                field = value
            }
    }
}
