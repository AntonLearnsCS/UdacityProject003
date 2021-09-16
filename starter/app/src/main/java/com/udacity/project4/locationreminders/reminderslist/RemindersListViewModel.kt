package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.app.usage.UsageEvents
import android.content.ContentProvider
import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.test.core.app.ApplicationProvider
import com.udacity.project4.MyApp
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.launch
import org.jetbrains.annotations.TestOnly

class RemindersListViewModel(
     app: Application,
    private val dataSource: ReminderDataSource
) : BaseViewModel(app) {
    //private val dataSource: ReminderDataSource
    val selectedReminder = MutableLiveData<ReminderDataItem>()
    // list that holds the reminder data to be displayed on the UI
    val remindersList = MutableLiveData<List<ReminderDataItem>>()
    //TODO: Receiving error: "No instrumentation registered!"
    //private val dataSource: ReminderDataSource = (requireContext(ApplicationProvider.getApplicationContext()).applicationContext as MyApp).taskRepository
    /**
     * Get all the reminders from the DataSource and add them to the remindersList to be shown on the UI,
     * or show error if any
     */
    fun setSelectedReminderToNull()
    {
        selectedReminder.value = null
    }

    fun loadReminders() {
        //showLoading is of type "SingleLiveEvent" - This LiveData only calls the observable if there's an
        //explicit call to setValue() or call().

        showLoading.value = true
        viewModelScope.launch {
            //interacting with the dataSource has to be through a coroutine
            //returns a list of database items
            val result  = dataSource.getReminders()
            showLoading.postValue(false)

            when (result) {
                is Result.Success<*> -> {
                    val dataList = ArrayList<ReminderDataItem>()
                    dataList.addAll((result.data as List<ReminderDTO>).map { reminder ->
                        //map the reminder data from the DB to the be ready to be displayed on the UI
                        ReminderDataItem(
                            reminder.title,
                            reminder.description,
                            reminder.location,
                            reminder.latitude,
                            reminder.longitude,
                            reminder.id
                        )
                    })
                    remindersList.value = dataList
                }
                is Result.Error ->
                    showSnackBar.value = result.message
            }

            //check if no data has to be shown
            invalidateShowNoData()
        }
        Toast.makeText(ApplicationProvider.getApplicationContext(),"Loaded Reminders",Toast.LENGTH_SHORT).show()
    }

    fun removeTaskFromList(id : String)
    {
        viewModelScope.launch {
            dataSource.deleteTaskReminder(id)
        }
    }

    /**
     * Inform the user that there's not any data if the remindersList is empty
     */
    private fun invalidateShowNoData() {
        showNoData.value = remindersList.value == null || remindersList.value!!.isEmpty()
    }
}