/*
 * Copyright (c) 2016  Marien Raat <marienraat@riseup.net>
 * Copyright (c) 2017  Stephen Michel <s@smichel.me>
 * SPDX-License-Identifier: GPL-3.0+
 */
package com.jmstudios.redmoon.filter

import com.jmstudios.redmoon.model.Profile

interface Filter {
    fun start()
    fun setColor(profile: Profile)
    fun stop()
}
