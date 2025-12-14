package com.SmartDine.repository;

import com.SmartDine.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {

    @Query("""
        SELECT r FROM Restaurant r
        WHERE (:cuisine IS NULL OR LOWER(r.cuisine) LIKE LOWER(CONCAT('%', :cuisine, '%')))
          AND (:location IS NULL OR LOWER(r.location) LIKE LOWER(CONCAT('%', :location, '%')))
          AND (:priceLevel IS NULL OR LOWER(r.priceLevel) = LOWER(:priceLevel))
          AND (:mood IS NULL OR LOWER(r.mood) LIKE LOWER(CONCAT('%', :mood, '%')))
          AND (:bodyNeed IS NULL OR LOWER(r.bodyNeed) LIKE LOWER(CONCAT('%', :bodyNeed, '%')))
          AND (:weather IS NULL OR LOWER(r.weather) LIKE LOWER(CONCAT('%', :weather, '%')))
          AND (:taste IS NULL OR LOWER(r.taste) LIKE LOWER(CONCAT('%', :taste, '%')))
          AND (:texture IS NULL OR LOWER(r.texture) LIKE LOWER(CONCAT('%', :texture, '%')))
          AND (:vegNonveg IS NULL OR LOWER(r.vegNonveg) = LOWER(:vegNonveg))
    """)
    List<Restaurant> searchRestaurants(
            @Param("cuisine") String cuisine,
            @Param("location") String location,
            @Param("priceLevel") String priceLevel,
            @Param("mood") String mood,
            @Param("bodyNeed") String bodyNeed,
            @Param("weather") String weather,
            @Param("taste") String taste,
            @Param("texture") String texture,
            @Param("vegNonveg") String vegNonveg
    );

}
