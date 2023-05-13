package com.udacity.location_reminders.test_utils

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.runner.Description

@ExperimentalCoroutinesApi
class MainCoroutineRule(val dispatcher: TestDispatcher = UnconfinedTestDispatcher()) :
    InstantTaskExecutorRule() {
    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain (dispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        dispatcher.scheduler.runCurrent()
        dispatcher.scheduler.advanceUntilIdle()
        Dispatchers.resetMain()
    }
}