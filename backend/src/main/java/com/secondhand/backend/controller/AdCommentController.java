package com.secondhand.backend.controller;

import com.secondhand.backend.entity.AdComment;
import com.secondhand.backend.entity.Advertisement;
import com.secondhand.backend.entity.User;
import com.secondhand.backend.repository.AdCommentRepository;
import com.secondhand.backend.repository.AdvertisementRepository;
import com.secondhand.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class AdCommentController {
    private final AdCommentRepository adCommentRepository ;
    private final AdvertisementRepository advertisementRepository ;
    private final UserRepository userRepository ;

    @PostMapping("/ad/{adId}")
    public Map<String , String> addComment(@PathVariable Long adId , @RequestBody Map<String, String> body, Principal principal){
        User user = userRepository.findByUserName(principal.getName())
                .orElseThrow(()-> new RuntimeException("User not found")) ;
        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(()-> new RuntimeException("Ad not found")) ;

        AdComment comment = new AdComment() ;
        comment.setContent(body.get("content"));
        comment.setUser(user);
        comment.setAdvertisement(ad);
        adCommentRepository.save(comment) ;

        return Map.of("status", "SUCCESS", "message", "Comment added successfully");
    }

    @GetMapping("/ad/{adId}")
    public List<Map<String, Object>> getAdComments(@PathVariable Long adId) {
        Advertisement ad = advertisementRepository.findById(adId)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        return adCommentRepository.findByAdvertisementOrderByCreatedAtDesc(ad).stream()
                .map(c -> {
                    // استفاده از HashMap معمولی به جای Map.of برای جلوگیری از خطای ناگهانی NullPointerException
                    Map<String, Object> map = new java.util.HashMap<>();
                    map.put("username", c.getUser() != null ? c.getUser().getUserName() : "Anonymous");
                    map.put("content", c.getContent() != null ? c.getContent() : "");
                    return map;
                })
                // استفاده از Collectors.toList برای سازگاری کامل و خروجی خامی که در لایه‌های دیگر قابل تغییر باشد
                .collect(java.util.stream.Collectors.toList());
    }
}
