package me.rootatkali.justify.repo;

import me.rootatkali.justify.model.Event;
import org.springframework.data.repository.CrudRepository;

public interface EventRepository extends CrudRepository<Event, Integer> {
}
