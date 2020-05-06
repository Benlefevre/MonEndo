package com.benlefevre.monendo.dashboard.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.benlefevre.monendo.dashboard.models.PainWithRelations
import java.util.*

@Dao
interface PainWithRelationsDao {

    @Transaction
    @Query("SELECT * FROM Pain WHERE date BETWEEN :dateBegin AND :dateEnd ORDER BY date")
    fun getAllPainsWithRelations(dateBegin : Date,dateEnd : Date) : LiveData<List<PainWithRelations>>
}