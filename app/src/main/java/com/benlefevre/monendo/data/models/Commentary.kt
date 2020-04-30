package com.benlefevre.monendo.data.models

import java.util.*

data class Commentary (val doctorId : String = "", val rating : Double = -1.0, val userInput : String = "", val authorName : String = "", val date : Date? = null)