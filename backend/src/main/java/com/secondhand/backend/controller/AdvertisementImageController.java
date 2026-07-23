package com.secondhand.backend.controller;

import com.secondhand.backend.dto.AdvertisementImageDeleteRequest;
import com.secondhand.backend.dto.AdvertisementImageDto;
import com.secondhand.backend.entity.Advertisement;
import com.secondhand.backend.entity.AdvertisementImage;
import com.secondhand.backend.entity.Role;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.AdvertisementRepository;
import com.secondhand.backend.repository.UserRepository;
import com.secondhand.backend.service.AdvertisementImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class AdvertisementImageController {

    private final AdvertisementImageService imageService;
    private final AdvertisementRepository advertisementRepository;
    private final UserRepository userRepository;

    /** Real multipart upload from the JavaFX client. */
    @PostMapping(value = "/upload/{advertisementId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdvertisementImageDto uploadImage(
            @PathVariable Long advertisementId,
            @RequestParam("file") MultipartFile file,
            Principal principal
    ) throws IOException {
        Advertisement advertisement = findAdvertisement(advertisementId);
        ensureOwnerOrAdmin(advertisement, principal);
        return mapToDto(imageService.uploadImage(file, advertisement));
    }

    /** Browser/JavaFX Image can load this public URL without an Authorization header. */
    @GetMapping("/file/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) throws IOException {
        Resource resource = imageService.loadImage(filename);
        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);
        return ResponseEntity.ok().contentType(mediaType).body(resource);
    }

    @DeleteMapping("/advertisement/{advertisementId}")
    public ResponseEntity<Void> deleteImage(
            @PathVariable Long advertisementId,
            @Valid @RequestBody AdvertisementImageDeleteRequest request,
            Principal principal
    ) throws IOException {
        Advertisement advertisement = findAdvertisement(advertisementId);
        ensureOwnerOrAdmin(advertisement, principal);
        imageService.deleteImage(advertisement, request.getImagePath());
        return ResponseEntity.noContent().build();
    }

    private Advertisement findAdvertisement(Long advertisementId) {
        return advertisementRepository.findById(advertisementId)
                .orElseThrow(() -> new IllegalArgumentException("Advertisement not found"));
    }

    private void ensureOwnerOrAdmin(Advertisement advertisement, Principal principal) {
        User currentUser = userRepository.findByUserName(principal.getName())
                .orElseThrow(() -> new IllegalArgumentException("Authenticated user not found"));
        boolean isOwner = advertisement.getSeller().getId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new SecurityException("You cannot manage images for another user's advertisement");
        }
    }

    private AdvertisementImageDto mapToDto(AdvertisementImage image) {
        return AdvertisementImageDto.builder()
                .id(image.getId())
                .imagePath(image.getImagePath())
                .advertisementId(image.getAdvertisement().getId())
                .build();
    }
}
