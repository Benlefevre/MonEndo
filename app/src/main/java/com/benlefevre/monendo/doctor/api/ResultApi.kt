package com.benlefevre.monendo.doctor.api

import com.google.gson.annotations.SerializedName

data class ResultApi (

    @SerializedName("nhits") val nhits : Int,
    @SerializedName("parameters") val parameters : Parameters,
    @SerializedName("records") val records : List<Records>,
    @SerializedName("facet_groups") val facetGroups : List<FacetGroups>
)

data class Parameters (

    @SerializedName("dataset") val dataset : String,
    @SerializedName("timezone") val timezone : String,
    @SerializedName("rows") val rows : Int,
    @SerializedName("format") val format : String,
    @SerializedName("facet") val facet : List<String>
)

data class Records (

    @SerializedName("datasetid") val datasetid : String,
    @SerializedName("recordid") val recordid : String,
    @SerializedName("fields") val fields : Fields,
    @SerializedName("geometry") val geometry : Geometry,
    @SerializedName("record_timestamp") val recordTimestamp : String
)

data class Geometry (

    @SerializedName("type") val type : String,
    @SerializedName("coordinates") val coordinates : List<Double>
)

data class Fields (

    @SerializedName("nom") val nom : String,
    @SerializedName("nom_reg") val nomReg : String,
    @SerializedName("codes_ccam") val codesCcam : String,
    @SerializedName("code_postal") val codePostal : Int,
    @SerializedName("nature_exercice") val natureExercice : String,
    @SerializedName("nom_epci") val nomEpci : String,
    @SerializedName("types_actes") val typesActes : String,
    @SerializedName("sesam_vitale") val sesamVitale : String,
    @SerializedName("civilite") val civilite : String,
    @SerializedName("adresse") val adresse : String,
    @SerializedName("telephone") val telephone : String,
    @SerializedName("nom_dep") val nomDep : String,
    @SerializedName("libelle_profession") val libelleProfession : String,
    @SerializedName("code_insee") val codeInsee : Int,
    @SerializedName("convention") val convention : String,
    @SerializedName("nom_com") val nomCom : String,
    @SerializedName("ccam_phase") val ccamPhase : String,
    @SerializedName("code_profession") val codeProfession : Int,
    @SerializedName("coordonnees") val coordonnees : List<Double>,
    @SerializedName("actes") val actes : String,
    @SerializedName("dist") val dist : Double

)

data class Facets (

    @SerializedName("count") val count : Int,
    @SerializedName("path") val path : String,
    @SerializedName("state") val state : String,
    @SerializedName("name") val name : String
)

data class FacetGroups (

    @SerializedName("facets") val facets : List<Facets>,
    @SerializedName("name") val name : String
)