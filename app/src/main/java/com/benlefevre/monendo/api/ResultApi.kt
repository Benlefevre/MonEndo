package com.benlefevre.monendo.api

import com.google.gson.annotations.SerializedName

data class ResultApi (

    @SerializedName("nhits") val nhits : Int,
    @SerializedName("parameters") val parameters : Parameters,
    @SerializedName("records") val records : List<Records>,
    @SerializedName("facet_groups") val facet_groups : List<Facet_groups>
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
    @SerializedName("record_timestamp") val record_timestamp : String
)

data class Geometry (

    @SerializedName("type") val type : String,
    @SerializedName("coordinates") val coordinates : List<Double>
)

data class Fields (

    @SerializedName("nom") val nom : String,
    @SerializedName("nom_reg") val nom_reg : String,
    @SerializedName("codes_ccam") val codes_ccam : String,
    @SerializedName("code_postal") val code_postal : Int,
    @SerializedName("nature_exercice") val nature_exercice : String,
    @SerializedName("nom_epci") val nom_epci : String,
    @SerializedName("types_actes") val types_actes : String,
    @SerializedName("sesam_vitale") val sesam_vitale : String,
    @SerializedName("civilite") val civilite : String,
    @SerializedName("adresse") val adresse : String,
    @SerializedName("telephone") val telephone : String,
    @SerializedName("nom_dep") val nom_dep : String,
    @SerializedName("libelle_profession") val libelle_profession : String,
    @SerializedName("code_insee") val code_insee : Int,
    @SerializedName("convention") val convention : String,
    @SerializedName("nom_com") val nom_com : String,
    @SerializedName("ccam_phase") val ccam_phase : String,
    @SerializedName("code_profession") val code_profession : Int,
    @SerializedName("coordonnees") val coordonnees : List<Double>,
    @SerializedName("actes") val actes : String
)

data class Facets (

    @SerializedName("count") val count : Int,
    @SerializedName("path") val path : String,
    @SerializedName("state") val state : String,
    @SerializedName("name") val name : String
)

data class Facet_groups (

    @SerializedName("facets") val facets : List<Facets>,
    @SerializedName("name") val name : String
)