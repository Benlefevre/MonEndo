package com.benlefevre.monendo.data.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.benlefevre.monendo.data.models.PainWithRelations
import java.util.*

@Dao
interface PainWithRelationsDao {

    @Query("SELECT * FROM Pain WHERE date BETWEEN :dateBegin AND :dateEnd")
    fun getAllPainsWithRelations(dateBegin : Date,dateEnd : Date) : LiveData<List<PainWithRelations>>
}