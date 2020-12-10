package com.benlefevre.monendo.doctor.api

import com.google.gson.annotations.SerializedName

data class Adresse (

	@SerializedName("type") val type : String,
	@SerializedName("version") val version : String,
	@SerializedName("features") val features : List<Features>,
	@SerializedName("attribution") val attribution : String,
	@SerializedName("licence") val licence : String,
	@SerializedName("query") val query : String,
	@SerializedName("filters") val filters : Filters,
	@SerializedName("limit") val limit : Int
)

data class Features (

	@SerializedName("type") val type : String,
	@SerializedName("geometry") val geometry : Geometry,
	@SerializedName("properties") val properties : Properties
)

data class Filters (

	@SerializedName("type") val type : String
)

data class Properties (

	@SerializedName("label") val label : String,
	@SerializedName("score") val score : Double,
	@SerializedName("id") val id : String,
	@SerializedName("type") val type : String,
	@SerializedName("name") val name : String,
	@SerializedName("postcode") val postcode : Int,
	@SerializedName("citycode") val citycode : String,
	@SerializedName("x") val x : Double,
	@SerializedName("y") val y : Double,
	@SerializedName("population") val population : Int,
	@SerializedName("city") val city : String,
	@SerializedName("context") val context : String,
	@SerializedName("importance") val importance : Double
)