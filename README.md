# CapsulaMongo

Este repositorio contiene el proyecto para la cápsula de MongoDB.

## Requerimientos

1. **Base de datos MongoDB:**
   - Crear una base de datos llamada "capsula" en MongoDB.
   - La ruta de conexión debe ser: `mongodb://localhost:27017/capsula`

2. **Redis (opcional):**
   - Es posible que se requiera iniciar Redis manualmente, aunque no debería ser necesario.
   - Para iniciar Redis, ejecuta en la consola: `redis-server`

3. **Postman:**
   - Se puede utilizar Postman para probar los endpoints.
   - La colección de endpoints se adjunta en este repositorio.

4. **Frontend:**
   - El frontend de la aplicación está disponible en la ruta: `http://localhost:8080/index.html`

## Dockerización

- No se pudo dockerizar el proyecto debido a problemas en la computadora.

## Documentos de la base de datos

- Se deben crear los siguientes documentos en la base de datos "capsula":
  - incidentes
  - rutas
  - tipos_incidente