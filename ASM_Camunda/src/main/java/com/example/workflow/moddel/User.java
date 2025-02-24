package com.example.workflow.moddel;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // Trường password dùng để đăng nhập (nên lưu mật khẩu đã được mã hóa)
    @Column(nullable = false)
    private String password;

    @Column(length = 20)
    private String phone;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();


    // Thêm trường role
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER; // Giá trị mặc định là USER

    // Enum định nghĩa các vai trò
    public enum Role {
        USER,
        ADMIN
    }
}