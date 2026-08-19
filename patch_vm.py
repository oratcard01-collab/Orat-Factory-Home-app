import re

with open('app/src/main/java/com/example/viewmodel/StockViewModel.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'fun deductStock(itemUid: String, userId: Int, quantityToDeduct: Float = 1f, reason: String? = null, onResult: (Boolean, StockItem?) -> Unit) {',
    'fun deductStock(itemUid: String, userId: Int, quantityToDeduct: Float = 1f, reason: String? = null, invoiceNumber: String? = null, onResult: (Boolean, StockItem?) -> Unit) {'
)

content = content.replace(
    'stockRepository.deductStock(itemUid, userId, quantityToDeduct, reason)',
    'stockRepository.deductStock(itemUid, userId, quantityToDeduct, reason, invoiceNumber)'
)

with open('app/src/main/java/com/example/viewmodel/StockViewModel.kt', 'w') as f:
    f.write(content)
