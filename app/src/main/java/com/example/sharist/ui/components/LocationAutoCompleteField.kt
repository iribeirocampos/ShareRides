package com.example.sharist.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.sharist.data.model.Location
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import java.util.UUID


@Composable
fun LocationAutocompleteField(
    label: String,
    onLocationSelected: (Location) -> Unit,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current
    val placesClient = remember {
        if (Places.isInitialized()) Places.createClient(context) else null
    }

    var query by remember { mutableStateOf("") }
    var predictions by remember { mutableStateOf(listOf<AutocompletePrediction>()) }

    Column(modifier = modifier) {
        // INPUT
        OutlinedTextField(
            value = query,
            onValueChange = { text ->

                query = text

                if (text.isNotBlank()) {

                    val request =
                        FindAutocompletePredictionsRequest.builder()
                            .setQuery(text)
                            .build()

                    placesClient?.findAutocompletePredictions(request)
                        ?.addOnSuccessListener { response ->
                            predictions = response.autocompletePredictions
                        }
                        ?.addOnFailureListener { exception ->
                            Log.e("LOCATION", "❌ ERRO GOOGLE PLACES: ${exception.message}")
                        }
                } else {
                    predictions = emptyList()
                }
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // SUGGESTIONS
        predictions.forEach { prediction ->

            ListItem(
                headlineContent = {
                    Text(prediction.getPrimaryText(null).toString())
                },
                supportingContent = {
                    Text(prediction.getSecondaryText(null).toString())
                },
                modifier = Modifier.clickable {

                    val placeId = prediction.placeId

                    // STEP 2: FETCH PLACE DETAILS
                    val request =
                        FetchPlaceRequest.builder(
                            placeId,
                            listOf(
                                Place.Field.NAME,
                                Place.Field.ADDRESS,
                                Place.Field.LAT_LNG
                            )
                        ).build()

                    placesClient?.fetchPlace(request)
                        ?.addOnSuccessListener { response ->

                            val place = response.place

                            val latLng = place.latLng

                            if (latLng != null) {
                                val location = Location(
                                    id =   UUID.randomUUID().toString(),
                                    name = place.name ?: "",
                                    address = place.address ?: "",
                                    latitude = latLng.latitude,
                                    longitude = latLng.longitude,
                                    userId = "", // fill in ViewModel
                                )

                                query = place.name ?: ""
                                predictions = emptyList()

                                onLocationSelected(location)
                            }
                        }
                }
            )
        }
    }
}