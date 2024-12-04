package com.bitcamp.drrate.domain.users.entity;

import com.bitcamp.drrate.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "Users") // 엔티티 테이블 이름 지정 필수 !!
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Users extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_pwd")
    private String password;

    @Column(name = "user_name")
    private String name;

    @Column(name = "user_birth")
    private String birth;

    @Column(nullable = false, name = "user_email", unique = true)
    private String email;

    // @Column(nullable = false)
    // private String nickName;

    // @Column(name = "profile_image_url")
    // private String profileImageUrl;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // @Column(name = "oauth_provider", nullable = false)
    // private String oauth;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
