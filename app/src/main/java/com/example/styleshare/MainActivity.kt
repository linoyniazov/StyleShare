package com.example.styleshare

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.styleshare.ui.theme.StyleShareTheme
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    private val db: FirebaseFirestore by lazy { Firebase.firestore } // Firestore Instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            StyleShareTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FirestoreTestScreen(
                        db = db,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun FirestoreTestScreen(db: FirebaseFirestore, modifier: Modifier = Modifier) {
    var message by remember { mutableStateOf("Fetching Firestore data...") }

    LaunchedEffect(Unit) {
        val testUser = hashMapOf(
            "name" to "John Doe",
            "email" to "john@example.com"
        )

        db.collection("users").document("testUser")
            .set(testUser)
            .addOnSuccessListener {
                message = "Firestore Connected: User added!"
                Log.d("Firestore", "User added successfully!")
            }
            .addOnFailureListener { e ->
                message = "Error connecting to Firestore"
                Log.e("Firestore", "Error adding user", e)
            }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.headlineMedium)
    }
}

@Preview(showBackground = true)
@Composable
fun FirestoreTestScreenPreview() {
    StyleShareTheme {
        FirestoreTestScreen(Firebase.firestore)
    }
}