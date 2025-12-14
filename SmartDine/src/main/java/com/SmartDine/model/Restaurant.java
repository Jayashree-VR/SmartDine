package com.SmartDine.model;

import jakarta.persistence.*;

@Entity
@Table(name = "restaurant")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "restaurant_name")
    private String restaurantName;

    private String cuisine;
    private String location;

    private Float rating;

    @Column(name = "dish_name")
    private String dishName;

    @Column(name = "avg_price")
    private Float avgPrice;

    @Column(name = "price_level")
    private String priceLevel;

    private String mood;

    @Column(name = "body_need")
    private String bodyNeed;

    private String weather;
    private String taste;
    private String texture;

    @Column(name = "veg_nonveg")
    private String vegNonveg;


    @Column(name = "img_url")
    private String imgUrl;

    private String bestMatchingDish;



    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        this.cuisine = cuisine;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Float getRating() {
        return rating == null ? 0.0f : rating;
    }

    public void setRating(Float rating) {
        this.rating = rating;
    }

    public String getDishName() {
        return dishName;
    }

    public void setDishName(String dishName) {
        this.dishName = dishName;
    }

    public Float getAvgPrice() {
        return avgPrice == null ? 0.0f : avgPrice;
    }

    public void setAvgPrice(Float avgPrice) {
        this.avgPrice = avgPrice;
    }

    public String getPriceLevel() {
        return priceLevel;
    }

    public void setPriceLevel(String priceLevel) {
        this.priceLevel = priceLevel;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public String getBodyNeed() {
        return bodyNeed;
    }

    public void setBodyNeed(String bodyNeed) {
        this.bodyNeed = bodyNeed;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getTaste() {
        return taste;
    }

    public void setTaste(String taste) {
        this.taste = taste;
    }

    public String getTexture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    public String getVegNonveg() {
        return vegNonveg;
    }

    public void setVegNonveg(String vegNonveg) {
        this.vegNonveg = vegNonveg;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getBestMatchingDish() {
        return bestMatchingDish;
    }

    public void setBestMatchingDish(String bestMatchingDish) {
        this.bestMatchingDish = bestMatchingDish;
    }

}
