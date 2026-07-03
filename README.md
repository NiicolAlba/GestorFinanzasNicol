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
