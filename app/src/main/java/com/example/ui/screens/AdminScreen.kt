package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.content.Context
import androidx.print.PrintHelper
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.R
import com.example.data.model.DeductionLog
import com.example.data.model.Role
import com.example.data.model.StockItem
import com.example.data.model.User
import com.example.viewmodel.AuthViewModel
import com.example.viewmodel.StockViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    authViewModel: AuthViewModel,
    stockViewModel: StockViewModel,
    onBack: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Inventory", "Logs")

    val isSuperAdmin = currentUser?.role == Role.SUPER_ADMIN

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(painter = painterResource(id = R.drawable.logo), contentDescription = "Logo", modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Admin Dashboard") 
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
        Column(modifier = Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> InventoryTab(stockViewModel, isSuperAdmin)
                1 -> LogsTab(stockViewModel, authViewModel)
                
            }
        }
    }
}

@Composable
fun InventoryTab(stockViewModel: StockViewModel, isSuperAdmin: Boolean) {
    val stockItems by stockViewModel.allStockItems.collectAsState()
    var showAddItemDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<StockItem?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(stockItems) { item ->
                StockItemCard(
                    item = item,
                    onDelete = { stockViewModel.deleteStockItem(item.id) },
                    onEdit = if (isSuperAdmin) { { itemToEdit = item } } else null
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        if (isSuperAdmin) {
            FloatingActionButton(
                onClick = { showAddItemDialog = true },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    }

    if (showAddItemDialog) {
        AddItemDialog(
            onDismiss = { showAddItemDialog = false },
            onAdd = { newItem ->
                stockViewModel.addOrUpdateStockItem(newItem)
                showAddItemDialog = false
            }
        )
    }
    
    if (itemToEdit != null) {
        AddItemDialog(
            initialItem = itemToEdit,
            onDismiss = { itemToEdit = null },
            onAdd = { updatedItem ->
                stockViewModel.addOrUpdateStockItem(updatedItem)
                itemToEdit = null
            }
        )
    }
}

@Composable
fun StockItemCard(item: StockItem, onDelete: () -> Unit, onEdit: (() -> Unit)? = null) {
    var showQrDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (item.quantity <= item.reorderThreshold && item.isEssential) {
                    Badge { Text("Low Stock") }
                } else if (item.isEssential) {
                    Badge(containerColor = MaterialTheme.colorScheme.primary) { Text("Essential") }
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Category: ${item.category}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Quantity: ${item.quantity} ${item.unitType}", style = MaterialTheme.typography.bodyLarge)
            
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (onEdit != null) {
                    TextButton(onClick = onEdit) {
                        Text("Edit")
                    }
                }
                TextButton(onClick = { showQrDialog = true }) {
                    Text("Show QR")
                }
                if (onEdit != null) {
                    TextButton(onClick = onDelete) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }

    if (showQrDialog) {
        val context = LocalContext.current
        val isSeries = item.category in listOf("Embroidery Stock", "Ready Stock", "Khaka Stock", "Un-Stitched Stock")
        val qty = item.quantity.toInt().coerceAtLeast(1)

        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            confirmButton = {
                TextButton(onClick = { showQrDialog = false }) { Text("Close") }
            },
            dismissButton = {
                TextButton(onClick = { printQrCodes(context, item, isSeries) }) { 
                    Icon(Icons.Default.Print, contentDescription = "Print")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Print") 
                }
            },
            title = { Text("QR Codes for ${item.name}") },
            text = {
                if (!isSeries) {
                    val bitmap = generateQrCode(item.uid)
                    if (bitmap != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "QR Code",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                            )
                            Text("ID: ${item.uid.take(8)}")
                        }
                    } else {
                        Text("Failed to generate QR code")
                    }
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(qty) { index ->
                            val pieceUid = "${item.uid}#${index + 1}"
                            val bitmap = remember(pieceUid) { generateQrCode(pieceUid, 256) }
                            if (bitmap != null) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Image(
                                        bitmap = bitmap.asImageBitmap(),
                                        contentDescription = "QR Code ${index + 1}",
                                        modifier = Modifier.size(100.dp)
                                    )
                                    Text("#${index + 1}", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        )
    }
}

fun printQrCodes(context: Context, item: StockItem, isSeries: Boolean) {
    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? android.print.PrintManager ?: return
    
    val qty = if (isSeries) item.quantity.toInt().coerceAtLeast(1) else 1
    val jobName = "Print QRs - ${item.name}"
    
    printManager.print(jobName, object : android.print.PrintDocumentAdapter() {
        private var pdfDocument: android.graphics.pdf.PdfDocument? = null
        
        override fun onLayout(
            oldAttributes: android.print.PrintAttributes?,
            newAttributes: android.print.PrintAttributes,
            cancellationSignal: android.os.CancellationSignal?,
            callback: LayoutResultCallback,
            extras: android.os.Bundle?
        ) {
            pdfDocument = android.graphics.pdf.PdfDocument()
            if (cancellationSignal?.isCanceled == true) {
                callback.onLayoutCancelled()
                return
            }
            val info = android.print.PrintDocumentInfo.Builder(jobName)
                .setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                .setPageCount(qty)
                .build()
            callback.onLayoutFinished(info, true)
        }

        override fun onWrite(
            pages: Array<out android.print.PageRange>,
            destination: android.os.ParcelFileDescriptor,
            cancellationSignal: android.os.CancellationSignal?,
            callback: WriteResultCallback
        ) {
            val qrSize = 512
            val padding = 64
            val textHeight = 64
            
            val paint = Paint().apply {
                color = android.graphics.Color.BLACK
                textSize = 40f
                textAlign = Paint.Align.CENTER
            }
            
            for (i in 0 until qty) {
                if (cancellationSignal?.isCanceled == true) {
                    pdfDocument?.close()
                    pdfDocument = null
                    callback.onWriteCancelled()
                    return
                }
                
                val content = if (isSeries) "${item.uid}#${i + 1}" else item.uid
                val label = if (isSeries) "${item.name} #${i + 1}" else item.name
                
                val qrBitmap = generateQrCode(content, qrSize) ?: continue
                
                val pageWidth = qrSize + padding * 2
                val pageHeight = qrSize + padding * 2 + textHeight
                
                val pageInfo = android.graphics.pdf.PdfDocument.PageInfo.Builder(pageWidth, pageHeight, i).create()
                val page = pdfDocument?.startPage(pageInfo) ?: continue
                val canvas = page.canvas
                
                canvas.drawColor(android.graphics.Color.WHITE)
                
                val x = padding.toFloat()
                val y = padding.toFloat()
                
                canvas.drawBitmap(qrBitmap, x, y, null)
                canvas.drawText(label, x + qrSize / 2f, y + qrSize + textHeight, paint)
                
                pdfDocument?.finishPage(page)
                qrBitmap.recycle()
            }
            
            try {
                pdfDocument?.writeTo(java.io.FileOutputStream(destination.fileDescriptor))
                callback.onWriteFinished(arrayOf(android.print.PageRange.ALL_PAGES))
            } catch (e: java.io.IOException) {
                callback.onWriteFailed(e.toString())
            } finally {
                pdfDocument?.close()
                pdfDocument = null
            }
        }
    }, null)
}

fun generateQrCode(content: String, size: Int = 512): Bitmap? {
    try {
        val writer = com.google.zxing.qrcode.QRCodeWriter()
        val bitMatrix = writer.encode(content, com.google.zxing.BarcodeFormat.QR_CODE, size, size)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    } catch (e: Exception) {
        return null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(onDismiss: () -> Unit, onAdd: (StockItem) -> Unit, initialItem: StockItem? = null) {
    var category by remember { mutableStateOf(initialItem?.category ?: "Fabric") }
    var isEssential by remember { mutableStateOf(initialItem?.isEssential ?: true) }
    var quantityStr by remember { mutableStateOf(initialItem?.quantity?.let { if (it % 1.0f == 0.0f) it.toInt().toString() else it.toString() } ?: "") }
    var thresholdStr by remember { mutableStateOf(initialItem?.reorderThreshold?.let { if (it % 1.0f == 0.0f) it.toInt().toString() else it.toString() } ?: "5") }
    
    // Dynamic fields
    var name by remember { mutableStateOf(initialItem?.name ?: "") }
    var fabricType by remember { mutableStateOf(initialItem?.fabricType ?: "Dyed") }
    var unitType by remember { mutableStateOf(initialItem?.unitType ?: "Meters") }
    var color by remember { mutableStateOf(initialItem?.color ?: "") }
    var designNameOrNumber by remember { mutableStateOf(initialItem?.designNameOrNumber ?: "") }
    var dressPart by remember { mutableStateOf(initialItem?.dressPart ?: "") }
    var size by remember { mutableStateOf(initialItem?.size ?: "") }

    val categories = listOf("Fabric", "Embroidery Stock", "Ready Stock", "Khaka Stock", "Un-Stitched Stock", "Stationary", "Accessories", "Others")
    var categoryExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(category) {
        val essentialCategories = listOf("Fabric", "Embroidery Stock", "Ready Stock", "Khaka Stock", "Un-Stitched Stock")
        isEssential = category in essentialCategories
        
        // Reset/set defaults based on category
        if (category == "Fabric") {
            unitType = "Meters"
        } else {
            unitType = "Pieces"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val qty = quantityStr.toFloatOrNull() ?: 0f
                    val threshold = thresholdStr.toFloatOrNull() ?: 0f
                    val finalName = if (category == "Fabric") name else if (designNameOrNumber.isNotBlank()) designNameOrNumber else name
                    
                    if (finalName.isNotBlank() || category != "Fabric") { // Minimal validation
                        onAdd(
                            StockItem(
                                id = initialItem?.id ?: 0,
                                uid = initialItem?.uid ?: java.util.UUID.randomUUID().toString(),
                                name = finalName.ifBlank { "Unnamed Item" },
                                category = category,
                                isEssential = isEssential,
                                unitType = unitType,
                                quantity = qty,
                                reorderThreshold = threshold,
                                fabricType = if (category == "Fabric") fabricType else null,
                                color = color.takeIf { it.isNotBlank() },
                                designNameOrNumber = designNameOrNumber.takeIf { it.isNotBlank() },
                                dressPart = dressPart.takeIf { it.isNotBlank() },
                                size = size.takeIf { it.isNotBlank() }
                            )
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        title = { Text("Add Stock Item") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        categories.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    category = selectionOption
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                when (category) {
                    "Fabric" -> {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Fabric Name") }, modifier = Modifier.fillMaxWidth())
                        
                        var fabricTypeExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = fabricTypeExpanded,
                            onExpandedChange = { fabricTypeExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = fabricType,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Fabric Type") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fabricTypeExpanded) },
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = fabricTypeExpanded,
                                onDismissRequest = { fabricTypeExpanded = false }
                            ) {
                                listOf("Dyed", "Non-Dyed").forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            fabricType = option
                                            fabricTypeExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        if (fabricType == "Dyed") {
                            OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Colour") }, modifier = Modifier.fillMaxWidth())
                        }
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = quantityStr, onValueChange = { quantityStr = it }, label = { Text("Quantity") }, modifier = Modifier.weight(1f))
                            
                            var unitExpanded by remember { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = unitExpanded,
                                onExpandedChange = { unitExpanded = it },
                                modifier = Modifier.weight(1f)
                            ) {
                                OutlinedTextField(
                                    value = unitType,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Unit") },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                                    modifier = Modifier.menuAnchor().fillMaxWidth()
                                )
                                ExposedDropdownMenu(
                                    expanded = unitExpanded,
                                    onDismissRequest = { unitExpanded = false }
                                ) {
                                    listOf("Meters", "Feet").forEach { option ->
                                        DropdownMenuItem(
                                            text = { Text(option) },
                                            onClick = {
                                                unitType = option
                                                unitExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    "Embroidery Stock" -> {
                        OutlinedTextField(value = designNameOrNumber, onValueChange = { designNameOrNumber = it }, label = { Text("Design Name") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Colour") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = dressPart, onValueChange = { dressPart = it }, label = { Text("Dress Part") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = size, onValueChange = { size = it }, label = { Text("Size") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = quantityStr, onValueChange = { quantityStr = it }, label = { Text("Quantity (Pieces)") }, modifier = Modifier.fillMaxWidth())
                    }
                    "Ready Stock", "Un-Stitched Stock" -> {
                        OutlinedTextField(value = designNameOrNumber, onValueChange = { designNameOrNumber = it }, label = { Text("Design Number/Name") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = size, onValueChange = { size = it }, label = { Text("Size") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = color, onValueChange = { color = it }, label = { Text("Colour") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = quantityStr, onValueChange = { quantityStr = it }, label = { Text("Quantity (Pieces)") }, modifier = Modifier.fillMaxWidth())
                    }
                    "Khaka Stock" -> {
                        OutlinedTextField(value = designNameOrNumber, onValueChange = { designNameOrNumber = it }, label = { Text("Khaka Design No.") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = size, onValueChange = { size = it }, label = { Text("Size") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = quantityStr, onValueChange = { quantityStr = it }, label = { Text("Quantity (Pieces)") }, modifier = Modifier.fillMaxWidth())
                    }
                    "Stationary", "Accessories", "Others" -> {
                        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = quantityStr, onValueChange = { quantityStr = it }, label = { Text("Quantity") }, modifier = Modifier.fillMaxWidth())
                    }
                }
                
                OutlinedTextField(value = thresholdStr, onValueChange = { thresholdStr = it }, label = { Text("Reorder Threshold") }, modifier = Modifier.fillMaxWidth())
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isEssential, onCheckedChange = { isEssential = it })
                    Text("Is Essential")
                }
            }
        }
    )
}

@Composable
fun LogsTab(stockViewModel: StockViewModel, authViewModel: AuthViewModel) {
    val logs by stockViewModel.deductionLogs.collectAsState()
    val users by authViewModel.allUsers.collectAsState(initial = emptyList())
    val stockItems by stockViewModel.allStockItems.collectAsState()
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        items(logs) { log ->
            val user = users.find { it.id == log.userId }
            val item = stockItems.find { it.id == log.itemId }
            val userName = user?.name ?: "User ID: ${log.userId}"
            val itemName = item?.name ?: "Item ID: ${log.itemId}"

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("$itemName | $userName", fontWeight = FontWeight.Bold)
                    Text("Deducted: ${log.quantityDeducted} | Remaining: ${log.remainingQuantity}")
                    if (log.invoiceNumber != null) {
                        Text("Invoice: ${log.invoiceNumber}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }
                    if (log.reason != null) {
                        Text("Reason: ${log.reason}", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                    Text("Time: ${dateFormat.format(Date(log.timestamp))}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun UsersTab(authViewModel: AuthViewModel) {
    val users by authViewModel.allUsers.collectAsState(initial = emptyList())
    var showAddUserDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<User?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(users) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(user.name, style = MaterialTheme.typography.titleMedium)
                            Text("Username: ${user.username}")
                            Text("Role: ${user.role.name}", color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { userToEdit = user }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit User")
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddUserDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add User")
        }
    }

    if (userToEdit != null) {
        val user = userToEdit!!
        var editName by remember { mutableStateOf(user.name) }
        var editUsername by remember { mutableStateOf(user.username) }
        var editPassword by remember { mutableStateOf(user.password) }
        var editRole by remember { mutableStateOf(user.role) }

        AlertDialog(
            onDismissRequest = { userToEdit = null },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editName.isNotBlank() && editUsername.isNotBlank() && editPassword.isNotBlank()) {
                            authViewModel.updateUser(user.copy(
                                name = editName,
                                username = editUsername,
                                password = editPassword,
                                role = editRole
                            ))
                            userToEdit = null
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToEdit = null }) { Text("Cancel") }
            },
            title = { Text("Edit User") },
            text = {
                Column {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Name") })
                    OutlinedTextField(value = editUsername, onValueChange = { editUsername = it }, label = { Text("Username") })
                    OutlinedTextField(value = editPassword, onValueChange = { editPassword = it }, label = { Text("Password") })
                    Text("Role:", modifier = Modifier.padding(top = 8.dp))
                    Role.values().forEach { role ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = editRole == role,
                                onClick = { editRole = role }
                            )
                            Text(role.name)
                        }
                    }
                }
            }
        )
    }

    if (showAddUserDialog) {
        var newName by remember { mutableStateOf("") }
        var newUsername by remember { mutableStateOf("") }
        var newPassword by remember { mutableStateOf("") }
        var newRole by remember { mutableStateOf(Role.GENERAL_STAFF) }

        AlertDialog(
            onDismissRequest = { showAddUserDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newName.isNotBlank() && newUsername.isNotBlank() && newPassword.isNotBlank()) {
                            authViewModel.addUser(newName, newUsername, newPassword, newRole)
                            showAddUserDialog = false
                        }
                    }
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddUserDialog = false }) { Text("Cancel") }
            },
            title = { Text("Add User") },
            text = {
                Column {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Name") })
                    OutlinedTextField(value = newUsername, onValueChange = { newUsername = it }, label = { Text("Username") })
                    OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("Password") })
                    // Need a dropdown for roles in real life, simplified here:
                    Text("Role:", modifier = Modifier.padding(top = 8.dp))
                    Role.values().forEach { role ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = newRole == role,
                                onClick = { newRole = role }
                            )
                            Text(role.name)
                        }
                    }
                }
            }
        )
    }
}
