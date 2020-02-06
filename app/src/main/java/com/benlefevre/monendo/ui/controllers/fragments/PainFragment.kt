package com.benlefevre.monendo.ui.controllers.fragments


import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.benlefevre.monendo.R
import com.benlefevre.monendo.data.models.Mood
import com.benlefevre.monendo.data.models.Pain
import com.benlefevre.monendo.data.models.Symptom
import com.benlefevre.monendo.injection.Injection
import com.benlefevre.monendo.ui.viewmodels.PainFragmentViewModel
import kotlinx.android.synthetic.main.fragment_pain.*
import java.util.*


class PainFragment : Fragment(), View.OnClickListener {

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
        val date = Date()
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



    private fun setupOnClickListener() {
        pain_save_btn.setOnClickListener(this)
        pain_card_sport.setOnClickListener(this)
        pain_card_sleep.setOnClickListener(this)
        pain_card_stress.setOnClickListener(this)
        pain_card_sex.setOnClickListener(this)
        pain_card_relaxation.setOnClickListener(this)
        pain_card_other.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.pain_save_btn -> {
                saveUserInputInDb()
                navController.navigate(R.id.dashboardFragment)
            }
        }
    }
}
