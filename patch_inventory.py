import re

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    content = f.read()

# 1. Update AdminScreen call to InventoryTab
content = content.replace(
    '0 -> InventoryTab(stockViewModel)',
    '0 -> InventoryTab(stockViewModel, isSuperAdmin)'
)

# 2. Update InventoryTab signature and add Edit logic
inventory_tab_old = '''@Composable
fun InventoryTab(stockViewModel: StockViewModel) {
    val stockItems by stockViewModel.allStockItems.collectAsState()
    var showAddItemDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp)
        ) {
            items(stockItems) { item ->
                StockItemCard(item = item, onDelete = { stockViewModel.deleteStockItem(item.id) })
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        FloatingActionButton(
            onClick = { showAddItemDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Item")
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
}'''

inventory_tab_new = '''@Composable
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
}'''

content = content.replace(inventory_tab_old, inventory_tab_new)

# 3. Update StockItemCard signature and add Edit button
content = content.replace(
    'fun StockItemCard(item: StockItem, onDelete: () -> Unit) {',
    'fun StockItemCard(item: StockItem, onDelete: () -> Unit, onEdit: (() -> Unit)? = null) {'
)

stock_item_card_buttons_old = '''            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { showQrDialog = true }) {
                    Text("Show QR")
                }
                TextButton(onClick = onDelete) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            }'''

stock_item_card_buttons_new = '''            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
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
            }'''

content = content.replace(stock_item_card_buttons_old, stock_item_card_buttons_new)

# 4. Update AddItemDialog signature and initialization
add_item_dialog_old = '''@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddItemDialog(onDismiss: () -> Unit, onAdd: (StockItem) -> Unit) {
    var category by remember { mutableStateOf("Fabric") }
    var isEssential by remember { mutableStateOf(true) }
    var quantityStr by remember { mutableStateOf("") }
    var thresholdStr by remember { mutableStateOf("5") }
    
    // Dynamic fields
    var name by remember { mutableStateOf("") }
    var fabricType by remember { mutableStateOf("Dyed") }
    var unitType by remember { mutableStateOf("Meters") }
    var color by remember { mutableStateOf("") }
    var designNameOrNumber by remember { mutableStateOf("") }
    var dressPart by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }'''

add_item_dialog_new = '''@OptIn(ExperimentalMaterial3Api::class)
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
    var size by remember { mutableStateOf(initialItem?.size ?: "") }'''

content = content.replace(add_item_dialog_old, add_item_dialog_new)

# 5. Update AddItemDialog saving logic
add_item_save_old = '''                        onAdd(
                            StockItem(
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
                        )'''

add_item_save_new = '''                        onAdd(
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
                        )'''

content = content.replace(add_item_save_old, add_item_save_new)

# 6. Make AddItemDialog title dynamic
content = content.replace(
    'title = { Text("Add New Item") },',
    'title = { Text(if (initialItem == null) "Add New Item" else "Edit Item") },'
)
content = content.replace(
    'TextButton(onClick = {',
    'TextButton(onClick = {'
) # Just in case

# Make Add button dynamic
content = content.replace(
    '''                    if (finalName.isNotBlank() || category != "Fabric") { // Minimal validation
                        onAdd(
                            StockItem(
                                id = initialItem?.id ?: 0,''',
    '''                    if (finalName.isNotBlank() || category != "Fabric") { // Minimal validation
                        onAdd(
                            StockItem(
                                id = initialItem?.id ?: 0,'''
) # Need to change "Add" to "Save"
content = content.replace(
    '''                        )
                    }
                }
            ) {
                Text("Add")
            }''',
    '''                        )
                    }
                }
            ) {
                Text(if (initialItem == null) "Add" else "Save")
            }'''
)

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'w') as f:
    f.write(content)

