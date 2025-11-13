package com.example.myapplication.ui.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.BuildConfig
import com.example.myapplication.data.remote.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.launch

data class TestResult(
    val name: String,
    val status: TestStatus,
    val message: String,
    val details: String = ""
)

enum class TestStatus {
    PENDING, SUCCESS, FAILED, RUNNING
}

class SupabaseTestViewModel : ViewModel() {
    var testResults by mutableStateOf<List<TestResult>>(emptyList())
        private set
    
    var isTestingInProgress by mutableStateOf(false)
        private set

    fun runTests() {
        isTestingInProgress = true
        testResults = listOf(
            TestResult("Configuration", TestStatus.RUNNING, "Checking..."),
            TestResult("Connection", TestStatus.PENDING, "Waiting..."),
            TestResult("Database", TestStatus.PENDING, "Waiting...")
        )

        viewModelScope.launch {
            // Test 1: Configuration
            val configTest = testConfiguration()
            updateTestResult(0, configTest)
            
            if (configTest.status == TestStatus.SUCCESS) {
                // Test 2: Connection
                val connectionTest = testConnection()
                updateTestResult(1, connectionTest)
                
                if (connectionTest.status == TestStatus.SUCCESS) {
                    // Test 3: Database Query
                    val dbTest = testDatabaseQuery()
                    updateTestResult(2, dbTest)
                }
            }
            
            isTestingInProgress = false
        }
    }

    private fun updateTestResult(index: Int, result: TestResult) {
        testResults = testResults.toMutableList().apply {
            this[index] = result
        }
    }

    private fun testConfiguration(): TestResult {
        return try {
            val url = BuildConfig.SUPABASE_URL
            val key = BuildConfig.SUPABASE_KEY
            
            if (url.isEmpty() || key.isEmpty()) {
                TestResult(
                    "Configuration",
                    TestStatus.FAILED,
                    "Missing credentials",
                    "Check local.properties file"
                )
            } else if (!url.startsWith("https://")) {
                TestResult(
                    "Configuration",
                    TestStatus.FAILED,
                    "Invalid URL format",
                    "URL should start with https://"
                )
            } else {
                TestResult(
                    "Configuration",
                    TestStatus.SUCCESS,
                    "Credentials loaded",
                    "URL: ${url.take(30)}..."
                )
            }
        } catch (e: Exception) {
            TestResult(
                "Configuration",
                TestStatus.FAILED,
                "Error: ${e.message}",
                e.stackTraceToString()
            )
        }
    }

    private suspend fun testConnection(): TestResult {
        return try {
            val client = SupabaseClient.client
            val url = client.supabaseUrl
            
            TestResult(
                "Connection",
                TestStatus.SUCCESS,
                "Client initialized",
                "Connected to: $url"
            )
        } catch (e: Exception) {
            TestResult(
                "Connection",
                TestStatus.FAILED,
                "Connection failed",
                e.message ?: "Unknown error"
            )
        }
    }

    private suspend fun testDatabaseQuery(): TestResult {
        return try {
            // Try to query the categories table
            val response = SupabaseClient.client
                .from("categories")
                .select()
            
            TestResult(
                "Database",
                TestStatus.SUCCESS,
                "Query successful",
                "Categories table accessible"
            )
        } catch (e: Exception) {
            val message = e.message ?: "Unknown error"
            val details = when {
                message.contains("relation") || message.contains("does not exist") -> 
                    "Table not found. Run supabase_schema.sql in your Supabase dashboard"
                message.contains("permission") || message.contains("policy") -> 
                    "Permission denied. Check Row Level Security policies"
                message.contains("network") || message.contains("timeout") -> 
                    "Network error. Check internet connection"
                else -> message
            }
            
            TestResult(
                "Database",
                TestStatus.FAILED,
                "Query failed",
                details
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupabaseTestScreen(
    onBack: () -> Unit = {},
    viewModel: SupabaseTestViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Supabase Connection Test") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Connection Status",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Testing your Supabase configuration and connectivity",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Test Button
            Button(
                onClick = { viewModel.runTests() },
                enabled = !viewModel.isTestingInProgress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (viewModel.isTestingInProgress) "Testing..." else "Run Tests",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Test Results
            if (viewModel.testResults.isNotEmpty()) {
                viewModel.testResults.forEach { result ->
                    TestResultCard(result)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            // Configuration Info
            Spacer(modifier = Modifier.height(24.dp))
            ConfigurationInfoCard()
        }
    }
}

@Composable
fun TestResultCard(result: TestResult) {
    val backgroundColor = when (result.status) {
        TestStatus.SUCCESS -> Color(0xFF4CAF50).copy(alpha = 0.1f)
        TestStatus.FAILED -> Color(0xFFF44336).copy(alpha = 0.1f)
        TestStatus.RUNNING -> Color(0xFF2196F3).copy(alpha = 0.1f)
        TestStatus.PENDING -> Color(0xFF9E9E9E).copy(alpha = 0.1f)
    }

    val iconColor = when (result.status) {
        TestStatus.SUCCESS -> Color(0xFF4CAF50)
        TestStatus.FAILED -> Color(0xFFF44336)
        TestStatus.RUNNING -> Color(0xFF2196F3)
        TestStatus.PENDING -> Color(0xFF9E9E9E)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Status Icon
            when (result.status) {
                TestStatus.SUCCESS -> Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
                TestStatus.FAILED -> Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = "Failed",
                    tint = iconColor,
                    modifier = Modifier.size(32.dp)
                )
                TestStatus.RUNNING -> CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = iconColor,
                    strokeWidth = 3.dp
                )
                TestStatus.PENDING -> CircularProgressIndicator(
                    modifier = Modifier.size(32.dp),
                    color = iconColor.copy(alpha = 0.3f),
                    strokeWidth = 3.dp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Test Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = result.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = result.message,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (result.details.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.details,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
fun ConfigurationInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Configuration",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            
            ConfigItem("Supabase URL", BuildConfig.SUPABASE_URL.take(40) + "...")
            Spacer(modifier = Modifier.height(8.dp))
            ConfigItem("API Key", BuildConfig.SUPABASE_KEY.take(20) + "...")
        }
    }
}

@Composable
fun ConfigItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace
        )
    }
}
