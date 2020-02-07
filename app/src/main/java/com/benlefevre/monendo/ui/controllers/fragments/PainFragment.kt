package com.benlefevre.monendo.ui.controllers.fragments


import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.benlefevre.monendo.R
import com.benlefevre.monendo.data.models.Mood
import com.benlefevre.monendo.data.models.Pain
import com.benlefevre.monendo.data.models.Symptom
import com.benlefevre.monendo.data.models.UserActivities
import com.benlefevre.monendo.injection.Injection
import com.benlefevre.monendo.ui.viewmodels.PainFragmentViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.custom_dialog_user_activities.view.*
import kotlinx.android.synthetic.main.fragment_pain.*
import java.util.*


class PainFragment : Fragment() {

    private lateinit var customView: View
    private lateinit var intensitySlider: Slider
    private lateinit var durationSlider: Slider
    private lateinit var otherTextInput: TextInputEditText

    private val viewModel by lazy {
        ViewModelProvider(
            this, Injection.providerViewModelFactory(
                activity!!.applicationContext
            )
        ).get(PainFragmentViewModel::class.java)
    }
    private val navController by lazy { findNavController() }
    private lateinit var pain: Pain
    var mood: Mood? = null
    private lateinit var symptoms: MutableList<Symptom>
    private var activityChoice: String = ""
    private val date = Date()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_pain, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
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
                navController.navigate(R.id.dashboardFragment)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun createPain() {
        var location = ""
        when {
            pain_chip_abdo.isChecked -> location = getString(R.string.lower_abdomen)
            pain_chip_bladder.isChecked -> location = getString(R.string.bladder)
            pain_chip_back.isChecked -> location = getString(R.string.back)
            pain_chip_breast.isChecked -> location = getString(R.string.breast)
            pain_chip_head.isChecked -> location = getString(R.string.head)
            pain_chip_intestine.isChecked -> location = getString(R.string.intestine)
            pain_chip_vagina.isChecked -> location = getString(R.string.vagina)
        }
        pain = Pain(Date(), pain_slider.value.toInt(), location)
    }

    fun createMood() {
        when {
            pain_chip_sad.isChecked -> mood = Mood(value = getString(R.string.sad))
            pain_chip_sick.isChecked -> mood = Mood(value = getString(R.string.sick))
            pain_chip_irritated.isChecked -> mood = Mood(value = getString(R.string.irritated))
            pain_chip_happy.isChecked -> mood = Mood(value = getString(R.string.happy))
            pain_chip_veryhappy.isChecked -> mood = Mood(value = getString(R.string.very_happy))
        }
    }

    fun createSymptomsList() {
        symptoms = mutableListOf()
        if (pain_chip_burns.isChecked) symptoms.add(Symptom(name = getString(R.string.burns), date = date))
        if (pain_chip_cramps.isChecked) symptoms.add(Symptom(name = getString(R.string.burns), date = date))
        if (pain_chip_bleeding.isChecked) symptoms.add(Symptom(name = getString(R.string.bleeding), date = date))
        if (pain_chip_fever.isChecked) symptoms.add(Symptom(name = getString(R.string.fever), date = date))
        if (pain_chip_bloating.isChecked) symptoms.add(Symptom(name = getString(R.string.bloating), date = date))
        if (pain_chip_chills.isChecked) symptoms.add(Symptom(name = getString(R.string.chills), date = date))
        if (pain_chip_constipation.isChecked) symptoms.add(Symptom(name = getString(R.string.constipation), date = date))
        if (pain_chip_diarrhea.isChecked) symptoms.add(Symptom(name = getString(R.string.diarrhea), date = date))
        if (pain_chip_hot_flush.isChecked) symptoms.add(Symptom(name = getString(R.string.hot_flush), date = date))
        if (pain_chip_nausea.isChecked) symptoms.add(Symptom(name = getString(R.string.nausea), date = date))
        if (pain_chip_tired.isChecked) symptoms.add(Symptom(name = getString(R.string.tired), date = date))
    }

    fun saveUserInputInDb() {
        createPain()
        createMood()
        createSymptomsList()
        viewModel.insertUserInput(pain, mood, symptoms)
    }

    fun configureCustomDialogAccordingToSelectedView(view: View) {
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
                        activityChoice = parent.getItemAtPosition(position).toString()
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

    fun openCustomDialog(view: View) {
        configureCustomDialogAccordingToSelectedView(view)
        MaterialAlertDialogBuilder(context).apply {
            setCancelable(false)
            setView(customView)
            setPositiveButton(getString(R.string.save)) { _, _ ->
                if (view.tag == getString(R.string.other)) activityChoice =
                    otherTextInput.text.toString().capitalize()
                val activity = UserActivities(
                    0, activityChoice, durationSlider.value.toInt(),
                    intensitySlider.value.toInt(), pain_slider.value.toInt(), date
                )
                viewModel.addActivities(activity)
            }
            setNegativeButton(getString(R.string.cancel)) { dialog, _ -> dialog.cancel() }
            show()
        }
    }

    fun createChipWhenUserActivityAdded() {
        viewModel.activitiesLiveData.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            pain_activity_chipgroup.removeAllViews()
            it.forEach { activity ->
                val chip = Chip(context)
                chip.apply {
                    text = activity.name
                    setTextColor(resources.getColor(R.color.colorOnPrimary))
                    setChipBackgroundColorResource(R.color.colorSecondary)
                    setOnClickListener {
                        Snackbar.make(pain_root, "${activity.name} during ${activity.duration} min with an intensity of ${activity.intensity}", Snackbar.LENGTH_SHORT)
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

    private fun setupOnClickListener() {
        pain_save_btn.setOnClickListener {
            saveUserInputInDb()
            navController.navigate(R.id.dashboardFragment)
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
    }
}
