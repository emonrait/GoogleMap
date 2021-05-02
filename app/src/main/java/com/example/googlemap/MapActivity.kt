package com.example.googlemap

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.lang.Double
import java.lang.Float

class MapActivity : FragmentActivity(), OnMapReadyCallback {
    var googleMap: GoogleMap? = null
    var sharedPreferences: SharedPreferences? = null
    var locationCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        // Getting Google Play availability status


        // Getting Google Play availability status
        val status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(baseContext)

        // Showing status

        // Showing status
        if (status != ConnectionResult.SUCCESS) { // Google Play Services are not available
            val requestCode = 10
            val dialog = GooglePlayServicesUtil.getErrorDialog(status, this, requestCode)
            dialog.show()
        } else { // Google Play Services are available

            // Getting reference to the SupportMapFragment of activity_main.xml
            val fm = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?

            // Getting GoogleMap object from the fragment
            //googleMap = fm.getMap()
            fm?.getMapAsync(this)

            // Enabling MyLocation Layer of Google Map
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
//            googleMap!!.isMyLocationEnabled = true

            // Opening the sharedPreferences object
            sharedPreferences = getSharedPreferences("location", 0)

            // Getting number of locations already stored
            locationCount = sharedPreferences!!.getInt("locationCount", 0)


            // Getting stored zoom level if exists else return 0
            val zoom = sharedPreferences!!.getString("zoom", "0")

            // If locations are already saved
            if (locationCount != 0) {
                var lat: String? = ""
                var lng: String? = ""

                // Iterating through all the locations stored
                for (i in 0 until locationCount) {

                    // Getting the latitude of the i-th location
                    lat = sharedPreferences!!.getString("lat$i", "0")

                    // Getting the longitude of the i-th location
                    lng = sharedPreferences!!.getString("lng$i", "0")

                    // Drawing marker on the map
                    drawMarker(LatLng(lat!!.toDouble(), lng!!.toDouble()))
                }

                // Moving CameraPosition to last clicked position
                googleMap!!.moveCamera(
                    CameraUpdateFactory.newLatLng(
                        LatLng(
                            lat!!.toDouble(),
                            lng!!.toDouble()
                        )
                    )
                )

                // Setting the zoom level in the map on last position  is clicked
                googleMap!!.animateCamera(CameraUpdateFactory.zoomTo(zoom!!.toFloat()))
            }
        }

        googleMap!!.setOnMapClickListener { point ->
            locationCount++

            // Drawing marker on the map
            drawMarker(point)
            /** Opening the editor object to write data to sharedPreferences  */
            /** Opening the editor object to write data to sharedPreferences  */
            val editor = sharedPreferences!!.edit()

            // Storing the latitude for the i-th location
            editor.putString(
                "lat" + Integer.toString(locationCount - 1),
                Double.toString(point.latitude)
            )

            // Storing the longitude for the i-th location
            editor.putString(
                "lng" + Integer.toString(locationCount - 1),
                Double.toString(point.longitude)
            )

            // Storing the count of locations or marker count
            editor.putInt("locationCount", locationCount)
            /** Storing the zoom level to the shared preferences  */
            /** Storing the zoom level to the shared preferences  */
            editor.putString(
                "zoom", Float.toString(
                    googleMap!!.cameraPosition.zoom
                )
            )
            /** Saving the values stored in the shared preferences  */
            /** Saving the values stored in the shared preferences  */
            editor.commit()
            Toast.makeText(baseContext, "Marker is added to the Map", Toast.LENGTH_SHORT).show()
        }


        googleMap!!.setOnMapLongClickListener { // Removing the marker and circle from the Google Map
            googleMap!!.clear()

            // Opening the editor object to delete data from sharedPreferences
            val editor = sharedPreferences!!.edit()

            // Clearing the editor
            editor.clear()

            // Committing the changes
            editor.commit()

            // Setting locationCount to zero
            locationCount = 0
        }


    }

    private fun drawMarker(point: LatLng) {
        // Creating an instance of MarkerOptions
        val markerOptions = MarkerOptions()

        // Setting latitude and longitude for the marker
        markerOptions.position(point)

        // Adding marker on the Google Map
        googleMap?.addMarker(markerOptions)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        this.googleMap = googleMap
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        googleMap?.isMyLocationEnabled
    }
}

