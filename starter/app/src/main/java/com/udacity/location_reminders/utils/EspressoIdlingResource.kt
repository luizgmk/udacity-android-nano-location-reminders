// From Android Testing Samples
package com.udacity.location_reminders.utils

import androidx.test.espresso.idling.CountingIdlingResource
import java.util.concurrent.atomic.AtomicInteger

object EspressoIdlingResource {

    private const val RESOURCE = "GLOBAL"

    private val counter = AtomicInteger(0)

    @JvmField
    val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}

inline fun <T> wrapEspressoIdlingResource(function: () -> T): T {
// Espresso does not work well with coroutines yet. See
// https://github.com/Kotlin/kotlinx.coroutines/issues/982
    EspressoIdlingResource.increment() // Set app as busy.
    return try {
        function()
    } finally {
        EspressoIdlingResource.decrement() // Set app as idle.
    }
}