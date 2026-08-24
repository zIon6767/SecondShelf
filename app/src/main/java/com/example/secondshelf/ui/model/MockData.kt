package com.example.secondshelf.ui.model

import androidx.compose.ui.graphics.Color

const val CURRENT_USER_ID = "user-1"

val mockUsers = listOf(
    User("user-1", "Alex Johnson", "alex.johnson@university.edu.my", "University of Technology Malaysia", "Skudai Campus"),
    User("user-2", "Ahmad R.", "ahmad.r@student.edu.my", "University of Technology Malaysia", "Skudai Campus"),
    User("user-3", "Sarah L.", "sarah.l@student.edu.my", "University of Technology Malaysia", "KL Campus"),
    User("user-4", "Daniel K.", "daniel.k@student.edu.my", "University of Technology Malaysia", "Skudai Campus"),
    User("user-5", "Aina M.", "aina.m@student.edu.my", "University of Technology Malaysia", "Pagoh Campus"),
)

val initialListings = listOf(
    Listing("listing-1", "Engineering Calculus Textbook", "Complete calculus textbook for engineering students. Minimal highlighting, all chapters intact.", 40, Category.BOOKS, Condition.LIKE_NEW, "user-2", "Skudai Campus", "2026-08-20", Color(0xFFE8F0FE)),
    Listing("listing-2", "Scientific Calculator", "Casio fx-991EX scientific calculator. Fully functional with original cover.", 45, Category.ELECTRONICS, Condition.GOOD, "user-3", "KL Campus", "2026-08-19", Color(0xFFFFF3E0)),
    Listing("listing-3", "Mechanical Keyboard", "RGB mechanical keyboard with blue switches. Includes USB-C cable.", 80, Category.ELECTRONICS, Condition.LIKE_NEW, "user-4", "Skudai Campus", "2026-08-18", Color(0xFFE8F0FE)),
    Listing("listing-4", "Chemistry Lab Manual", "Lab manual for CHM101 with completed experiment notes.", 25, Category.BOOKS, Condition.FAIR, "user-5", "Pagoh Campus", "2026-08-17", Color(0xFFE8F5E9)),
    Listing("listing-5", "Physics Lab Equipment Set", "Basic physics lab kit including multimeter probes and wires.", 65, Category.LAB_EQUIPMENT, Condition.GOOD, "user-2", "Skudai Campus", "2026-08-16", Color(0xFFF3E8FF)),
    Listing("listing-6", "Premium Notebook Pack (5 pcs)", "Set of five A4 notebooks with grid paper. Unused.", 18, Category.STATIONERY, Condition.LIKE_NEW, "user-3", "KL Campus", "2026-08-15", Color(0xFFFCE4EC)),
    Listing("listing-7", "University Hoodie (Size M)", "Official university hoodie, navy blue, size medium.", 55, Category.CLOTHING, Condition.LIKE_NEW, "user-4", "Skudai Campus", "2026-08-14", Color(0xFFE0F2FE)),
    Listing("listing-8", "Study Desk Lamp", "Adjustable LED desk lamp with warm/cool light modes.", 35, Category.FURNITURE, Condition.GOOD, "user-5", "Pagoh Campus", "2026-08-13", Color(0xFFFFF9C4)),
    Listing("listing-9", "Data Structures & Algorithms", "Popular DSA textbook in great condition.", 50, Category.BOOKS, Condition.GOOD, "user-2", "Skudai Campus", "2026-08-12", Color(0xFFE8F0FE)),
    Listing("listing-10", "USB-C Hub 7-in-1", "Multi-port USB-C hub with HDMI and SD card reader.", 40, Category.ELECTRONICS, Condition.LIKE_NEW, CURRENT_USER_ID, "Skudai Campus", "2026-08-11", Color(0xFFE8F5E9)),
    Listing("listing-11", "Mini Whiteboard + Markers", "Portable whiteboard for group study with markers.", 22, Category.STATIONERY, Condition.GOOD, "user-3", "KL Campus", "2026-08-10", Color(0xFFFCE4EC)),
    Listing("listing-12", "Dorm Mini Fridge", "Compact fridge ideal for hostel rooms.", 150, Category.FURNITURE, Condition.GOOD, "user-4", "Skudai Campus", "2026-08-09", Color(0xFFFFF3E0)),
)

val initialNotifications = listOf(
    AppNotification("notif-1", CURRENT_USER_ID, NotificationType.SAVED, "Listing Saved", "Someone saved your USB-C Hub 7-in-1 listing", "2 min ago", false),
    AppNotification("notif-2", CURRENT_USER_ID, NotificationType.MESSAGE, "New Message", "Sarah L.: \"Is the calculator still available?\"", "15 min ago", false),
    AppNotification("notif-3", CURRENT_USER_ID, NotificationType.INTEREST, "Listing Interest", "Your USB-C Hub listing received 3 new views today", "1 hour ago", false),
    AppNotification("notif-4", CURRENT_USER_ID, NotificationType.DEAL, "Campus Deals", "New campus deals available — textbooks up to 40% off", "Yesterday", true),
    AppNotification("notif-5", CURRENT_USER_ID, NotificationType.LISTING_POSTED, "Listing Posted", "Your listing was successfully posted!", "3 days ago", true),
)

val initialMessages = listOf(
    Message("msg-1", "user-3", CURRENT_USER_ID, "listing-2", "Hi! Is the calculator still available?", "2026-08-24T07:10:00Z", false),
    Message("msg-2", CURRENT_USER_ID, "user-3", "listing-2", "Yes it is! Meet at the library?", "2026-08-24T07:12:00Z", true),
    Message("msg-3", "user-3", CURRENT_USER_ID, "listing-2", "Perfect, tomorrow at 2pm works for me.", "2026-08-24T07:15:00Z", false),
    Message("msg-4", "user-2", CURRENT_USER_ID, "listing-1", "Would you take RM 35 for the textbook?", "2026-08-23T14:00:00Z", true),
    Message("msg-5", CURRENT_USER_ID, "user-2", "listing-1", "I can do RM 38, lowest I can go.", "2026-08-23T14:30:00Z", true),
)

val initialConversations = listOf(
    Conversation("conv-1", "listing-2", "user-3", "Perfect, tomorrow at 2pm works for me.", "2026-08-24T07:15:00Z", 1),
    Conversation("conv-2", "listing-1", "user-2", "I can do RM 38, lowest I can go.", "2026-08-23T14:30:00Z", 0),
)

val searchSuggestions = listOf("calculator", "textbook", "keyboard", "lab manual", "notebook", "hoodie", "desk lamp")

fun getUserById(id: String) = mockUsers.find { it.id == id }

fun getSellerName(sellerId: String) = getUserById(sellerId)?.name ?: "Unknown Seller"
