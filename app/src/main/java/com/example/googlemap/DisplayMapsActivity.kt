package com.example.googlemap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.android.synthetic.main.activity_display_maps.*
import java.text.DecimalFormat
import java.util.*
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request


class DisplayMapsActivity : AppCompatActivity(),
    OnMapReadyCallback,
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMarkerDragListener {
    var log = 0.0
    var lat = 0.0
    var loc = ""
    private lateinit var globalVariable: GlobalVariable
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    var end_lat = 0.0
    var end_long = 0.0
    var latitude = 0.0
    var longitude = 0.0

    private lateinit var mMap: GoogleMap

    private var cameraPosition: CameraPosition? = null

    // The entry point to the Places API.
    private lateinit var placesClient: PlacesClient


    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private val defaultLocation = LatLng(-33.8523341, 151.2106085)
    private var locationPermissionGranted = false

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private var lastKnownLocation: Location? = null
    private var likelyPlaceNames: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAddresses: Array<String?> = arrayOfNulls(0)
    private var likelyPlaceAttributions: Array<List<*>?> = arrayOfNulls(0)
    private var likelyPlaceLatLngs: Array<LatLng?> = arrayOfNulls(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_maps)
        globalVariable = this.applicationContext as GlobalVariable

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Construct a PlacesClient
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)


        log = intent.getStringExtra("log")!!.toDouble()
        lat = intent.getStringExtra("lat")!!.toDouble()
        loc = intent.getStringExtra("loc")!!.toString()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        mMap?.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val sydney = LatLng(-34.0, 151.0)
        val boundBuilder = LatLngBounds.Builder()

/*
        for (place in globalVariable.atmList!!) {
            val latLng = LatLng(place.atmLogitude.toDouble(), place.atmLatitued.toDouble())
            boundBuilder.include(latLng)
            mMap.addMarker(MarkerOptions().position(latLng).title(place.atmLocation))
        }
        // mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 1000, 1000, 0))
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 1000, 1000, 0))*/

        // Prompt the user for permission.
        getLocationPermission()

        // Turn on the My Location layer and the related control on the map.
        // updateLocationUI()

        // Get the current location of the device and set the position of the map.
        // getDeviceLocation()

        //  showCurrentPlace()

        /*  CalculationByDistance(
              sydney,
              LatLng(lat, log)
          )*/

/*
        val sydney = LatLng(log, lat)

        mMap.addMarker(MarkerOptions().position(sydney).title(loc))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/

        mMap.setOnMarkerClickListener(this)
        mMap.setOnMarkerDragListener(this)


    }

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(
                this.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
            updateLocationUI()
            getDeviceLocation()

            //onClick()
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        }
    }

    companion object {
        private val TAG = DisplayMapsActivity::class.java.simpleName
        private const val DEFAULT_ZOOM = 15
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1

        // Keys for storing activity state.
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"

        // Used for selecting the current place.
        private const val M_MAX_ENTRIES = 5
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }

    private fun updateLocationUI() {
        if (mMap == null) {
            return
        }
        try {
            if (locationPermissionGranted) {
                mMap?.isMyLocationEnabled = true
                mMap?.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                mMap?.isMyLocationEnabled = false
                mMap?.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {

                val geocoder: Geocoder
                var addresses: List<Address>
                geocoder = Geocoder(this, Locale.getDefault())


                val locationResult = fusedLocationClient.lastLocation
                locationResult.addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            latitude = lastKnownLocation!!.latitude
                            longitude = lastKnownLocation!!.longitude
                            addresses = geocoder.getFromLocation(
                                lastKnownLocation!!.latitude,
                                lastKnownLocation!!.longitude,
                                1
                            ) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                            getAllList()

                            val address: String =
                                addresses[0].getAddressLine(0) // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                            val city: String = addresses[0].getLocality()
                            val state: String = addresses[0].getAdminArea()
                            val country: String = addresses[0].getCountryName()
                            val postalCode: String = addresses[0].getPostalCode()
                            val knownName: String = addresses[0].getFeatureName()

                            val options = MarkerOptions().position(
                                LatLng(
                                    lastKnownLocation!!.latitude,
                                    lastKnownLocation!!.longitude
                                )
                            ).title(address)

                            mMap?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    LatLng(
                                        lastKnownLocation!!.latitude,
                                        lastKnownLocation!!.longitude
                                    ), DEFAULT_ZOOM.toFloat()
                                )
                            )
                            mMap?.addMarker(options)

                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        mMap?.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(defaultLocation, DEFAULT_ZOOM.toFloat())
                        )
                        mMap?.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    @SuppressLint("MissingPermission")
    private fun showCurrentPlace() {
        if (map == null) {
            return
        }
        if (locationPermissionGranted) {
            // Use fields to define the data types to return.
            val placeFields = listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)

            // Use the builder to create a FindCurrentPlaceRequest.
            val request = FindCurrentPlaceRequest.newInstance(placeFields)

            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            val placeResult = placesClient.findCurrentPlace(request)
            placeResult.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val likelyPlaces = task.result

                    // Set the count, handling cases where less than 5 entries are returned.
                    val count =
                        if (likelyPlaces != null && likelyPlaces.placeLikelihoods.size < M_MAX_ENTRIES) {
                            likelyPlaces.placeLikelihoods.size
                        } else {
                            M_MAX_ENTRIES
                        }
                    var i = 0
                    likelyPlaceNames = arrayOfNulls(count)
                    likelyPlaceAddresses = arrayOfNulls(count)
                    likelyPlaceAttributions = arrayOfNulls<List<*>?>(count)
                    likelyPlaceLatLngs = arrayOfNulls(count)
                    for (placeLikelihood in likelyPlaces?.placeLikelihoods ?: emptyList()) {
                        // Build a list of likely places to show the user.
                        likelyPlaceNames[i] = placeLikelihood.place.name
                        likelyPlaceAddresses[i] = placeLikelihood.place.address
                        likelyPlaceAttributions[i] = placeLikelihood.place.attributions
                        likelyPlaceLatLngs[i] = placeLikelihood.place.latLng
                        i++
                        if (i > count - 1) {
                            break
                        }
                    }

                    // Show a dialog offering the user the list of likely places, and add a
                    // marker at the selected place.
                    //openPlacesDialog()
                } else {
                    Log.e(TAG, "Exception: %s", task.exception)
                }
            }
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.")

            // Add a default marker, because the user hasn't selected a place.
            mMap?.addMarker(
                MarkerOptions()
                    .title(getString(R.string.default_info_title))
                    .position(defaultLocation)
                    .snippet(getString(R.string.default_info_snippet))
            )

            // Prompt the user for permission.
            getLocationPermission()
        }
    }

    fun CalculationByDistance(StartP: LatLng, EndP: LatLng): Double {
        val Radius = 6371 // radius of earth in Km
        val lat1 = StartP.latitude
        val lat2 = EndP.latitude
        val lon1 = StartP.longitude
        val lon2 = EndP.longitude
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = (Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + (Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2)))
        val c = 2 * Math.asin(Math.sqrt(a))
        val valueResult = Radius * c
        val km = valueResult / 1000
        val newFormat = DecimalFormat("####")
        val kmInDec: Int = Integer.valueOf(newFormat.format(km))
        val meter = valueResult % 1000
        val meterInDec: Int = Integer.valueOf(newFormat.format(meter))
        Log.i(
            "Radius Value", "" + valueResult + "   KM  " + kmInDec
                    + " Meter   " + meterInDec
        )
        return Radius * c
    }

    private fun getAllList() {
        val boundBuilder = LatLngBounds.Builder()


        for (place in globalVariable.atmList!!) {
            val latLng = LatLng(place.atmLogitude.toDouble(), place.atmLatitued.toDouble())
            val currentlatlang = LatLng(latitude, longitude)
            val results = FloatArray(10)
            Location.distanceBetween(
                latitude,
                longitude,
                place.atmLogitude.toDouble(),
                place.atmLatitued.toDouble(),
                results
            )
            Log.e(
                "OK", "Cur Lat: " + latitude.toString() +
                        "Cur Long" + longitude.toString() +
                        "Lat" + place.atmLogitude +
                        "Long " + place.atmLatitued
            )

            val Radius = 6371 // radius of earth in Km
            val lat1 = latitude
            val lat2 = place.atmLogitude.toDouble()
            val lon1 = longitude
            val lon2 = place.atmLogitude.toDouble()
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = (Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + (Math.cos(Math.toRadians(lat1))
                    * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                    * Math.sin(dLon / 2)))
            val c = 2 * Math.asin(Math.sqrt(a))
            val valueResult = Radius * c
            val km = valueResult / 1000
            val newFormat = DecimalFormat("####.000")
            val kmInDec = newFormat.format(km)
            /* val meter = valueResult % 1000
             val meterInDec: Int = Integer.valueOf(newFormat.format(meter))*/


            val kmiformat = DecimalFormat("###.000")

            boundBuilder.include(latLng)
            mMap.addMarker(
                MarkerOptions().position(latLng).title(place.atmLocation)
                    .snippet(
                        kmInDec + " KM"
                    )
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.location))
            )

            Log.d("GoogleMap", "before URL")
            val URL = getDirectionURL(currentlatlang, latLng)
            Log.d("GoogleMap", "URL : $URL")
            GetDirection(URL).execute()


        }
        // mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 1000, 1000, 0))
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundBuilder.build(), 1000, 1000, 0))
    }

    override fun onMarkerClick(marker: Marker?): Boolean {
        marker?.isDraggable = true
        return false
    }

    override fun onMarkerDragStart(marker: Marker?) {
        TODO("Not yet implemented")
    }

    override fun onMarkerDrag(marker: Marker?) {
        TODO("Not yet implemented")
    }

    override fun onMarkerDragEnd(marker: Marker?) {
        end_lat = marker!!.position.latitude
        end_long = marker!!.position.longitude
    }

    private fun onClick() {

        val results = FloatArray(10)
        Location.distanceBetween(latitude, longitude, end_long, end_long, results)
        val options = MarkerOptions()
            .position(LatLng(end_lat, end_long))
            .title("DESTINATION")
            .snippet(results[0].toString())
        mMap.addMarker(options)


    }

    fun getDirectionURL(origin: LatLng, dest: LatLng): String {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving"
    }

    private inner class GetDirection(val url: String) :
        AsyncTask<Void, Void, List<List<LatLng>>>() {
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body()!!.string()
            Log.d("GoogleMap", " data : $data")
            val result = ArrayList<List<LatLng>>()
            try {
                val respObj = Gson().fromJson(data, GoogleMapDTO::class.java)

                val path = ArrayList<LatLng>()

                for (i in 0..(respObj.routes[0].legs[0].steps.size - 1)) {
//                    val startLatLng = LatLng(respObj.routes[0].legs[0].steps[i].start_location.lat.toDouble()
//                            ,respObj.routes[0].legs[0].steps[i].start_location.lng.toDouble())
//                    path.add(startLatLng)
//                    val endLatLng = LatLng(respObj.routes[0].legs[0].steps[i].end_location.lat.toDouble()
//                            ,respObj.routes[0].legs[0].steps[i].end_location.lng.toDouble())
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices) {
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.BLUE)
                lineoption.geodesic(true)
            }
            mMap.addPolyline(lineoption)
        }
    }

    public fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5), (lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }
}
