package com.example.gestorfinanzasnicol;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class UbicacionActivity extends AppCompatActivity {

    private TextView tvLatitud, tvLongitud, tvDireccion;
    private Button btnObtenerUbicacion;
    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String[]> solicitarPermisosUbicacionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicacion);

        tvLatitud = findViewById(R.id.tvLatitud);
        tvLongitud = findViewById(R.id.tvLongitud);
        tvDireccion = findViewById(R.id.tvDireccion);
        btnObtenerUbicacion = findViewById(R.id.btnObtenerUbicacion);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Configurar el lanzador de permisos
        solicitarPermisosUbicacionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                    Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                    if (fineLocationGranted != null && fineLocationGranted) {
                        Toast.makeText(this, "Permiso GPS Preciso concedido", Toast.LENGTH_SHORT).show();
                        obtenerUltimaUbicacion();
                    } else if (coarseLocationGranted != null && coarseLocationGranted) {
                        Toast.makeText(this, "Permiso Ubicación Aproximada concedido", Toast.LENGTH_SHORT).show();
                        obtenerUltimaUbicacion();
                    } else {
                        Toast.makeText(this, "Permisos de ubicación denegados", Toast.LENGTH_LONG).show();
                    }
                }
        );

        btnObtenerUbicacion.setOnClickListener(v -> verificarPermisosYObtenerUbicacion());
    }

    private void verificarPermisosYObtenerUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            obtenerUltimaUbicacion();
        } else {
            solicitarPermisosUbicacionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    private void obtenerUltimaUbicacion() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                tvLatitud.setText("Latitud: " + lat);
                tvLongitud.setText("Longitud: " + lon);

                convertirCoordenadasADireccion(lat, lon);
            } else {
                Toast.makeText(this, "No se encontró ubicación. Activa el GPS.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void convertirCoordenadasADireccion(double lat, double lon) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> direcciones = geocoder.getFromLocation(lat, lon, 1);
            if (direcciones != null && !direcciones.isEmpty()) {
                Address dir = direcciones.get(0);
                String direccionCompleta = dir.getAddressLine(0);
                tvDireccion.setText(direccionCompleta);
            } else {
                tvDireccion.setText("Dirección no disponible para estas coordenadas.");
            }
        } catch (IOException e) {
            tvDireccion.setText("Error al consultar el Geocoder: " + e.getMessage());
        }
    }
}