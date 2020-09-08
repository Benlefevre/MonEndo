package com.benlefevre.monendo.pain


import android.app.DatePickerDialog
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.benlefevre.monendo.R
import com.benlefevre.monendo.dashboard.models.Mood
import com.benlefevre.monendo.dashboard.models.Pain
import com.benlefevre.monendo.dashboard.models.Symptom
import com.benlefevre.monendo.dashboard.models.UserActivities
import com.benlefevre.monendo.utils.formatDateWithYear
import com.benlefevre.monendo.utils.parseStringInDate
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.custom_dialog_user_activities.view.*
import kotlinx.android.synthetic.main.fragment_pain.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class PainFragment : Fragment(R.layout.fragment_pain) {

    private lateinit var customView: View
    private lateinit var intensitySlider: Slider
    private lateinit var durationSlider: Slider
    private lateinit var otherTextInput: TextInputEditText

    private val viewModel: PainFragmentViewModel by viewModel()

    private val navController by lazy { findNavController() }
    private lateinit var pain: Pain
    var mood: Mood? = null
    private lateinit var symptoms: MutableList<Symptom>
    private var activityChoice: String = ""
    private val date = Date()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
        pain_card_date.setText(formatDateWithYear(Date()))
        setupOnClickListener()
        createChipWhenUserActivityAdded()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.pain_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.pain_fragment_save -> {
                saveUserInputInDb()
                navController.popBackStack()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createPain() {
        val location = when {
            pain_chip_abdo.isChecked -> getString(R.string.lower_abdomen)
            pain_chip_bladder.isChecked -> getString(R.string.bladder)
            pain_chip_back.isChecked -> getString(R.string.back)
            pain_chip_breast.isChecked -> getString(R.string.breast)
            pain_chip_head.isChecked -> getString(R.string.head)
            pain_chip_intestine.isChecked -> getString(R.string.intestine)
            pain_chip_vagina.isChecked -> getString(R.string.vagina)
            else -> ""
        }
        val date = parseStringInDate(pain_card_date.text.toString())
        pain = Pain(
            if (date != Date(-1L)) date else Date(),
            pain_slider.value.toInt(),
            location
        )
    }

    private fun createMood() {
        when {
            pain_chip_sad.isChecked -> mood =
                Mood(value = getString(R.string.sad))
            pain_chip_sick.isChecked -> mood =
                Mood(value = getString(R.string.sick))
            pain_chip_irritated.isChecked -> mood =
                Mood(value = getString(R.string.irritated))
            pain_chip_happy.isChecked -> mood =
                Mood(value = getString(R.string.happy))
            pain_chip_veryhappy.isChecked -> mood =
                Mood(value = getString(R.string.very_happy))
        }
    }

    private fun createSymptomsList() {
        symptoms = mutableListOf()
        if (pain_chip_burns.isChecked) symptoms.add(
            Symptom(
                name = getString(R.string.burns),
                date = date
            )
        )
        if (pain_chip_cramps.isChecked) symptoms.add(
            Symptom(
                name = getString(R.string.cramps),
                date = date
            )
        )
        if (pain_chip_bleeding.isChecked) symptoms.add(
            Symptom(
                name = getString(R.string.bleeding),
                date = date
            )
        )
        if (pain_chip_fever.isChecked) symptoms.add(
            Symptom(
                name = getString(R.string.fever),
                date = date
            )
        )
        if (pain_chip_bloating.isChecked) symptoms.add(
            Symptom(
                name = getString(R.string.bloating),
                date = date
            )
        )
        if (pain_chip_chills.isChecked) symptoms.add(
            Symptom(
                name = getString(R.string.chills),
                date = date
            )
        )
        if (pain_chip_constipation.isChecked) symptoms.add(
            Symptom(
                name = getString(R.string.constipation),
                date = date
            )
        )
        if (pain_chip_diarrhea.isChecked) symptoms.add(
            Symptom(
                name = getString(R.string.diarrhea),
                date = date
            )
        )
        if (pain_chip_hot_flush.isChecked) symptoms.add(
            Symptom(
                name = getString(R.string.hot_flush),
                date = date
            )
        )
        if (pain_chip_nausea.isChecked) symptoms.add(
            Symptom(
                name = getString(R.string.nausea),
                date = date
            )
        )
        if (pain_chip_tired.isChecked) symptoms.add(
            Symptom(
                name = getString(R.string.tired),
                date = date
            )
        )
    }

    private fun saveUserInputInDb() {
        createPain()
        createMood()
        createSymptomsList()
        viewModel.insertUserInput(pain, mood, symptoms)
    }

    private fun configureCustomDialogAccordingToSelectedView(view: View) {
        customView = layoutInflater.inflate(R.layout.custom_dialog_user_activities, null)
        intensitySlider = customView.dialog_intensity_slider
        durationSlider = customView.dialog_duration_slider
        otherTextInput = customView.dialog_other_choice_text

        customView.apply {
            when (view.id) {
                R.id.pain_card_sport -> {
                    val sportAdapter = ArrayAdapter<CharSequence>(
                        context,
                        R.layout.support_simple_spinner_dropdown_item,
                        resources.getStringArray(R.array.sport)
                    )
                    dialog_sport_choice_text.setAdapter(sportAdapter)
                    dialog_sport_choice_text.setOnItemClickListener { parent, _, position, _ ->
                        activityChoice = getString(
                            R.string.sport_activities,
                            parent.getItemAtPosition(position).toString().capitalize(Locale.ROOT)
                        )
                    }
                }
                R.id.pain_card_sleep -> {
                    dialog_sport_legend.visibility = View.GONE
                    dialog_duration_slider.visibility = View.GONE
                    dialog_duration_legend.visibility = View.GONE
                    dialog_intensity_legend.text = getString(R.string.rate_sleep_quality)
                }
                R.id.pain_card_stress -> {
                    dialog_sport_legend.visibility = View.GONE
                    dialog_duration_slider.visibility = View.GONE
                    dialog_duration_legend.visibility = View.GONE
                }
                R.id.pain_card_relaxation -> dialog_sport_legend.visibility = View.GONE
                R.id.pain_card_sex -> dialog_sport_legend.visibility = View.GONE
                R.id.pain_card_other -> {
                    dialog_other_legend.visibility = View.VISIBLE
                    dialog_sport_legend.visibility = View.GONE
                }
            }
        }
    }

    private fun openCustomDialog(view: View) {
        configureCustomDialogAccordingToSelectedView(view)
        MaterialAlertDialogBuilder(requireContext()).apply {
            setCancelable(false)
            setView(customView)
            setPositiveButton(getString(R.string.save)) { _, _ ->
                if (view.tag == getString(R.string.other)) activityChoice = getString(
                    R.string.other_activities,
                    otherTextInput.text.toString().capitalize(Locale.ROOT)
                )
                val activity =
                    UserActivities(
                        0, activityChoice, durationSlider.value.toInt(),
                        intensitySlider.value.toInt(), pain_slider.value.toInt(), date
                    )
                viewModel.addActivities(activity)
            }
            setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
            show()
        }
    }

    private fun createChipWhenUserActivityAdded() {
        viewModel.activitiesLiveData.observe(viewLifecycleOwner, {
            pain_activity_chipgroup.removeAllViews()
            it.forEach { activity ->
                val chip = Chip(context).apply {
                    text = activity.name
                    setTextColor(getColor(context, R.color.colorOnPrimary))
                    setChipBackgroundColorResource(R.color.colorSecondary)
                    setOnClickListener {
                        Snackbar.make(
                            pain_root,
                            getString(
                                R.string.detail_of_selected_activity,
                                activity.name,
                                activity.duration,
                                activity.intensity
                            ),
                            Snackbar.LENGTH_SHORT
                        )
                            .setAction(getString(R.string.delete)) {
                                viewModel.removeActivities(activity)
                            }
                            .show()
                    }
                }
                pain_activity_chipgroup.addView(chip)
            }
        })
    }

    private fun openDatePicker() {
        val calendar = Calendar.getInstance()
        val dateListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
            }
            pain_card_date.setText(formatDateWithYear(calendar.time))
        }
        context?.let {
            DatePickerDialog(
                it, dateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupOnClickListener() {
        pain_save_btn.setOnClickListener {
            saveUserInputInDb()
            navController.popBackStack()
        }
        pain_card_sport.setOnClickListener {
            openCustomDialog(it)
            it.tag = getString(R.string.sport)
        }
        pain_card_sleep.setOnClickListener {
            openCustomDialog(it)
            activityChoice = getString(R.string.sleep)
        }
        pain_card_stress.setOnClickListener {
            openCustomDialog(it)
            activityChoice = getString(R.string.stress)
        }
        pain_card_sex.setOnClickListener {
            openCustomDialog(it)
            activityChoice = getString(R.string.sex)
        }
        pain_card_relaxation.setOnClickListener {
            openCustomDialog(it)
            activityChoice = getString(R.string.relaxation)
        }
        pain_card_other.setOnClickListener {
            openCustomDialog(it)
            it.tag = getString(R.string.other)
        }
        pain_card_date.setOnClickListener {
            openDatePicker()
        }
    }
}
