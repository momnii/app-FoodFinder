package com.example.foodfinder

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.location.Geocoder
import android.net.Uri
import org.osmdroid.config.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.foodfinder.R
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit


class MapFragment : Fragment(), FilterFragment.OnFilterChangedListener {

    private lateinit var mapView: MapView
    private lateinit var searchBar: EditText
    private lateinit var searchButton: Button
    private lateinit var restaurantCountTextView: TextView
    private lateinit var sharedPreferences: SharedPreferences

    private val PREFS_NAME = "FilterPrefs"
    private val VEGETARIAN_KEY = "vegetarian"
    private val VEGAN_KEY = "vegan"


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // SharedPreferences initialisieren
        sharedPreferences = requireActivity().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // MapView initialisieren
        mapView = view.findViewById(R.id.map)
        searchBar = view.findViewById(R.id.search_bar)
        searchButton = view.findViewById(R.id.search_button)
        restaurantCountTextView = view.findViewById(R.id.restaurant_count_textview)

        // OpenStreetMap-Initialisierung
        Configuration.getInstance().userAgentValue = requireActivity().packageName

        // Karten-Quelle und Startposition festlegen (z.B. Deutschland)
        mapView.setTileSource(org.osmdroid.tileprovider.tilesource.TileSourceFactory.MAPNIK)
        val startPoint = GeoPoint(51.1657, 10.4515) // Koordinaten für Deutschland
        mapView.controller.setZoom(8.0) // Setze den Zoomlevel
        mapView.controller.setCenter(startPoint)

        // Button-Klick-Listener für die Suchleiste
        searchButton.setOnClickListener {
            val city = searchBar.text.toString()
            if (city.isNotEmpty()) {
                geocodeAndCenterMap(city)
            } else {
                Toast.makeText(requireContext(), "Bitte eine Stadt eingeben", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun geocodeAndCenterMap(city: String) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocationName(city, 1)

            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                val geoPoint = GeoPoint(address.latitude, address.longitude)

                // Karte zentrieren
                mapView.controller.setCenter(geoPoint)
                mapView.controller.setZoom(14.0) // Zoomlevel erhöhen

                // Optional: Marker hinzufügen
                val marker = Marker(mapView)
                marker.position = geoPoint
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = city
                mapView.overlays.add(marker)
                mapView.invalidate() // Karte aktualisieren

                // Überprüfen, ob die Filter für vegetarisch oder vegan gesetzt sind
                val isVegetarian = sharedPreferences.getBoolean(VEGETARIAN_KEY, false)
                val isVegan = sharedPreferences.getBoolean(VEGAN_KEY, false)

                // Wenn eine der Checkboxes ausgewählt ist, updateMapFilters aufrufen
                if (isVegetarian || isVegan) {
                    updateMapFilters(isVegetarian, isVegan)
                } else {
                    // Falls keine Filter gesetzt sind, rufe fetchRestaurants direkt auf
                    fetchRestaurants(geoPoint)
                }

                mapView.invalidate() // Karte aktualisieren
            } else {
                Toast.makeText(requireContext(), "Stadt nicht gefunden", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Geocoding fehlgeschlagen: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    // Beispielmethode, die beim Ändern der Filter aufgerufen wird

    fun updateMapFilters(isVegetarian: Boolean, isVegan: Boolean) {
        val geoPoint = mapView.mapCenter as GeoPoint
        val baseQuery = "[out:json];"
        val restaurantQuery = StringBuilder("node[\"amenity\"=\"restaurant\"](around:5000,${geoPoint.latitude},${geoPoint.longitude});")

        // Filter basierend auf den Checkboxes hinzufügen
        if (isVegetarian) {
            restaurantQuery.append("node[\"diet:vegetarian\"=\"yes\"](around:5000,${geoPoint.latitude},${geoPoint.longitude});")
        }
        if (isVegan) {
            restaurantQuery.append("node[\"diet:vegan\"=\"yes\"](around:5000,${geoPoint.latitude},${geoPoint.longitude});")
        }

        val fullQuery = "${baseQuery}${restaurantQuery}out body;"
        executeRestaurantQuery(fullQuery)
    }

    private fun fetchRestaurants(geoPoint: GeoPoint) {
        val baseQuery = "[out:json];"
        val restaurantQuery = StringBuilder("node[\"amenity\"=\"restaurant\"](around:5000,${geoPoint.latitude},${geoPoint.longitude});")

        // Gesamte Abfrage
        val fullQuery = "${baseQuery}${restaurantQuery}out body;"

        // Führe die Abfrage aus
        executeRestaurantQuery(fullQuery)
    }

    private fun executeRestaurantQuery(query: String) {
        val url = "https://overpass-api.de/api/interpreter?data=${Uri.encode(query)}"

        Log.d("Restaurants", "Fetching URL: $url") // Log die URL für Debugging

        val client = OkHttpClient.Builder()
            .callTimeout(30, TimeUnit.SECONDS) // Timeout auf 30 Sekunden
            .build()

        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Restaurants", "Fehler beim Abrufen der Restaurants: ${e.message}")
                e.printStackTrace() // Fehlerbehandlung
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (response.isSuccessful) {
                        val jsonResponse = response.body?.string()
                        Log.d("Restaurants", "Response: $jsonResponse")
                        addRestaurantMarkers(jsonResponse)
                    } else {
                        Log.e("Restaurants", "Fehler: ${response.code} - ${response.message}")
                    }
                }
            }
        })
    }


    // Methode zum Aktualisieren der Anzeige der Anzahl der Restaurants
    private fun updateRestaurantCount(count: Int) {
        requireActivity().runOnUiThread {
            restaurantCountTextView.text = "Gefundene Restaurants: $count"
        }
    }

    private fun addRestaurantMarkers(jsonResponse: String?) {
        if (jsonResponse == null) {
            Log.e("Restaurants", "Keine Daten erhalten.")
            return
        }

        val jsonObject = JSONObject(jsonResponse)
        val elements = jsonObject.getJSONArray("elements")

        // Anzahl der gefundenen Restaurants zählen
        val restaurantCount = elements.length()

        // Anzeige der Anzahl der gefundenen Restaurants aktualisieren
        requireActivity().runOnUiThread {
            updateRestaurantCount(restaurantCount)
        }

        // Filter auf vegetarische und vegane Restaurants anwenden
        val isVegetarian = sharedPreferences.getBoolean(VEGETARIAN_KEY, false)
        val isVegan = sharedPreferences.getBoolean(VEGAN_KEY, false)

        for (i in 0 until elements.length()) {
            val element = elements.getJSONObject(i)
            val latitude = element.getDouble("lat")
            val longitude = element.getDouble("lon")

            // Tags abrufen
            val tags = element.getJSONObject("tags")
            val name = tags.optString("name", "Unbekanntes Restaurant")
            val isElementVegan = tags.optString("diet:vegan", "no") == "yes"
            val isElementVegetarian = tags.optString("diet:vegetarian", "no") == "yes"

            // Wenn keine Filter gesetzt sind, alle Restaurants anzeigen
            if (!isVegetarian && !isVegan) {
                // Füge alle Restaurants zur Karte hinzu
                addMarkerToMap(name, latitude, longitude)
                continue
            }

            // Überprüfen, ob das Restaurant den Filterkriterien entspricht
            if (isVegetarian && !isElementVegetarian) {
                continue // Überspringe nicht-vegetarische Restaurants
            }
            if (isVegan && !isElementVegan) {
                continue // Überspringe nicht-vegane Restaurants
            }

            // Logging der Marker-Daten
            Log.d("Restaurants", "Hinzufügen des Markers: $name bei ($latitude, $longitude), Vegan: $isElementVegan, Vegetarisch: $isElementVegetarian")

            // Marker hinzufügen
            addMarkerToMap(name, latitude, longitude)
        }

        // Karte aktualisieren
        mapView.invalidate()
    }

    // Hilfsmethode zum Hinzufügen eines Markers
    private fun addMarkerToMap(name: String, latitude: Double, longitude: Double) {
        val marker = Marker(mapView)
        marker.position = GeoPoint(latitude, longitude)
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = name

        // Klick-Listener für den Marker hinzufügen
        marker.setOnMarkerClickListener { _, _ ->
            showRestaurantInfoDialog(name, latitude, longitude)
            true
        }

        // Marker zur Karte hinzufügen
        mapView.overlays.add(marker)
    }




    private fun showRestaurantInfoDialog(name: String, latitude: Double, longitude: Double) {
        // Erstellen eines Dialogs mit Restaurant-Informationen und einem Button
        val builder = AlertDialog.Builder(context)
        builder.setTitle(name)
        builder.setMessage("Möchten Sie dieses Restaurant in Google Maps öffnen?")

        builder.setPositiveButton("Öffnen in Google Maps") { _, _ ->
            // URL zum Öffnen von Google Maps erstellen
            val uri = "geo:$latitude,$longitude?q=$latitude,$longitude($name)"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            requireContext().startActivity(intent) // Google Maps öffnen
        }

        builder.setNegativeButton("Abbrechen") { dialog, _ ->
            dialog.dismiss() // Dialog schließen
        }

        builder.create().show() // Dialog anzeigen
    }

    override fun onFilterChanged(isVegetarian: Boolean, isVegan: Boolean) {
        sharedPreferences.edit()
            .putBoolean(VEGETARIAN_KEY, isVegetarian)
            .putBoolean(VEGAN_KEY, isVegan)
            .apply()

        val geoPoint = mapView.mapCenter as GeoPoint
        updateMapFilters(isVegetarian, isVegan)
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }
}