package com.benlefevre.monendo.treatment.models

data class Treatment(
    var name: String = "",
    var duration: String = "",
    var dosage: String = "",
    var format: String = "",
    var morning: String = "",
    var noon: String = "",
    var afternoon: String = "",
    var evening: String = "",
    var isTakenMorning : Boolean = false,
    var isTakenNoon : Boolean = false,
    var isTakenAfternoon : Boolean = false,
    var isTakenEvening : Boolean = false
)
