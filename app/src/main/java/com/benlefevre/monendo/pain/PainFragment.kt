package com.benlefevre.monendo.pain

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat.getColor
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.benlefevre.monendo.R
import com.benlefevre.monendo.dashboard.models.Mood
import com.benlefevre.monendo.dashboard.models.Pain
import com.benlefevre.monendo.dashboard.models.Symptom
import com.benlefevre.monendo.dashboard.models.UserActivities
import com.benlefevre.monendo.databinding.CustomDialogUserActivitiesBinding
import com.benlefevre.monendo.databinding.FragmentPainBinding
import com.benlefevre.monendo.utils.formatDateWithYear
import com.benlefevre.monendo.utils.parseStringInDate
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*

class PainFragment : Fragment(R.layout.fragment_pain) {

    private var _binding: FragmentPainBinding? = null
    private val binding get() = _binding!!
    private var _dialogBinding: CustomDialogUserActivitiesBinding? = null
    private val dialogBinding get() = _dialogBinding!!
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
        _binding = FragmentPainBinding.bind(view)
        setHasOptionsMenu(true)
        binding.cardDate.setText(formatDateWithYear(Date()))
        setupOnClickListener()
        createChipWhenUserActivityAdded()
        viewModel.insertDone.observe(viewLifecycleOwner, configureObserver())
    }

    private fun configureObserver(): Observer<Boolean> {
        return Observer {
            when (it) {
                true -> navController.popBackStack()
                false -> return@Observer
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.pain_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.pain_fragment_save -> {
                saveUserInputInDb()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createPain() {
        val location = when {
            binding.chipAbdo.isChecked -> getString(R.string.lower_abdomen)
            binding.chipBladder.isChecked -> getString(R.string.bladder)
            binding.chipBack.isChecked -> getString(R.string.back)
            binding.chipBreast.isChecked -> getString(R.string.breast)
            binding.chipHead.isChecked -> getString(R.string.head)
            binding.chipIntestine.isChecked -> getString(R.string.intestine)
            binding.chipVagina.isChecked -> getString(R.string.vagina)
            else -> ""
        }
        val date = parseStringInDate(binding.cardDate.text.toString())
        pain = Pain(
            if (date != Date(-1L)) date else Date(),
            binding.painSlider.value.toInt(),
            location
        )
    }

    private fun createMood() {
        when {
            binding.chipSad.isChecked -> mood =
                Mood(value = getString(R.string.sad))
            binding.chipSick.isChecked -> mood =
                Mood(value = getString(R.string.sick))
            binding.chipIrritated.isChecked -> mood =
                Mood(value = getString(R.string.irritated))
            binding.chipHappy.isChecked -> mood =
                Mood(value = getString(R.string.happy))
            binding.chipVeryhappy.isChecked -> mood =
                Mood(value = getString(R.string.very_happy))
        }
    }

    private fun createSymptomsList() {
        symptoms = mutableListOf()
        if (binding.chipBurns.isChecked) symptoms.add(
            Symptom(
                name = getString(R.string.burns),
                date = date
            )
        )
        if (binding.chipCramps.isChecked) symptoms.add(
            Symptom(
                name = getString(R.string.cramps),
                date = date
            )
        )
        if (binding.chipBleeding.isChecked) symptoms.add(
            Symptom(
                name = getString(R.string.bleeding),
                date = date
            )
        )
        if (binding.chipFever.isChecked) symptoms.add(
            Symptom(
                name = getString(R.string.fever),
                date = date
            )
        )
        if (binding.chipBloating.isChecked) symptoms.add(
            Symptom(
                name = getString(R.string.bloating),
                date = date
            )
        )
        if (binding.chipChills.isChecked) symptoms.add(
            Symptom(
                name = getString(R.string.chills),
                date = date
            )
        )
        if (binding.chipConstipation.isChecked) symptoms.add(
            Symptom(
                name = getString(R.string.constipation),
                date = date
            )
        )
        if (binding.chipDiarrhea.isChecked) symptoms.add(
            Symptom(
                name = getString(R.string.diarrhea),
                date = date
            )
        )
        if (binding.chipHotFlush.isChecked) symptoms.add(
            Symptom(
                name = getString(R.string.hot_flush),
                date = date
            )
        )
        if (binding.chipNausea.isChecked) symptoms.add(
            Symptom(
                name = getString(R.string.nausea),
                date = date
            )
        )
        if (binding.chipTired.isChecked) symptoms.add(
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
        _dialogBinding =
            CustomDialogUserActivitiesBinding.inflate(LayoutInflater.from(context), null, false)
        intensitySlider = dialogBinding.intensitySlider
        durationSlider = dialogBinding.durationSlider
        otherTextInput = dialogBinding.otherChoiceText

        dialogBinding.root.apply {
            when (view.id) {
                R.id.card_sport -> {
                    val sportAdapter = ArrayAdapter<CharSequence>(
                        context,
                        R.layout.support_simple_spinner_dropdown_item,
                        resources.getStringArray(R.array.sport)
                    )
                    dialogBinding.sportChoiceText.apply {
                        setAdapter(sportAdapter)
                        setOnItemClickListener { parent, _, position, _ ->
                            activityChoice = getString(
                                R.string.sport_activities,
                                parent.getItemAtPosition(position).toString()
                                    .capitalize(Locale.ROOT)
                            )
                        }
                    }
                }
                R.id.card_sleep -> {
                    dialogBinding.sportLegend.visibility = View.GONE
                    dialogBinding.durationSlider.visibility = View.GONE
                    dialogBinding.durationLegend.visibility = View.GONE
                    dialogBinding.intensityLegend.text =
                        getString(R.string.rate_sleep_quality)
                }
                R.id.card_stress -> {
                    dialogBinding.sportLegend.visibility = View.GONE
                    dialogBinding.durationSlider.visibility = View.GONE
                    dialogBinding.durationLegend.visibility = View.GONE
                }
                R.id.card_relaxation -> dialogBinding.sportLegend.visibility = View.GONE
                R.id.card_sex -> dialogBinding.sportLegend.visibility = View.GONE
                R.id.card_other -> {
                    dialogBinding.otherLegend.visibility = View.VISIBLE
                    dialogBinding.sportLegend.visibility = View.GONE
                }
            }
        }
    }

    private fun openCustomDialog(view: View) {
        configureCustomDialogAccordingToSelectedView(view)
        MaterialAlertDialogBuilder(requireContext()).apply {
            setCancelable(false)
            setView(dialogBinding.root)
            setPositiveButton(getString(R.string.save)) { _, _ ->
                if (view.tag == getString(R.string.other)) activityChoice = getString(
                    R.string.other_activities,
                    otherTextInput.text.toString().capitalize(Locale.ROOT)
                )
                val activity =
                    UserActivities(
                        0, activityChoice, durationSlider.value.toInt(),
                        intensitySlider.value.toInt(), binding.painSlider.value.toInt(), date
                    )
                viewModel.addActivities(activity)
            }
            setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
            show()
        }
    }

    private fun createChipWhenUserActivityAdded() {
        viewModel.activitiesLiveData.observe(viewLifecycleOwner, {
            binding.activityChipgroup.removeAllViews()
            it.forEach { activity ->
                val chip = Chip(context).apply {
                    text = activity.name
                    setTextColor(getColor(context, R.color.colorOnPrimary))
                    setChipBackgroundColorResource(R.color.colorSecondary)
                    setOnClickListener {
                        Snackbar.make(
                            binding.painRoot,
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
                binding.activityChipgroup.addView(chip)
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
            binding.cardDate.setText(formatDateWithYear(calendar.time))
        }
        context?.let {
            DatePickerDialog(
                it, dateListener, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupOnClickListener() {
        binding.saveBtn.setOnClickListener {
            saveUserInputInDb()
        }
        binding.cardSport.setOnClickListener {
            openCustomDialog(it)
            it.tag = getString(R.string.sport)
        }
        binding.cardSleep.setOnClickListener {
            openCustomDialog(it)
            activityChoice = getString(R.string.sleep)
        }
        binding.cardStress.setOnClickListener {
            openCustomDialog(it)
            activityChoice = getString(R.string.stress)
        }
        binding.cardSex.setOnClickListener {
            openCustomDialog(it)
            activityChoice = getString(R.string.sex)
        }
        binding.cardRelaxation.setOnClickListener {
            openCustomDialog(it)
            activityChoice = getString(R.string.relaxation)
        }
        binding.cardOther.setOnClickListener {
            openCustomDialog(it)
            it.tag = getString(R.string.other)
        }
        binding.cardDate.setOnClickListener {
            openDatePicker()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        _dialogBinding = null
    }
}
