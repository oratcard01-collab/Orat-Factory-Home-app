import re

with open('app/src/main/java/com/example/ui/screens/ScanScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'var deductReasonStr by remember { mutableStateOf("") }',
    'var deductReasonStr by remember { mutableStateOf("") }\n                    var invoiceNumberStr by remember { mutableStateOf("") }'
)

new_fields = '''                        OutlinedTextField(
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
                        Spacer(modifier = Modifier.height(16.dp))'''

content = content.replace(
    '''                        OutlinedTextField(
                            value = deductReasonStr,
                            onValueChange = { deductReasonStr = it },
                            label = { Text("Reason for deduction") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))''',
    new_fields
)

content = content.replace(
    '''                            val qty = deductQuantityStr.toFloatOrNull()
                            val reason = deductReasonStr.takeIf { it.isNotBlank() }
                            if (qty != null && qty > 0f && reason != null) {
                                showDeductionPrompt = false
                                stockViewModel.deductStock(baseUid, currentUser!!.id, qty, reason) { success, updatedItem ->''',
    '''                            val qty = deductQuantityStr.toFloatOrNull()
                            val reason = deductReasonStr.takeIf { it.isNotBlank() }
                            val invoice = invoiceNumberStr.takeIf { it.isNotBlank() }
                            if (qty != null && qty > 0f && reason != null && invoice != null) {
                                showDeductionPrompt = false
                                stockViewModel.deductStock(baseUid, currentUser!!.id, qty, reason, invoice) { success, updatedItem ->'''
)

content = content.replace(
    'enabled = deductReasonStr.isNotBlank()',
    'enabled = deductReasonStr.isNotBlank() && invoiceNumberStr.isNotBlank()'
)

content = content.replace(
    'deductReasonStr = ""',
    'deductReasonStr = ""\n                            invoiceNumberStr = ""'
)

with open('app/src/main/java/com/example/ui/screens/ScanScreen.kt', 'w') as f:
    f.write(content)

