import re

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'r') as f:
    content = f.read()

content = re.sub(
    r'val availableTabs = if \(isSuperAdmin\) tabs else tabs\.take\(2\)\s+availableTabs\.forEachIndexed \{ index, title ->',
    r'tabs.forEachIndexed { index, title ->',
    content
)
content = re.sub(r'2 -> if \(isSuperAdmin\) UsersTab\(authViewModel\)', r'', content)

with open('app/src/main/java/com/example/ui/screens/AdminScreen.kt', 'w') as f:
    f.write(content)
