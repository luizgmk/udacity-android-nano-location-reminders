package com.udacity.location_reminders

import android.app.Application
import com.udacity.location_reminders.authentication.AuthenticationViewModel
import com.udacity.location_reminders.location_reminders.data.ReminderDataSource
import com.udacity.location_reminders.location_reminders.data.local.LocalDB
import com.udacity.location_reminders.location_reminders.data.local.RemindersLocalRepository
import com.udacity.location_reminders.location_reminders.reminderslist.RemindersListViewModel
import com.udacity.location_reminders.location_reminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) Timber.plant(Timber.DebugTree());

        /**
         * use Koin Library as a service locator
         */
        val myModule = module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            viewModel {
                AuthenticationViewModel(get())
            }
            //Declare singleton definitions to be later injected using by inject()
            single {
                //This view model is declared singleton to be used across multiple fragments
                SaveReminderViewModel(
                    get(),
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(this@MyApp) }
        }

        startKoin {
            androidContext(this@MyApp)
            modules(listOf(myModule))
        }
    }
}