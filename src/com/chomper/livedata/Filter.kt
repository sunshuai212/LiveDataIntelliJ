package com.chomper.livedata

import com.intellij.usages.Usage

/**
 * Created by kgmyshin on 2015/06/07.
 */
interface Filter {
    fun shouldShow(usage: Usage): Boolean
}
