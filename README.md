# Mis Finanzas - Gestor de Gastos Personales
Aplicación móvil Android para el registro de ingresos y gastos personales.

## Descripción
Aplicación desarrollada en Android Studio con Java que permite gestionar finanzas personales mediante un CRUD completo. Los datos se almacenan localmente en SQLite y se visualizan en un RecyclerView con colores para diferenciar ingresos y gastos.

## Funcionalidades
- Agregar transacciones (Ingreso/Gasto)
- Buscar transacciones por ID
- Editar transacciones existentes
- Eliminar transacciones
- Visualizar todas las transacciones en lista
- Colores diferenciados: Ingresos (verde) / Gastos (rojo)

## Capturas de pantalla
### Lista de transacciones
<img width="448" height="679" alt="lista de transacciones" src="https://github.com/user-attachments/assets/dc319801-94bc-4c8d-8d2a-0a36ac05114d" />

### Formulario de registro
<img width="454" height="678" alt="formulario de registro" src="https://github.com/user-attachments/assets/278e54fa-cd48-448b-9282-9084ed39c022" />

## Estructura del proyecto

Transaccion.java: Modelo de datos

AdminSQLiteOpenHelper.java: Base de datos SQLite

AdaptadorTransaccion.java: Adaptador RecyclerView

MainActivity.java: Lógica CRUD principal

activity_main.xml: Pantalla principal

item_transaccion.xml: Diseño de cada fila

## Autor
Nicol Tatiana González Alba

## Fecha de entrega
3 de Julio de 2026


# MOMENTO 2 - Migración a Firebase

## Descripción
Migración de SQLite a Firebase Firestore para sincronización en tiempo real y acceso desde múltiples dispositivos.

## Tecnologías agregadas
- Firebase Firestore
- Firebase Authentication
- Material Design

## Nuevas funcionalidades
- Login y Registro de usuarios
- Sincronización en tiempo real
- Datos en la nube (Firestore)

## Capturas de pantalla
### Pantalla de Registro
<img width="454" height="674" alt="pantalla registro" src="https://github.com/user-attachments/assets/a4a5d6ad-379f-48b1-a075-a4003b14b917" />

### Pantalla de Login
<img width="451" height="640" alt="pantalla login" src="https://github.com/user-attachments/assets/52d6e606-30eb-4020-94b5-760638698c7e" />

### Lista de transacciones
<img width="450" height="648" alt="lista de transacciones - momento 2" src="https://github.com/user-attachments/assets/087744fe-f520-4c35-bbfc-1e5ab52d3388" />

### Transacciones - Firebase
<img width="1365" height="674" alt="transacciones - firestore" src="https://github.com/user-attachments/assets/6c09bdc6-25df-4cbd-a212-4c0bba8a003f" />

### Usuarios - Firebase
<img width="1361" height="677" alt="usuarios - firestore" src="https://github.com/user-attachments/assets/15e5665e-97a9-46ad-b915-0f8602c3986d" />

## Fecha de entrega
20 de Agosto de 2026
