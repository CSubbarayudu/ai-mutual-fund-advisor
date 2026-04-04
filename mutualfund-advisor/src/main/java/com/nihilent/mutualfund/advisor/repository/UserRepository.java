package com.nihilent.mutualfund.advisor.repository;

import com.nihilent.mutualfund.advisor.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Users, Long> {
}
