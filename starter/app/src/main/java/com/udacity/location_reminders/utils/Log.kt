package com.udacity.location_reminders.utils

import timber.log.Timber

class Log(private val TAG: String) {
    private enum class LogLevel {
        INFO,
        WARNING,
        DEBUG,
        ERROR
    }

    fun i(message: String) {
        log(LogLevel.INFO, message)
    }

    fun w(message: String) {
        log(LogLevel.WARNING, message)
    }

    fun d(message: String) {
        log(LogLevel.DEBUG, message)
    }

    fun e(message: String) {
        log(LogLevel.ERROR, message)
    }

    private fun log(level: LogLevel, message: String) {
        val threadId = Thread.currentThread().id
        val m = "$TAG :: thread $threadId :: $message"
        when(level) {
            LogLevel.INFO -> Timber.i(m)
            LogLevel.WARNING -> Timber.w(m)
            LogLevel.DEBUG -> Timber.d(m)
            LogLevel.ERROR -> Timber.e(m)
        }
    }
}