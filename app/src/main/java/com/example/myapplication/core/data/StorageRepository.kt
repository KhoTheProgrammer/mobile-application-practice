package com.example.myapplication.core.data

import android.content.Context
import android.net.Uri
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import io.github.jan.supabase.storage.FileObject
import java.util.UUID

sealed class StorageResult<out T> {
   data class Success<T>(val data: T) : StorageResult<T>()
   data class Error(val message: String) : StorageResult<Nothing>()
}

class StorageRepository {
   private val client = SupabaseClient.client
   private val storage: Storage by lazy { client.storage }

   companion object {
       const val BUCKET_NAME = "donation-photos"
   }

   /**
    * Upload an image to Supabase Storage
    * @param context Android context for accessing content resolver
    * @param imageUri URI of the image to upload
    * @param donationId ID of the donation (used for organizing files)
    * @return URL of the uploaded image or error
    */
   suspend fun uploadDonationImage(
       context: Context,
       imageUri: Uri,
       donationId: String
   ): StorageResult<String> {
       return try {
           // Generate unique filename
           val fileName = "${donationId}_${UUID.randomUUID()}.jpg"
           val filePath = "donations/$donationId/$fileName"

           // Read image bytes from URI
           val imageBytes = context.contentResolver.openInputStream(imageUri)?.use { inputStream ->
               inputStream.readBytes()
           } ?: return StorageResult.Error("Failed to read image")

           // Upload to Supabase Storage
           storage.from(BUCKET_NAME).upload(filePath, imageBytes)

           // Get public URL
           val publicUrl = storage.from(BUCKET_NAME).publicUrl(filePath)

           StorageResult.Success(publicUrl)
       } catch (e: Exception) {
           StorageResult.Error(e.message ?: "Failed to upload image")
       }
   }

   /**
    * Upload multiple images for a donation
    */
   suspend fun uploadDonationImages(
       context: Context,
       imageUris: List<Uri>,
       donationId: String
   ): StorageResult<List<String>> {
       return try {
           val uploadedUrls = mutableListOf<String>()

           for (uri in imageUris) {
               when (val result = uploadDonationImage(context, uri, donationId)) {
                   is StorageResult.Success -> uploadedUrls.add(result.data)
                   is StorageResult.Error -> return StorageResult.Error(result.message)
               }
           }

           StorageResult.Success(uploadedUrls)
       } catch (e: Exception) {
           StorageResult.Error(e.message ?: "Failed to upload images")
       }
   }

   /**
    * Delete an image from storage
    */
   suspend fun deleteDonationImage(imageUrl: String): StorageResult<Unit> {
       return try {
           // Extract file path from URL
           val filePath = imageUrl.substringAfter("$BUCKET_NAME/")

           storage.from(BUCKET_NAME).delete(listOf(filePath))

           StorageResult.Success(Unit)
       } catch (e: Exception) {
           StorageResult.Error(e.message ?: "Failed to delete image")
       }
   }

   /**
    * Delete all images for a donation
    */
   suspend fun deleteDonationImages(donationId: String): StorageResult<Unit> {
       return try {
           val folderPath = "donations/$donationId"

           // List all files in the folder
           val files = storage.from(BUCKET_NAME).list(folderPath)

           // Delete each file
           val filePaths = files.map { "$folderPath/${it.name}" }
           storage.from(BUCKET_NAME).delete(filePaths)

           StorageResult.Success(Unit)
       } catch (e: Exception) {
           StorageResult.Error(e.message ?: "Failed to delete images")
       }
   }
}
