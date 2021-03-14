package me.rootatkali.justify;

import me.rootatkali.justify.model.Role;
import me.rootatkali.justify.model.User;
import me.rootatkali.justify.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Controller
public class ContentController {
  @Autowired
  private UserRepository users;
  
  // see ApiController.java
  private void validateUser(String mashovId, UUID token, Role role) {
    User user = users.findById(mashovId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)
    );
    
    if (user.getRole() != role) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    if (!User.validateToken(user, mashovId, token)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
        "Token expired, please log out.");
  }
  
  @GetMapping(path = "/student/login")
  public String getLoginPage() {
    return "login";
  }
  
  @GetMapping(path = "/student")
  public String getStudentPage(@CookieValue(name = "mashovId") String mashovId, @CookieValue(name = "token") UUID token) {
    validateUser(mashovId, token, Role.STUDENT);
    return "student";
  }
  
  @GetMapping(path = "/teacher/login")
  public String getTeacherLoginPage() {
    return "teacher_login";
  }
  
  @GetMapping(path = "/teacher")
  public String getTeacherPage(@CookieValue(name = "mashovId") String mashovId, @CookieValue(name = "token") UUID token) {
    validateUser(mashovId, token, Role.TEACHER);
    return "teacher";
  }
  
  @GetMapping(path = "/privacy")
  public String getPrivacyPage() {
    return "privacy";
  }
}
