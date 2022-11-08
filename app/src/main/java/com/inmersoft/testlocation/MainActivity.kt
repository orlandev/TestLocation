package com.inmersoft.testlocation

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.inmersoft.testlocation.ui.theme.TestLocationTheme

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestLocationTheme {

                MapScreen()


            }
        }
    }
}

@Composable
fun MapScreen() {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 10f)
    }

    val markerState = MarkerState(position = LatLng(0.0, 0.0))
    val context = LocalContext.current
    var permissionIsGranted by remember {
        mutableStateOf(false)
    }


    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        permissionIsGranted = isGranted
        if (isGranted) {
            getMyLocation(context) {
                cameraPositionState.position
            }
        }
    }


    when (PackageManager.PERMISSION_GRANTED) {
        ContextCompat.checkSelfPermission(
            context, android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) -> {
            permissionIsGranted = true
            getMyLocation(context) {
                cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 10f)
                markerState.position = it
            }
        }
        else -> SideEffect {
            launcher.launch(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }

    if (permissionIsGranted) {
    GoogleMap(
        modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState
    ) {
        MarkerInfoWindow(
            state = markerState,
        ) { marker ->
            Column(
                Modifier
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp)
            ) {
                Text(marker.title ?: "Default Marker Title", color = Color.Red)
                Text(marker.snippet ?: "Default Marker Snippet", color = Color.Red)
            }
        }
        markerState.showInfoWindow()

    }
}
}

private fun getMyLocation(
    context: Context, onMyLocation: (LatLng) -> Unit
) {
    try {
        val fusedLocationclient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationclient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                val currentLocation = LatLng(location.latitude, location.longitude)
                onMyLocation(currentLocation)
            } ?: run {}

        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}