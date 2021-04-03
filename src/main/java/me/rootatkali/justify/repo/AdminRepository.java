package me.rootatkali.justify.repo;

import me.rootatkali.justify.model.Admin;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface AdminRepository extends CrudRepository<Admin, UUID> {
}
