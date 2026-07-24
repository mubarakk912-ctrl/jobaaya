package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.viewmodel.JobaayaViewModel
import com.example.ui.localization.JobaayaLocalization
import com.example.data.model.Product
import androidx.core.net.toUri
import java.io.File
import java.util.UUID

@Composable
fun MarketplaceScreen(
    viewModel: JobaayaViewModel,
    onStartChat: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentLang by viewModel.currentLanguage.collectAsState()
    val myProfile by viewModel.myProfile.collectAsState()
    val deviceLocation by viewModel.deviceLocation.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showAddProductDialog by remember { mutableStateOf(false) }
    var showStorageWarning by remember { mutableStateOf(false) }

    val userCity = myProfile?.fullAddress?.split(",")?.lastOrNull()?.trim() ?: ""
    val categories = listOf("All", "Electronics", "Fashion", "Home", "Tools", "Services", "Books")

    // Products list from ViewModel
    val productsList by (if (searchQuery.isNotBlank()) {
        viewModel.searchProducts(searchQuery)
    } else {
        remember { kotlinx.coroutines.flow.flowOf(emptyList<Product>()) }
    }).collectAsState(initial = emptyList())

    // Priority Location Logic: 1. Device GPS, 2. Profile Address, 3. Fallback Delhi
    val myLat = deviceLocation?.latitude ?: myProfile?.latitude ?: 28.6139
    val myLon = deviceLocation?.longitude ?: myProfile?.longitude ?: 77.2090

    // Filter and Sort based on city and distance
    val filteredAndSortedProducts = remember(productsList, userCity, selectedCategory, myLat, myLon) {
        productsList.filter { 
            selectedCategory == "All" || it.category == selectedCategory
        }.sortedWith(
            compareByDescending<Product> { 
                it.sellerLocation.contains(userCity, ignoreCase = true)
            }.thenBy { product ->
                if (product.latitude != 0.0 && product.longitude != 0.0) {
                    viewModel.calculateDistanceKm(myLat, myLon, product.latitude, product.longitude)
                } else {
                    9999.0
                }
            }
        )
    }

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF0D131D))) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Text(
                text = "Marketplace",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, start = 16.dp, bottom = 16.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search products (City prioritized)...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                trailingIcon = { 
                    IconButton(onClick = { Toast.makeText(context, "Filter feature coming soon!", Toast.LENGTH_SHORT).show() }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color(0xFF22C55E))
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF22C55E),
                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF16202E),
                    unfocusedContainerColor = Color(0xFF16202E)
                ),
                singleLine = true
            )

            if (searchQuery.isBlank()) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Storefront, null, modifier = Modifier.size(64.dp), tint = Color.Gray.copy(alpha = 0.3f))
                        Spacer(Modifier.height(16.dp))
                        Text("Search for products in your city", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    // Categories
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { category ->
                                val isSelected = selectedCategory == category
                                Surface(
                                    color = if (isSelected) Color(0xFF22C55E) else Color(0xFF16202E),
                                    shape = RoundedCornerShape(20.dp),
                                    modifier = Modifier.clickable { selectedCategory = category }
                                ) {
                                    Text(
                                        text = category,
                                        color = if (isSelected) Color.Black else Color.White,
                                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    if (filteredAndSortedProducts.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                                Text("No products found for '$searchQuery'", color = Color.Gray)
                            }
                        }
                    } else {
                        items(filteredAndSortedProducts, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                viewModel = viewModel,
                                myLat = myLat,
                                myLon = myLon,
                                onChatClick = { onStartChat(product.sellerId) },
                                onCallClick = {
                                    try {
                                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                            data = "tel:${product.sellerPhone}".toUri()
                                        }
                                        context.startActivity(dialIntent)
                                    } catch (_: Exception) { }
                                },
                                onRatingChange = { newRating ->
                                    val totalPoints = product.rating * product.reviewsCount
                                    val newReviewsCount = product.reviewsCount + 1
                                    val newAvgRating = (totalPoints + newRating) / newReviewsCount
                                    viewModel.updateProduct(product.copy(rating = newAvgRating, reviewsCount = newReviewsCount))
                                }
                            )
                        }
                    }
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { 
                if (myProfile == null || myProfile?.name == "Guest User") {
                    Toast.makeText(context, "Please create a profile first to add products!", Toast.LENGTH_LONG).show()
                } else {
                    showStorageWarning = true 
                }
            },
            containerColor = Color(0xFF22C55E),
            contentColor = Color.Black,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Product", fontWeight = FontWeight.Bold)
            }
        }

        if (showStorageWarning) {
            AlertDialog(
                onDismissRequest = { showStorageWarning = false },
                title = { Text("Local Storage Warning") },
                text = { Text("यह प्रोडक्ट डेटा आपके मोबाइल में सुरक्षित रहेगा। यदि आप गैलरी या ऐप डेटा साफ़ करते हैं, तो प्रोडक्ट की जानकारी भी हट सकती है।") },
                confirmButton = {
                    Button(onClick = { 
                        showStorageWarning = false
                        showAddProductDialog = true 
                    }) {
                        Text("I Understand")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showStorageWarning = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (showAddProductDialog) {
            AddProductDialog(
                categories = categories.filter { it != "All" },
                onDismiss = { showAddProductDialog = false },
                onAdd = { name, price, category, imageUri ->
                    val localImagePath = if (imageUri != null) viewModel.saveProductImage(imageUri) else ""
                    viewModel.addProduct(Product(
                        name = name,
                        price = if (price.startsWith("₹")) price else "₹$price",
                        imageUrl = localImagePath,
                        sellerName = myProfile?.name ?: "Unknown",
                        sellerLocation = myProfile?.fullAddress?.split(",")?.lastOrNull()?.trim() ?: "Unknown",
                        sellerId = myProfile?.id ?: "",
                        sellerPhone = myProfile?.mobileNumber ?: "",
                        category = category,
                        latitude = myProfile?.latitude ?: 0.0,
                        longitude = myProfile?.longitude ?: 0.0
                    ))
                    showAddProductDialog = false
                    Toast.makeText(context, "Product added locally!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

@Composable
fun AddProductDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onAdd: (String, String, String, Uri?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val photoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16202E))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add New Product",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(20.dp))

                // Image Picker Box
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0D131D))
                        .clickable { photoLauncher.launch("image/*") }
                        .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedImageUri != null) {
                        AsyncImage(
                            model = selectedImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                            Text("Add Photo", color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF22C55E)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (e.g. ₹500)", color = Color.Gray) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color(0xFF22C55E)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Category", color = Color.Gray, modifier = Modifier.align(Alignment.Start), fontSize = 12.sp)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(categories) { category ->
                        val isSel = selectedCategory == category
                        Surface(
                            modifier = Modifier.clickable { selectedCategory = category },
                            color = if (isSel) Color(0xFF22C55E) else Color(0xFF0D131D),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) Color.Transparent else Color.Gray.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = category,
                                color = if (isSel) Color.Black else Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (name.isNotBlank() && price.isNotBlank()) {
                            onAdd(name, price, selectedCategory, selectedImageUri)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E))
                ) {
                    Text("Post Product", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun ProductCard(
    product: Product,
    viewModel: JobaayaViewModel,
    myLat: Double,
    myLon: Double,
    onChatClick: () -> Unit,
    onCallClick: () -> Unit,
    onRatingChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val distanceText = remember(product, myLat, myLon) {
        if (myLat != 0.0 && myLon != 0.0 && product.latitude != 0.0 && product.longitude != 0.0) {
            val dist = viewModel.calculateDistanceKm(myLat, myLon, product.latitude, product.longitude)
            if (dist < 1.0) {
                "${(dist * 1000).toInt()} mtr away"
            } else {
                String.format(java.util.Locale.getDefault(), "%.1f km away", dist)
            }
        } else {
            ""
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF16202E)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Product Image Section
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNotEmpty()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(File(product.imageUrl))
                            .crossfade(true)
                            .build(),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(Icons.Default.Image, null, tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(44.dp))
                }
                
                Surface(
                    color = Color(0xFF22C55E),
                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text("HOT", color = Color.Black, fontSize = 8.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Info Section
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = product.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (distanceText.isNotEmpty()) {
                        Text(
                            text = distanceText,
                            color = Color(0xFF22C55E),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = product.price, color = Color(0xFF22C55E), fontSize = 17.sp, fontWeight = FontWeight.Black)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.Default.LocationOn, null, tint = Color.Gray, modifier = Modifier.size(12.dp))
                    Text(text = product.sellerLocation, color = Color.Gray, fontSize = 11.sp)
                }

                // Interactive Rating
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        val starRating = index + 1
                        Icon(
                            imageVector = if (product.rating >= starRating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = if (product.rating >= starRating) Color(0xFFFFB300) else Color.Gray,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onRatingChange(starRating) }
                        )
                    }
                    Text(
                        text = " (${product.reviewsCount})",
                        color = Color.Gray,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                // Action Buttons: Chat & Call
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onChatClick,
                        modifier = Modifier.height(36.dp).weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B3A51)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, null, modifier = Modifier.size(14.dp), tint = Color.White)
                        Spacer(Modifier.width(6.dp))
                        Text("Chat", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = onCallClick,
                        modifier = Modifier.height(36.dp).weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF22C55E)),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Icon(Icons.Default.Call, null, modifier = Modifier.size(14.dp), tint = Color.Black)
                        Spacer(Modifier.width(6.dp))
                        Text("Call", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
