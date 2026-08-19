import re

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    content = f.read()

content = content.replace(
    '''                    if (log.reason != null) {
                        Text("Reason: ${log.reason}", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }''',
    '''                    if (log.invoiceNumber != null) {
                        Text("Invoice: ${log.invoiceNumber}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                    }
                    if (log.reason != null) {
                        Text("Reason: ${log.reason}", fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }'''
)

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'w') as f:
    f.write(content)

