package me.rootatkali.justify.repo;

import me.rootatkali.justify.model.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, String> {
}
