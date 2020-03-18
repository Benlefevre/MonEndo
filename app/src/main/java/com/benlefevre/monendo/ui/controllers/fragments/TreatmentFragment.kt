package com.benlefevre.monendo.ui.controllers.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.Data
import androidx.work.WorkManager
import com.benlefevre.monendo.R
import com.benlefevre.monendo.data.models.Treatment
import com.benlefevre.monendo.ui.adapters.TreatmentAdapter
import com.benlefevre.monendo.ui.adapters.TreatmentViewHolder
import com.benlefevre.monendo.ui.customview.ContraceptiveTablet
import com.benlefevre.monendo.ui.customview.Pill
import com.benlefevre.monendo.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.custom_dialog_treatment.view.*
import kotlinx.android.synthetic.main.fragment_treatment.*
import java.util.*

class TreatmentFragment : Fragment(R.layout.fragment_treatment) {

    private lateinit var notifHour: TextInputEditText
    private lateinit var dayMens: TextInputEditText
    private lateinit var pillTablet: ContraceptiveTablet
    private lateinit var addButton: ImageButton
    private lateinit var customDialog: View
    private lateinit var dialog: androidx.appcompat.app.AlertDialog
    private lateinit var treatment: Treatment

    private val treatmentList: MutableList<Treatment> by lazy {
        mutableListOf<Treatment>()
    }
    private val checkedPills: MutableList<Pill> by lazy {
        mutableListOf<Pill>()
    }

    private lateinit var sharedPreferences: SharedPreferences
    private val gson by lazy { Gson() }
    private lateinit var calendar: Calendar
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TreatmentAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        setupClickListener()
        sharedPreferences = requireContext().getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)
        calendar = Calendar.getInstance()
        getCheckedPills()
        getUserInput()
        getUserTreatment()
        removeFinishTreatment()
        configureRecyclerView()
        calculateElapsedTime()
    }

    private fun initViews() {
        notifHour = treatment_mens_notif_txt
        dayMens = treatment_mens_txt
        pillTablet = pill_tablet
        addButton = treatment_add_button
        recyclerView = treatment_recycler_view
    }

    /**
     * Fetches a Json in SharedPreferences and sets the list of checked pills in the tablet
     */
    private fun getCheckedPills() {
        gson.fromJson<List<Pill>>(
            sharedPreferences.getString(CHECKED_PILLS, null),
            object : TypeToken<List<Pill>>() {}.type
        )?.let {
            checkedPills.addAll(it)
        }
        if (!checkedPills.isNullOrEmpty())
            pillTablet.pills = checkedPills
    }

    /**
     * Bind user's input saved in SharedPreferences into fields
     */
    private fun getUserInput() {
        val monthLabel = Calendar.getInstance().get(Calendar.MONTH)
        sharedPreferences.getString(monthLabel.toString(), null)?.let {
            dayMens.setText(it)
        }
        sharedPreferences.getString(PILL_HOUR_NOTIF, null)?.let {
            notifHour.setText(it)
        }
    }

    /**
     * Fetches a Json in SharedPreferences and sets the list of user's treatments
     */
    private fun getUserTreatment() {
        gson.fromJson<List<Treatment>>(
                sharedPreferences.getString(TREATMENT, null),
                object : TypeToken<List<Treatment>>() {}.type
            )
            ?.let {
                treatmentList.addAll(it)
            }
    }

    /**
     * Removes each treatment that the duration is finished
     */
    private fun removeFinishTreatment() {
        val tempList = treatmentList.filter {
            it.duration != formatDateWithYear(with(Calendar.getInstance()) {
                add(Calendar.DAY_OF_YEAR, -1)
                time
            })
        }
        treatmentList.clear()
        treatmentList.addAll(tempList)
    }

    private fun configureRecyclerView() {
        adapter = TreatmentAdapter(treatmentList)
        adapter.setOnClickListener(View.OnClickListener { it ->
            val holder = it.tag as TreatmentViewHolder
            val position = holder.adapterPosition
            WorkManager.getInstance(requireContext())
                .cancelAllWorkByTag(treatmentList[position].name)
            treatmentList.removeAt(position)
            adapter.notifyDataSetChanged()
        })
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        recyclerView.adapter = adapter
    }

    /**
     * Calculates the elapsed time between the first day of the menstruation and the current day
     */
    private fun calculateElapsedTime() {
        var today = Date(-1L)
        if (!treatment_mens_txt.text.isNullOrBlank())
            today = parseStringInDate(treatment_mens_txt.text.toString())
        if (today != Date(-1L)) {
            var elapsedTime = System.currentTimeMillis() - today.time
            elapsedTime /= (24 * 60 * 60 * 1000)
            pillTablet.setupTablet(elapsedTime.toInt())
        }
    }

    /**
     * Sets a OnDateSetListener with updateMenstruationDate() and shows a DatePickerDialog
     */
    private fun openDatePicker() {
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            updateMenstruationDate()
        }
        context?.let {
            DatePickerDialog(
                it, dateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    /**
     * Fetches the date set in the DatePicker to bind it into the corresponding field and saves the
     * value into SharedPreferences.
     */
    private fun updateMenstruationDate() {
        dayMens.setText(formatDateWithYear(calendar.time))
        if (!dayMens.text.isNullOrBlank()) {
            val monthLabel = with(Calendar.getInstance()) {
                get(Calendar.MONTH)
            }
            sharedPreferences.edit()
                .putString(monthLabel.toString(), dayMens.text.toString())
                .apply()
        }
        calculateElapsedTime()
    }

    /**
     * Sets a OnTimeSetListener with updateNotifHour() ans shows a TimePickerDialog
     */
    private fun openTimePicker() {
        val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.apply {
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
            }
            updateNotifHour()
        }
        context?.let { it ->
            TimePickerDialog(
                it, timeListener, calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), true
            ).apply {
                setButton(DialogInterface.BUTTON_NEUTRAL, "Delete the time") { _, _ ->
                    notifHour.setText("")
                    sharedPreferences.edit().remove(PILL_HOUR_NOTIF).apply()
                    WorkManager.getInstance(context).cancelAllWorkByTag(PILL_TAG)
                }
            }.show()
        }
    }

    /**
     * Fetches the chosen time by user, updates the corresponding field and saves the value in
     * SharedPreferences.
     * Calls configurePillNotification() to enqueue a request with WorkManager
     */
    private fun updateNotifHour() {
        notifHour.setText(formatTime(calendar.time))
        if (!notifHour.text.isNullOrBlank()) {
            sharedPreferences.edit().putString(PILL_HOUR_NOTIF, notifHour.text.toString())
                .apply()
            val data = Data.Builder().putString(TREATMENT, PILL_TAG).build()
            context?.let { configureTreatmentNotification(it, data, notifHour.text.toString()) }
        }
    }

    /**
     * Sets and open a Dialog with a custom layout
     */
    private fun openDialogTreatment() {
        setupTreatmentDialog()
        treatment = Treatment()
        dialog = MaterialAlertDialogBuilder(context)
            .setView(customDialog)
            .setCancelable(false)
            .show()
    }

    /**
     * Configures all the dialog's fields behaviors
     */
    private fun setupTreatmentDialog() {
        customDialog = layoutInflater.inflate(R.layout.custom_dialog_treatment, null, false)
        customDialog.custom_treatment_duration.apply {
            setAdapter(ArrayAdapter(context, R.layout.support_simple_spinner_dropdown_item, resources.getStringArray(R.array.duration)))
        }
        customDialog.custom_treatment_format.apply {
            setAdapter(ArrayAdapter(context, R.layout.support_simple_spinner_dropdown_item, resources.getStringArray(R.array.format)))
        }
        customDialog.custom_treatment_morning_chip.apply {
            setOnCheckedChangeListener { view, isChecked ->
                if (isChecked) {
                    openTreatmentTimePicker(MORNING)
                } else {
                    view.text = getString(R.string.morning)
                    treatment.morning = ""
                }
            }
        }
        customDialog.custom_treatment_noon_chip.apply {
            setOnCheckedChangeListener { view, isChecked ->
                if (isChecked) {
                    openTreatmentTimePicker(NOON)
                } else {
                    view.text = getString(R.string.noon)
                    treatment.noon = ""
                }
            }
        }
        customDialog.custom_treatment_afternoon_chip.apply {
            setOnCheckedChangeListener { view, isChecked ->
                if (isChecked) {
                    openTreatmentTimePicker(AFTERNOON)
                } else {
                    view.text = getString(R.string.afternoon)
                    treatment.afternoon = ""
                }
            }
        }
        customDialog.custom_treatment_evening_chip.apply {
            setOnCheckedChangeListener { view, isChecked ->
                if (isChecked) {
                    openTreatmentTimePicker(EVENING)
                } else {
                    view.text = getString(R.string.evening)
                    treatment.evening = ""
                }
            }
        }
        customDialog.custom_treatment_cancel_btn.setOnClickListener {
            dialog.cancel()
        }
        customDialog.custom_treatment_save_btn.setOnClickListener {
            if (verifyTreatmentInput()) {
                setupTreatment()
                setTreatmentNotification()
                dialog.cancel()
            }
        }
    }

    /**
     * Defines a treatment with the user's input and adds it into the list
     */
    private fun setupTreatment() {
        treatment.format = customDialog.custom_treatment_format.text.toString()
        treatment.dosage = customDialog.custom_treatment_dosage.text.toString()
        treatment.duration =
            calculateTreatmentDuration(customDialog.custom_treatment_duration.text.toString())
        treatment.name = customDialog.custom_treatment_name.text.toString().capitalize()
        treatmentList.add(treatment)
        adapter.notifyDataSetChanged()
    }

    /**
     * Calculates the elapsed time between today and the last day of the given treatment
     */
    private fun calculateTreatmentDuration(lastDay: String): String {
        return if (lastDay != getString(R.string.permanent)) {
            calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, lastDay.substring(0, 2).trim().toInt())
            }
            formatDateWithYear(calendar.time)
        } else
            lastDay
    }

    /**
     * Defines the needed information that are passed to the NotificationWorker
     */
    private fun setTreatmentNotification() {
        val data = Data.Builder().apply {
            putString(TREATMENT, TREATMENT_TAG)
            putString(TREATMENT_NAME, treatment.name)
            putString(TREATMENT_DOSAGE, treatment.dosage)
            putString(TREATMENT_FORMAT, treatment.format)
        }.build()

        if (customDialog.custom_treatment_morning_chip.isChecked)
            context?.let { configureTreatmentNotification(it, data, treatment.morning, treatment.name) }
        if (customDialog.custom_treatment_noon_chip.isChecked)
            context?.let { configureTreatmentNotification(it, data, treatment.noon, treatment.name) }
        if (customDialog.custom_treatment_afternoon_chip.isChecked)
            context?.let { configureTreatmentNotification(it, data, treatment.afternoon, treatment.name) }
        if (customDialog.custom_treatment_evening_chip.isChecked)
            context?.let { configureTreatmentNotification(it, data, treatment.evening, treatment.name) }
    }

    /**
     * Verifies that all the dialog's fields are correctly filled
     */
    private fun verifyTreatmentInput(): Boolean {
        val isNameCorrect = verifyTreatmentName()
        val isDurationCorrect = verifyTreatmentDuration()
        val isDosageCorrect = verifyTreatmentDosage()
        val isFormatCorrect = verifyTreatmentFormat()
        return isNameCorrect && isDurationCorrect && isDosageCorrect && isFormatCorrect
    }

    /**
     * Verifies if the format field is correctly filled and configures the field's error label
     */
    private fun verifyTreatmentFormat(): Boolean {
        return if (customDialog.custom_treatment_format.text.isNullOrBlank()) {
            customDialog.custom_treatment_format_label.error = "Please enter the treatment's format"
            false
        } else {
            customDialog.custom_treatment_format_label.isErrorEnabled = false
            true
        }
    }

    /**
     * Verifies if the dosage field is correctly filled and configures the field's error label
     */
    private fun verifyTreatmentDosage(): Boolean {
        return if (customDialog.custom_treatment_dosage.text.isNullOrBlank()) {
            customDialog.custom_treatment_dosage_label.error = "Please enter the treatment's dosage"
            false
        } else {
            customDialog.custom_treatment_dosage_label.isErrorEnabled = false
            true
        }
    }

    /**
     * Verifies if the duration field is correctly filled and configures the field's error label
     */
    private fun verifyTreatmentDuration(): Boolean {
        return if (customDialog.custom_treatment_duration.text.isNullOrBlank()) {
            customDialog.custom_treatment_duration_label.error = "Please enter treatment's duration"
            false
        } else {
            customDialog.custom_treatment_duration_label.isErrorEnabled = false
            true
        }
    }

    /**
     * Verifies if the name field is correctly filled and configures the field's error label
     */
    private fun verifyTreatmentName(): Boolean {
        return if (customDialog.custom_treatment_name.text.isNullOrBlank()) {
            customDialog.custom_treatment_name_label.error = "Please enter the treatment's name"
            false
        } else {
            customDialog.custom_treatment_name_label.isErrorEnabled = false
            true
        }
    }

    /**
     * Sets a OnTimeSetListener with updateTreatmentHour() ans shows a TimePickerDialog
     */
    private fun openTreatmentTimePicker(hour: String) {
        val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            calendar.apply {
                set(Calendar.HOUR_OF_DAY, hourOfDay)
                set(Calendar.MINUTE, minute)
            }
            updateTreatmentHour(hour)
        }
        context?.let { it ->
            TimePickerDialog(
                it, timeListener, calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), true
            ).apply {
                setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel)) { dialog, _ ->
                    when (hour) {
                        MORNING -> customDialog.custom_treatment_morning_chip.isChecked = false
                        NOON -> customDialog.custom_treatment_noon_chip.isChecked = false
                        AFTERNOON -> customDialog.custom_treatment_afternoon_chip.isChecked = false
                        EVENING -> customDialog.custom_treatment_evening_chip.isChecked = false
                    }
                    dialog.cancel()
                }
            }.show()

        }
    }

    /**
     * Defines the treatment's taking hour according to the user's input
     */
    private fun updateTreatmentHour(hour: String) {
        when (hour) {
            MORNING -> {
                treatment.morning = formatTime(calendar.time)
                customDialog.custom_treatment_morning_chip.text = treatment.morning
            }
            NOON -> {
                treatment.noon = formatTime(calendar.time)
                customDialog.custom_treatment_noon_chip.text = treatment.noon
            }
            AFTERNOON -> {
                treatment.afternoon = formatTime(calendar.time)
                customDialog.custom_treatment_afternoon_chip.text = treatment.afternoon
            }
            EVENING -> {
                treatment.evening = formatTime(calendar.time)
                customDialog.custom_treatment_evening_chip.text = treatment.evening
            }
        }
    }

    private fun setupClickListener() {
        dayMens.setOnClickListener {
            openDatePicker()
        }
        notifHour.setOnClickListener {
            openTimePicker()
        }
        addButton.setOnClickListener {
            openDialogTreatment()
        }
    }

    override fun onPause() {
        super.onPause()
//        Save in SharedPreferences the checked pills
        sharedPreferences.edit().putString(CHECKED_PILLS, gson.toJson(pillTablet.pills)).apply()
//        Save in SharedPreferences the user's treatments
        sharedPreferences.edit().putString(TREATMENT, gson.toJson(treatmentList)).apply()
    }
}
