package com.secondhand.backend.service;

import com.secondhand.backend.entity.Advertisement;
import com.secondhand.backend.entity.AdvertisementImage;
import com.secondhand.backend.repository.AdvertisementImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service // 👈 به اسپرینگ‌بوت می‌گوید این کلاس یک لایه منطق تجاری (Business Logic) است.
@RequiredArgsConstructor // 👈 به لومبوک می‌گوید سازنده را برای تزریق وابستگی repository بسازد.
public class AdvertisementImageService {

    // 👈 تزریق مخزن دیتابیس برای اینکه بتوانیم آدرس عکس را ذخیره کنیم
    private final AdvertisementImageRepository imageRepository;

    // 👈 تعریف مسیر پوشه‌ای روی کامپیوتر که عکس‌ها آنجا ذخیره می‌شوند
    private final String UPLOAD_DIR = System.getProperty("user.home") + "/Desktop/secondhand_images/";

    /**
     * وظیفه متد: ذخیره فیزیکی فایل عکس روی هارد و ثبت آدرس آن در دیتابیس
     */
    public AdvertisementImage uploadImage(MultipartFile file, Advertisement advertisement) throws IOException {
        // ۱. بررسی اینکه فایل خالی نباشد
        if (file.isEmpty()) {
            throw new IllegalArgumentException("فایل ارسالی خالی است.");
        }

        // ۲. ساخت پوشه ذخیره‌سازی در دسکتاپ (اگر از قبل وجود نداشته باشد)
        File directory = new File(UPLOAD_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        // ۳. ساخت یک نام کاملاً منحصربه‌فرد (مثلاً uuid_myphoto.png) تا عکس‌ها جایگزین هم نشوند
        String originalFilename = file.getOriginalFilename();
        String uniqueFilename = UUID.randomUUID().toString() + "_" + originalFilename;

        // ۴. ذخیره فیزیکی فایل در مسیر پوشه
        File destinationFile = new File(UPLOAD_DIR + uniqueFilename);
        file.transferTo(destinationFile);

        // ۵. ساختن شیء انتیتی و ذخیره مسیر (URL) متنی آن در دیتابیس
        AdvertisementImage advImage = new AdvertisementImage();
        advImage.setImagePath("/images/" + uniqueFilename); // آدرسی که بعداً فرانت‌اَند با آن عکس را لود می‌کند
        advImage.setAdvertisement(advertisement); // متصل کردن عکس به آگهی مربوطه

        // ۶. ذخیره در دیتابیس از طریق ریپازیتوری و بازگرداندن نتیجه
        return imageRepository.save(advImage);
    }
}