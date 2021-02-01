package com.benlefevre.monendo.ui

import android.content.SharedPreferences
import com.benlefevre.monendo.utils.*

fun insertDataInSharedPreferences(preferences: SharedPreferences) {
    preferences.edit().apply {
        putBoolean(CURRENT_CHECKED, true)
        putString(CHECKED_PILLS, checkedPills)
        putBoolean(NEED_CLEAR, true)
        putString(LAST_PILL_DATE, lastDate)
        putString(NEXT_PILL_DATE, nextDate)
        putString(TREATMENT, treatments)
        putString(PILL_HOUR_NOTIF, notifHour)
        putInt(DURATION, 28)
        putString(CURRENT_MENS, lastDate)
        putString(NEXT_MENS, nextDate)
        putString(NUMBER_OF_PILLS,"28")
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
