package com.benlefevre.monendo.doctor.models

import com.benlefevre.monendo.utils.NO_PHOTO_URL
import java.util.*

data class Commentary (val doctorId : String = "", val rating : Double = -1.0, val userInput : String = "", val authorName : String = "",val authorPhotoUrl : String = NO_PHOTO_URL, val date : Date = Date())