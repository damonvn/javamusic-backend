package com.example.javamusic.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import com.example.javamusic.model.User;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    // Bạn có thể thêm các phương thức tùy chỉnh nếu cần
}