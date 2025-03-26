# MovilTPI

MovilTPI es una aplicación móvil Android que permite a los usuarios compartir experiencias de viaje e interactuar a través de publicaciones, comentarios y chats en tiempo real. Utiliza Parse Server como backend para la gestión de datos y autenticación de usuarios.

## Características Principales

-   **Autenticación**: Registro e inicio de sesión con correo electrónico y contraseña.
-   **Publicaciones (Posts)**: Creación, visualización y filtrado de publicaciones con título, descripción, categoría, duración y presupuesto.
-   **Comentarios**: Los usuarios pueden comentar en las publicaciones.
-   **Chats en Tiempo Real**: Comunicación directa entre usuarios mediante mensajes, con notificaciones en tiempo real utilizando Parse LiveQuery y polling como respaldo.
-   **Gestión de Imágenes**: Subida y visualización de imágenes de perfil y publicaciones, optimizadas con Picasso y Glide.
-   **Filtros**: Filtrado de publicaciones por categoría y orden (recientes o populares).
-   **Perfil de Usuario**: Visualización y edición de datos personales, incluyendo foto de perfil y publicaciones asociadas.
-   **Interfaz Moderna**: Diseño responsive con Material Design y componentes personalizados como RecyclerView, BottomNavigationView y FloatingActionButton.

## Tecnologías Utilizadas

-   **Lenguaje**: Java
-   **Framework**: Android SDK
-   **Backend**: Parse Server (hosteado en Back4App)
-   **Bases de Datos**: Parse Database
-   **Librerías Principales**:
    -   Parse SDK
    -   Picasso y Glide para manejo de imágenes
    -   ShapeOfView para vistas personalizadas
    -   Material Components for Android para diseño de interfaz
    -   androidx (AppCompat, RecyclerView, ViewModel, LiveData, etc.)
    -   androidx.viewpager2 (deslizadores de imágenes)

## Requisitos Previos

-   Android Studio (versión recomendada: 2023.1.1 o superior)
-   JDK 11
-   Cuenta en Back4App para configurar el backend
-   Emulador Android o dispositivo físico con Android 9.0 (API 28) o superior

## Instalación

1.  **Clonar el repositorio**:

    ```bash
    git clone https://github.com/ULP-UGD/MovilTPI.git
    cd MovilTPI
    ```

2.  **Abrir en Android Studio**:
    -   Abre Android Studio y selecciona "Open an existing project".
    -   Navega hasta la carpeta clonada y selecciona el proyecto.

3.  **Configurar el backend**:
    -   Crea una aplicación en Back4App.
    -   Copia el Application ID, Client Key y Server URL generados.
    -   Abre el archivo `res/values/strings.xml` y actualiza los siguientes valores:

    ```xml
    <string name="back4app_server_url">[https://parseapi.back4app.com/](https://parseapi.back4app.com/)</string>
    <string name="back4app_app_id">TU_APP_ID</string>
    <string name="back4app_client_key">TU_CLIENT_KEY</string>
    ```

>[!NOTE]
> Para que funcione correctamente el chat, debes configurar lo siguiente en Cloud Code en Back4App
>
>Debes agregar el siguiente codigo en cloud/main.js y luego hacer clic en Deploy
>
> ```js
> Parse.Cloud.beforeSave('Messages', async (request) => {
>   const message = request.object;
>   if (!message.existed()) {
>     const receiverId = message.get('receiver').id;
>
>     const query = new Parse.Query(Parse.Installation);
>     query.equalTo('user', { __type: 'Pointer', className: '_User', objectId: receiverId });
>
>     const payload = {
>       data: {
>         alert: `New message from ${message.get('sender').get('username')}`,
>         title: 'New Message',
>         messageId: message.id,
>         senderId: message.get('sender').id
>       },
>     };
>
>     try {
>       await Parse.Push.send({
>         where: query,
>         data: payload
>       }, { useMasterKey: true });
>     } catch (error) {
>       console.error('Error while sending push notification', error);
>     }
>   }
> });
>
> Parse.Cloud.beforeSave(Parse.User, async (request) => {
>   const user = request.object;
>
>   // Crear un ACL público
>   const acl = new Parse.ACL();
>   acl.setPublicReadAccess(true); // Permite lectura pública
>
>   // Aplicar el ACL al usuario
>   user.setACL(acl);
> });
> ```

4.  **Sincronizar dependencias**:
    -   Haz clic en "Sync Project with Gradle Files" en Android Studio para descargar todas las dependencias.

5.  **Ejecutar la aplicación**:
    -   Conecta un dispositivo Android o inicia un emulador.
    -   Haz clic en "Run" en Android Studio.

## Uso

-   **Inicio de sesión o registro**: Al abrir la aplicación, inicia sesión con un usuario existente o regístrate con un nuevo correo y contraseña.
-   **Explorar publicaciones**: En la pantalla principal (HomeActivity), verás una lista de publicaciones. Usa el botón de filtros para personalizar la vista.
-   **Crear una publicación**: Haz clic en el botón flotante (FAB) en la pantalla principal para crear un nuevo post. Completa los campos y sube imágenes si lo deseas.
-   **Chat**: Navega a la sección "Chats" desde el menú inferior, selecciona un usuario y comienza a enviar mensajes.
-   **Perfil**: Accede a tu perfil desde el menú inferior para ver tus publicaciones y datos personales.

## Estructura del Proyecto
```
MovilTPI/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/moviltpi/
│   │   │   │   ├── core/              # Clases base y utilidades
│   │   │   │   │   ├── models/        # Modelos de datos (Post, User, Comentario, Mensaje)
│   │   │   │   │   ├── utils/         # Utilidades (ImageUtils, Validaciones, etc.)
│   │   │   │   │   └── MyApplication.java  # Configuración inicial de Parse
│   │   │   │   ├── features/          # Funcionalidades de la app
│   │   │   │   │   ├── auth/          # Autenticación (Login, Registro)
│   │   │   │   │   ├── chat/          # Sistema de chat
│   │   │   │   │   ├── posts/         # Gestión de publicaciones
│   │   │   │   │   └── users/         # Gestión de perfiles
│   │   │   ├── res/                   # Recursos (layouts, drawables, valores)
│   │   │   └── AndroidManifest.xml    # Configuración de la app
│   └── build.gradle.kts               # Configuración de Gradle
└── README.md                          # Este archivo
```

```mermaid
flowchart TD
    %% User Entry Point
    U("User"):::external

    %% Android App Architecture Breakdown
    subgraph "Android App Architecture"
        direction TB

        %% UI Layer
        subgraph "UI Layer"
            UA["User Interface\n• MainActivity\n• HomeActivity\n• PostActivity\n• RegisterActivity\n• ChatFragment\n• HomeFragment\n• PerfilFragment"]:::ui
        end

        %% Presentation Layer
        subgraph "Presentation Layer"
            PL["ViewModels\n• AuthViewModel\n• MainViewModel\n• RegisterViewModel\n• ChatViewModel\n• PostViewModel\n• PostDetailViewModel"]:::viewModel
        end

        %% Domain Layer
        subgraph "Domain Layer"
            DL["Models\n• Post\n• Comentario\n• Mensaje\n• User"]:::model
        end

        %% Data Layer
        subgraph "Data Layer"
            DPL["Data Providers\n• AuthProvider\n• ChatProvider\n• PostProvider"]:::provider
        end

        %% Utilities & Services
        subgraph "Utilities & Services"
            US["Core Services\n• MyApplication\n• ImageUtils\n• EfectoTransformer\n• Validaciones\n• ImageAdapter\n• ImageSliderAdapter"]:::utility
        end
    end

    %% External Services
    subgraph "External Services"
        ES["External APIs & Libraries\n• Parse Server\n• Third-Party Libraries\n  (Picasso, Glide,\n  Material Components)"]:::external
    end

    %% Relationships
    U --> UA
    UA --> PL
    PL --> DL
    PL --> DPL
    DPL --> ES
    US --> UA
    US --> PL
    US --> DPL

    %% Styles
    classDef ui fill:#cce5ff,stroke:#004085,stroke-width:2px,border-radius:5px;
    classDef viewModel fill:#d4edda,stroke:#155724,stroke-width:2px,border-radius:5px;
    classDef model fill:#fff3cd,stroke:#856404,stroke-width:2px,border-radius:5px;
    classDef provider fill:#f8d7da,stroke:#721c24,stroke-width:2px,border-radius:5px;
    classDef utility fill:#d1ecf1,stroke:#0c5460,stroke-width:2px,border-radius:5px;
    classDef external fill:#ffefc1,stroke:#b8860b,stroke-width:2px,border-radius:5px;
```

>[!NOTE]
>
>Este diagrama es una modificacion del obtenido en https://gitdiagram.com/ulp-ugd/moviltpi
