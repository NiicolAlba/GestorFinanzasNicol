package com.example.gestorfinanzasnicol;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapaActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap miMapa;
    private Button btnVolverMapa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);

        btnVolverMapa = findViewById(R.id.btnVolverMapa);
        btnVolverMapa.setOnClickListener(v -> finish());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapaFragment);

        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        miMapa = googleMap;

        miMapa.getUiSettings().setZoomControlsEnabled(true);

        // Coordenadas de Bogotá
        LatLng bogota = new LatLng(4.5981, -74.0760);

        miMapa.animateCamera(CameraUpdateFactory.newLatLngZoom(bogota, 14f));

        MarkerOptions pin = new MarkerOptions()
                .position(bogota)
                .title("Sede Principal")
                .snippet("Punto de registro de finanzas");
        miMapa.addMarker(pin);

        miMapa.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();
            return false;
        });
    }
}