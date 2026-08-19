import re

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'r') as f:
    content = f.read()

# Update signature
content = content.replace(
    'onNavigateToScan: (String) -> Unit,\n    onLogout: () -> Unit',
    'onNavigateToScan: (String) -> Unit,\n    onNavigateToUsers: () -> Unit,\n    onNavigateToSearch: () -> Unit,\n    onLogout: () -> Unit'
)

# Update icon buttons
content = content.replace(
    'Row {\n                    IconButton(onClick = { }) {\n                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground)\n                    }\n                    IconButton(onClick = { }) {\n                        Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onBackground)\n                    }\n                }',
    '''Row {
                    IconButton(onClick = onNavigateToSearch) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onBackground)
                    }
                    if (currentUser?.role == Role.SUPER_ADMIN) {
                        IconButton(onClick = onNavigateToUsers) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile", tint = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }'''
)

with open('app/src/main/java/com/example/ui/screens/DashboardScreen.kt', 'w') as f:
    f.write(content)
