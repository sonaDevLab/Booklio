package org.sonadev.booklio.repository;

import org.sonadev.booklio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
