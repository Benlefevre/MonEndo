package com.benlefevre.monendo.data.models

import androidx.room.Embedded
import androidx.room.Relation

data class PainWithRelations(@Embedded val pain: Pain,
                             @Relation(parentColumn = "id",entityColumn = "painId",entity = Symptom::class) val symptoms : List<Symptom>,
                             @Relation(parentColumn = "id",entityColumn = "painId",entity = UserActivities::class) val userActivities : List<UserActivities>,
                             @Relation(parentColumn = "id",entityColumn = "painId",entity = Mood::class) val moods : List<Mood>)