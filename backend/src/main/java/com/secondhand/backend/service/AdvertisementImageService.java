package com.secondhand.backend.service;

import com.secondhand.backend.entity.Advertisement;
import com.secondhand.backend.entity.AdvertisementImage;
import com.secondhand.backend.repository.AdvertisementImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * Persists real image files on the backend and the corresponding metadata in
 * advertisement_images. Frontend clients never store local file:// paths.
 */
@Service
@RequiredArgsConstructor
public class AdvertisementImageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final AdvertisementImageRepository imageRepository;

    @Value("${app.upload-directory:./data/uploads}")
    private String uploadDirectory;

    public AdvertisementImage uploadImage(MultipartFile file, Advertisement advertisement) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        String originalFilename = file.getOriginalFilename() == null ? "image" : file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Only JPG, JPEG, PNG, GIF and WEBP images are allowed");
        }

        Path directory = Path.of(uploadDirectory).toAbsolutePath().normalize();
        Files.createDirectories(directory);

        String storedFilename = UUID.randomUUID() + "." + extension;
        Path destination = directory.resolve(storedFilename).normalize();
        if (!destination.startsWith(directory)) {
            throw new IllegalArgumentException("Invalid image path");
        }

        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

        AdvertisementImage image = new AdvertisementImage();
        image.setImagePath("/api/images/file/" + storedFilename);
        image.setAdvertisement(advertisement);
        return imageRepository.save(image);
    }

    public List<AdvertisementImage> getImages(Advertisement advertisement) {
        return imageRepository.findByAdvertisementOrderByIdAsc(advertisement);
    }

    public void deleteImage(Advertisement advertisement, String imagePath) throws IOException {
        AdvertisementImage image = imageRepository.findByAdvertisementAndImagePath(advertisement, imagePath)
                .orElseThrow(() -> new IllegalArgumentException("Image not found for this advertisement"));

        String filename = extractFilename(image.getImagePath());
        Path filePath = Path.of(uploadDirectory).toAbsolutePath().normalize().resolve(filename).normalize();
        Path directory = Path.of(uploadDirectory).toAbsolutePath().normalize();
        if (filePath.startsWith(directory)) {
            Files.deleteIfExists(filePath);
        }
        imageRepository.delete(image);
    }

    public Resource loadImage(String filename) throws MalformedURLException {
        String safeFilename = extractFilename(filename);
        Path directory = Path.of(uploadDirectory).toAbsolutePath().normalize();
        Path filePath = directory.resolve(safeFilename).normalize();
        if (!filePath.startsWith(directory)) {
            throw new IllegalArgumentException("Invalid image path");
        }
        Resource resource = new UrlResource(filePath.toUri());
        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalArgumentException("Image file not found");
        }
        return resource;
    }

    private String getExtension(String filename) {
        int index = filename.lastIndexOf('.');
        if (index < 0 || index == filename.length() - 1) {
            throw new IllegalArgumentException("Image file extension is required");
        }
        return filename.substring(index + 1).toLowerCase(Locale.ROOT);
    }

    private String extractFilename(String pathOrFilename) {
        String value = pathOrFilename == null ? "" : pathOrFilename.trim();
        int slash = value.lastIndexOf('/');
        String filename = slash >= 0 ? value.substring(slash + 1) : value;
        if (filename.isBlank() || filename.contains("..") || filename.contains("\\") || filename.contains("/")) {
            throw new IllegalArgumentException("Invalid image filename");
        }
        return filename;
    }
}
