package com.benlefevre.monendo.data.models

data class Treatment(
    var name: String = "",
    var duration: String = "",
    var dosage: String = "",
    var format: String = "",
    var morning: String = "",
    var noon: String = "",
    var afternoon: String = "",
    var evening: String = ""
)
