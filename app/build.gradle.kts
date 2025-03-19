import org.gradle.kotlin.dsl.implementation

/**
 * Configuración de plugins para el módulo de la aplicación.
 * Aquí se define el plugin principal de Android Application.
 */
plugins {
    // Uso de un alias definido en `libs.versions.toml` para el plugin de aplicación Android
    alias(libs.plugins.android.application)
}

/**
 * Configuración específica del módulo Android.
 */
android {
    // Namespace único para la aplicación
    namespace = "com.example.moviltpi"

    // Versión del SDK de Android para compilar
    compileSdk = 35

    /**
     * Configuración por defecto de la aplicación.
     */
    defaultConfig {
        applicationId = "com.example.moviltpi"  // Identificador único de la aplicación
        minSdk = 28                             // SDK mínimo soportado (Android 9.0 Pie)
        targetSdk = 35                          // SDK objetivo (Android 15)
        versionCode = 1                         // Código de versión interno
        versionName = "1.0"                     // Nombre de versión visible

        // Corredor de pruebas para instrumentación en Android
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    /**
     * Tipos de compilación disponibles.
     */
    buildTypes {
        // Configuración para la variante "release"
        release {
            isMinifyEnabled = false  // Deshabilitar minificación del código
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),  // Reglas ProGuard por defecto
                "proguard-rules.pro"                                      // Reglas personalizadas
            )
        }
    }

    /**
     * Opciones de compilación para el compilador Java.
     */
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11  // Compatibilidad de código fuente con Java 11
        targetCompatibility = JavaVersion.VERSION_11  // Compatibilidad de bytecode con Java 11
    }

    /**
     * Características de compilación habilitadas.
     */
    buildFeatures {
        buildConfig = true   // Generar clase BuildConfig automáticamente
        viewBinding = true   // Habilitar View Binding para vinculación automática de vistas
    }
}

/**
 * Dependencias del proyecto.
 * Aquí se listan las bibliotecas externas y de Android utilizadas.
 */
dependencies {
    // Dependencias básicas de Android
    implementation(libs.appcompat)                  // AppCompat para compatibilidad con versiones antiguas
    implementation(libs.material)                   // Componentes de Material Design
    testImplementation(libs.junit)                  // JUnit para pruebas unitarias
    androidTestImplementation(libs.ext.junit)       // JUnit para pruebas instrumentadas
    androidTestImplementation(libs.espresso.core)   // Espresso para pruebas de UI

    // Dependencias de AndroidX
    implementation("androidx.activity:activity-ktx:1.7.2")            // Extensiones Kotlin para Activity
    implementation("androidx.activity:activity:1.7.2")                // Biblioteca base de Activity
    implementation("androidx.fragment:fragment-ktx:1.6.0")            // Extensiones Kotlin para Fragment
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.0")  // ConstraintLayout para Compose
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")  // ConstraintLayout estándar
    implementation("androidx.cardview:cardview:1.0.0")                // CardView para tarjetas
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")  // CoordinatorLayout para coordinación de vistas
    implementation("androidx.recyclerview:recyclerview:1.3.0")        // RecyclerView para listas

    // Dependencias de Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.8.7")    // ViewModel para gestión de datos
    implementation("androidx.lifecycle:lifecycle-livedata:2.8.7")     // LiveData para datos observables

    // Picasso para manejo de imágenes
    implementation("com.squareup.picasso:picasso:2.71828")            // Biblioteca para cargar y manejar imágenes

    // CircleImageView para imágenes circulares
    implementation("de.hdodenhof:circleimageview:3.1.0")              // Vista de imagen circular

    // ShapeOfView para formas personalizadas de vistas
    implementation("io.github.florent37:shapeofview:1.4.7")           // Biblioteca para formas de vista personalizadas

    // Parse SDK para backend
    implementation("com.github.parse-community.Parse-SDK-Android:bolts-tasks:4.3.0")  // Tareas asíncronas de Parse
    implementation("com.github.parse-community.Parse-SDK-Android:parse:4.3.0")        // SDK principal de Parse
    implementation("com.github.parse-community:ParseLiveQuery-Android:1.2.2")         // Consultas en tiempo real
    implementation("com.github.parse-community.Parse-SDK-Android:fcm:4.3.0")          // Integración con Firebase Cloud Messaging

    // Media3 para interfaces multimedia
    implementation("androidx.media3:media3-ui:1.4.1")                // UI para reproducción multimedia
    implementation("androidx.media3:media3-ui-leanback:1.4.1")       // UI Leanback para Android TV

    // ViewPager2 para deslizamiento de vistas
    implementation("androidx.viewpager2:viewpager2:1.0.0")           // ViewPager2 básico
    implementation("androidx.viewpager2:viewpager2:1.1.0")           // Versión actualizada de ViewPager2

    // Glide para manejo de imágenes
    implementation("com.github.bumptech.glide:glide:4.16.0")         // Biblioteca para cargar imágenes

    // SwipeRefreshLayout para refresco por deslizamiento
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")  // Componente de refresco
}