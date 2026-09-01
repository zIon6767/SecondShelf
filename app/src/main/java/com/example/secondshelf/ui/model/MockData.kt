package com.example.secondshelf.ui.model

import androidx.compose.ui.graphics.Color

const val CURRENT_USER_ID = "user-1"

val mockUsers = listOf(
    User("user-1", "Chong Ming",      "chongming@student.tarc.edu.my",     "TAR UMT", "KL Main Campus"),
    User("user-2", "Ahmad Faris",     "ahmadfaris@student.tarc.edu.my",    "TAR UMT", "KL Main Campus"),
    User("user-3", "Sarah Lim",       "sarahlim@student.tarc.edu.my",      "TAR UMT", "Penang Branch Campus"),
    User("user-4", "Daniel Kumar",    "danielkumar@student.tarc.edu.my",   "TAR UMT", "KL Main Campus"),
    User("user-5", "Aina Maisarah",   "ainamaisarah@student.tarc.edu.my",  "TAR UMT", "Perak Branch Campus"),
    User("user-6", "Nurul Ain",       "nurulain@student.tarc.edu.my",      "TAR UMT", "Johor Branch Campus"),
)

val initialListings = listOf(

    // ── Books ──────────────────────────────────────────────────────────────────
    Listing(
        id = "listing-1",
        title = "Engineering Calculus Textbook",
        description = "Calculus: Early Transcendentals by James Stewart, 8th Edition. Minimal pencil marks, all pages intact. Perfect for first-year engineering students.",
        price = 40,
        category = Category.BOOKS,
        condition = Condition.LIKE_NEW,
        sellerId = "user-2",
        location = "KL Main Campus",
        createdAt = "2026-08-24",
        cardColor = Color(0xFFE8F0FE),
        imageUrl = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?auto=format&fit=crop&w=600&q=80"
    ),
    Listing(
        id = "listing-2",
        title = "Chemistry Lab Manual (CHM101)",
        description = "Lab manual for CHM101 with full experiment write-ups and results. Great reference for upcoming lab sessions.",
        price = 18,
        category = Category.BOOKS,
        condition = Condition.GOOD,
        sellerId = "user-5",
        location = "Perak Branch Campus",
        createdAt = "2026-08-23",
        cardColor = Color(0xFFE8F0FE),
        imageUrl = "https://images.unsplash.com/photo-1532094349884-543bc11b234d?auto=format&fit=crop&w=600&q=80"
    ),
    Listing(
        id = "listing-3",
        title = "Data Structures & Algorithms",
        description = "Introduction to Algorithms (CLRS), 3rd Edition. Lightly annotated. Must-have for CS and software engineering students.",
        price = 55,
        category = Category.BOOKS,
        condition = Condition.GOOD,
        sellerId = "user-3",
        location = "KL Main Campus",
        createdAt = "2026-08-22",
        cardColor = Color(0xFFE8F0FE),
        imageUrl = "https://images.unsplash.com/photo-1515879218367-8466d910aaa4?auto=format&fit=crop&w=600&q=80"
    ),
    Listing(
        id = "listing-4",
        title = "Principles of Economics",
        description = "Mankiw Economics 8th Edition. No highlights, clean pages. Suitable for FBM students taking ECO basics.",
        price = 35,
        category = Category.BOOKS,
        condition = Condition.LIKE_NEW,
        sellerId = "user-6",
        location = "KL Main Campus",
        createdAt = "2026-08-21",
        cardColor = Color(0xFFE8F0FE),
        imageUrl = "https://images.unsplash.com/photo-1590283603385-17ffb3a7f29f?auto=format&fit=crop&w=600&q=80"
    ),
    Listing(
        id = "listing-5",
        title = "Fluid Mechanics – Frank White",
        description = "Fluid Mechanics 8th Edition by Frank White. A few pages highlighted in yellow. No missing pages.",
        price = 45,
        category = Category.BOOKS,
        condition = Condition.FAIR,
        sellerId = "user-4",
        location = "KL Main Campus",
        createdAt = "2026-08-20",
        cardColor = Color(0xFFE8F0FE),
        imageUrl = "https://images.unsplash.com/photo-1589829085413-56de8ae18c73?auto=format&fit=crop&w=600&q=80"
    ),

    // ── Electronics ───────────────────────────────────────────────────────────
    Listing(
        id = "listing-6",
        title = "Casio fx-991EX Calculator",
        description = "Casio scientific calculator, fully functional with original hard cover. Works perfectly for exams.",
        price = 45,
        category = Category.ELECTRONICS,
        condition = Condition.GOOD,
        sellerId = "user-3",
        location = "KL Main Campus",
        createdAt = "2026-08-23",
        cardColor = Color(0xFFFFF3E0),
        imageUrl = "https://images.unsplash.com/photo-1587145820266-a5951ee6f620?auto=format&fit=crop&w=600&q=80"
    ),
    Listing(
        id = "listing-7",
        title = "USB-C Hub 7-in-1",
        description = "Multi-port USB-C hub with HDMI 4K, 3x USB-A, SD card slot, and 100W PD charging. Brand: Ugreen.",
        price = 55,
        category = Category.ELECTRONICS,
        condition = Condition.LIKE_NEW,
        sellerId = CURRENT_USER_ID,
        location = "KL Main Campus",
        createdAt = "2026-08-20",
        cardColor = Color(0xFFFFF3E0),
        imageUrl = "https://images.unsplash.com/photo-1625842268584-8f3296236761?auto=format&fit=crop&w=600&q=80"
    ),
    Listing(
        id = "listing-8",
        title = "Mechanical Keyboard (TKL)",
        description = "Tenkeyless mechanical keyboard with Gateron Brown switches and RGB backlighting. Includes USB-C braided cable.",
        price = 95,
        category = Category.ELECTRONICS,
        condition = Condition.LIKE_NEW,
        sellerId = "user-4",
        location = "KL Main Campus",
        createdAt = "2026-08-19",
        cardColor = Color(0xFFFFF3E0),
        imageUrl = "https://images.unsplash.com/photo-1618384887929-16ec33fab9ef?auto=format&fit=crop&w=600&q=80"
    ),
    Listing(
        id = "listing-9",
        title = "Wireless Earbuds (Anker Q20)",
        description = "Anker Soundcore Q20 wireless earbuds with ANC. Battery life 40 hours. Selling due to upgrade.",
        price = 60,
        category = Category.ELECTRONICS,
        condition = Condition.GOOD,
        sellerId = "user-6",
        location = "KL Main Campus",
        createdAt = "2026-08-18",
        cardColor = Color(0xFFFFF3E0),
        imageUrl = "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?auto=format&fit=crop&w=600&q=80"
    ),

    // ── Stationery ────────────────────────────────────────────────────────────
    Listing(
        id = "listing-10",
        title = "A4 Grid Notebook Pack (5 pcs)",
        description = "Set of 5 A4 grid-ruled notebooks, 80gsm paper, 200 pages each. All unused and sealed.",
        price = 18,
        category = Category.STATIONERY,
        condition = Condition.LIKE_NEW,
        sellerId = "user-3",
        location = "KL Main Campus",
        createdAt = "2026-08-17",
        cardColor = Color(0xFFFCE4EC),
        imageUrl = "https://images.unsplash.com/photo-1531346878377-a5be20888e57?auto=format&fit=crop&w=600&q=80"
    ),
    Listing(
        id = "listing-11",
        title = "Drawing Set (Staedtler)",
        description = "Staedtler technical drawing set with compass, rulers, protractor and pencils. Used for one semester only.",
        price = 25,
        category = Category.STATIONERY,
        condition = Condition.GOOD,
        sellerId = "user-2",
        location = "KL Main Campus",
        createdAt = "2026-08-16",
        cardColor = Color(0xFFFCE4EC),
        imageUrl = "https://images.unsplash.com/photo-1585776245991-cf89dd7fc73a?auto=format&fit=crop&w=600&q=80"
    ),

    // ── Lab Equipment ─────────────────────────────────────────────────────────
    Listing(
        id = "listing-12",
        title = "Physics Lab Kit",
        description = "Basic physics lab kit including a digital multimeter, hook-up wires, alligator clips and a breadboard.",
        price = 50,
        category = Category.LAB_EQUIPMENT,
        condition = Condition.GOOD,
        sellerId = "user-5",
        location = "Perak Branch Campus",
        createdAt = "2026-08-15",
        cardColor = Color(0xFFF3E8FF),
        imageUrl = "https://images.unsplash.com/photo-1581092160607-ee22621dd758?auto=format&fit=crop&w=600&q=80"
    ),
    Listing(
        id = "listing-13",
        title = "Digital Multimeter",
        description = "Uni-T UT61E+ digital multimeter with True RMS. Ideal for electrical engineering lab work.",
        price = 70,
        category = Category.LAB_EQUIPMENT,
        condition = Condition.LIKE_NEW,
        sellerId = "user-4",
        location = "KL Main Campus",
        createdAt = "2026-08-14",
        cardColor = Color(0xFFF3E8FF),
        imageUrl = "https://images.unsplash.com/photo-1581092335397-9583fe92d232?auto=format&fit=crop&w=600&q=80"
    ),

    // ── Clothing ──────────────────────────────────────────────────────────────
    Listing(
        id = "listing-14",
        title = "UTM Hoodie – Size M (Navy)",
        description = "Official UTM printed hoodie, navy blue, size M. Worn only twice. Still in great shape.",
        price = 48,
        category = Category.CLOTHING,
        condition = Condition.LIKE_NEW,
        sellerId = "user-6",
        location = "KL Main Campus",
        createdAt = "2026-08-13",
        cardColor = Color(0xFFE0F2FE),
        imageUrl = "https://images.unsplash.com/photo-1556905055-8f358a7a47b2?auto=format&fit=crop&w=600&q=80"
    ),
    Listing(
        id = "listing-15",
        title = "Lab Coat – Size S/M",
        description = "White lab coat suitable for chemistry and biology labs. Washed and clean, no stains.",
        price = 20,
        category = Category.CLOTHING,
        condition = Condition.GOOD,
        sellerId = "user-5",
        location = "Perak Branch Campus",
        createdAt = "2026-08-12",
        cardColor = Color(0xFFE0F2FE),
        imageUrl = "https://images.unsplash.com/photo-1584467735815-f778f274e296?auto=format&fit=crop&w=600&q=80"
    ),

    // ── Furniture ─────────────────────────────────────────────────────────────
    Listing(
        id = "listing-16",
        title = "Adjustable LED Desk Lamp",
        description = "LED desk lamp with 5 brightness levels and warm/cool colour modes. USB charging port on base.",
        price = 35,
        category = Category.FURNITURE,
        condition = Condition.GOOD,
        sellerId = "user-2",
        location = "KL Main Campus",
        createdAt = "2026-08-11",
        cardColor = Color(0xFFFFF9C4),
        imageUrl = "https://images.unsplash.com/photo-1534353436294-0dbd4bdac845?auto=format&fit=crop&w=600&q=80"
    ),
    Listing(
        id = "listing-17",
        title = "Hostel Mini Fridge (70L)",
        description = "Sharp 70L single-door mini fridge. Works perfectly, very quiet. Moving out so must sell fast.",
        price = 130,
        category = Category.FURNITURE,
        condition = Condition.GOOD,
        sellerId = "user-4",
        location = "KL Main Campus",
        createdAt = "2026-08-10",
        cardColor = Color(0xFFFFF9C4),
        imageUrl = "https://images.unsplash.com/photo-1584992236310-6edddc08acff?auto=format&fit=crop&w=600&q=80"
    ),
    Listing(
        id = "listing-18",
        title = "Foldable Study Chair",
        description = "Lightweight foldable chair with padded seat. Easy to store under the bed. Great for hostel rooms.",
        price = 40,
        category = Category.FURNITURE,
        condition = Condition.FAIR,
        sellerId = "user-3",
        location = "KL Main Campus",
        createdAt = "2026-08-09",
        cardColor = Color(0xFFFFF9C4),
        imageUrl = "https://images.unsplash.com/photo-1580481077197-9f2997109594?auto=format&fit=crop&w=600&q=80"
    ),
)

val initialNotifications = listOf(
    AppNotification("notif-1", CURRENT_USER_ID, NotificationType.SAVED,          "Listing Saved",     "Ahmad Faris saved your USB-C Hub listing",              "3 min ago",   read = false),
    AppNotification("notif-2", CURRENT_USER_ID, NotificationType.MESSAGE,        "New Message",       "Sarah Lim: \"Is the USB-C hub still available?\"",      "20 min ago",  read = false),
    AppNotification("notif-3", CURRENT_USER_ID, NotificationType.INTEREST,       "Listing Interest",  "Your USB-C Hub listing got 5 new views today",          "1 hour ago",  read = false),
    AppNotification("notif-4", CURRENT_USER_ID, NotificationType.DEAL,           "Campus Deals",      "New textbooks posted near you — up to 40% cheaper",     "Yesterday",   read = true),
    AppNotification("notif-5", CURRENT_USER_ID, NotificationType.LISTING_POSTED, "Listing Posted",    "Your USB-C Hub 7-in-1 was successfully listed!",        "3 days ago",  read = true),
)

val initialMessages = listOf(
    Message("msg-1", "user-3", CURRENT_USER_ID, "listing-7", "Hi! Is the USB-C hub still available?",          "2026-08-24T08:10:00Z", read = false),
    Message("msg-2", CURRENT_USER_ID, "user-3", "listing-7", "Yes it is! You can collect from my hostel block.",  "2026-08-24T08:15:00Z", read = true),
    Message("msg-3", "user-3", CURRENT_USER_ID, "listing-7", "Great! Can we meet tomorrow afternoon?",           "2026-08-24T08:20:00Z", read = false),
    Message("msg-4", "user-2", CURRENT_USER_ID, "listing-7", "Would you take RM 48 for the hub?",               "2026-08-23T14:00:00Z", read = true),
    Message("msg-5", CURRENT_USER_ID, "user-2", "listing-7", "Sorry, RM 55 is the lowest I can go.",            "2026-08-23T14:30:00Z", read = true),
)

val initialConversations = listOf(
    Conversation("conv-1", "listing-7", "user-3", "Great! Can we meet tomorrow afternoon?", "2026-08-24T08:20:00Z", unreadCount = 2),
    Conversation("conv-2", "listing-7", "user-2", "Sorry, RM 55 is the lowest I can go.",  "2026-08-23T14:30:00Z", unreadCount = 0),
)

val searchSuggestions = listOf(
    "calculator", "textbook", "keyboard", "lab manual", "notebook",
    "hoodie", "desk lamp", "fridge", "multimeter", "earbuds", "hub"
)

fun getUserById(id: String) = mockUsers.find { it.id == id }

fun getSellerName(sellerId: String) = getUserById(sellerId)?.name ?: "Unknown Seller"
