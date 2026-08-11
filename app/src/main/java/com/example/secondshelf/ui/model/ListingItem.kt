package com.example.secondshelf.ui.model

import androidx.compose.ui.graphics.Color

data class ListingItem(
    val id: Int,
    val title: String,
    val price: String,
    val category: String,
    val condition: String,
    val seller: String,
    val cardColor: Color = Color(0xFFE8F0FE)
)

val mockListings = listOf(
    ListingItem(1, "Engineering Calculus Textbook", "RM 40", "Books", "Like New", "Ahmad R.", Color(0xFFE8F0FE)),
    ListingItem(2, "Scientific Calculator", "RM 45", "Electronics", "Good", "Sarah L.", Color(0xFFFFF3E0)),
    ListingItem(3, "Mechanical Keyboard", "RM 120", "Electronics", "Like New", "James T.", Color(0xFFE8F0FE)),
    ListingItem(4, "Chemistry Lab Manual", "RM 15", "Books", "Fair", "Priya K.", Color(0xFFE8F5E9)),
    ListingItem(5, "Laptop Stand", "RM 55", "Accessories", "New", "Wei Ming", Color(0xFFFCE4EC)),
    ListingItem(6, "Badminton Racket", "RM 80", "Sports", "Good", "Fatimah Z.", Color(0xFFE8F0FE)),
    ListingItem(7, "Data Structures Book", "RM 30", "Books", "Good", "Kevin L.", Color(0xFFFFF3E0)),
    ListingItem(8, "USB-C Hub 7-in-1", "RM 40", "Electronics", "Like New", "Ravi S.", Color(0xFFE8F5E9)),
    ListingItem(9, "Dorm Mini Fridge", "RM 150", "Appliances", "Good", "Mei Lin", Color(0xFFFCE4EC)),
    ListingItem(10, "Physics Textbook", "RM 20", "Books", "Fair", "Amir K.", Color(0xFFE8F0FE)),
)

val mockMyListings = listOf(
    ListingItem(1, "Engineering Calculus Textbook", "RM 35", "Books", "Like New", "Me", Color(0xFFE8F0FE)),
    ListingItem(2, "Laptop Stand", "RM 55", "Accessories", "New", "Me", Color(0xFFFCE4EC)),
    ListingItem(3, "USB-C Hub 7-in-1", "RM 40", "Electronics", "Like New", "Me", Color(0xFFE8F5E9)),
    ListingItem(4, "Chemistry Lab Manual", "RM 15", "Books", "Fair", "Me", Color(0xFFFFF3E0)),
)
