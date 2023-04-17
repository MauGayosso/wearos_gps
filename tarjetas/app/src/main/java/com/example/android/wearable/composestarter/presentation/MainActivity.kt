package com.example.android.wearable.composestarter.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.wear.ambient.AmbientModeSupport
import androidx.wear.compose.material.*
import com.example.android.wearable.composestarter.presentation.theme.WearAppTheme
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.android.wearable.composestarter.R
import com.google.android.gms.location.*
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import java.io.IOException
import java.util.*
import kotlinx.coroutines.delay

class MainActivity : FragmentActivity(), AmbientModeSupport.AmbientCallbackProvider {
    private val STEP_SENSOR_CODE = 10
    private lateinit var ambientModeSupport: AmbientModeSupport.AmbientController

    //TODO ambient mode support
    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.BODY_SENSORS
        )
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("permisos", "Permisos Concedidos")
            //TODO
            makeRequest()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACTIVITY_RECOGNITION, Manifest.permission.BODY_SENSORS),
            STEP_SENSOR_CODE
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(
            requestCode,
            permissions, grantResults
        )
        when (requestCode) {
            STEP_SENSOR_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("DENIED", "Permisos Denegados")
                } else {
                    Log.i("GRANTED", "Permisos Concedidos")
                }
            }
        }
        fun getAmbientCallBack(): AmbientModeSupport.AmbientCallback =
            MyAmbientCallback()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupPermissions()
        ambientModeSupport = AmbientModeSupport.attach(this)
        setContent {
            steps = stepsTaken
            FirebaseApp.initializeApp(this)
            WearApp()
        }
        val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (sensor != null) {
            sensorManager.registerListener(
                object : SensorEventListener {
                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                    override fun onSensorChanged(event: SensorEvent?) {
                        stepsTaken = event?.values?.get(0)?.toInt() ?: 0
                    }
                },
                sensor,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun getAmbientCallback(): AmbientModeSupport.AmbientCallback = MyAmbientCallback()
}


private class MyAmbientCallback : AmbientModeSupport.AmbientCallback() {
    override fun onEnterAmbient(ambientDetails: Bundle?) {
        super.onEnterAmbient(ambientDetails)
    }

    override fun onExitAmbient() {
        super.onExitAmbient()
    }

    override fun onUpdateAmbient() {
        super.onUpdateAmbient()
    }
}

object NavRoute {
    const val Inicio = "Index de la app"
    const val Distancia = "Distancia"
    const val SCREEN_2 = "screen2"
    const val SCREEN_3 = "screen3"
    const val SCREEN_4 = "screen4/{sexo}/{edad}"
    const val DETAILSCREEN = "detailScreen/{sexo}/{edad}/{altura}/{peso}"
    const val CORRER = "correr"
    const val PREFERENCIAS = "preferencias"
    const val SENTDILLAS = "sendillas"
    const val ABDOMINALES = "abdominales"
    const val GPS = "gps"
    const val PODOMETRO = "podometro"
    const val DISTANCIARECORRIDA = "distanciaRecorrida"
}

@Composable
fun WearApp() {
    WearAppTheme {
        val listState = rememberScalingLazyListState()
        Scaffold(timeText = {
            if (!listState.isScrollInProgress) {
                TimeText()

            }
        },
            vignette = {
                Vignette(vignettePosition = VignettePosition.Top)
            },
            positionIndicator = {
                PositionIndicator(scalingLazyListState = listState)
            }
        ) {
            ScalingLazyColumn(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally,
            )
            {

                item {
                    val navController = rememberSwipeDismissableNavController()
                    SwipeDismissableNavHost(
                        navController = navController,
                        startDestination = NavRoute.Inicio
                    ) {
                        composable(NavRoute.Inicio) {
                            Inicio(navController)
                        }
                        composable(NavRoute.SCREEN_2) {
                            Screen2(navigation = navController)
                        }
                        composable(NavRoute.SCREEN_3) {
                            Screen3(navigation = navController)
                        }
                        composable("screen4/{sexo}/{edad}") { backStackEntry ->
                            backStackEntry.arguments?.getString("sexo")
                            Screen4(
                                navigation = navController,
                                sexo = backStackEntry.arguments?.getString("sexo") ?: "0",
                                edad = backStackEntry.arguments?.getString("edad") ?: "0"
                            )
                        }
                        composable("detailScreen/{sexo}/{edad}/{altura}/{peso}") { backStackEntry ->
                            backStackEntry.arguments?.getString("sexo")
                            detailScreen(
                                sexo = backStackEntry.arguments?.getString("sexo") ?: "0",
                                edad = backStackEntry.arguments?.getString("edad") ?: "0",
                                altura = backStackEntry.arguments?.getString("altura") ?: "0",
                                peso = backStackEntry.arguments?.getString("peso") ?: "0",
                                viewModel = preferenciasviewmodel()

                            )
                        }
                        composable(NavRoute.CORRER) {
                            Correr(navigation = navController)
                        }
                        composable(NavRoute.PREFERENCIAS) {
                            preferencias(navigation = navController)
                        }
                        composable((NavRoute.SENTDILLAS)) {
                            sendillas(
                                viewModel = preferenciasviewmodel(),
                                navigation = navController, totalTime = 18000,
                                handleColor = Color.Green,
                                inactiveBarColor = Color.DarkGray,
                                activeBarColor = Color(0xFF37B900)
                            )
                        }
                        composable((NavRoute.ABDOMINALES)) {
                            abdo(
                                viewModel = preferenciasviewmodel(),
                                navigation = navController, totalTime = 240,
                                handleColor = Color.Green,
                                inactiveBarColor = Color.DarkGray,
                                activeBarColor = Color(0xFF37B900)
                            )
                        }
                        composable(NavRoute.GPS) {
                            GPS(navigation = navController)
                        }
                        composable(NavRoute.PODOMETRO) {
                            PODOMETRO(navigation = navController)
                        }
                        composable(NavRoute.DISTANCIARECORRIDA) {
                            DISTANCIARECORRIDA(navigation = navController)
                        }
                    }

                }
            }
        }
    }
}

data class workoutData(val distancia: Int, val calorias_quemadas: Int)

data class preferenciass(
    val sexo: String = "",
    val edad: String = "",
    val altura: String = "",
    val peso: String = ""
)

@Composable
fun Inicio(navigation: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(35.dp))
        Text(text = "Healt Services", style = MaterialTheme.typography.title2)
        Spacer(modifier = Modifier.height(5.dp))
        Chip(
            label = { Text(text = "Correr") },
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            onClick = { navigation.navigate(NavRoute.CORRER) },
            colors = ChipDefaults.imageBackgroundChipColors(
                backgroundImagePainter = painterResource(id = R.drawable.steps)
            )
        )
        Chip(
            label = { Text(text = "Sentadillas") },
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            onClick = { navigation.navigate(NavRoute.SENTDILLAS) },
            colors = ChipDefaults.imageBackgroundChipColors(
                backgroundImagePainter = painterResource(id = R.drawable.sentadilla)
            )
        )
        Chip(
            label = { Text(text = "Abdominales") },
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            onClick = { navigation.navigate(NavRoute.ABDOMINALES) },
            colors = ChipDefaults.imageBackgroundChipColors(
                backgroundImagePainter = painterResource(id = R.drawable.abdominal)
            )
        )
        Chip(
            label = { Text(text = "Preferencias") },
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            onClick = { navigation.navigate(NavRoute.PREFERENCIAS) },
            colors = ChipDefaults.imageBackgroundChipColors(
                backgroundImagePainter = painterResource(id = R.drawable.distancia)
            )
        )
        Chip(
            label = { Text(text = "GPS") },
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            onClick = { navigation.navigate(NavRoute.GPS) },
            colors = ChipDefaults.imageBackgroundChipColors(
                backgroundImagePainter = painterResource(id = R.drawable.gps)
            )
        )
        Chip(
            label = { Text(text = "PODOMETRO") },
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            onClick = { navigation.navigate(NavRoute.PODOMETRO) },
            colors = ChipDefaults.imageBackgroundChipColors(
                backgroundImagePainter = painterResource(id = R.drawable.podometro)
            )
        )
        Chip(
            label = { Text(text = "Distancia Recorrida") },
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            onClick = { navigation.navigate(NavRoute.DISTANCIARECORRIDA) },
            colors = ChipDefaults.imageBackgroundChipColors(
                backgroundImagePainter = painterResource(id = R.drawable.gps)
            )
        )
        Spacer(modifier = Modifier.height(35.dp))
    }
}


@Composable
fun distancia(): Int {
    var count = remember {
        mutableStateOf(0)
    }
    val steps = getSteps()
    val distance = getSteps() * 0.762

    return distance.toInt()
}

@Composable
fun getSteps(): Int {
    val ctx = LocalContext.current
    val sensorManager: SensorManager =
        ctx.getSystemService(
            Context.SENSOR_SERVICE
        ) as SensorManager
    val HeartRateSensor: Sensor = sensorManager.getDefaultSensor(
        Sensor.TYPE_STEP_COUNTER
    )
    var ststatus = remember {
        mutableStateOf(0f)
    }
    val heartRateSensorListener = object : SensorEventListener {
        override fun onSensorChanged(p0: SensorEvent?) {
            p0 ?: return
            p0.values.firstOrNull()?.let {
                ststatus.value = p0.values[0]
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            println("onAccuracyChanged  : Sensor : $p0; accuracy $p1")
        }

    }
    sensorManager.registerListener(
        heartRateSensorListener,
        HeartRateSensor,
        SensorManager.SENSOR_DELAY_NORMAL
    )
    return ststatus.value.toInt()
}

@Composable
fun getHeartRate(): String {
    val ctx = LocalContext.current
    val sensorManager: SensorManager =
        ctx.getSystemService(
            Context.SENSOR_SERVICE
        ) as SensorManager
    val HeartRateSensor: Sensor = sensorManager.getDefaultSensor(
        Sensor.TYPE_HEART_RATE
    )
    var hrtatus = remember {
        mutableStateOf("")
    }
    val heartRateSensorListener = object : SensorEventListener {
        override fun onSensorChanged(p0: SensorEvent?) {
            p0 ?: return
            p0.values.firstOrNull()?.let {
                hrtatus.value = p0.values[0].toString()
            }
        }

        override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
            println("onAccuracyChanged  : Sensor : $p0; accuracy $p1")
        }

    }
    sensorManager.registerListener(
        heartRateSensorListener,
        HeartRateSensor,
        SensorManager.SENSOR_DELAY_NORMAL
    )
    return hrtatus.value
}


@Composable
fun Screen2(navigation: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            "\nSelecciona tus preferencias\n1.- Sexo\n2.- Edad\n3.- Altura\n4.- Peso",
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            onClick = { navigation.navigate("Screen3") }, colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Blue,
                contentColor = Color.Magenta
            )
        ) {
            Icon(
                imageVector = Icons.Rounded.ArrowForward,
                contentDescription = "Next",
                tint = Color.Cyan
            )
        }

    }
}

@Composable
fun detailScreen(
    viewModel: preferenciasviewmodel,
    sexo: String,
    edad: String,
    altura: String,
    peso: String
) {
    var sexoV = 0

    val index = viewModel.preferencias.value.size
    if (sexo == "Hombre") {
        var sexoV = "Hombre"
        var peso = peso
        var edad = edad
        var altura = altura

        viewModel.writeToDB(preferenciass(sexoV, edad, altura, peso), index)

        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = "Tus datos han sido actualizados",
        )


    } else {
        var sexoV = "Mujer"
        var peso = peso
        var edad = edad
        var altura = altura

        viewModel.writeToDB(preferenciass(sexoV, edad, altura, peso), index)

        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            text = "Tus datos han sido actualizados",
        )


    }
}

@Composable
fun Screen3(navigation: NavHostController) {
    val sexo = listOf("Hombre", "Mujer")
    val state = rememberPickerState(sexo.size)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterStart
    ) {

        Picker(
            state = state,
            modifier = Modifier.size(100.dp, 100.dp)
        ) {
            Text(sexo[it], modifier = Modifier.padding(10.dp))
        }

    }
    //picker 2
    val edad = listOf(
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "10",
        "11",
        "12",
        "13",
        "14",
        "15",
        "16",
        "17",
        "18",
        "19",
        "20",
        "21",
        "22",
        "23",
        "24",
        "25",
        "26",
        "27",
        "28",
        "29",
        "30",
        "31",
        "32",
        "33",
        "34",
        "35",
        "36",
        "37",
        "38",
        "39",
        "40",
        "41",
        "42",
        "43",
        "44",
        "45",
        "46",
        "47",
        "48",
        "49",
        "50",
        "51",
        "52",
        "53",
        "54",
        "55",
        "56",
        "57",
        "58",
        "59",
        "60",
        "61",
        "62",
        "63",
        "64",
        "65",
        "66",
        "67",
        "68",
        "69",
        "70",
        "71",
        "72",
        "73",
        "74",
        "75",
        "76",
        "77",
        "78",
        "79",
        "80",
        "81",
        "82",
        "83",
        "84",
        "85",
        "86",
        "87",
        "88",
        "89",
        "90",
        "91",
        "92",
        "93",
        "94",
        "95",
        "96",
        "97",
        "98",
        "99",
        "100"
    )
    val state2 = rememberPickerState(edad.size)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Picker(
            state = state2,
            modifier = Modifier.size(100.dp, 100.dp)
        ) {
            Text(edad[it], modifier = Modifier.padding(10.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .height(100.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            var position = state.selectedOption
            var position2 = state2.selectedOption
            var sexoE = sexo[position]
            var edadE = edad[position2]
            Button(
                onClick = {
                    navigation.navigate("screen4/${sexoE.toString()}/$edadE")
                }, colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Yellow,
                    contentColor = Color.Magenta
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowForward,
                    contentDescription = "Next",
                    tint = Color.Cyan
                )

            }

        }
    }
}

@Composable
fun Screen4(sexo: String, edad: String, navigation: NavController) {
    val sexo = sexo
    val edad = edad
    val altura = listOf(
        "100",
        "101",
        "102",
        "103",
        "104",
        "105",
        "106",
        "107",
        "108",
        "109",
        "110",
        "111",
        "112",
        "113",
        "114",
        "115",
        "116",
        "117",
        "118",
        "119",
        "120",
        "121",
        "122",
        "123",
        "124",
        "125",
        "126",
        "127",
        "128",
        "129",
        "130",
        "131",
        "132",
        "133",
        "134",
        "135",
        "136",
        "137",
        "138",
        "139",
        "140",
        "141",
        "142",
        "143",
        "144",
        "145",
        "146",
        "147",
        "148",
        "149",
        "150",
        "151",
        "152",
        "153",
        "154",
        "155",
        "156",
        "157",
        "158",
        "159",
        "160",
        "161",
        "162",
        "163",
        "164",
        "165",
        "166",
        "167",
        "168",
        "169",
        "170",
        "171",
        "172",
        "173",
        "174",
        "175",
        "176",
        "177",
        "178",
        "179",
        "180",
        "181",
        "182",
        "183",
        "184",
        "185",
        "186",
        "187",
        "188",
        "189",
        "190",
        "191",
        "192",
        "193",
        "194",
        "195",
        "196",
        "197",
        "198",
        "199",
        "200",
    )
    val state = rememberPickerState(altura.size)
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.CenterStart
    ) {

        Picker(
            state = state,
            modifier = Modifier.size(100.dp, 100.dp)
        ) {
            Text(altura[it], modifier = Modifier.padding(10.dp))
        }

    }
    //picker 2

    val peso = listOf(
        "1",
        "2",
        "3",
        "4",
        "5",
        "6",
        "7",
        "8",
        "9",
        "10",
        "11",
        "12",
        "13",
        "14",
        "15",
        "16",
        "17",
        "18",
        "19",
        "20",
        "21",
        "22",
        "23",
        "24",
        "25",
        "26",
        "27",
        "28",
        "29",
        "30",
        "31",
        "32",
        "33",
        "34",
        "35",
        "36",
        "37",
        "38",
        "39",
        "40",
        "41",
        "42",
        "43",
        "44",
        "45",
        "46",
        "47",
        "48",
        "49",
        "50",
        "51",
        "52",
        "53",
        "54",
        "55",
        "56",
        "57",
        "58",
        "59",
        "60",
        "61",
        "62",
        "63",
        "64",
        "65",
        "66",
        "67",
        "68",
        "69",
        "70",
        "71",
        "72",
        "73",
        "74",
        "75",
        "76",
        "77",
        "78",
        "79",
        "80",
        "81",
        "82",
        "83",
        "84",
        "85",
        "86",
        "87",
        "88",
        "89",
        "90",
        "91",
        "92",
        "93",
        "94",
        "95",
        "96",
        "97",
        "98",
        "99",
        "100"
    )
    val state2 = rememberPickerState(peso.size)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        Picker(
            state = state2,
            modifier = Modifier.size(100.dp, 100.dp)
        ) {
            Text(peso[it], modifier = Modifier.padding(10.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .height(100.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            var position = state.selectedOption
            var position2 = state2.selectedOption
            var altura = altura[position]
            var peso = peso[position2]
            Button(
                onClick = {
                    navigation.navigate("detailScreen/$sexo/$edad/${altura.toInt()}/${peso.toInt()}")
                }, colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.Yellow,
                    contentColor = Color.Magenta
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.ArrowForward,
                    contentDescription = "Next",
                    tint = Color.Cyan
                )

            }

        }
    }
}

var xd = mutableStateOf<preferenciass>(preferenciass("", "", "", ""));

class preferenciasviewmodel : ViewModel() {
    private val database =
        Firebase.database("https://fir-pyrebase-b3338-default-rtdb.firebaseio.com/")
    private var _preferencias = mutableStateOf<List<preferenciass>>(emptyList())
    val preferencias: State<List<preferenciass>> = _preferencias

    fun getData() {
        database.getReference("preferencias").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    xd.value = snapshot.getValue<List<preferenciass>>()!!.get(0)
                    _preferencias.value = snapshot.getValue<List<preferenciass>>()!!
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(ContentValues.TAG, "Error reading value")
                }

            }
        )
    }

    fun writeToDB(workout: preferenciass, index: Int) {
        val database =
            Firebase.database("https://fir-pyrebase-b3338-default-rtdb.firebaseio.com/")
        val myRef = database.getReference("preferencias")
        listOf(workout).forEach() {
            myRef.child(index.toString()).setValue(it)
        }
    }
}


class workOutviewmodel : ViewModel() {
    private val database =
        Firebase.database("https://fir-pyrebase-b3338-default-rtdb.firebaseio.com/")
    private var _workoutData = mutableStateOf<List<workoutData>>(emptyList())
    val workoutData: State<List<workoutData>> = _workoutData

    fun getData() {
        database.getReference("workout").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    _workoutData.value = snapshot.getValue<List<workoutData>>()!!
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.w(ContentValues.TAG, "Error reading value")
                }

            }
        )
    }

    fun writeToDB(workout: workoutData, index: Int) {
        val database =
            Firebase.database("https://fir-pyrebase-b3338-default-rtdb.firebaseio.com/")
        val myRef = database.getReference("workout")
        listOf(workout).forEach() {
            myRef.child(index.toString()).setValue(it)
        }
    }
}

@Composable
fun workOutScreen(viewModel: workOutviewmodel) {
    viewModel.getData()
    val index = viewModel.workoutData.value.size
    ScalingLazyColumn() {
        items(viewModel.workoutData.value) { workout ->
            Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center) {
                Text(
                    text = workout.distancia.toString(),
                    //modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
                Text(
                    text = workout.calorias_quemadas.toString(),
                    //modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color.White
                )
            }
        }
        item {
            Button(onClick = { viewModel.writeToDB(workoutData(123, 76), index) }) {
                Text("Add to Firebase")
            }
        }
    }
}

@Composable
fun preferencias(navigation: NavController) {
    Screen2(navigation)
}

@Composable
fun calorias(viewModel: preferenciasviewmodel): Float {
    var calorias = 0.0
    viewModel.getData()
    println("CHALE: " + xd.value)

    if (xd.value.sexo == "") {
        return calorias.toFloat()
    }

    var sexo = xd.value.sexo.toString()
    var edad = xd.value.edad.toInt()
    var altura = xd.value.altura.toInt()
    var peso = xd.value.peso.toFloat()

    println(sexo)
    println(edad)
    println(altura)
    println(peso)

    if (sexo == "Hombre") {
        calorias = (66 + (13.7 * peso) + (5 * altura) - (6.8 * edad)).toFloat().toDouble()
    } else {
        calorias = (655 + (9.6 * peso) + (1.8 * altura) - (4.7 * edad)).toFloat().toDouble()
    }
    return calorias.toFloat()


}

var calorias = 0.0

@Composable
fun sendillas(
    viewModel: preferenciasviewmodel, navigation: NavController,


    // total time of the timer
    totalTime: Long,

    // circular handle color
    handleColor: Color,

    // color of inactive bar / progress bar
    inactiveBarColor: Color,

    // color of active bar
    activeBarColor: Color,
    modifier: Modifier = Modifier,

    // set initial value to 1
    initialValue: Float = 1f,
    strokeWidth: Dp = 5.dp
): Float {
    viewModel.getData()
    println("CHALE: " + xd.value)

    if (xd.value.sexo == "") {
        return calorias.toFloat()
    }

    var sexo = xd.value.sexo.toString()
    var edad = xd.value.edad.toInt()
    var altura = xd.value.altura.toInt()
    var peso = xd.value.peso.toFloat()

    // create variable for
    // size of the composable
    var size by remember {
        mutableStateOf(IntSize.Zero)
    }
    // create variable for value
    var value by remember {
        mutableStateOf(initialValue)
    }
    // create variable for current time
    var currentTime by remember {
        mutableStateOf(totalTime)
    }
    // create variable for isTimerRunning
    var isTimerRunning by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = currentTime, key2 = isTimerRunning) {
        if (currentTime > 0 && isTimerRunning) {
            delay(1000L)
            currentTime -= 1000L
            value = currentTime / totalTime.toFloat()
        }
    }
    fun time(): String {
        var total = (currentTime-- / 600L).toString()
        return total
    }

    fun control(): Long {
        if (isTimerRunning == true) {
            if (currentTime == 0L) {
                isTimerRunning = false
                calorias += (5.0 * 3.5 * peso / 200) * 5
            }
            time()
        } else {
            currentTime == currentTime
        }
        return currentTime / 60
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        // add value of the timer
        Text(
            text = "Segundos" + "\n" + control().toString(),
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        // create button to start or stop the timer
        Button(
            onClick = {
                if (currentTime <= 0L) {
                    currentTime = totalTime
                    isTimerRunning = true
                } else {
                    isTimerRunning = !isTimerRunning
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),

            // change button color
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (!isTimerRunning || currentTime <= 0L) {
                    Color.Green
                } else {
                    Color.Red
                }
            )
        ) {
            Text(
                // change the text of button based on values
                text = if (isTimerRunning && currentTime >= 0L) "Stop"
                else if (!isTimerRunning && currentTime >= 0L) "Start"
                else "Restart"
            )
        }
        Button(
            onClick = {
                currentTime = currentTime
                isTimerRunning = false
            }
        ) {
            Text(text = "Pausa")
        }
        Text(
            text = "\nQuemaste alrededor de $calorias calorias",
            textAlign = TextAlign.Center

        )
        Spacer(modifier = Modifier.height(80.dp))
    }
    return calorias.toFloat()
}

var calorias2 = 0.0

@Composable
fun abdo(
    viewModel: preferenciasviewmodel, navigation: NavController,


    // total time of the timer
    totalTime: Long,

    // circular handle color
    handleColor: Color,

    // color of inactive bar / progress bar
    inactiveBarColor: Color,

    // color of active bar
    activeBarColor: Color,
    modifier: Modifier = Modifier,

    // set initial value to 1
    initialValue: Float = 1f,
    strokeWidth: Dp = 5.dp
): Float {
    viewModel.getData()
    println("CHALE: " + xd.value)

    if (xd.value.sexo == "") {
        return calorias2.toFloat()
    }

    var sexo = xd.value.sexo.toString()
    var edad = xd.value.edad.toInt()
    var altura = xd.value.altura.toInt()
    var peso = xd.value.peso.toFloat()

    // create variable for
    // size of the composable
    var size by remember {
        mutableStateOf(IntSize.Zero)
    }
    // create variable for value
    var value by remember {
        mutableStateOf(initialValue)
    }
    // create variable for current time
    var currentTime by remember {
        mutableStateOf(totalTime)
    }
    // create variable for isTimerRunning
    var isTimerRunning by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = currentTime, key2 = isTimerRunning) {
        if (currentTime > 0 && isTimerRunning) {
            delay(1000L)
            currentTime -= 1000L
            value = currentTime / totalTime.toFloat()
        }
    }
    fun time(): String {
        var total = (currentTime-- / 600L).toString()
        return total
    }

    fun control(): Long {
        if (isTimerRunning == true) {
            if (currentTime == 0L) {
                isTimerRunning = false
                calorias2 += (peso * 7 * 0.0175) * 5
            }
            time()
        } else {
            currentTime == currentTime
        }
        return currentTime / 60
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background), verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(40.dp))

        // add value of the timer
        Text(
            text = "Segundos" + "\n" + control().toString(),
            textAlign = TextAlign.Center,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        // create button to start or stop the timer
        Button(
            onClick = {
                if (currentTime <= 0L) {
                    currentTime = totalTime
                    isTimerRunning = true
                } else {
                    isTimerRunning = !isTimerRunning
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),

            // change button color
            colors = ButtonDefaults.buttonColors(
                backgroundColor = if (!isTimerRunning || currentTime <= 0L) {
                    Color.Green
                } else {
                    Color.Red
                }
            )
        ) {
            Text(
                // change the text of button based on values
                text = if (isTimerRunning && currentTime >= 0L) "Stop"
                else if (!isTimerRunning && currentTime >= 0L) "Start"
                else "Restart"
            )
        }
        Button(
            onClick = {
                currentTime = currentTime
                isTimerRunning = false
            }
        ) {
            Text(text = "Pausa")
        }
        Text(
            text = "\nQuemaste alrededor de $calorias2 calorias",
            textAlign = TextAlign.Center

        )
        Spacer(modifier = Modifier.height(80.dp))
    }
    return calorias2.toFloat()
}


@Composable
fun Correr(navigation: NavController) {
    Text(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        text = "Distancia recorrida\n" + distancia() + "\nPasos\n" + getSteps() + "\nRitmo cardiaco\n" + getHeartRate() + "\nCalorias quemadas\n" + calorias(
            viewModel = preferenciasviewmodel()
        ),
        textAlign = TextAlign.Center,
    )
}

var locationCallback: LocationCallback? = null
var fusedLocationClient: FusedLocationProviderClient? = null
var locationRequired = false

@Composable
fun GPS(navigation: NavController) {
    val contex = LocalContext.current
    var currentLocation by remember {
        mutableStateOf(LocationDetails(0.toDouble(), 0.toDouble()))
    }
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(contex)
    locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            for (location in p0.locations) {
                currentLocation = LocationDetails(location.latitude, location.longitude)
            }
        }
    }
    val launcherMultiplePermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val areGranted = permissionsMap.values.reduce { acc, next -> acc && next }
        if (areGranted) {
            locationRequired = true
            Toast.makeText(contex, "Location Permission Granted", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(contex, "Location Permission Denied", Toast.LENGTH_SHORT)
                .show()
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        Button(onClick = {
            if (permissions.all {
                    ContextCompat.checkSelfPermission(
                        contex,
                        it
                    ) == PackageManager.PERMISSION_GRANTED

                }) {
                //startlocationsupdate
                startLocationUpdates()
                getAdress(latitude= currentLocation.latitude,longitude = currentLocation.longitude, context = contex)
                val add = getAdress(latitude= currentLocation.latitude,longitude = currentLocation.longitude, context = contex)
                //writeDB(currentLocation.latitude, currentLocation.longitude, add.toString())
            } else {
                launcherMultiplePermissions.launch(permissions)
            }
        }) {
            Text(text = "Obtener ubicacion")
        }
        //Text(text = "Latitud: " + currentLocation.latitude)
        //Text(text = "Longitud: " + currentLocation.longitude)
        Text(text = getAdress(latitude= currentLocation.latitude,longitude = currentLocation.longitude, context = contex).toString() )
    }
}

fun getAdress(context: Context, latitude: Double, longitude: Double): String? {
    val geocoder = Geocoder(context, Locale.getDefault())
    try {
        val adresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (adresses != null && adresses.size > 0) {
            val adress = adresses[0]
            print("UBICACION: $adress")
            return adress.getAddressLine(0)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
}

fun uploadLocation1(context: Context, latitude: Double, longitude: Double): String? {
    val geocoder = Geocoder(context, Locale.getDefault())
    val database = Firebase.database("https://fir-pyrebase-b3338-default-rtdb.firebaseio.com/")
    val myRef = database.getReference("location1")
    try {
        val adresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (adresses != null && adresses.size > 0) {
            val adress = adresses[0]
            myRef.child("latitude").setValue(latitude)
            myRef.child("longitude").setValue(longitude)
            return adress.getAddressLine(0)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
}

fun uploadLocation2(context: Context, latitude: Double, longitude: Double): String? {
    val geocoder = Geocoder(context, Locale.getDefault())
    val database = Firebase.database("https://fir-pyrebase-b3338-default-rtdb.firebaseio.com/")
    val myRef = database.getReference("location2")
    try {
        val adresses = geocoder.getFromLocation(latitude, longitude, 1)
        if (adresses != null && adresses.size > 0) {
            val adress = adresses[0]
            myRef.child("latitude").setValue(latitude)
            myRef.child("longitude").setValue(longitude)
            return adress.getAddressLine(0)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return null
}

var lat1 = 0.toDouble()
var lat2 = 0.toDouble()
var lon1 = 0.toDouble()
var lon2 = 0.toDouble()
var distancias = ""
fun distanceBetween2Points() {
    val database = Firebase.database("https://fir-pyrebase-b3338-default-rtdb.firebaseio.com/")
    val myRef1 = database.getReference("location1/latitude")
    val myRef2 = database.getReference("location1/longitude")
    val myRef3 = database.getReference("location2/latitude")
    val myRef4 = database.getReference("location2/longitude")
    myRef1.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            lat1 = snapshot.getValue(Double::class.java)!!
        }

        override fun onCancelled(error: DatabaseError) {
            println("Failed to read value.")
        }
    })
    myRef2.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            lon1 = snapshot.getValue(Double::class.java)!!
        }

        override fun onCancelled(error: DatabaseError) {
            println("Failed to read value.")
        }
    })
    myRef3.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            lat2 = snapshot.getValue(Double::class.java)!!
        }

        override fun onCancelled(error: DatabaseError) {
            println("Failed to read value.")
        }
    })
    myRef4.addValueEventListener(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            lon2 = snapshot.getValue(Double::class.java)!!
        }

        override fun onCancelled(error: DatabaseError) {
            println("Failed to read value.")
        }
    })
}

fun distances(): String {
    val locationA: Location = Location("point A")
    val locationB: Location = Location("point B")
    println("LATITUD 1: $lat1")
    println("LONGITUD 1: $lon1")
    println("LATITUD 2: $lat2")
    println("LONGITUD 2: $lon2")
    locationA.latitude = lat1
    locationA.longitude = lon1
    locationB.latitude = lat2
    locationB.longitude = lon2
    var distance: Float = locationA.distanceTo(locationB)
    distance /= 1000
    distancias = distance.toString()
    println("DISTANCIA: $distance")
    return distancias
}

@SuppressLint("MissingPermission")
private fun startLocationUpdates() {
    locationCallback?.let {
        val locationRequest = LocationRequest.create().apply {
            interval = 3000
            fastestInterval = 1500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        fusedLocationClient?.requestLocationUpdates(
            locationRequest, it, Looper.getMainLooper()
        )
    }
}

fun onResume() {

    if (locationRequired) {
        startLocationUpdates()
    }
}

fun onPause() {
    locationCallback?.let {
        fusedLocationClient?.removeLocationUpdates(it)
    }
}
var stepsTaken = 0
var steps = 0
@Composable
fun PODOMETRO(navigation: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ){
        Text("Pasos dados: $stepsTaken")
    }
}

@Composable
fun DISTANCIARECORRIDA(navigation: NavController) {
    val contex = LocalContext.current
    var currentLocation by remember {
        mutableStateOf(LocationDetails(0.toDouble(), 0.toDouble()))
    }
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(contex)
    locationCallback = object : LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            for (location in p0.locations) {
                currentLocation = LocationDetails(location.latitude, location.longitude)
            }
        }
    }
    val launcherMultiplePermissions = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        val areGranted = permissionsMap.values.reduce { acc, next -> acc && next }
        if (areGranted) {
            locationRequired = true
            Toast.makeText(contex, "Location Permission Granted", Toast.LENGTH_SHORT)
                .show()
        } else {
            Toast.makeText(contex, "Location Permission Denied", Toast.LENGTH_SHORT)
                .show()
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        Button(onClick = {
            if (permissions.all {
                    ContextCompat.checkSelfPermission(
                        contex,
                        it
                    ) == PackageManager.PERMISSION_GRANTED

                }) {
                //startlocationsupdate

                startLocationUpdates()
                uploadLocation1(
                    latitude = currentLocation.latitude,
                    longitude = currentLocation.longitude,
                    context = contex
                )
                val add = getAdress(
                    latitude = currentLocation.latitude,
                    longitude = currentLocation.longitude,
                    context = contex
                )
                //writeDB(currentLocation.latitude, currentLocation.longitude, add.toString())
            } else {
                launcherMultiplePermissions.launch(permissions)
            }
        }) {
            Text(text = "Start")
        }
        // BOTON 2
        Button(onClick = {
            if (permissions.all {
                    ContextCompat.checkSelfPermission(
                        contex,
                        it
                    ) == PackageManager.PERMISSION_GRANTED

                }) {
                //startlocationsupdate

                startLocationUpdates()
                distanceBetween2Points()
                distances()
                uploadLocation2(
                    latitude = currentLocation.latitude,
                    longitude = currentLocation.longitude,
                    context = contex
                )
                val add = getAdress(
                    latitude = currentLocation.latitude,
                    longitude = currentLocation.longitude,
                    context = contex
                )
                //writeDB(currentLocation.latitude, currentLocation.longitude, add.toString())
            } else {
                launcherMultiplePermissions.launch(permissions)
            }
        }) {
            Text(text = "End")
        }
        //Text(text = "Latitud: " + currentLocation.latitude)
        // Text(text = "Longitud: " + currentLocation.longitude)
        //Text(text = getAdress(latitude= currentLocation.latitude,longitude = currentLocation.longitude, context = contex).toString() )
        Text(text = "Distancia:" + distancias)
    }
}
