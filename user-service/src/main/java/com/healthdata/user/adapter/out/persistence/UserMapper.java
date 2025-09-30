package com.healthdata.user.adapter.out.persistence;

import com.healthdata.user.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserJpaEntity toEntity(User user) {
        return new UserJpaEntity(
                user.getName(),
                user.getNickname(),
                user.getEmail().getValue(),
                user.getPassword().getValue(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public User toDomain(UserJpaEntity entity) {
        return User.fromPersistence(
                entity.getId(),
                entity.getName(),
                entity.getNickname(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}