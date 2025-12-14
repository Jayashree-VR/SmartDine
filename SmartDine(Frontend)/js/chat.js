
let currentRecommendedDishes = [];

let sendBtn;
let input;
let messages;
let resultsDiv;
let sortByElement;

document.addEventListener('DOMContentLoaded', () => {
    sendBtn = document.getElementById("sendButton");
    input = document.getElementById("chatInput");
    messages = document.getElementById("chatMessages");
    resultsDiv = document.getElementById("recommendationList");
    sortByElement = document.getElementById("sort-by");

    if (sendBtn) {
        sendBtn.onclick = sendMessage;
    }
    if (input) {
        input.addEventListener("keypress", e => {
            if (e.key === "Enter") sendMessage();
        });
    }

    if (sortByElement) {
        sortByElement.addEventListener("change", handleSort);
    }

    if (!resultsDiv) {
        console.error("Critical Error: 'recommendationList' (resultsDiv) not found in the DOM.");
    }
});

async function sendMessage() {

    if (!messages || !input) return;


    if (sortByElement) {
        sortByElement.value = "default";
    }

    const text = input.value.trim();
    if (!text) return;

    addMessage(text, "user-message");
    input.value = "";

    addMessage("Thinking...", "ai-message");

    messages.scrollTop = messages.scrollHeight;

    try {
        const res = await fetch("http://localhost:8080/api/ai/suggest", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ message: text })
        });

        if (!res.ok) {
            throw new Error(`Server returned status: ${res.status}`);
        }

        const data = await res.json();

        const last = messages.lastElementChild;
        if (last && last.classList.contains("ai-message") && last.textContent === "Thinking...") {
            last.remove();
        }
        currentRecommendedDishes = data.restaurants || [];

        addMessage(data.message, "ai-message");
        showRecommendations(currentRecommendedDishes);

    } catch (error) {
        console.error("Fetch Error:", error);
        const last = messages.lastElementChild;
        if (last && last.classList.contains("ai-message") && last.textContent === "Thinking...") {
            last.remove();
        }
        addMessage("Error connecting to server ❌. Please check console.", "ai-message");
    }

    messages.scrollTop = messages.scrollHeight;
}

function addMessage(text, type) {
    if (!messages) return;
    const div = document.createElement("div");
    div.className = `message ${type}`;
    div.textContent = text;
    messages.appendChild(div);
}

function handleSort() {
    if (!sortByElement) return;

    const sortBy = sortByElement.value;
    let sortedDishes = [...currentRecommendedDishes];

    if (sortBy === 'price-asc') {
        sortedDishes.sort((a, b) => (a.avgPrice || 0) - (b.avgPrice || 0));
    } else if (sortBy === 'price-desc') {
        sortedDishes.sort((a, b) => (b.avgPrice || 0) - (a.avgPrice || 0));
    } else if (sortBy === 'rating-desc') {
        sortedDishes.sort((a, b) => (b.rating || 0) - (a.rating || 0));
    }

    showRecommendations(sortedDishes);
}



function generateStars(rating) {
    const numericRating = parseFloat(rating);
    if (isNaN(numericRating) || numericRating <= 0) {
        return 'No rating';
    }
    const displayedRating = numericRating.toFixed(1);
    return `<span class="star-icon">★</span> ${displayedRating}`;
}

function showRecommendations(list) {
    if (!resultsDiv) return;

    resultsDiv.innerHTML = "";
    if (!Array.isArray(list) || list.length === 0) {
        const info = document.createElement("p");
        info.style.padding = "10px";
        info.textContent = "No dishes were recommended for your query.";
        resultsDiv.appendChild(info);
        return;
    }

    list.forEach(r => {
        const ratingValue = r.rating || r.dishRating || r.restaurantRating || r.ratingValue || r.stars || 0;
        const starsHtml = generateStars(ratingValue);
        const dishImageUrl = r.imgUrl || r.img_url || 'assets/default-food.jpg';

        const card = document.createElement("div");
        card.className = "recommendation-card";

        card.innerHTML = `
            <div class="card-image" style="background-image: url('${dishImageUrl}')"></div>
            <div class="card-details">
                <h4>${r.dishName || "Dish Name Missing"}</h4>
                <p>Restaurant: ${r.restaurantName || "Local Favorite"}</p>
                <p><b>Cuisine:</b> ${r.cuisine || "Mixed"}</p>

                <div class="card-rating">${starsHtml}</div>
            </div>

            <span class="card-price">₹${r.avgPrice ? r.avgPrice.toFixed(2) : '--'}</span>
        `;
        resultsDiv.appendChild(card);
    });
}
