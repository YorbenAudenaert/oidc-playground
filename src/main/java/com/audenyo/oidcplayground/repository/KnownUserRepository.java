package com.audenyo.oidcplayground.repository;

import com.audenyo.oidcplayground.model.entity.KnownUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnownUserRepository extends JpaRepository<KnownUser, String> {
}
