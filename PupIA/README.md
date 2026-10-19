# Notas para mi bebe 💞 (Android Nativa)

Aplicación nativa para Android construida con **Kotlin**, **Jetpack Compose**, **Material 3** y arquitectura reactiva por capas. Diseñada para mantener a una pareja conectada en tiempo real mediante notas románticas, sincronización instantánea y notificaciones interactivas.

---

## 🚀 Arquitectura y Componentes Implementados

- **Motor de Escucha en Vivo (`NtfyLiveService`):**
  - Implementado como `ForegroundService` persistente (`specialUse`) para garantizar que la conexión HTTP/2 SSE no muera cuando el teléfono entra en *Doze Mode* o la pantalla se apaga.
  - Reconexión con *exponential backoff* (2s, 5s, 10s, máx 30s) y monitoreo de red con `ConnectivityManager.NetworkCallback` para reactivación instantánea al volver el Wi-Fi o datos móviles.
  - Dos canales de notificación:
    1. `live_connection_channel` (`IMPORTANCE_LOW`): Notificación silenciosa del servicio en primer plano (*"Conectado en vivo con tu bebé 💞"*).
    2. `notes_channel` (`IMPORTANCE_HIGH`): Alerta flotante prioritaria con sonido/vibración (*"Nueva nota de tu bebé"*).
- **Notificaciones Ricas e Interactivas:**
  - **Tocar la notificación:** Navega directamente a `list/today` (*"📅 Notas de hoy"*).
  - **❤️ Favorita:** Acción rápida que guarda la nota en Room en segundo plano sin abrir la app.
  - **💬 Responder (`RemoteInput`):** Permite escribir y responder al instante desde la barra de notificaciones.
- **Persistencia Local (Room Database + DataStore):**
  - `Note`: Entidad con `id`, `content`, `timestamp`, `isFromMe`, `isFavorite`.
  - `NoteDao`: Consultas reactivas con `Flow` (`getAllNotes`, `getNotesByOrigin`, `getFavoriteNotes`, `getNotesFromToday`).
  - `PreferenceRepository`: Almacenamiento seguro en DataStore del `secret_code` y generación atómica de `sender_id` (UUID único por dispositivo).
- **Interfaz Romántica en Jetpack Compose (Material 3):**
  - Paleta pastel y azul cielo suave (`#F0F7FF`, `#E0EFFF`, `#3B82F6`, corazones `#F43F5E`).
  - Tarjetas redondeadas (`24.dp`):
    1. ✏️ **Crear nueva nota** (*"Escribe algo especial"*)
    2. 📅 **Notas de hoy** (*"Lo que te han enviado hoy"*)
    3. 💌 **Mis notas** (*"Las notas que has escrito"*)
    4. ❤️ **Tus favoritas** (*"Las notas que guardaste"*)
  - Diálogos de confirmación para eliminar notas y desconexión segura.

---

## 🛠️ Comandos de Compilación y Pruebas

### 1. Ejecutar Pruebas Unitarias
```bash
.\gradlew.bat test
```
Verifica:
- Sanitización de topics y prevención de colisiones en `ntfy.sh`.
- Construcción y deserialización de payloads (`{senderId}||{contenido}`).
- Filtro de ventana de 5 minutos para alertas push y deduplicación por ID.

### 2. Generar el APK
```bash
.\gradlew.bat assembleDebug
```
El archivo ejecutable se genera en:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🧪 Simulación de Pareja en Vivo desde Terminal

Puedes simular el envío de notas desde la pareja directamente usando PowerShell hacia el topic configurado:

```powershell
# Reemplaza <codigo_secreto> por el código configurado en la app (ej. amor2026 -> notas_bebe_amor2026)
Invoke-RestMethod -Method Post -Uri "https://ntfy.sh/notas_bebe_<codigo_secreto>" -Body "pareja-uuid-123||Hola mi amor, esta es una nota en vivo ❤️"
```

El dispositivo recibirá inmediatamente la notificación prioritaria *"Nueva nota de tu bebé"* con los botones de acción interactiva.
