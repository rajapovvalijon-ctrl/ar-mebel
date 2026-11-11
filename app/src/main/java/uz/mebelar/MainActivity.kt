package uz.mebelar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.ar.core.Config
import com.google.ar.core.Frame
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.arcore.createAnchorOrNull
import io.github.sceneview.ar.arcore.isValid
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCollisionSystem
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView

/** Katalogdagi mebel: nomi, assets ichidagi fayl, AR dagi boshlang'ich eng katta o'lchami (metr). */
data class Furniture(val title: String, val file: String, val units: Float)

val catalog = listOf(
    Furniture("Divan-kreslo", "models/sofa_chair.glb", 1.0f),
    Furniture("Divan va stol", "models/sofa_and_table.glb", 2.2f),
    Furniture("Kitob javoni", "models/book_shelf.glb", 1.8f),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) { Root() }
            }
        }
    }
}

@Composable
fun Root() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var granted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        granted = it
    }
    if (granted) {
        ArScreen()
    } else {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("AR uchun kameraga ruxsat kerak")
            Button(
                onClick = { launcher.launch(Manifest.permission.CAMERA) },
                modifier = Modifier.padding(top = 16.dp)
            ) { Text("Ruxsat berish") }
        }
    }
}

@Composable
fun ArScreen() {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val cameraNode = rememberARCameraNode(engine)
    val childNodes = rememberNodes()
    val view = rememberView(engine)
    val collisionSystem = rememberCollisionSystem(view)

    var selected by remember { mutableStateOf(catalog.first()) }
    var showPlanes by remember { mutableStateOf(true) }
    var frame by remember { mutableStateOf<Frame?>(null) }

    Box(Modifier.fillMaxSize()) {
        ARScene(
            modifier = Modifier.fillMaxSize(),
            childNodes = childNodes,
            engine = engine,
            view = view,
            modelLoader = modelLoader,
            collisionSystem = collisionSystem,
            cameraNode = cameraNode,
            planeRenderer = showPlanes,
            sessionConfiguration = { session, config ->
                config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
                config.lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
                config.depthMode =
                    if (session.isDepthModeSupported(Config.DepthMode.AUTOMATIC))
                        Config.DepthMode.AUTOMATIC else Config.DepthMode.DISABLED
            },
            onSessionUpdated = { _, updatedFrame -> frame = updatedFrame },
            onGestureListener = rememberOnGestureListener(
                onSingleTapConfirmed = { motionEvent, tappedNode ->
                    // Mebelga emas, bo'sh joyga bosilganda yangi mebel qo'yiladi
                    if (tappedNode == null) {
                        val hit = frame?.hitTest(motionEvent.x, motionEvent.y)
                            ?.firstOrNull { it.isValid(depthPoint = false, point = false) }
                        hit?.createAnchorOrNull()?.let { anchor ->
                            val anchorNode = AnchorNode(engine = engine, anchor = anchor)
                            val model = ModelNode(
                                modelInstance = modelLoader.createModelInstance(selected.file),
                                scaleToUnits = selected.units,
                                // gorizontal markazda, pastki qismi yuzaga tegib turadi
                                centerOrigin = Position(x = 0f, y = -1f, z = 0f)
                            ).apply { isEditable = true } // sudrash, burish, kattalashtirish
                            anchorNode.addChildNode(model)
                            childNodes += anchorNode
                            showPlanes = false
                        }
                    }
                }
            )
        )

        Column(
            Modifier.align(Alignment.TopCenter).statusBarsPadding().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(color = Color(0xAA000000), shape = MaterialTheme.shapes.medium) {
                Text(
                    "Telefonni sekin aylantiring, yuza topilgach ekranga bosing.\n" +
                        "Mebelni 1 barmoq bilan siljiting, 2 barmoq bilan buring va kattalashtiring.",
                    color = Color.White,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        Column(
            Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(12.dp)
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                catalog.forEach { item ->
                    FilterChip(
                        selected = item == selected,
                        onClick = { selected = item },
                        label = { Text(item.title) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                OutlinedButton(onClick = {
                    childNodes.lastOrNull()?.let { it.destroy(); childNodes.remove(it) }
                }) { Text("Oxirgisini o'chirish") }
                OutlinedButton(
                    onClick = {
                        childNodes.toList().forEach { it.destroy() }
                        childNodes.clear()
                        showPlanes = true
                    },
                    modifier = Modifier.padding(start = 8.dp)
                ) { Text("Hammasini tozalash") }
            }
        }
    }
}
