package com.secondhand.backend.controller;

import com.secondhand.backend.entity.Advertisement;
import com.secondhand.backend.repository.AdvertisementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private AdvertisementRepository advertisementRepository ;

    @GetMapping
    public ResponseEntity<List<Advertisement>> searchAds(
            @RequestParam(required = false) String query ,
            @RequestParam(required = false) String category ,
            @RequestParam(required = false) Double minPrice ,
            @RequestParam(required = false) Double maxPrice
    ){
        //first we receive all active and confirmed ads
        List<Advertisement> ads = advertisementRepository.findAll() ;

        List<Advertisement> filteredAds = ads.stream()
                .filter(ad-> "ACTIVE".equalsIgnoreCase(ad.getStatus().name()))
                .filter(ad-> query == null || query.isBlank() ||
                        ad.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                        ad.getDescription().toLowerCase().contains(query.toLowerCase()))
                .filter(ad -> category == null || category.isBlank() ||
                        ad.getCategory().equals(category))
                .filter(ad ->minPrice == null || ad.getPrice() >= minPrice)
                .filter(ad->maxPrice== null || ad.getPrice() <= maxPrice)
                .collect(Collectors.toList());
        return ResponseEntity.ok(filteredAds) ;

    }


}
