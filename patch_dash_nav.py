import re

with open('app/src/main/java/com/example/ui/navigation/AppNavigation.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'onNavigateToScan = { mode -> navController.navigate("scan/$mode") },',
    'onNavigateToScan = { mode -> navController.navigate("scan/$mode") },\n                onNavigateToUsers = { navController.navigate("users") },\n                onNavigateToSearch = { navController.navigate("search") },'
)

with open('app/src/main/java/com/example/ui/navigation/AppNavigation.kt', 'w') as f:
    f.write(content)
