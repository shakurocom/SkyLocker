/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.shakuro.skylocker

import android.os.SystemClock
import android.util.Log

import java.util.ArrayList

/**
 * A utility class to help log timings splits throughout a method call.
 * Typical usage is:

 * <pre>
 * TimingLogger timings = new TimingLogger(TAG, "methodA");
 * // ... do some work A ...
 * timings.addSplit("work A");
 * // ... do some work B ...
 * timings.addSplit("work B");
 * // ... do some work C ...
 * timings.addSplit("work C");
 * timings.dumpToLog();
</pre> *

 *
 * The dumpToLog call would add the following to the log:

 * <pre>
 * D/TAG     ( 3459): methodA: begin
 * D/TAG     ( 3459): methodA:      9 ms, work A
 * D/TAG     ( 3459): methodA:      1 ms, work B
 * D/TAG     ( 3459): methodA:      6 ms, work C
 * D/TAG     ( 3459): methodA: end, 16 ms
</pre> *
 */
class TimingLogger
/**
 * Create and initialize a TimingLogger object that will log using
 * the specific tag. If the Log.isLoggable is not enabled to at
 * least the Log.VERBOSE level for that tag at creation time then
 * the addSplit and dumpToLog call will do nothing.
 * @param label a string to be displayed with each log
 */
(label: String) {

    /** A label to be included in every log.  */
    private var mLabel: String? = null

    /** Used to track whether Log.isLoggable was enabled at reset time.  */
    private val mDisabled = false

    /** Stores the time of each split.  */
    internal var mSplits: ArrayList<Long>? = null

    /** Stores the labels for each split.  */
    internal var mSplitLabels: ArrayList<String> = ArrayList<String>()

    init {
        reset(label)
    }

    /**
     * Clear and initialize a TimingLogger object that will log using
     * the specific tag. If the Log.isLoggable is not enabled to at
     * least the Log.VERBOSE level for that tag at creation time then
     * the addSplit and dumpToLog call will do nothing.
     * @param label a string to be displayed with each log
     */
    fun reset(label: String) {
        mLabel = label
        reset()
    }

    /**
     * Clear and initialize a TimingLogger object that will log using
     * the tag and label that was specified previously, either via
     * the constructor or a call to reset(tag, label). If the
     * Log.isLoggable is not enabled to at least the Log.VERBOSE
     * level for that tag at creation time then the addSplit and
     * dumpToLog call will do nothing.
     */
    fun reset() {
        if (mDisabled) return
        if (mSplits == null) {
            mSplits = ArrayList<Long>()
            mSplitLabels = ArrayList<String>()
        } else {
            mSplits!!.clear()
            mSplitLabels.clear()
        }
        addSplit(null)
    }

    /**
     * Add a split for the current time, labeled with splitLabel. If
     * Log.isLoggable was not enabled to at least the Log.VERBOSE for
     * the specified tag at construction or reset() time then this
     * call does nothing.
     * @param splitLabel a label to associate with this split.
     */
    fun addSplit(splitLabel: String?) {
        if (mDisabled) return
        val now = SystemClock.elapsedRealtime()
        if (splitLabel != null) {
            mSplits!!.add(now)
            mSplitLabels.add(splitLabel)
        }
    }

    /**
     * Dumps the timings to the log using Log.d(). If Log.isLoggable was
     * not enabled to at least the Log.VERBOSE for the specified tag at
     * construction or reset() time then this call does nothing.
     */
    fun dumpToLog() {
        println(mLabel!! + ": begin")
        val first = mSplits!![0]
        var now = first
        for (i in 1..mSplits!!.size - 1) {
            now = mSplits!![i]
            val splitLabel = mSplitLabels[i]
            val prev = mSplits!![i - 1]

            println(mLabel + ":      " + (now - prev) + " ms, " + splitLabel)
        }
        println(mLabel + ": end, " + (now - first) + " ms")
    }
}

