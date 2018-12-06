/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.chomper.livedata

import com.intellij.openapi.util.Condition
import org.jetbrains.annotations.NonNls

import javax.swing.*
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by kgmyshin on 2015/06/07.
 */
class PingEDT(
    @param:NonNls private val myName: String, private val myShutUpCondition: Condition<*>,
    private val myMaxUnitOfWorkThresholdMs: Int //-1 means indefinite
    , private val pingAction: Runnable
) {
    @Volatile
    private var stopped: Boolean = false
    @Volatile
    private var pinged: Boolean = false

    private val invokeLaterScheduled = AtomicBoolean()
    private val myUpdateRunnable = Runnable {
        val b = invokeLaterScheduled.compareAndSet(true, false)
        assert(b)
        if (stopped || myShutUpCondition.value(null)) {
            stop()
            return@Runnable
        }
        val start = System.currentTimeMillis()
        var processed = 0
        while (true) {
            if (processNext()) {
                processed++
            } else {
                break
            }
            val finish = System.currentTimeMillis()
            if (myMaxUnitOfWorkThresholdMs != -1 && finish - start > myMaxUnitOfWorkThresholdMs) break
        }
        if (!isEmpty) {
            scheduleUpdate()
        }
    }

    private val isEmpty: Boolean
        get() = !pinged

    private fun processNext(): Boolean {
        pinged = false
        pingAction.run()
        return pinged
    }

    // returns true if invokeLater was called
    fun ping(): Boolean {
        pinged = true
        return scheduleUpdate()
    }

    // returns true if invokeLater was called
    private fun scheduleUpdate(): Boolean {
        if (!stopped && invokeLaterScheduled.compareAndSet(false, true)) {
            SwingUtilities.invokeLater(myUpdateRunnable)
            return true
        }
        return false
    }

    fun stop() {
        stopped = true
    }
}
