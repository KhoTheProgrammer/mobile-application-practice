package com.example.myapplication

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.myapplication.ui.theme.MyApplicationTheme
import android.content.Intent // For resolving the 'Intent' class



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(modifier = Modifier.fillMaxSize()){
                    Messagecard(msg = Message("Bandwagon", "Welcome to compose tutorials"))
                }
            }
        }
    }
}

data class Message(val author: String, val body: String)

@Composable
fun Messagecard(msg: Message){
    // Add padding around our message
Column {
    Row(modifier = Modifier.padding(all = 15.dp)){
        Image(
            painter = painterResource(R.drawable.khoo),
            contentDescription = "Contact profile picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = msg.author,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall)
            // Add a vertical space between the author and message texts
            Spacer(modifier = Modifier.height(4.dp))
            Surface(shape = MaterialTheme.shapes.medium, shadowElevation = 1.dp) {
                Text(text = msg.body,
                    modifier = Modifier.padding(all = 4.dp))
            }
            // In another to aActivity, e.g., inside a button's onClick in MainActivity
            val context = LocalContext.current
            Button(onClick = {
                context.startActivity(Intent(context, LoginActivity::class.java))
            }) {
                Text("Go to Login")
            }

        }

    }
}
}

@Preview
@Preview(name = "Light mode")
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES,
    showBackground = true,
    name = "Dark mode")
@Composable
fun PreviewMessageCard(){
    Messagecard(msg = Message("Bandwagon", "Welcome to compose tutorials"))
}

//@Composable
//fun LoginScreen(paddingValues: PaddingValues){
//    var email by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var passwordVisible by remember { mutableStateOf(false) }
//
//    var emailError by remember { mutableStateOf(false) }
//    var passwordError by remember { mutableStateOf(false) }
//
//    Column { modifier = Modifier.fillMaxSize().padding(paddingValues)horizontalAlignment = Alignment.CenterHorizontally
//    }
//}






