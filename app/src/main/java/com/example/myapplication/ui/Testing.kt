package com.example.myapplication.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Preview
@Composable
fun TestingScreen(
    onNextClick: () -> Unit = {}
){
    // variable to remember users name
    var name by remember {  mutableStateOf("")}


    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextScreen(name)
        TextFieldScreen(name, ({
            name = it
        }))
        NextButton {
            onNextClick()
        }
    }
}

@Composable
fun TextScreen(name: String){
    Text("Hello $name",
        style = MaterialTheme.typography.headlineMedium)
}

@Composable
fun TextFieldScreen(name: String, onName: (String) -> Unit){
    OutlinedTextField(
        value = name,
        onValueChange = {
            onName(it)
        },
        label = { Text("Name") }
    )
}

@Composable
fun NextButton(
    onClick: () -> Unit = {}
){
    Button(
        onClick = onClick
    ) {
        Text("Next")
    }
}
