package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.R
import com.example.data.model.Role
import com.example.data.model.StockItem
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.StockViewModel
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    authViewModel: AuthViewModel,
    stockViewModel: StockViewModel,
    mode: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentUser by authViewModel.currentUser.collectAsState()
    
    // Check if mode is deduction AND user has permission
    val isDeductionMode = mode == "deduction" && (currentUser?.role == Role.DEDUCTION_OPERATOR || currentUser?.role == Role.ADMIN || currentUser?.role == Role.SUPER_ADMIN)

    val coroutineScope = rememberCoroutineScope()

    var scannedResult by remember { mutableStateOf<String?>(null) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasCameraPermission = isGranted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(id = R.drawable.logo), contentDescription = "Logo", modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isDeductionMode) "Deduction Scanner" else "Lookup Scanner")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (scannedResult == null) {
                if (hasCameraPermission) {
                    // Camera Preview
                    Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        AndroidView(
                            factory = { ctx ->
                            val previewView = PreviewView(ctx)
                            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                            val executor = ContextCompat.getMainExecutor(ctx)
                            val imageAnalysisExecutor = Executors.newSingleThreadExecutor()

                            cameraProviderFuture.addListener({
                                val cameraProvider = cameraProviderFuture.get()
                                val preview = Preview.Builder().build().also {
                                    it.setSurfaceProvider(previewView.surfaceProvider)
                                }

                                val options = BarcodeScannerOptions.Builder()
                                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                                    .build()
                                val scanner = BarcodeScanning.getClient(options)

                                val imageAnalyzer = ImageAnalysis.Builder()
                                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                    .build()
                                    .also { analysis ->
                                        analysis.setAnalyzer(imageAnalysisExecutor) { imageProxy ->
                                            val mediaImage = imageProxy.image
                                            if (mediaImage != null) {
                                                val image = InputImage.fromMediaImage(
                                                    mediaImage,
                                                    imageProxy.imageInfo.rotationDegrees
                                                )
                                                scanner.process(image)
                                                    .addOnSuccessListener { barcodes ->
                                                        for (barcode in barcodes) {
                                                            barcode.rawValue?.let { value ->
                                                                if (!isProcessing) {
                                                                    isProcessing = true
                                                                    scannedResult = value
                                                                }
                                                            }
                                                        }
                                                    }
                                                    .addOnCompleteListener {
                                                        imageProxy.close()
                                                    }
                                            } else {
                                                imageProxy.close()
                                            }
                                        }
                                    }

                                try {
                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        imageAnalyzer
                                    )
                                } catch (e: Exception) {
                                    Log.e("Camera", "Use case binding failed", e)
                                }
                            }, executor)
                            previewView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Camera permission is required to scan QR codes.")
                }
            }
        } else {
            // Result screen
                Column(
                    modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    var item by remember { mutableStateOf<StockItem?>(null) }
                    var baseUid by remember { mutableStateOf("") }
                    var pieceId by remember { mutableStateOf<String?>(null) }
                    
                    var showDeductionPrompt by remember { mutableStateOf(false) }
                    var deductQuantityStr by remember { mutableStateOf("") }
                    var deductReasonStr by remember { mutableStateOf("") }
                    var invoiceNumberStr by remember { mutableStateOf("") }
                    
                    LaunchedEffect(scannedResult) {
                        val parts = scannedResult!!.split("#")
                        baseUid = parts[0]
                        if (parts.size > 1) {
                            pieceId = parts[1]
                        }
                        
                        item = stockViewModel.getStockItemByUid(baseUid)
                        if (item == null) {
                            resultMessage = "Item not found in database."
                        } else {
                            if (isDeductionMode) {
                                showDeductionPrompt = true
                                if (item!!.category != "Fabric") {
                                    deductQuantityStr = "1"
                                }
                            } else {
                                resultMessage = "Item: ${item!!.name}\nCategory: ${item!!.category}\nQuantity: ${item!!.quantity} ${item!!.unitType}\n" +
                                        (if (pieceId != null) "Piece: #$pieceId" else "")
                            }
                        }
                    }

                    if (showDeductionPrompt) {
                        Text(
                            text = "Item: ${item?.name}" + (if (pieceId != null) " (Piece #$pieceId)" else ""),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = deductQuantityStr,
                            onValueChange = { deductQuantityStr = it },
                            label = { Text("Quantity to deduct (${item?.unitType})") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = item?.category == "Fabric"
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = deductReasonStr,
                            onValueChange = { deductReasonStr = it },
                            label = { Text("Reason for deduction") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = invoiceNumberStr,
                            onValueChange = { invoiceNumberStr = it },
                            label = { Text("Bill/Invoice Number (Required)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = {
                            val qty = deductQuantityStr.toFloatOrNull()
                            val reason = deductReasonStr.takeIf { it.isNotBlank() }
                            val invoice = invoiceNumberStr.takeIf { it.isNotBlank() }
                            if (qty != null && qty > 0f && reason != null && invoice != null) {
                                showDeductionPrompt = false
                                stockViewModel.deductStock(baseUid, currentUser!!.id, qty, reason, invoice) { success, updatedItem ->
                                    if (success) {
                                        resultMessage = "✅ $qty ${updatedItem?.unitType} deducted — ${updatedItem?.name} — new stock: ${updatedItem?.quantity}"
                                    } else {
                                        resultMessage = "❌ Failed to deduct stock."
                                    }
                                }
                            } else if (reason == null) {
                                // optional: show error for reason
                            }
                        }, enabled = deductReasonStr.isNotBlank() && invoiceNumberStr.isNotBlank()) {
                            Text("Deduct")
                        }
                    } else if (resultMessage != null) {
                        Text(
                            text = resultMessage!!,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(16.dp)
                        )
                    } else {
                        CircularProgressIndicator()
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    if (!showDeductionPrompt) {
                        Button(onClick = {
                            scannedResult = null
                            resultMessage = null
                            isProcessing = false
                            pieceId = null
                            showDeductionPrompt = false
                            deductReasonStr = ""
                            invoiceNumberStr = ""
                        }) {
                            Text("Scan Again")
                        }
                    }
                }
            }
        }
    }
}
