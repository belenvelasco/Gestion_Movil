# Gestión Móvil de Oportunidades y Activaciones Corporativas

**Caso 007D** · DSY1105 Desarrollo de Aplicaciones Móviles · Evaluación Parcial 3

Aplicación Android que permite identificar, registrar, priorizar y gestionar oportunidades de activación corporativa (ferias, eventos y actividades), centralizando la información de eventos, organizaciones, contactos, gestiones y resultados.

> Todos los datos de la aplicación (usuarios, contactos, organizaciones, eventos) son **ficticios**.

---

## 1. Nombre del proyecto

**Gestión Móvil de Oportunidades y Activaciones Corporativas** (`Gestion_Movil`)

## 2. Integrantes

| Integrante | Usuario GitHub  |
|---|---|
| Belén Velasco | `belenvelasco` |
| Sebastián Neira | `sebaneirag-creator` |

- **Repositorio de la app:** https://github.com/belenvelasco/Gestion_Movil
- **Repositorio del microservicio:** https://github.com/sebaneirag-creator/GestionMovilBackend

## 3. Funcionalidades

### Perfiles y acceso
- Inicio de sesión con usuario y contraseña ficticios, con validación de campos.
- Dos perfiles, con menú y permisos distintos:
  - **Ejecutivo comercial:** registra y edita oportunidades, contactos y gestiones.
  - **Supervisor comercial:** consulta, filtra, ajusta prioridad y reasigna responsable.

### Ejecutivo comercial
- Registro de oportunidad: evento o actividad, fecha, comuna, organización, contacto, público esperado y prioridad (alta, media, baja).
- Ubicación del evento por GPS y lectura de código QR para precargar datos.
- Registro de contactos y de gestiones (llamada, reunión, propuesta), con **próxima acción** definida.
- Edición de la oportunidad con historial de cambios automático.

### Supervisor comercial
- Lista de oportunidades con búsqueda y filtro por prioridad, con detalle e historial.
- Sección de supervisión en el detalle: **ajustar prioridad** y **reasignar responsable** (queda registrado en el historial).
- Panel con totales, distribución por estado, prioridad y responsable, próximos eventos y acciones pendientes.

### Gestión de activaciones (módulo disponible para ambos perfiles)
- Confirmar participación y registrar condiciones (espacio, fecha, costo, requisitos).
- Asignar responsable.
- Adjuntar evidencias: fotos, videos (cámara con CameraX) y documentos.
- Registrar resultado de la activación, observaciones e impacto, y cerrar la oportunidad.

### Estados de la oportunidad
`Evento identificado` → `Contacto pendiente` → `Contacto realizado` → `Propuesta enviada` → `En espera de respuesta` → `Participación confirmada` / `Oportunidad descartada`

### Integraciones
- **Microservicio propio (Spring Boot):** consulta de oportunidades desde el servidor.
- **API externa pública (Retrofit):** catálogo de eventos externos, mostrado en la app sin interferir con los datos locales ni con el microservicio.

### Persistencia local
Room / SQLite con las entidades: oportunidades, organizaciones, contactos, seguimientos (gestiones), activaciones, resultados, evidencias e historial de cambios.

## 4. Endpoints utilizados

### 4.1 Microservicio propio (Spring Boot)

El backend se limita a **un microservicio con consulta `GET`** para comprobar la integración.

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `/api/v1/oportunidades` | Lista las oportunidades registradas en el servidor |

- URL base en el emulador: `http://10.0.2.2:8080/`
- Respuesta (JSON, lista de objetos):

```json
[
  {
    "id": 1,
    "nombre": "Feria Expo Pyme",
    "tipo": "Feria empresarial",
    "descripcion": "Descripción de la oportunidad",
    "fuente": "Contacto directo",
    "prioridad": "Alta",
    "potencial": "Alto",
    "estado": "Evento identificado"
  }
]
```

- Pantalla de la app: **Menú → Oportunidades del servidor**, con indicador de carga, mensaje de error y botón de reintento.
- Base de datos del microservicio: COMPLETAR (por ejemplo H2 / MySQL / PostgreSQL).

### 4.2 API externa (JSONPlaceholder, servicio público de prueba)

| Método | Endpoint | Descripción |
|---|---|---|
| `GET` | `https://jsonplaceholder.typicode.com/posts?_limit=20` | Registros públicos usados como eventos |
| `GET` | `https://jsonplaceholder.typicode.com/users` | Usuarios usados como organizaciones y comunas |

- Pantalla de la app: **Menú → Catálogo de eventos**.
- Es una integración de prueba con datos ficticios y públicos; no es una integración productiva.

## 5. Pasos para ejecutar

### Requisitos
- Android Studio con **AGP 9.4.1**, Kotlin 2.2.10 y JDK 17 o superior (el que trae Android Studio sirve).
- Android SDK con `compileSdk 37`. La app corre en Android 7.0 (API 24) o superior.
- Emulador o dispositivo físico con cámara/GPS opcionales (las funciones de cámara, QR y ubicación piden permisos al usarlas).

### Pasos
1. **Clonar** el repositorio y abrirlo en Android Studio, esperar el *Sync* de Gradle.
2. **Levantar el microservicio** (repositorio del backend)
3. **Ejecutar la app** en un emulador (`Run ▶`). El emulador accede al PC mediante `10.0.2.2`.
4. **Iniciar sesión** con una cuenta ficticia:

   | Perfil | Usuario | Contraseña |
   |---|---|---|
   | Ejecutivo comercial | `ejecutivo` | `1234` |
   | Supervisor comercial | `supervisor` | `1234` |

> **Dispositivo físico:** cambiar `BASE_URL` en `data/remote/BackendInstance.kt` por la IP del PC en la red local (por ejemplo `http://192.168.1.50:8080/`) y agregar esa IP en `res/xml/network_security_config.xml`. Teléfono y PC deben estar en la misma red.

### Pruebas
```bash
# Pruebas unitarias (JVM)
./gradlew testDebugUnitTest

# Pruebas instrumentadas de interfaz (requieren emulador o dispositivo)
./gradlew connectedDebugAndroidTest
```

## Arquitectura y tecnologías

Patrón **MVVM** con repositorios y flujos reactivos (`StateFlow`).

```
ui/            Pantallas Jetpack Compose + ViewModels + estados de UI
data/local/    Room: entidades y DAOs
data/remote/   Retrofit: microservicio propio y API externa
data/repository/  Repositorios (lógica de negocio y de historial)
data/model/    Modelos y reglas puras (ResumenPanel, Responsable)
camara/ lecturaQR/ ubicacion/   CameraX, ML Kit y GPS
```

| Tecnología | Versión |
|---|---|
| Kotlin | 2.2.10 |
| Jetpack Compose (BOM) | 2026.02.01 |
| Room | 2.8.4 |
| Retrofit + Gson | 2.11.0 |
| CameraX | 1.4.2 |
| ML Kit Barcode Scanning | 17.3.0 |
| JUnit, MockK, kotlinx-coroutines-test | 4.x, 1.14.12, 1.10.2 |

## Pruebas unitarias y cobertura

Las pruebas están en `app/src/test` (JUnit + MockK) y cubren ViewModels, repositorios y reglas del dominio. Las pantallas de login y selector se prueban con Compose UI Test en `app/src/androidTest`.

Cobertura de líneas, medida con la herramienta de cobertura de Android Studio, sobre la capa de lógica (ViewModel y Repository):

## Notas

- Los usuarios, contraseñas, contactos y organizaciones son ficticios y solo existen para la demostración.
- El microservicio es de solo lectura (`GET`)
- La integración con la API externa es opcional y de prueba; si no hay internet, la app muestra un mensaje de error y sigue funcionando con los datos locales.
