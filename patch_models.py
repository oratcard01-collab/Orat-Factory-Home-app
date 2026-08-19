import re

with open('app/src/main/java/com/example/data/model/Models.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'val reason: String? = null,\n    val timestamp: Long = System.currentTimeMillis()',
    'val reason: String? = null,\n    val invoiceNumber: String? = null,\n    val timestamp: Long = System.currentTimeMillis()'
)

with open('app/src/main/java/com/example/data/model/Models.kt', 'w') as f:
    f.write(content)
