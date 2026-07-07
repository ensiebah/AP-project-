package com.secondhand.backend.controller;

import com.secondhand.backend.dto.AdvertisementCreateDto;
import com.secondhand.backend.dto.AdvertisementDto;
import com.secondhand.backend.service.AdvertisementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/advertisements")
@RequiredArgsConstructor
public class AdvertisementController {

    private final AdvertisementService advertisementService;

    @PostMapping("/create")
    public AdvertisementDto createAdvertisement(@RequestBody AdvertisementCreateDto dto, Principal principal) {
        String username = principal.getName();
        return advertisementService.createAdvertisementByUsername(dto, username);
    }

    @GetMapping("/{id}")
    public AdvertisementDto getAdvertisementById(@PathVariable Long id) {
        return advertisementService.getAdvertisementById(id);
    }

    @GetMapping("/active")
    public List<AdvertisementDto> getAllActiveAdvertisements() {
        return advertisementService.getAllActiveAdvertisement();
    }

    @GetMapping("/pending")
    public List<AdvertisementDto> getAllPendingAdvertisements() {
        return advertisementService.getAllPendingAdvertisements();
    }

    @PostMapping("/search")
    public List<AdvertisementDto> searchAdvertisements(@RequestBody Map<String, String> searchRequest) {
        String keywords = searchRequest.getOrDefault("query", "");
        return advertisementService.searchByTitle(keywords);
    }

    @PutMapping("/{id}")
    public AdvertisementDto updateAdvertisement(@PathVariable Long id, @RequestBody AdvertisementDto dto) {
        return advertisementService.updateAdvertisement(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteAdvertisement(@PathVariable Long id) {
        advertisementService.deleteAdvertisement(id);
    }

    @PutMapping("/{id}/approve")
    public AdvertisementDto approveAdvertisements(@PathVariable Long id) {
        return advertisementService.approveAdvertisement(id);
    }

    @PutMapping("/{id}/reject")
    public AdvertisementDto rejectAdvertisement(@PathVariable Long id) {
        return advertisementService.rejectAdvertisement(id);
    }
}