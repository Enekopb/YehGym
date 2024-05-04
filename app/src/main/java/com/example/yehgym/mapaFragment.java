package com.example.yehgym;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class mapaFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private FusedLocationProviderClient fusedLocationClient;
    private double miLatitud;
    private double miLongitud;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mapafragment, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Inicializar el cliente de ubicación fusionada
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        return view;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Agregar marcadores para los gimnasios cercanos en Bilbao
        LatLng gimnasio1 = new LatLng(43.26138103585541, -2.9267904155771944);
        mMap.addMarker(new MarkerOptions().position(gimnasio1).title("Gimnasio 1"));

        LatLng gimnasio2 = new LatLng(43.27579624505627, -2.918482500509598);
        mMap.addMarker(new MarkerOptions().position(gimnasio2).title("Gimnasio 2"));

        LatLng gimnasio3 = new LatLng(43.26399529693949, -2.9491794740333135);
        mMap.addMarker(new MarkerOptions().position(gimnasio3).title("Gimnasio 3"));

        LatLng gimnasio4 = new LatLng(43.24579173525677, -2.9367976311997173);
        mMap.addMarker(new MarkerOptions().position(gimnasio4).title("Gimnasio 4"));

        // Mover la cámara al primer marcador
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(gimnasio1, 15));

        // Solicitar permisos de ubicación si no están concedidos
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    miLatitud = location.getLatitude();
                    miLongitud = location.getLongitude();
                    LatLng miUbicacion = new LatLng(miLatitud, miLongitud);

                    // Personalizar el icono del marcador
                    mMap.addMarker(new MarkerOptions().position(miUbicacion).title("Mi ubicación").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));

                } else {
                    Log.d("maps", "Posicion desconocida");
                }
            }
        });

        // Habilitar la capa de mi ubicación
        if (ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso de ubicación concedido, habilitar la capa de mi ubicación
                if (ContextCompat.checkSelfPermission(requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            } else {
                // Permiso de ubicación denegado, mostrar mensaje al usuario
                Toast.makeText(requireContext(), "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
