package com.example.myapplication.data.remote

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Simple test class to verify Supabase connection
 * Call testConnection() from your MainActivity or ViewModel to verify setup
 */
object SupabaseTest {
    private const val TAG = "SupabaseTest"
    
    fun testConnection() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Testing Supabase connection...")
                Log.d(TAG, "Supabase URL: ${SupabaseClient.client.supabaseUrl}")
                Log.d(TAG, "✅ Supabase client initialized successfully!")
                
                // You can add a simple query here once your tables are set up
                // Example: val categories = SupabaseClient.client.from("categories").select().decodeList<Category>()
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Supabase connection failed: ${e.message}", e)
            }
        }
    }
}
