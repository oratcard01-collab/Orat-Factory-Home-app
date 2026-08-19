import re

with open('app/src/main/java/com/example/data/repository/StockRepository.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'suspend fun deductStock(itemUid: String, userId: Int, quantityToDeduct: Float = 1f, reason: String? = null): Boolean {',
    'suspend fun deductStock(itemUid: String, userId: Int, quantityToDeduct: Float = 1f, reason: String? = null, invoiceNumber: String? = null): Boolean {'
)

content = content.replace(
    'reason = reason\n            )',
    'reason = reason,\n                invoiceNumber = invoiceNumber\n            )'
)

with open('app/src/main/java/com/example/data/repository/StockRepository.kt', 'w') as f:
    f.write(content)
