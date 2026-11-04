# Mis Finanzas - Aplicación Móvil Completa

Aplicación Android para el registro y gestión de finanzas personales con seguridad, multimedia, geolocalización y notificaciones en la nube.

## Características Principales (Momento 3)

### Seguridad y Roles (RBAC)
- Autenticación con Firebase Auth (Login/Registro).
- Persistencia de sesión (SharedPreferences).
- **Roles:** Administrador (acceso total) y Vendedor (permisos restringidos).

### Datos y Multimedia
- CRUD completo en Firestore en tiempo real.
- Subida de fotos de recibos a Firebase Storage.
- Visualización de fotos en la lista con Glide.

### Geolocalización y Mapas
- Captura de GPS real con `FusedLocationProviderClient`.
- Geocodificación inversa (coordenadas a dirección real).
- Mapa interactivo con Google Maps SDK y pines.

### Notificaciones
- Notificación local al registrar una transacción.
- Servicio configurado para notificaciones push con Firebase Cloud Messaging (FCM).

### Validaciones
- Validaciones en tiempo real con `TextWatcher` (monto > 0, concepto mínimo 3 caracteres).

## Credenciales de Prueba

| Rol | Correo | Contraseña |
| :--- | :--- | :--- |
| **Administrador** | `admin@cesde.edu.co` | `admin123` |
| **Vendedor** | `vendedor@cesde.edu.co` | `vendedor123` |

## Tecnologías Utilizadas
- **Android Studio** (Java)
- **Firebase:** Auth, Firestore, Storage, Messaging
- **Google Maps SDK** y **Play Services Location**
- **Glide** (Imágenes)
- **Material Design**

## Autor
**Nicol Tatiana Alba González**
