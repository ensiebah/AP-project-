package com.secondhand.frontend.model;

public class UserDto {
    private Long id;
    private String fullName;
    private String username;
    private String email;
    private String phone;
    private String role; // به صورت String یا Enum فرانت‌اند
    private boolean blocked; // 🟢 نام فیلد دقیقاً هم‌نام با کلید JSON بک‌اند (blocked)

    // Constructor خالی برای تبدیل JSON
    public UserDto() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    // 🟢 در جاوا برای فیلدهای boolean، نام گتر استاندارد با is شروع می‌شود
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
}