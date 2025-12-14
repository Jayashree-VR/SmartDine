# 🍽️ SmartDine – Hybrid AI Restaurant Recommendation System

SmartDine is a **hybrid AI-powered restaurant recommendation system** that understands natural language user queries, extracts intent using AI, applies rule-based filtering with database queries, and returns **friendly, human-like responses**.

The project combines **AI (LLM-based understanding)** + **Spring Boot backend** + **SQL database** + **chat-style frontend**, making it scalable, explainable, and production-ready.

---

## 🚀 Key Features

* 🤖 **AI-based Natural Language Understanding**
* 🧠 **Intent → Structured JSON extraction**
* ⚙️ **Rule Engine + SQL filtering**
* 💬 **AI-generated friendly responses**
* 🍕 Supports dish, cuisine, location, budget, body-need & mood-based queries
* 🏪 Restaurant & dish-level recommendations
* 🔁 Handles exact matches and close matches gracefully

---

## 🧩 System Architecture

```
User (Chat UI)
   ↓
POST /api/chat
   ↓
AI Understanding Layer
   ↓
Structured JSON (intent)
   ↓
Rule Engine + DB Queries
   ↓
AI Response Generator
   ↓
User-friendly Reply
```

---

## 🧠 AI Pipeline Design

### 1️⃣ AI Understanding Layer

Endpoint:

```
POST /api/ai/understand
```

**Input:**

```json
{
  "message": "I want spicy biryani under 200 in Gandhipuram"
}
```

**Output (Structured JSON):**

```json
{
  "dishName": "Biryani",
  "cuisine": "South Indian",
  "location": "Gandhipuram",
  "avgPrice": 200,
  "bodyNeed": "energetic"
}
```

This layer uses an LLM to convert free-text into structured fields.

---

### 2️⃣ Rule Engine + Database Layer

Endpoint:

```
POST /api/restaurants/recommend
```

**Responsibilities:**

* Normalize input
* Apply rules (price, location, cuisine, dish)
* Handle:

  * Exact matches
  * Partial matches
  * Close matches

**Tech:**

* Spring Data JPA
* Custom repository queries

---

### 3️⃣ AI Response Generation Layer

Endpoint:

```
POST /api/ai/respond
```

**Purpose:**

* Convert SQL results into friendly, conversational text
* Handle empty or partial results politely

**Example Output:**

```
Here are the best biryani spots near Gandhipuram that match your budget 😋
```

---

### 4️⃣ Unified Chat Flow

Endpoint:

```
POST /api/chat
```

**Flow:**

1. Accept user message
2. Call `/ai/understand`
3. Pass JSON to `/restaurants/recommend`
4. Send results to `/ai/respond`
5. Return final response to frontend

---

## 🗂️ Project Structure

### 🔹 Backend (Spring Boot)

```
SmartDine/
├── controller/
│   ├── ChatController.java
│   ├── AiController.java
│
├── service/
│   ├── AiService.java
│   ├── RestaurantService.java
│
├── model/
│   ├── Restaurant.java
│
├── repository/
│   ├── RestaurantRepository.java
│
├── config/
│   ├── SecurityConfig.java
│
├── resources/
│   ├── application.properties
│
└── README.md
```

### 🔹 Frontend (Static Web UI)

```
SmartDine(Frontend)/
├── assets/
│   ├── images/
│   ├── icons/
│
├── js/
│   └── chat.js
│
├── styles/
│   └── styles.css
│
├── home.html
├── restaurant.html
├── smartassist_ai.html
├── user.html
├── about.html
├── signin.html

````

---

## 🛠️ Tech Stack

| Layer | Technology |
|-----|-----------|
| Backend | Spring Boot, Java |
| AI | Google GenAI / LLM |
| Database | MySQL |
| ORM | Spring Data JPA |
| Frontend | HTML, CSS, JavaScript (Chat UI) |
| Security | Spring Security |

---

## 📦 Database Schema (Restaurant)

```sql
Restaurant (
  id INT AUTO_INCREMENT PRIMARY KEY,
  restaurant_name VARCHAR(255) NOT NULL,
  cuisine VARCHAR(50),
  location VARCHAR(255),
  rating DECIMAL(2,1),
  dish_name VARCHAR(255),
  avg_price DECIMAL(10,2),
  price_level VARCHAR(20),
  mood VARCHAR(255),
  body_need VARCHAR(100),
  weather VARCHAR(50),
  taste VARCHAR(50),
  texture VARCHAR(50),
  veg_nonveg VARCHAR(10),
  img_url VARCHAR(2048)
);
```

---

## 🔍 Example Queries

* "I want pizza."
* "Suggest cheap South Indian food"
* "Feeling tired, need energy food"
* "Biryani under 250 in Gandhipuram"

---

## 🧪 Error Handling

* Graceful fallback when no exact match exists
* Suggests **close matches instead of failing**
* Handles null or missing fields safely

---

## 🌱 Future Enhancements

* User personalization & history
* Voice-based input
* Real-time restaurant availability
* Deployment with Docker & Cloud

---

## 👩‍💻 Author

**Jayashree VR**
Final Year ECE | Java | Spring Boot | AI-integrated Systems

---

## ⭐ If you like this project

Give it a **star ⭐** and feel free to fork or contribute!
