package com.benlefevre.monendo.mappers

import com.benlefevre.monendo.doctor.api.*
import com.benlefevre.monendo.doctor.createDoctorsFromCpamApi
import org.junit.Assert.assertEquals
import org.junit.Test

class MapperDoctorTest{

    private val result = ResultApi(
        2,
        Parameters("", "", 0, "", listOf("")),
        listOf(
            Records(
                "",
                "1",
                Fields(
                    "Lefevre", "",
                    "", 92370, "", "", "Chirurgie",
                    "", "Mr", "15 avenue Charles de Gaulle 75000 Paris", "061789956230", "92",
                    "Gynécologue", 0, "1", "Paris",
                    "", 0, listOf(22.0, 23.0), "Chirurgie complète",600.0
                ),
                Geometry(
                    "",
                    listOf(22.0, 23.0)
                ),
                ""
            )
        ),
        listOf(
            Facet_groups(
                listOf(Facets(0, "", "", "")),
                ""
            )
        )
    )

    @Test
    fun createDoctorFromCpamApi_success_correctDataReturned(){
        val doctor =
            createDoctorsFromCpamApi(result)
        assertEquals("1",doctor[0].id)
        assertEquals("Lefevre",doctor[0].name)
        assertEquals("Mr",doctor[0].civilite)
        assertEquals("15 avenue Charles de Gaulle 75000 Paris",doctor[0].address)
        assertEquals("Gynécologue",doctor[0].spec)
        assertEquals("1",doctor[0].convention)
        assertEquals("061789956230",doctor[0].phone)
        assertEquals("Chirurgie complète",doctor[0].actes)
        assertEquals("Chirurgie",doctor[0].typesActes)
        assertEquals(listOf(22.0,23.0),doctor[0].coordonnees)
        assertEquals(0,doctor[0].nbComment)
        assertEquals(0.0,doctor[0].rating,0.0)
        assertEquals(600.0,doctor[0].dist,0.0)
    }
}

