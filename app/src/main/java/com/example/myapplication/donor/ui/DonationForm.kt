package com.example.myapplication.donor.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myapplication.core.ui.theme.MyApplicationTheme
import com.example.myapplication.core.ui.components.CustomAppBar
import com.example.myapplication.core.ui.components.AppBarAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationFormScreen(
    onBackClick: () -> Unit = {},
    onSubmitSuccess: () -> Unit = {},
    viewModel: com.example.myapplication.donor.domain.DonationFormViewModel? = null
) {

    // State variables
    var selectedCategory by remember { mutableStateOf("") }
    var selectedSubcategory by remember { mutableStateOf("") }
    var selectedCondition by remember { mutableStateOf("") }
    var itemDescription by remember { mutableStateOf("") }
    var pickupAddress by remember { mutableStateOf("") }
    var willDropOff by remember { mutableStateOf(false) }
    var uploadedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var categoryExpanded by remember { mutableStateOf(false) }
    var subcategoryExpanded by remember { mutableStateOf(false) }
    var conditionExpanded by remember { mutableStateOf(false) }
    
    // Watch for successful donation creation
    LaunchedEffect(viewModel?.uiState?.donationCreated) {
        if (viewModel?.uiState?.donationCreated == true) {
            onSubmitSuccess()
        }
    }
    
    // Show error if any
    LaunchedEffect(viewModel?.uiState?.error) {
        viewModel?.uiState?.error?.let {
            errorMessage = it
            showError = true
        }
    }

    // Image picker
    val pickMedia = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5)
    ) { uris ->
        if (uris.isNotEmpty()) {
            uploadedImages = uris
        }
    }

    // Categories data
    val categories = listOf(
        "Food", "Clothes", "Furniture", "Books", "Toys", "Electronics", "Others"
    )

    val subcategories = mapOf(
        "Food" to listOf("Grains", "Canned Food", "Fresh Produce", "Dairy", "Beverages"),
        "Clothes" to listOf("Men's Clothing", "Women's Clothing", "Children's Clothing", "Shoes", "Accessories"),
        "Furniture" to listOf("Chairs", "Tables", "Beds", "Sofas", "Cabinets"),
        "Books" to listOf("Textbooks", "Story Books", "Educational", "Magazines", "Others"),
        "Toys" to listOf("Educational Toys", "Outdoor Toys", "Board Games", "Stuffed Toys"),
        "Electronics" to listOf("Mobile Phones", "Laptops", "Tablets", "Small Appliances"),
        "Others" to listOf("Medical Supplies", "School Supplies", "Kitchenware", "Sports Equipment")
    )

    val conditions = listOf(
        "Brand New",
        "Like New",
        "Good Condition",
        "Fair Condition",
        "Needs Repair"
    )

    Scaffold(
        topBar = {
            CustomAppBar(
                title = "Donation Form",
                subtitle = "Fill in the details",
                onNavigationClick = onBackClick
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
            // Category Dropdown
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "What do you want to donate?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = !categoryExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                            },
                            placeholder = {
                                Text("Select category")
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            categories.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        selectedSubcategory = "" // Reset subcategory
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Subcategory Dropdown (only show if category is selected and has subcategories)
            if (selectedCategory.isNotEmpty() && subcategories[selectedCategory]?.isNotEmpty() == true) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = "Subcategory",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = subcategoryExpanded,
                            onExpandedChange = { subcategoryExpanded = !subcategoryExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedSubcategory,
                                onValueChange = { },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                readOnly = true,
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = subcategoryExpanded)
                                },
                                placeholder = {
                                    Text("Select subcategory")
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = subcategoryExpanded,
                                onDismissRequest = { subcategoryExpanded = false }
                            ) {
                                subcategories[selectedCategory]?.forEach { subcategory ->
                                    DropdownMenuItem(
                                        text = { Text(subcategory) },
                                        onClick = {
                                            selectedSubcategory = subcategory
                                            subcategoryExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Condition Dropdown
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Condition of Item",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = conditionExpanded,
                        onExpandedChange = { conditionExpanded = !conditionExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedCondition,
                            onValueChange = { },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = conditionExpanded)
                            },
                            placeholder = {
                                Text("Select condition")
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        ExposedDropdownMenu(
                            expanded = conditionExpanded,
                            onDismissRequest = { conditionExpanded = false }
                        ) {
                            conditions.forEach { condition ->
                                DropdownMenuItem(
                                    text = { Text(condition) },
                                    onClick = {
                                        selectedCondition = condition
                                        conditionExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Item Description
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "Item Description",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    OutlinedTextField(
                        value = itemDescription,
                        onValueChange = { itemDescription = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text("Describe the items you're donating...")
                        },
                        minLines = 3,
                        maxLines = 5,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }

            // Photo Upload Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Upload Photos",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(onClick = {
                            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Add Photo",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uploadedImages) { uri ->
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }

            // Pickup/Drop-off Section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        text = "How will you get the items to us?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = willDropOff,
                            onClick = { willDropOff = true },
                            label = { Text("I'll drop it off") },
                            leadingIcon = if (willDropOff) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = "Selected",
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else null
                        )

                        FilterChip(
                            selected = !willDropOff,
                            onClick = { willDropOff = false },
                            label = { Text("Pick it up for me") },
                            leadingIcon = if (!willDropOff) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Done,
                                        contentDescription = "Selected",
                                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                                    )
                                }
                            } else null
                        )
                    }

                    if (!willDropOff) {
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = pickupAddress,
                            onValueChange = { pickupAddress = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text("Enter pickup address")
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Home,
                                    contentDescription = "Address"
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Error message
        if (showError) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Submit Button
        Button(
            onClick = {
                // Validate form
                if (selectedCategory.isEmpty()) {
                    errorMessage = "Please select a category"
                    showError = true
                    return@Button
                }
                if (selectedSubcategory.isEmpty()) {
                    errorMessage = "Please select a subcategory"
                    showError = true
                    return@Button
                }
                if (selectedCondition.isEmpty()) {
                    errorMessage = "Please select item condition"
                    showError = true
                    return@Button
                }
                if (itemDescription.isBlank()) {
                    errorMessage = "Please describe the items you're donating"
                    showError = true
                    return@Button
                }
                if (!willDropOff && pickupAddress.isBlank()) {
                    errorMessage = "Please enter pickup address"
                    showError = true
                    return@Button
                }
                
                showError = false
                
                // If viewModel is provided, use it to submit
                if (viewModel != null) {
                    // Update viewModel with form data
                    viewModel.onItemDescriptionChange("$selectedCategory - $selectedSubcategory: $itemDescription (Condition: $selectedCondition)")
                    viewModel.onQuantityChange("1") // Default quantity
                    viewModel.onNoteChange(if (willDropOff) "Will drop off" else "Pickup at: $pickupAddress")
                    viewModel.onDonationTypeChange(com.example.myapplication.donor.data.DonationType.IN_KIND)
                    viewModel.submitDonation()
                } else {
                    // Fallback: just navigate
                    onSubmitSuccess()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            enabled = viewModel?.uiState?.isLoading != true
        ) {
            if (viewModel?.uiState?.isLoading == true) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text(
                    text = "Submit Donation",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
    }
    
    // Error dialog
    if (showError && errorMessage.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showError = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error"
                )
            },
            title = {
                Text("Validation Error")
            },
            text = {
                Text(errorMessage)
            },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("OK")
                }
            }
        )
    }
}


@Preview(showBackground = true)
@Composable
fun DonationFormScreenPreview() {
    MyApplicationTheme(darkTheme = true) {
        DonationFormScreen()
    }
}
