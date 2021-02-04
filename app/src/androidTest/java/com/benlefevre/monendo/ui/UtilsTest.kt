package com.benlefevre.monendo.ui

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import androidx.test.espresso.matcher.ViewMatchers
import com.benlefevre.monendo.R
import com.benlefevre.monendo.dashboard.models.*
import com.benlefevre.monendo.utils.*
import com.google.android.material.slider.Slider
import org.hamcrest.Matcher
import java.util.*

fun insertDataInSharedPreferences(preferences: SharedPreferences) {
    preferences.edit().apply {
        putBoolean(CURRENT_CHECKED, true)
        putString(CHECKED_PILLS, checkedPills)
        putBoolean(NEED_CLEAR, true)
        putString(LAST_PILL_DATE, lastDate)
        putString(NEXT_PILL_DATE, nextDate)
        putString(TREATMENT, treatments)
        putString(PILL_HOUR_NOTIF, notifHour)
        putString(DURATION, "28")
        putString(CURRENT_MENS, lastDate)
        putString(NEXT_MENS, nextDate)
        putString(NUMBER_OF_PILLS, "28")
    }.apply()
}

fun removeDataInSharedPreferences(preferences: SharedPreferences) {
    preferences.edit().apply {
        remove(CURRENT_CHECKED)
        remove(CHECKED_PILLS)
        remove(NEED_CLEAR)
        remove(LAST_PILL_DATE)
        remove(NEXT_PILL_DATE)
        remove(TREATMENT)
        remove(PILL_HOUR_NOTIF)
        remove(DURATION)
        remove(CURRENT_MENS)
        remove(NEXT_MENS)
    }.apply()
}

fun getLastDate(preferences: SharedPreferences): String {
    val calendar = Calendar.getInstance()
    val lastDate = formatDateWithYear(calendar.apply {
        add(Calendar.DAY_OF_YEAR, -1)
    }.time)
    preferences.edit().putString(CURRENT_MENS, lastDate).apply()
    return lastDate
}

fun getVeryLastDate(preferences: SharedPreferences): String {
    val calendar = Calendar.getInstance()
    val lastDate = formatDateWithYear(calendar.apply {
        add(Calendar.DAY_OF_YEAR, -30)
    }.time)
    val nextDate = formatDateWithYear(calendar.apply {
        add(Calendar.DAY_OF_YEAR, 28)
    }.time)
    preferences.edit().apply {
        putString(CURRENT_MENS, lastDate)
        putString(NEXT_MENS, nextDate)
        putString(DURATION, "28")
    }.apply()
    return nextDate
}

fun getPainsWithRelations(context: Context): List<PainWithRelations> {
    val calendar = Calendar.getInstance()
    val date1 = calendar.apply {
        add(Calendar.DAY_OF_YEAR, -3)
    }.time
    val date2 = calendar.apply {
        add(Calendar.DAY_OF_YEAR, -7)
    }.time
    val date3 = calendar.apply {
        add(Calendar.DAY_OF_YEAR, -14)
    }.time
    val date4 = calendar.apply {
        add(Calendar.DAY_OF_YEAR, -180)
    }.time
    val date5 = calendar.apply {
        add(Calendar.DAY_OF_YEAR, -250)
    }.time
    val pain1 = Pain(date1, 8, context.getString(R.string.bladder))
    pain1.id = 1
    val pain2 = Pain(date2, 6, context.getString(R.string.back))
    pain2.id = 2
    val pain3 = Pain(date3, 4, context.getString(R.string.intestine))
    pain3.id = 3
    val pain4 = Pain(date4, 2, context.getString(R.string.head))
    pain4.id = 4
    val pain5 = Pain(date5, 0, context.getString(R.string.lower_abdomen))
    pain5.id = 5

    return listOf(
        PainWithRelations(
            pain1,
            getAllSymptoms(pain1, context),
            getAllActivities(pain1, context),
            listOf(
                Mood(1, context.getString(R.string.sad)),
            )
        ),
        PainWithRelations(
            pain2,
            getAllSymptoms(pain2, context),
            getAllActivities(pain2, context),
            listOf(
                Mood(2, context.getString(R.string.sick))
            )
        ),
        PainWithRelations(
            pain3,
            getAllSymptoms(pain3, context),
            getAllActivities(pain3, context),
            listOf(
                Mood(3, context.getString(R.string.irritated))
            )
        ),
        PainWithRelations(
            pain4,
            getAllSymptoms(pain4, context),
            getAllActivities(pain4, context),
            listOf(
                Mood(4, context.getString(R.string.happy))
            )
        ),
        PainWithRelations(
            pain5,
            getAllSymptoms(pain5, context),
            getAllActivities(pain5, context),
            listOf(
                Mood(5, context.getString(R.string.very_happy))
            )
        ),
    )
}


private fun getAllSymptoms(pain : Pain, context: Context): List<Symptom> {
    return listOf(
        Symptom(pain.id, context.getString(R.string.bleeding), pain.date),
        Symptom(pain.id, context.getString(R.string.bloating), pain.date),
        Symptom(pain.id, context.getString(R.string.cramps), pain.date),
        Symptom(pain.id, context.getString(R.string.chills), pain.date),
        Symptom(pain.id, context.getString(R.string.fever), pain.date),
        Symptom(pain.id, context.getString(R.string.hot_flush), pain.date),
        Symptom(pain.id, context.getString(R.string.constipation), pain.date),
        Symptom(pain.id, context.getString(R.string.diarrhea), pain.date),
        Symptom(pain.id, context.getString(R.string.nausea), pain.date),
        Symptom(pain.id, context.getString(R.string.burns), pain.date),
        Symptom(pain.id, context.getString(R.string.tired), pain.date),
    )
}

private fun getAllActivities(pain : Pain, context: Context): List<UserActivities> {
    return listOf(
        UserActivities(
            pain.id,
            "BasketBall",
            60,
            7,
            pain.intensity,
            pain.date
        ),
        UserActivities(
            pain.id,
            context.getString(R.string.relaxation),
            100,
            4,
            pain.intensity,
            pain.date
        ),
        UserActivities(
            pain.id,
            context.getString(R.string.stress),
            0,
            7,
            pain.intensity,
            pain.date
        ),
        UserActivities(
            pain.id,
            context.getString(R.string.sex),
            60,
            7,
            pain.intensity,
            pain.date
        ),
        UserActivities(
            pain.id,
            context.getString(R.string.other_activities, "Work"),
            60,
            7,
            pain.intensity,
            pain.date
        ),
    )
}

fun setValue(value: Float): ViewAction {
    return object : ViewAction {
        override fun getDescription(): String {
            return "Set Slider value to $value"
        }

        override fun getConstraints(): Matcher<View> {
            return ViewMatchers.isAssignableFrom(Slider::class.java)
        }

        override fun perform(uiController: UiController?, view: View) {
            val seekBar = view as Slider
            seekBar.value = value
        }
    }
}

const val treatmentName = "Doliprane"
const val treatmentMorning = "06:00"
const val treatmentNoon = "12:00"
const val treatmentDuration = "15/01/35"
const val checkedPills =
    "[{\"color\":-3341310,\"height\":137.6,\"isChecked\":false,\"isClickable\":true,\"multiHeight\":1,\"multiWidth\":2,\"radius\":40.0,\"shadowRectF\":{\"bottom\":170.93333,\"left\":187.11111,\"right\":253.77777,\"top\":104.26668},\"width\":110.22222,\"x\":220.44444,\"y\":137.6},{\"color\":-3341310,\"height\":137.6,\"isChecked\":false,\"isClickable\":true,\"multiHeight\":1,\"multiWidth\":3,\"radius\":40.0,\"shadowRectF\":{\"bottom\":170.93333,\"left\":297.3333,\"right\":364.0,\"top\":104.26668},\"width\":110.22222,\"x\":330.66666,\"y\":137.6},{\"color\":-3341310,\"height\":137.6,\"isChecked\":false,\"isClickable\":true,\"multiHeight\":1,\"multiWidth\":4,\"radius\":40.0,\"shadowRectF\":{\"bottom\":170.93333,\"left\":407.55554,\"right\":474.22223,\"top\":104.26668},\"width\":110.22222,\"x\":440.8889,\"y\":137.6},{\"color\":-3341310,\"height\":137.6,\"isChecked\":false,\"isClickable\":true,\"multiHeight\":1,\"multiWidth\":5,\"radius\":40.0,\"shadowRectF\":{\"bottom\":170.93333,\"left\":517.7778,\"right\":584.4444,\"top\":104.26668},\"width\":110.22222,\"x\":551.1111,\"y\":137.6},{\"color\":-3341310,\"height\":137.6,\"isChecked\":false,\"isClickable\":true,\"multiHeight\":1,\"multiWidth\":6,\"radius\":40.0,\"shadowRectF\":{\"bottom\":170.93333,\"left\":628.0,\"right\":694.6666,\"top\":104.26668},\"width\":110.22222,\"x\":661.3333,\"y\":137.6},{\"color\":-3341310,\"height\":137.6,\"isChecked\":false,\"isClickable\":true,\"multiHeight\":1,\"multiWidth\":7,\"radius\":40.0,\"shadowRectF\":{\"bottom\":170.93333,\"left\":738.2222,\"right\":804.88885,\"top\":104.26668},\"width\":110.22222,\"x\":771.55554,\"y\":137.6},{\"color\":-3341310,\"height\":137.6,\"isChecked\":false,\"isClickable\":true,\"multiHeight\":1,\"multiWidth\":8,\"radius\":40.0,\"shadowRectF\":{\"bottom\":170.93333,\"left\":848.44446,\"right\":915.1111,\"top\":104.26668},\"width\":110.22222,\"x\":881.7778,\"y\":137.6},{\"color\":-3341310,\"height\":137.6,\"isChecked\":false,\"isClickable\":true,\"multiHeight\":2,\"multiWidth\":2,\"radius\":40.0,\"shadowRectF\":{\"bottom\":308.53336,\"left\":187.11111,\"right\":253.77777,\"top\":241.86668},\"width\":110.22222,\"x\":220.44444,\"y\":275.2},{\"color\":-3341310,\"height\":137.6,\"isChecked\":false,\"isClickable\":true,\"multiHeight\":2,\"multiWidth\":3,\"radius\":40.0,\"shadowRectF\":{\"bottom\":308.53336,\"left\":297.3333,\"right\":364.0,\"top\":241.86668},\"width\":110.22222,\"x\":330.66666,\"y\":275.2},{\"color\":-16738680,\"height\":137.6,\"isChecked\":false,\"isClickable\":true,\"multiHeight\":2,\"multiWidth\":4,\"radius\":40.0,\"shadowRectF\":{\"bottom\":308.53336,\"left\":407.55554,\"right\":474.22223,\"top\":241.86668},\"width\":110.22222,\"x\":440.8889,\"y\":275.2},{\"color\":-1,\"height\":137.6,\"isChecked\":false,\"isClickable\":false,\"multiHeight\":2,\"multiWidth\":5,\"radius\":40.0,\"shadowRectF\":{\"bottom\":308.53336,\"left\":517.7778,\"right\":584.4444,\"top\":241.86668},\"width\":110.22222,\"x\":551.1111,\"y\":275.2},{\"color\":-1,\"height\":137.6,\"isChecked\":false,\"isClickable\":false,\"multiHeight\":2,\"multiWidth\":6,\"radius\":40.0,\"shadowRectF\":{\"bottom\":308.53336,\"left\":628.0,\"right\":694.6666,\"top\":241.86668},\"width\":110.22222,\"x\":661.3333,\"y\":275.2},{\"color\":-1,\"height\":137.6,\"isChecked\":false,\"isClickable\":false,\"multiHeight\":2,\"multiWidth\":7,\"radius\":40.0,\"shadowRectF\":{\"bottom\":308.53336,\"left\":738.2222,\"right\":804.88885,\"top\":241.86668},\"width\":110.22222,\"x\":771.55554,\"y\":275.2},{\"color\":-1,\"height\":137.6,\"isChecked\":false,\"isClickable\":false,\"multiHeight\":2,\"multiWidth\":8,\"radius\":40.0,\"shadowRectF\":{\"bottom\":308.53336,\"left\":848.44446,\"right\":915.1111,\"top\":241.86668},\"width\":110.22222,\"x\":881.7778,\"y\":275.2},{\"color\":-1,\"height\":137.6,\"isChecked\":false,\"isClickable\":false,\"multiHeight\":3,\"multiWidth\":2,\"radius\":40.0,\"shadowRectF\":{\"bottom\":446.13336,\"left\":187.11111,\"right\":253.77777,\"top\":379.46667},\"width\":110.22222,\"x\":220.44444,\"y\":412.80002},{\"color\":-1,\"height\":137.6,\"isChecked\":false,\"isClickable\":false,\"multiHeight\":3,\"multiWidth\":3,\"radius\":40.0,\"shadowRectF\":{\"bottom\":446.13336,\"left\":297.3333,\"right\":364.0,\"top\":379.46667},\"width\":110.22222,\"x\":330.66666,\"y\":412.80002},{\"color\":-1,\"height\":137.6,\"isChecked\":false,\"isClickable\":false,\"multiHeight\":3,\"multiWidth\":4,\"radius\":40.0,\"shadowRectF\":{\"bottom\":446.13336,\"left\":407.55554,\"right\":474.22223,\"top\":379.46667},\"width\":110.22222,\"x\":440.8889,\"y\":412.80002},{\"color\":-1,\"height\":137.6,\"isChecked\":false,\"isClickable\":false,\"multiHeight\":3,\"multiWidth\":5,\"radius\":40.0,\"shadowRectF\":{\"bottom\":446.13336,\"left\":517.7778,\"right\":584.4444,\"top\":379.46667},\"width\":110.22222,\"x\":551.1111,\"y\":412.80002},{\"color\":-1,\"height\":137.6,\"isChecked\":false,\"isClickable\":false,\"multiHeight\":3,\"multiWidth\":6,\"radius\":40.0,\"shadowRectF\":{\"bottom\":446.13336,\"left\":628.0,\"right\":694.6666,\"top\":379.46667},\"width\":110.22222,\"x\":661.3333,\"y\":412.80002},{\"color\":-1,\"height\":137.6,\"isChecked\":false,\"isClickable\":false,\"multiHeight\":3,\"multiWidth\":7,\"radius\":40.0,\"shadowRectF\":{\"bottom\":446.13336,\"left\":738.2222,\"right\":804.88885,\"top\":379.46667},\"width\":110.22222,\"x\":771.55554,\"y\":412.80002},{\"color\":-1,\"height\":137.6,\"isChecked\":false,\"isClickable\":false,\"multiHeight\":3,\"multiWidth\":8,\"radius\":40.0,\"shadowRectF\":{\"bottom\":446.13336,\"left\":848.44446,\"right\":915.1111,\"top\":379.46667},\"width\":110.22222,\"x\":881.7778,\"y\":412.80002},{\"color\":-1,\"height\":137.6,\"isChecked\":false,\"isClickable\":false,\"multiHeight\":4,\"multiWidth\":2,\"radius\":40.0,\"shadowRectF\":{\"bottom\":583.73334,\"left\":187.11111,\"right\":253.77777,\"top\":517.0667},\"width\":110.22222,\"x\":220.44444,\"y\":550.4},{\"color\":-1,\"height\":137.6,\"isChecked\":false,\"isClickable\":false,\"multiHeight\":4,\"multiWidth\":3,\"radius\":40.0,\"shadowRectF\":{\"bottom\":583.73334,\"left\":297.3333,\"right\":364.0,\"top\":517.0667},\"width\":110.22222,\"x\":330.66666,\"y\":550.4},{\"color\":-1,\"height\":137.6,\"isChecked\":false,\"isClickable\":false,\"multiHeight\":4,\"multiWidth\":4,\"radius\":40.0,\"shadowRectF\":{\"bottom\":583.73334,\"left\":407.55554,\"right\":474.22223,\"top\":517.0667},\"width\":110.22222,\"x\":440.8889,\"y\":550.4},{\"color\":-1,\"height\":137.6,\"isChecked\":false,\"isClickable\":false,\"multiHeight\":4,\"multiWidth\":5,\"radius\":40.0,\"shadowRectF\":{\"bottom\":583.73334,\"left\":517.7778,\"right\":584.4444,\"top\":517.0667},\"width\":110.22222,\"x\":551.1111,\"y\":550.4},{\"color\":-1,\"height\":137.6,\"isChecked\":false,\"isClickable\":false,\"multiHeight\":4,\"multiWidth\":6,\"radius\":40.0,\"shadowRectF\":{\"bottom\":583.73334,\"left\":628.0,\"right\":694.6666,\"top\":517.0667},\"width\":110.22222,\"x\":661.3333,\"y\":550.4},{\"color\":-1,\"height\":137.6,\"isChecked\":false,\"isClickable\":false,\"multiHeight\":4,\"multiWidth\":7,\"radius\":40.0,\"shadowRectF\":{\"bottom\":583.73334,\"left\":738.2222,\"right\":804.88885,\"top\":517.0667},\"width\":110.22222,\"x\":771.55554,\"y\":550.4},{\"color\":-1,\"height\":137.6,\"isChecked\":false,\"isClickable\":false,\"multiHeight\":4,\"multiWidth\":8,\"radius\":40.0,\"shadowRectF\":{\"bottom\":583.73334,\"left\":848.44446,\"right\":915.1111,\"top\":517.0667},\"width\":110.22222,\"x\":881.7778,\"y\":550.4}]"
const val treatments =
    "[{\"afternoon\":\"\",\"dosage\":3,\"duration\":\"$treatmentDuration\",\"evening\":\"\",\"format\":\"cachet\",\"isTakenAfternoon\":false,\"isTakenEvening\":false,\"isTakenMorning\":false,\"isTakenNoon\":false,\"morning\":\"$treatmentMorning\",\"name\":\"$treatmentName\",\"noon\":\"$treatmentNoon\"}]"
const val lastDate = "20/01/21"
const val nextDate = "08/02/21"
const val notifHour = "20:00"
