import re

with open('app/src/main/java/com/example/ui/navigation/AppNavigation.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'import com.example.ui.screens.CategoryStockScreen',
    'import com.example.ui.screens.CategoryStockScreen\nimport com.example.ui.screens.UsersScreen\nimport com.example.ui.screens.SearchScreen'
)

new_routes = """
        composable("users") {
            UsersScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("search") {
            SearchScreen(
                stockViewModel = stockViewModel,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
"""

content = re.sub(r'\s*\}\s*\}\s*$', new_routes, content)

with open('app/src/main/java/com/example/ui/navigation/AppNavigation.kt', 'w') as f:
    f.write(content)
