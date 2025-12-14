package com.SmartDine.service;

import java.util.*;

import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.SmartDine.model.Restaurant;
import com.SmartDine.repository.RestaurantRepository;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;

@Service
public class AiService {

    public static final String SURPRISE_ME_COMMAND = "Surprise me with a random dish";

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private Client geminiClient;

    public AiSuggestionResponse suggestRestaurants(String userMessage) {
        Restaurant extracted = extractRestaurant(userMessage);
        AiSuggestionResponse response = new AiSuggestionResponse();
        List<Restaurant> finalMatches;
        String baseMessage;

        if (SURPRISE_ME_COMMAND.equals(userMessage.trim())) {
            return getSingleRandomDish();
        }

        List<Restaurant> exactMatches = findExactMatches(extracted);

        if (!exactMatches.isEmpty()) {
            finalMatches = exactMatches;
            baseMessage = "Here are the exact matches (" + exactMatches.size() + ") for your request!";
        } else {
            List<Restaurant> partialMatches = findPartialMatches(extracted);

            if (!partialMatches.isEmpty()) {
                finalMatches = partialMatches;
                baseMessage = "No exact matches found. Here are some close matches " + partialMatches.size();

            } else if (!safe(extracted.getDishName()).isEmpty()) {
                List<Restaurant> dishMatches = findAllDishMatches(extracted.getDishName());

                if (!dishMatches.isEmpty()) {
                    finalMatches = dishMatches;
                    baseMessage = "We found " + dishMatches.size() + " dishes matching " + extracted.getDishName();
                } else {
                    finalMatches = Collections.emptyList();
                    baseMessage = "Sorry, we couldn't find any restaurants matching your request.";
                }

            } else {
                finalMatches = Collections.emptyList();
                baseMessage = "Sorry, we couldn't find any restaurants matching your request.";
            }
        }

        List<Restaurant> ranked = rankMatches(finalMatches, extracted);
        List<Restaurant> uniqueRestaurants = removeDuplicates(ranked);

        String contextualMessage;

        if (uniqueRestaurants.size() == 1) {
            contextualMessage = "Wow! Here's the exact dish you are looking for!";
        } else {
            contextualMessage = generateContextualMessage(uniqueRestaurants, baseMessage);
        }

        response.setRestaurants(uniqueRestaurants);
        response.setMessage(contextualMessage);

        return response;
    }

    private AiSuggestionResponse getSingleRandomDish() {
        AiSuggestionResponse response = new AiSuggestionResponse();
        List<Restaurant> allDishes = restaurantRepository.findAll();

        if (allDishes.isEmpty()) {
            response.setMessage("I'm sorry, the kitchen seems to be empty! Please try again later.");
            response.setRestaurants(Collections.emptyList());
            return response;
        }
        Random random = new Random();
        int randomIndex = random.nextInt(allDishes.size());
        Restaurant randomDish = allDishes.get(randomIndex);
        String message = String.format(
                "I've picked %s's %s for you! It's our random dish of the moment. Enjoy!",
                safe(randomDish.getRestaurantName()),
                safe(randomDish.getDishName())
        );
        response.setMessage(message);
        response.setRestaurants(Collections.singletonList(randomDish));
        return response;
    }

    private String generateContextualMessage(List<Restaurant> rankedMatches, String baseMessage) {
        if (rankedMatches.isEmpty()) {
            return baseMessage;
        }

        Restaurant bestMatch = rankedMatches.get(0);

        String restaurantName = bestMatch.getRestaurantName();
        String dishName = bestMatch.getBestMatchingDish();

        if (dishName == null || dishName.isEmpty()) {
            return "Based on your request, we recommend " + restaurantName +
                    " first! Here are other options which may suit your taste:";
        }

        String conversationalMessage = String.format(
                "Try %s's %s first! It's highly rated and exactly matches your criteria. " +
                        "Here are other options which may suit your taste:",
                restaurantName,
                dishName
        );

        return conversationalMessage;
    }

    public Restaurant extractRestaurant(String userMessage) {
        if (userMessage == null || userMessage.isBlank()) return new Restaurant();
        try{
            String prompt =
                    """
                            Extract restaurant attributes from this user message.
                            JSON Keys and Rules:
                            The JSON object MUST include all 14 keys listed below.
                            - Keys: restaurant_name, cuisine, location, rating, dish_name, avg_price, mood, body_need, weather, taste, texture, veg_nonveg, price_level, special_request.
                            - Missing Values: If an attribute is NOT mentioned or cannot be inferred from the user message, set its value to "null" (as a string).
                            Specific Conversion Rules:
                                1.  avg_price (Numeric):
                                      - Convert phrases like "under X", "less than X", or "max price X" to the numeric value X. E.g., "under 300" → 300.
                                2.  price_level (String):
                                      - cheap / low price / budget / not expensive → "low"
                                      - moderate / mid range → "medium"
                                      - expensive / premium → "high"
                                3.  rating (Numeric):
                                      - Convert phrases like "rating above X", "at least X stars", or "minimum X" to the numeric value X. E.g., "rating above 4" → 4.
                            Predefined Attribute Lists (Use these values strictly, recognizing synonyms):
                                * weather(Synonyms): [hot(sunny,summer,burning), cold(winter,cool,snow), rainy(rain, chilly,cool), all weather]
                                * mood: [happy, excited, romantic, family, party, healthy, null]
                                * body_need(Synonyms): [energetic(active,hyper), comfort(filling), filling(comfort), light(mild), protein-rich(diet), carb(energy rich), vegan, snack, vegetarian, null]
                                * taste (Synonyms): [spicy (fiery, piquant, hot), aromatic (fragrant, scented), rich (decadent, intense, heavy), mild (subtle, delicate), savory (umami, meaty, salty), creamy (smooth, velvety), fresh (zesty, bright), umami (savory, meaty), salty (briny), tangy (acidic, sharp), sweet (sugary), bitter (acrid), balanced (harmonious), buttery (oily, fatty), cheesy (fromage), soft (tender, gentle), juicy (succulent), null]
                                * texture (Synonyms): [moist (damp, succulent), grainy (sandy), mixed, soft (tender, delicate), crispy (crunchy, brittle), thin (delicate), creamy (smooth, velvety), thick (dense, viscous), chewy (gummy), sticky (tacky), light (fluffy, airy), juicy (succulent), fresh, firm (hard), layered (flaky), tender (soft, delicate), springy (bouncy), smooth (silky), crumbly (brittle), flaky (layered), buttery (oily), crunchy (crispy), fluffy (light, airy), silky (smooth), airy (light, fluffy), brothy (soupy, liquid), null]
                                * veg_nonveg: [Veg, Non-Veg, null]
                            User message: "%s"
                    """.formatted(userMessage);

            GenerateContentResponse resp = geminiClient.models.generateContent(
                    "gemini-2.5-flash",
                    prompt,
                    null
            );
            String text = resp.text().trim();
            JSONObject obj;
            try {
                obj = new JSONObject(text);
            } catch (Exception ex) {
                int start = text.indexOf("{");
                int end = text.lastIndexOf("}");
                obj = new JSONObject(text.substring(start, end + 1));
            }
            Restaurant r = new Restaurant();
            r.setRestaurantName(obj.optString("restaurant_name", ""));
            r.setCuisine(obj.optString("cuisine", ""));
            r.setLocation(obj.optString("location", ""));
            r.setRating((float) obj.optDouble("rating", 0.0));
            r.setDishName(obj.optString("dish_name", ""));
            r.setAvgPrice((float) obj.optDouble("avg_price", 0.0));
            r.setMood(obj.optString("mood", ""));
            r.setBodyNeed(obj.optString("body_need", ""));
            r.setWeather(obj.optString("weather", ""));
            r.setTaste(obj.optString("taste", ""));
            r.setTexture(obj.optString("texture", ""));
            r.setVegNonveg(obj.optString("veg_nonveg", ""));
            r.setPriceLevel(obj.optString("price_level", ""));

            String veg = r.getVegNonveg().toLowerCase();
            if (veg.contains("non")) {
                r.setVegNonveg("Non-Veg");
            } else if (veg.contains("veg")) {
                r.setVegNonveg("Veg");
            } else {
                r.setVegNonveg("");
            }
            if (r.getPriceLevel() == null || r.getPriceLevel().isEmpty()) {
                float price = r.getAvgPrice();
                if (price > 0) {
                    if (price <= 180) r.setPriceLevel("low");
                    else if (price <= 300) r.setPriceLevel("medium");
                    else r.setPriceLevel("high");
                }
            }
            return r;
        } catch (Exception e) {
            e.printStackTrace();
            return new Restaurant();
        }
    }

    private List<Restaurant> findExactMatches(Restaurant f) {
        List<Restaurant> all = restaurantRepository.findAll();
        List<Restaurant> exact = new ArrayList<>();
        for (Restaurant r : all) {
            boolean allMatch = true;

            if (mismatches(f.getRestaurantName(), r.getRestaurantName())) allMatch = false;
            if (mismatches(f.getCuisine(), r.getCuisine())) allMatch = false;
            if (mismatches(f.getLocation(), r.getLocation())) allMatch = false;
            if (mismatches(f.getDishName(), r.getDishName())) allMatch = false;
            if (mismatches(f.getTaste(), r.getTaste())) allMatch = false;
            if (mismatches(f.getTexture(), r.getTexture())) allMatch = false;
            if (mismatches(f.getMood(), r.getMood())) allMatch = false;
            if (mismatches(f.getWeather(), r.getWeather())) allMatch = false;
            if (mismatches(f.getBodyNeed(), r.getBodyNeed())) allMatch = false;
            if (mismatches(f.getVegNonveg(), r.getVegNonveg())) allMatch = false;
            if (mismatches(f.getPriceLevel(), r.getPriceLevel())) allMatch = false;

            float restaurantRating = safeFloat(r.getRating());
            if (f.getRating() > 0 && (restaurantRating < f.getRating())) {
                allMatch = false;
            }

            float restaurantPrice = safeFloat(r.getAvgPrice());
            if (f.getAvgPrice() > 0 && (restaurantPrice > f.getAvgPrice())) {
                allMatch = false;
            }

            if (allMatch) {
                System.out.println("EXACT MATCH: " + r.getDishName() +
                        " / " + r.getCuisine() +
                        " / " + r.getLocation() +
                        " / " + safeFloat(r.getAvgPrice()));
                exact.add(r);
            }
        }
        return exact;
    }

    private boolean mismatches(String extractedValue, String databaseValue) {
        if (extractedValue == null || extractedValue.isEmpty() || extractedValue.equalsIgnoreCase("null")) {
            return false;
        }
        if (databaseValue == null || databaseValue.isEmpty() || databaseValue.equalsIgnoreCase("null")) {
            return true;
        }
        return !extractedValue.trim().equalsIgnoreCase(databaseValue.trim());
    }

    private float safeFloat(Float value) {
        return value == null ? 0.0f : value;
    }

    private List<Restaurant> findAllDishMatches(String dishName) {
        if (safe(dishName).isEmpty()) return new ArrayList<>();
        String searchName = safe(dishName).toLowerCase();
        List<Restaurant> all = restaurantRepository.findAll();
        List<Restaurant> matches = new ArrayList<>();

        for (Restaurant r : all) {
            if (safe(r.getDishName()).toLowerCase().contains(searchName)) {
                matches.add(r);
            }
        }
        return matches;
    }

    private List<Restaurant> findPartialMatches(Restaurant f) {
        List<Restaurant> all = restaurantRepository.findAll();
        List<Restaurant> partial = new ArrayList<>();
        int totalConditions = 0;
        if (!safe(f.getRestaurantName()).isEmpty()) totalConditions++;
        if (!safe(f.getCuisine()).isEmpty()) totalConditions++;
        if (!safe(f.getLocation()).isEmpty()) totalConditions++;
        if (!safe(f.getDishName()).isEmpty()) totalConditions++;
        if (!safe(f.getTaste()).isEmpty()) totalConditions++;
        if (!safe(f.getTexture()).isEmpty()) totalConditions++;
        if (!safe(f.getMood()).isEmpty()) totalConditions++;
        if (!safe(f.getWeather()).isEmpty()) totalConditions++;
        if (!safe(f.getBodyNeed()).isEmpty()) totalConditions++;
        if (!safe(f.getVegNonveg()).isEmpty()) totalConditions++;
        if (!safe(f.getPriceLevel()).isEmpty()) totalConditions++;
        if (f.getRating() > 0) totalConditions++;
        if (f.getAvgPrice() > 0) totalConditions++;
        if (totalConditions == 0) {
            return Collections.emptyList();
        }
        int requiredMatches = (totalConditions + 1) / 2;
        for (Restaurant r : all) {
            int matchCount = 0;
            if (!safe(f.getRestaurantName()).isEmpty() && safe(r.getRestaurantName()).toLowerCase().contains(safe(f.getRestaurantName()).toLowerCase())) matchCount++;
            if (!safe(f.getCuisine()).isEmpty() && safe(r.getCuisine()).toLowerCase().contains(safe(f.getCuisine()).toLowerCase())) matchCount++;
            if (!safe(f.getLocation()).isEmpty() && safe(r.getLocation()).toLowerCase().contains(safe(f.getLocation()).toLowerCase())) matchCount++;
            if (!safe(f.getDishName()).isEmpty() && safe(r.getDishName()).toLowerCase().contains(safe(f.getDishName()).toLowerCase())) matchCount++;
            if (!safe(f.getTaste()).isEmpty() && safe(r.getTaste()).toLowerCase().contains(safe(f.getTaste()).toLowerCase())) matchCount++;
            if (!safe(f.getTexture()).isEmpty() && safe(r.getTexture()).toLowerCase().contains(safe(f.getTexture()).toLowerCase())) matchCount++;
            if (!safe(f.getMood()).isEmpty() && safe(r.getMood()).toLowerCase().contains(safe(f.getMood()).toLowerCase())) matchCount++;
            if (!safe(f.getWeather()).isEmpty() && safe(r.getWeather()).toLowerCase().contains(safe(f.getWeather()).toLowerCase())) matchCount++;
            if (!safe(f.getBodyNeed()).isEmpty() && safe(r.getBodyNeed()).toLowerCase().contains(safe(f.getBodyNeed()).toLowerCase())) matchCount++;
            if (!safe(f.getVegNonveg()).isEmpty() && safe(r.getVegNonveg()).toLowerCase().contains(safe(f.getVegNonveg()).toLowerCase())) matchCount++;
            if (!safe(f.getPriceLevel()).isEmpty() && safe(r.getPriceLevel()).toLowerCase().contains(safe(f.getPriceLevel()).toLowerCase())) matchCount++;
            float restaurantRating = safeFloat(r.getRating());
            float restaurantPrice = safeFloat(r.getAvgPrice());
            if (f.getRating() > 0 && restaurantRating >= f.getRating()) matchCount++;
            if (f.getAvgPrice() > 0 && restaurantPrice <= f.getAvgPrice()) matchCount++;
            if (matchCount >= requiredMatches) {
                partial.add(r);
            }
        }
        return partial.size() > 5 ? partial.subList(0, 5) : partial;
    }

    private List<Restaurant> rankMatches(List<Restaurant> matches, Restaurant extracted) {
        if (matches.isEmpty()) return Collections.emptyList();

        List<RestaurantScore> scored = new ArrayList<>();
        for (Restaurant r : matches) {
            int score = 0;
            score += keywordScore(extracted.getCuisine(), r.getCuisine());
            score += keywordScore(extracted.getTaste(), r.getTaste());
            score += keywordScore(extracted.getTexture(), r.getTexture());
            score += keywordScore(extracted.getDishName(), r.getDishName());
            score += keywordScore(extracted.getMood(), r.getMood());
            score += keywordScore(extracted.getRestaurantName(), r.getRestaurantName());
            score += keywordScore(extracted.getLocation(), r.getLocation());
            scored.add(new RestaurantScore(r, score));
        }
        scored.sort((a, b) -> {
            int scoreComparison = Integer.compare(b.score, a.score);
            if (scoreComparison != 0) return scoreComparison;
            Float ratingA = a.res.getRating();
            Float ratingB = b.res.getRating();
            if (ratingA == null && ratingB == null) return 0;
            if (ratingA == null) return 1;
            if (ratingB == null) return -1;
            return Float.compare(ratingB, ratingA);
        });

        List<Restaurant> ranked = new ArrayList<>();
        for (RestaurantScore s : scored) ranked.add(s.res);

        Restaurant bestMatch = ranked.get(0);
        String topDishName = findSpecificBestDish(bestMatch, extracted);
        bestMatch.setBestMatchingDish(topDishName);

        return ranked;
    }

    private String findSpecificBestDish(Restaurant bestMatch, Restaurant criteria) {
        String restaurantName = bestMatch.getRestaurantName();

        if (!safe(bestMatch.getDishName()).isEmpty()) {
            return bestMatch.getDishName();
        }

        List<Restaurant> allDishes = restaurantRepository.findAll();

        return allDishes.stream()
                .filter(d -> safe(d.getRestaurantName()).equalsIgnoreCase(restaurantName))
                .filter(d -> criteriaMatch(d, criteria))
                .max(Comparator.comparing(Restaurant::getRating))
                .map(Restaurant::getDishName)
                .orElse("Chef's Special");
    }

    private boolean criteriaMatch(Restaurant dish, Restaurant criteria) {
        if (!safe(criteria.getTaste()).isEmpty() && !safe(dish.getTaste()).equalsIgnoreCase(safe(criteria.getTaste()))) return false;
        if (!safe(criteria.getTexture()).isEmpty() && !safe(dish.getTexture()).equalsIgnoreCase(safe(criteria.getTexture()))) return false;
        if (!safe(criteria.getMood()).isEmpty() && !safe(dish.getMood()).equalsIgnoreCase(safe(criteria.getMood()))) return false;

        return true;
    }


    private List<Restaurant> removeDuplicates(List<Restaurant> list) {
        List<Restaurant> unique = new ArrayList<>();
        Set<String> uniqueKeys = new HashSet<>();

        for (Restaurant r : list) {
            String key = safe(r.getRestaurantName()) + ":" + safe(r.getDishName());
            if (r.getId() != null && uniqueKeys.add(key)) {
                unique.add(r);
            }
        }
        return unique;
    }

    private int keywordScore(String a, String b) {
        if (safe(a).isEmpty()) return 0;
        return safe(b).toLowerCase().contains(safe(a).toLowerCase()) ? 2 : 0;
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private static class RestaurantScore {
        Restaurant res;
        int score;
        RestaurantScore(Restaurant r, int s) { this.res = r; this.score = s; }
    }

    public static class AiSuggestionResponse {
        private String message;
        private List<Restaurant> restaurants;
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public List<Restaurant> getRestaurants() { return restaurants; }
        public void setRestaurants(List<Restaurant> restaurants) { this.restaurants = restaurants; }
    }
}