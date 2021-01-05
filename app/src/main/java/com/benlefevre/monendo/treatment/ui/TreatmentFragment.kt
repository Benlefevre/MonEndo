package com.benlefevre.monendo.treatment.ui

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.benlefevre.monendo.R
import com.benlefevre.monendo.databinding.CustomDialogNbPillsBinding
import com.benlefevre.monendo.databinding.CustomDialogTreatmentBinding
import com.benlefevre.monendo.databinding.FragmentTreatmentBinding
import com.benlefevre.monendo.notification.AlarmReceiver
import com.benlefevre.monendo.notification.cancelPillAlarm
import com.benlefevre.monendo.notification.cancelTreatmentAlarm
import com.benlefevre.monendo.notification.createAlarmAtTheUserTime
import com.benlefevre.monendo.treatment.TreatmentAdapter
import com.benlefevre.monendo.treatment.TreatmentViewHolder
import com.benlefevre.monendo.treatment.models.Pill
import com.benlefevre.monendo.treatment.models.Treatment
import com.benlefevre.monendo.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.koin.android.ext.android.inject
import timber.log.Timber
import java.util.*

class TreatmentFragment : Fragment(R.layout.fragment_treatment) {

    private var _binding: FragmentTreatmentBinding? = null
    private val binding get() = _binding!!
    private var _dialogBinding : CustomDialogTreatmentBinding? = null
    private val dialogBinding get() = _dialogBinding!!
    private lateinit var dialog: androidx.appcompat.app.AlertDialog
    private lateinit var treatment: Treatment

    private val treatmentList = mutableListOf<Treatment>()
    private val checkedPills = mutableListOf<Pill>()

    private val sharedPreferences: SharedPreferences by inject()
    private val gson by lazy { Gson() }
    private lateinit var calendar: Calendar
    private lateinit var treatmentAdapter: TreatmentAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentTreatmentBinding.bind(view)
        setupClickListener()
        calendar = Calendar.getInstance()
        getCheckedPills()
        getUserInput()
        getUserTreatment()
        removeFinishTreatment()
        configureRecyclerView()
        calculateElapsedTime()
        calculateNextPill()
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.treatment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.pill_settings -> {
                openNumberOfPillsDialog()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun openNumberOfPillsDialog() {
        var userChoice = ""
        val numberPills = sharedPreferences.getString(NUMBER_OF_PILLS, "28")
        val pillsBinding = CustomDialogNbPillsBinding.inflate(
            LayoutInflater.from(context),
            null,
            false
        )
        val adapter =
            ArrayAdapter(
                requireContext(),
                R.layout.support_simple_spinner_dropdown_item,
                resources.getStringArray(R.array.pill_format)
            )
        pillsBinding.pillFormat.apply {
            setText(if (numberPills == "29") "21 + 7" else numberPills)
            setAdapter(adapter)

            setOnItemClickListener { parent, _, position, _ ->
                userChoice = parent.getItemAtPosition(position).toString()
                when (userChoice) {
                    "21 + 7" -> userChoice = "29"
                }
            }
        }
        MaterialAlertDialogBuilder(requireContext())
            .setView(pillsBinding.root)
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton("Ok") { dialog, _ ->
                if (userChoice.isNotBlank()) {
                    saveNbPillsInSharedPreferences(userChoice)
                    binding.pillTablet.setNumberOfPills(userChoice.toInt())
                    calculateNextPill()
                }
                dialog.cancel()
            }
            .setCancelable(false)
            .show()
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
        if (!checkedPills.isNullOrEmpty()) {
            binding.pillTablet.pills = checkedPills
        }
    }

    /**
     * Bind user's input saved in SharedPreferences into fields
     */
    private fun getUserInput() {
        val currentPill = sharedPreferences.getString(LAST_PILL_DATE, null)
        val nextPill = sharedPreferences.getString(NEXT_PILL_DATE, null)
        val nextPillDate = nextPill?.let { parseStringInDate(it) }
        val nextPillCalendar = Calendar.getInstance().apply {
            nextPillDate?.let {
                time = it
            }
        }
        if (Calendar.getInstance().before(nextPillCalendar)) {
            currentPill?.let {
                binding.mensTxt.setText(it)
            }
        } else {
            nextPill?.let {
                binding.mensTxt.setText(it)
            }
            sharedPreferences.edit().putString(LAST_PILL_DATE, nextPill).apply()
            calculateNextPill()
        }

        sharedPreferences.getString(PILL_HOUR_NOTIF, null)?.let {
            binding.mensNotifTxt.setText(it)
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
        val removeListTreatment = treatmentList.filter {
            it.duration == formatDateWithYear(with(Calendar.getInstance()) {
                add(Calendar.DAY_OF_YEAR, -1)
                time
            })
        }
        val tempList = treatmentList.filter {
            it.duration != formatDateWithYear(with(Calendar.getInstance()) {
                add(Calendar.DAY_OF_YEAR, -1)
                time
            })
        }
        removeListTreatment.forEach {
            cancelTreatmentWork(it)
        }
        treatmentList.clear()
        treatmentList.addAll(tempList)
    }

    /**
     * Configures the recycler view and its onClick's behavior.
     */
    private fun configureRecyclerView() {
        treatmentAdapter =
            TreatmentAdapter(treatmentList)
        treatmentAdapter.setOnClickListener {
            val holder = it.tag as TreatmentViewHolder
            val position = holder.adapterPosition
            cancelTreatmentWork(treatmentList[position])
            treatmentList.removeAt(position)
            treatmentAdapter.notifyDataSetChanged()
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = treatmentAdapter
        }
    }

    /**
     * Calculates the elapsed time between the first day of the menstruation and the current day
     */
    private fun calculateElapsedTime() {
        var today = Date(-1L)
        if (!binding.mensTxt.text.isNullOrBlank())
            today = parseStringInDate(binding.mensTxt.text.toString())
        if (today != Date(-1L)) {
            var elapsedTime = System.currentTimeMillis() - today.time
            elapsedTime /= (24 * 60 * 60 * 1000)
            Timber.i("current time = ${System.currentTimeMillis()} / todayTime = ${today.time} / elapsedTime = $elapsedTime")
            binding.pillTablet.setupTablet(elapsedTime.toInt())
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
        binding.mensTxtLabel.isErrorEnabled = false
        binding.mensTxt.setText(formatDateWithYear(calendar.time))
        if (!binding.mensTxt.text.isNullOrBlank()) {
            sharedPreferences.edit()
                .putString(LAST_PILL_DATE, binding.mensTxt.text.toString())
                .apply()
        }
        binding.pillTablet.clearTablet()
        configureResetCurrentChecked(requireContext())
        calculateNextPill()
        calculateElapsedTime()
    }

    /**
     * According to the number of pills in a tablet, calculates the start day of the next tablet
     */
    private fun calculateNextPill() {
        if (binding.mensTxt.text.isNullOrBlank()) {
            return
        }
        val nbPills = sharedPreferences.getString(NUMBER_OF_PILLS, "28")!!.toInt()
        val nextPillDate = parseStringInDate(binding.mensTxt.text.toString())
        val nextPill = if (nextPillDate != Date(-1L)) {
            formatDateWithYear(with(Calendar.getInstance()) {
                time = nextPillDate
                add(Calendar.DAY_OF_YEAR, if (nbPills == 29 || nbPills == 21) 28 else nbPills)
                time
            })
        } else {
            binding.mensTxt.text.toString()
        }
        sharedPreferences.edit()
            .putString(NEXT_PILL_DATE, nextPill)
            .apply()
        getUserInput()
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
                setButton(DialogInterface.BUTTON_NEUTRAL, getString(R.string.delete_time)) { _, _ ->
                    binding.mensNotifTxt.setText("")
                    sharedPreferences.edit().remove(PILL_HOUR_NOTIF).apply()
                    cancelPillWorks()
                }
            }.show()
        }
    }

    /**
     * Cancels all works or Alarms that are enqueued with PILL tags
     */
    private fun cancelPillWorks() {
        Timber.i("cancelPillWorks")
        cancelPillAlarm(requireContext())
    }

    /**
     * Cancels all works or Alarms that are enqueued with different tags created from the treatment's name
     */
    private fun cancelTreatmentWork(treatment: Treatment) {
        Timber.i("cancelTreatmentWork")
        cancelTreatmentAlarm(requireContext(), treatment, treatmentList.indexOf(treatment))
    }

    /**
     * Fetches the chosen time by user, updates the corresponding field and saves the value in
     * SharedPreferences.
     * Calls configurePillNotification() to enqueue a request with WorkManager
     */
    private fun updateNotifHour() {
        cancelPillWorks()
        binding.mensNotifTxt.setText(formatTime(calendar.time))
        if (!binding.mensNotifTxt.text.isNullOrBlank()) {
            sharedPreferences.edit()
                .putString(PILL_HOUR_NOTIF, binding.mensNotifTxt.text.toString())
                .apply()

            val repeatHour = setRepeatHour(calendar.time)

            val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
                putExtra(TREATMENT, PILL_TAG)
            }
            context?.let {
                createAlarmAtTheUserTime(
                    requireContext(),
                    intent,
                    binding.mensNotifTxt.text.toString(),
                    PILL_ID
                )
            }

            val repeatIntent = Intent(requireContext(), AlarmReceiver::class.java).apply {
                putExtra(TREATMENT, PILL_REPEAT)
            }
            context?.let {
                createAlarmAtTheUserTime(
                    requireContext(),
                    repeatIntent,
                    repeatHour,
                    PILL_REPEAT_ID
                )
            }
        }
    }

    /**
     * Sets and open a Dialog with a custom layout
     */
    private fun openDialogTreatment() {
        setupTreatmentDialog()
        treatment = Treatment()
        dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogBinding.root)
            .setCancelable(false)
            .show()
    }

    /**
     * Configures all the dialog's fields behaviors
     */
    private fun setupTreatmentDialog() {
        _dialogBinding =
            CustomDialogTreatmentBinding.inflate(
                LayoutInflater.from(requireContext()),
                null,
                false
            )
        with(dialogBinding) {
            treatmentDuration.apply {
                setAdapter(
                    ArrayAdapter(
                        context,
                        R.layout.support_simple_spinner_dropdown_item,
                        resources.getStringArray(R.array.duration)
                    )
                )
            }
            treatmentFormat.apply {
                setAdapter(
                    ArrayAdapter(
                        context,
                        R.layout.support_simple_spinner_dropdown_item,
                        resources.getStringArray(R.array.format)
                    )
                )
            }
            morningChip.apply {
                setOnCheckedChangeListener { view, isChecked ->
                    if (isChecked) {
                        openTreatmentTimePicker(MORNING)
                    } else {
                        view.text = getString(R.string.morning)
                        treatment.morning = ""
                    }
                }
            }
            noonChip.apply {
                setOnCheckedChangeListener { view, isChecked ->
                    if (isChecked) {
                        openTreatmentTimePicker(NOON)
                    } else {
                        view.text = getString(R.string.noon)
                        treatment.noon = ""
                    }
                }
            }
            afternoonChip.apply {
                setOnCheckedChangeListener { view, isChecked ->
                    if (isChecked) {
                        openTreatmentTimePicker(AFTERNOON)
                    } else {
                        view.text = getString(R.string.afternoon)
                        treatment.afternoon = ""
                    }
                }
            }
            eveningChip.apply {
                setOnCheckedChangeListener { view, isChecked ->
                    if (isChecked) {
                        openTreatmentTimePicker(EVENING)
                    } else {
                        view.text = getString(R.string.evening)
                        treatment.evening = ""
                    }
                }
            }
            cancelBtn.setOnClickListener {
                dialog.cancel()
            }
            saveBtn.setOnClickListener {
                if (verifyTreatmentInput()) {
                    setupTreatment()
                    setTreatmentNotification()
                    configureResetCurrentChecked(requireContext())
                    dialog.cancel()
                }
            }
        }
    }

    /**
     * Defines a treatment with the user's input and adds it into the list
     */
    private fun setupTreatment() {
        treatment.format = dialogBinding.treatmentFormat.text.toString()
        treatment.dosage = dialogBinding.treatmentDosage.text.toString()
        treatment.duration =
            calculateTreatmentDuration(dialogBinding.treatmentDuration.text.toString())
        treatment.name = dialogBinding.treatmentName.text.toString().capitalize(Locale.ROOT)
        treatmentList.add(treatment)
        treatmentAdapter.notifyDataSetChanged()
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
        val treatmentId = treatmentList.indexOf(treatment)

        val intent = Intent(requireContext(), AlarmReceiver::class.java).apply {
            putExtra(TREATMENT, TREATMENT_TAG)
            putExtra(TREATMENT_NAME, treatment.name)
            putExtra(TREATMENT_DOSAGE, treatment.dosage)
            putExtra(TREATMENT_FORMAT, treatment.format)
        }

        if (dialogBinding.morningChip.isChecked) {
            context?.let {
                createAlarmAtTheUserTime(
                    it,
                    intent,
                    treatment.morning,
                    treatmentId + 10
                )
            }
        }
        if (dialogBinding.noonChip.isChecked) {
            context?.let {
                createAlarmAtTheUserTime(
                    it,
                    intent,
                    treatment.noon,
                    treatmentId + 20
                )
            }
        }
        if (dialogBinding.afternoonChip.isChecked) {
            context?.let {
                createAlarmAtTheUserTime(
                    it,
                    intent,
                    treatment.afternoon,
                    treatmentId + 30
                )
            }
        }
        if (dialogBinding.eveningChip.isChecked)
            context?.let {
                createAlarmAtTheUserTime(
                    it,
                    intent,
                    treatment.evening,
                    treatmentId + 40
                )
            }
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
        return if (dialogBinding.treatmentFormat.text.isNullOrBlank()) {
            dialogBinding.treatmentFormatLabel.error =
                getString(R.string.enter_treatment_format)
            false
        } else {
            dialogBinding.treatmentFormatLabel.isErrorEnabled = false
            true
        }
    }

    /**
     * Verifies if the dosage field is correctly filled and configures the field's error label
     */
    private fun verifyTreatmentDosage(): Boolean {
        return if (dialogBinding.treatmentDosage.text.isNullOrBlank()) {
            dialogBinding.treatmentDosageLabel.error =
                getString(R.string.enter_treatment_dosage)
            false
        } else {
            dialogBinding.treatmentDosageLabel.isErrorEnabled = false
            true
        }
    }

    /**
     * Verifies if the duration field is correctly filled and configures the field's error label
     */
    private fun verifyTreatmentDuration(): Boolean {
        return if (dialogBinding.treatmentDuration.text.isNullOrBlank()) {
            dialogBinding.treatmentDurationLabel.error =
                getString(R.string.enter_treatment_duration)
            false
        } else {
            dialogBinding.treatmentDurationLabel.isErrorEnabled = false
            true
        }
    }

    /**
     * Verifies if the name field is correctly filled and configures the field's error label
     */
    private fun verifyTreatmentName(): Boolean {
        return if (dialogBinding.treatmentName.text.isNullOrBlank()) {
            dialogBinding.treatmentNameLabel.error =
                getString(R.string.enter_treatment_name)
            false
        } else {
            dialogBinding.treatmentNameLabel.isErrorEnabled = false
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
                setButton(
                    DialogInterface.BUTTON_NEGATIVE,
                    getString(R.string.cancel)
                ) { dialog, _ ->
                    when (hour) {
                        MORNING -> dialogBinding.morningChip.isChecked = false
                        NOON -> dialogBinding.noonChip.isChecked = false
                        AFTERNOON -> dialogBinding.afternoonChip.isChecked = false
                        EVENING -> dialogBinding.eveningChip.isChecked = false
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
                dialogBinding.morningChip.text = treatment.morning
            }
            NOON -> {
                treatment.noon = formatTime(calendar.time)
                dialogBinding.noonChip.text = treatment.noon
            }
            AFTERNOON -> {
                treatment.afternoon = formatTime(calendar.time)
                dialogBinding.afternoonChip.text = treatment.afternoon
            }
            EVENING -> {
                treatment.evening = formatTime(calendar.time)
                dialogBinding.eveningChip.text = treatment.evening
            }
        }
    }

    private fun setupClickListener() {
        binding.mensTxt.setOnClickListener {
            openDatePicker()
        }
        binding.mensNotifTxt.setOnClickListener {
            if (isStartDateEntered()) {
                openTimePicker()
            }
        }
        binding.addButton.setOnClickListener {
            openDialogTreatment()
        }
    }

    private fun isStartDateEntered(): Boolean {
        return if (binding.mensTxt.text.isNullOrBlank()) {
            binding.mensTxtLabel.error =
                getString(R.string.start_date_pill)
            false
        } else {
            binding.mensTxtLabel.isErrorEnabled = false
            true
        }
    }

    private fun saveNbPillsInSharedPreferences(userChoice: String) {
        sharedPreferences.edit()
            .putString(NUMBER_OF_PILLS, userChoice)
            .remove(CHECKED_PILLS)
            .apply()
    }

    override fun onPause() {
        super.onPause()
//        Save in SharedPreferences the checked pills
        sharedPreferences.edit().putString(CHECKED_PILLS, gson.toJson(binding.pillTablet.pills))
            .apply()
//        Save in SharedPreferences the user's treatments
        sharedPreferences.edit().putString(TREATMENT, gson.toJson(treatmentList)).apply()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _dialogBinding = null
    }
}
