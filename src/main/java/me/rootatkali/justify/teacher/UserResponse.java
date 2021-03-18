package me.rootatkali.justify.teacher;

import lombok.*;
import me.rootatkali.justify.model.User;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class UserResponse {
  private User user;
  private String cookies;
  private String csrfToken;
}