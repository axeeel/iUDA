package com.example.iuda




data class Direction(val address: Address,
                     val boundingbox: List<Double>,
                     val display_name: String,
                     val lat: Double,
                     val licence: String,
                     val lon: Double,
                     val osm_id: String,
                     val osm_type: String,
                     val place_id: String)
