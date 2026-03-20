package com.pixel.countdown

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.*
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class CountdownEvent(val id: Int = (0..1000).random(), val name: String, val date: LocalDate, val isPriority: Boolean = false)

class EventViewModel {
    var events = mutableStateListOf(
        CountdownEvent(name = "出差回家", date = LocalDate.of(2026, 4, 1), isPriority = true),
        CountdownEvent(name = "五一假期", date = LocalDate.of(2026, 5, 1))
    )
    val priorityEvent get() = events.find { it.isPriority } ?: events.firstOrNull()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = EventViewModel()
        setContent {
            val context = LocalContext.current
            // 联动安卓 16 智能取色
            val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (isSystemInDarkTheme()) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            }

            MaterialTheme(colorScheme = colorScheme) {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = {
                        NavigationBar(tonalElevation = 0.dp) {
                            NavigationBarItem(icon = { Icon(Icons.Default.Timer, "倒数") }, label = { Text("倒数") }, selected = true, onClick = { navController.navigate("home") })
                            NavigationBarItem(icon = { Icon(Icons.Default.Settings, "设置") }, label = { Text("设置") }, selected = false, onClick = { navController.navigate("settings") })
                        }
                    }
                ) { innerPadding ->
                    NavHost(navController, "home", Modifier.padding(innerPadding)) {
                        composable("home") { HomeScreen(viewModel) }
                        composable("settings") { SettingsScreen(viewModel) }
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(vm: EventViewModel) {
    val event = vm.priorityEvent ?: return
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("距 ${event.name} 还有", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(20.dp))
            // 像素风加粗超大数字
            Text("${ChronoUnit.DAYS.between(LocalDate.now(), event.date)}", 
                fontSize = 140.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            Text("天", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun SettingsScreen(vm: EventViewModel) {
    Column(Modifier.padding(24.dp)) {
        Text("我的倒数事件", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Text("点击切换首页展示的事件", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Spacer(Modifier.height(20.dp))
        LazyColumn {
            items(vm.events) { event ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = if(event.isPriority) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { 
                        val newList = vm.events.map { it.copy(isPriority = it.id == event.id) }
                        vm.events.clear(); vm.events.addAll(newList)
                    }
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(event.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("日期: ${event.date}", style = MaterialTheme.typography.bodySmall)
                        }
                        if (event.isPriority) Icon(Icons.Default.CheckCircle, "优先")
                    }
                }
            }
        }
    }
}
