package com.example.gestorfinanzasnicol;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MiServicioFCM extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token){
        super.onNewToken(token);
        Log.d("FCM_TOKEN", "El token de este dispositivo es: " + token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage){
        super.onMessageReceived(remoteMessage);
        Log.d("FCM_MESSAGE", "Mensaje recibido desde: " + remoteMessage.getFrom());

        if(remoteMessage.getNotification() != null){
            String titulo = remoteMessage.getNotification().getTitle();
            String cuerpo = remoteMessage.getNotification().getBody();
            Log.d("FCM_MESSAGE", "Titulo: " + titulo + " | Contenido: " + cuerpo);
        }
    }
}