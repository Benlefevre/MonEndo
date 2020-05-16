package com.benlefevre.monendo.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.Observer
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.benlefevre.monendo.MainActivity
import com.benlefevre.monendo.R
import com.benlefevre.monendo.doctor.CommentaryAdapter
import com.benlefevre.monendo.doctor.models.Commentary
import com.benlefevre.monendo.notification.cancelPillAlarm
import com.benlefevre.monendo.notification.cancelTreatmentAlarm
import com.benlefevre.monendo.treatment.models.Treatment
import com.benlefevre.monendo.utils.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.custom_dialog_comment_list.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : PreferenceFragmentCompat(), CommentaryAdapter.CommentaryListAdapterListener {

    private val viewModel: SettingViewModel by viewModel()

    private lateinit var dataStore: PreferenceDataStore
    private lateinit var adapter : CommentaryAdapter
    private val comments = mutableListOf<Commentary>()
    private val commentIdList = mutableListOf<String>()
    private val selectedCommentList = mutableListOf<Commentary>()

    private val preferences : SharedPreferences by inject()

    private lateinit var preferencePill: ListPreference
    private lateinit var dataPreferences: Preference
    private lateinit var dataTreatment: Preference
    private lateinit var dataMenstruation: Preference
    private lateinit var dataTemperature: Preference
    private lateinit var dataDoctor: Preference
    private val gson = Gson()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings, rootKey)
        dataStore = DataStore(preferences)
        val preferenceManager = preferenceManager
        preferenceManager.preferenceDataStore = dataStore
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = CommentaryAdapter(comments,this)
        getCommentaries()
        initPreferences()
        configurePreferencesBehaviors()
    }

    private fun initPreferences() {
        preferencePill = findPreference(NUMBER_OF_PILLS)!!
        dataPreferences = findPreference("pain_data")!!
        dataTreatment = findPreference("reset_treatment_data")!!
        dataMenstruation = findPreference("mens_data")!!
        dataTemperature = findPreference("temp_data")!!
        dataDoctor = findPreference("comment_data")!!
    }

    private fun configurePreferencesBehaviors() {
        preferencePill.apply {
            value = preferences.getString(NUMBER_OF_PILLS, "28")
            setOnPreferenceChangeListener { _, _ ->
                preferences.edit().remove(CHECKED_PILLS).apply()
                true
            }
        }
        dataPreferences.apply {
            setOnPreferenceClickListener {
                openCustomDialog(getString(R.string.delete_pains), getString(R.string.sure_delete_pains), "data")
                true
            }
        }
        dataTreatment.apply {
            setOnPreferenceClickListener {
                openCustomDialog(getString(R.string.delete_treatments), getString(R.string.sure_delete_treatment), "treatment")
                true
            }
        }
        dataMenstruation.apply {
            setOnPreferenceClickListener {
                openCustomDialog(getString(R.string.delete_mens_data), getString(R.string.sure_delete_mens_data), "menstruation")
                true
            }
        }
        dataTemperature.apply {
            setOnPreferenceClickListener {
                openCustomDialog(getString(R.string.delete_temp_data), getString(R.string.sure_delete_temp), "temperature")
                true
            }
        }
        dataDoctor.apply {
            setOnPreferenceClickListener {
                openCommentaryDialog()
                true
            }
        }
    }

    private fun getCommentaries() {
        comments.clear()
        commentIdList.clear()
        gson.fromJson<List<String>>(
            preferences.getString(COMMENT_ID, ""),
            object : TypeToken<List<String>>() {}.type
        )?.let {
            commentIdList.addAll(it)
        }
        viewModel.getUserCommentaries(commentIdList)
        viewModel.commentLiveData.observe(viewLifecycleOwner, Observer {
            comments.clear()
            comments.addAll(it)
            adapter.notifyDataSetChanged()
        })
    }

    private fun removeMenstruationData() {
        preferences.edit().apply {
            remove(DURATION)
            remove(CURRENT_MENS)
            remove(NEXT_MENS)
        }.apply()
    }

    private fun removeTreatmentData() {
        val treatmentList = mutableListOf<Treatment>()
        gson.fromJson<List<Treatment>>(
            preferences.getString(TREATMENT, ""),
            object : TypeToken<List<Treatment>>() {}.type
        )?.let {
            treatmentList.addAll(it)
        }
        for((index,treatment) in treatmentList.withIndex()){
            cancelTreatmentAlarm(requireContext(), treatment, index)
        }
        cancelPillAlarm(requireContext())
//        val workManager = WorkManager.getInstance(requireContext())
//        treatmentList.forEach {
//            cancelTreatmentWorkWithWorker(requireContext(),it)
//        }
//        cancelPillWorkWithWorker(requireContext())
        preferences.edit().apply {
            remove(PILL_HOUR_NOTIF)
            remove(CHECKED_PILLS)
            remove(CURRENT_CHECKED)
            remove(NEED_CLEAR)
            remove(LAST_PILL_DATE)
            remove(NEXT_PILL_DATE)
            remove(TREATMENT)
        }.apply()
    }

    private fun openCustomDialog(title: String, message: String, origin: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton(getString(R.string.yes_sure)) { _, _ ->
                when (origin) {
                    "data" -> viewModel.deleteAllPains()
                    "treatment" -> removeTreatmentData()
                    "temperature" -> viewModel.deleteAllTemperatures()
                    "menstruation" -> removeMenstruationData()
                }
            }
            .show()
    }

    private fun openCommentaryDialog(){
        val customView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_dialog_comment_list,null)
        val recyclerView = customView.custom_dialog_comment_list_recycler_view
        val posBtn = customView.custom_dialog_comment_list_pos_btn
        val negBtn = customView.custom_dialog_comment_list_neg_btn
        recyclerView.apply {
            adapter = this@SettingsFragment.adapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(customView)
            .setTitle(getString(R.string.delete_comment))
            .setMessage(getString(R.string.click_comment_to_delete))
            .show()
        posBtn.setOnClickListener {
            removeCommentaries()
            dialog.cancel()
        }
        negBtn.setOnClickListener {
            dialog.cancel()
        }
    }

    private fun removeCommentaries() {
        val userId = MainActivity.user.id
        selectedCommentList.forEach {
            val id = "${it.doctorName}-$userId"
            commentIdList.remove(id)
            viewModel.deleteCommentary(id)
            comments.remove(it)
        }
        val idList = gson.toJson(commentIdList)
        preferences.edit().putString(COMMENT_ID,idList).apply()
    }

    override fun onCommentarySelected(commentary: Commentary) {
        if (selectedCommentList.contains(commentary)){
            selectedCommentList.remove(commentary)
        }else{
            selectedCommentList.add(commentary)
        }
    }

}

class DataStore(private val preferences: SharedPreferences) : PreferenceDataStore() {

    override fun putString(key: String, value: String?) {
        preferences.edit().putString(key, value).apply()
    }

    override fun getString(key: String, defValue: String?): String? {
        return preferences.getString(key, defValue)
    }
}
