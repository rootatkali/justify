package me.rootatkali.justify;

import me.rootatkali.justify.model.Role;
import me.rootatkali.justify.model.User;
import me.rootatkali.justify.repo.AdminRepository;
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
  
  @Autowired
  private AdminRepository admin;
  
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
    try {
      validateUser(mashovId, token, Role.STUDENT);
    } catch (ResponseStatusException e) { // Redirect to login page if unauthorized
      if (e.getStatus() != HttpStatus.FORBIDDEN)
        return "login";
      throw e;
    }
    return "student";
  }
  
  @GetMapping(path = "/teacher/login")
  public String getTeacherLoginPage() {
    return "teacher_login";
  }
  
  @GetMapping(path = "/teacher")
  public String getTeacherPage(@CookieValue(name = "mashovId") String mashovId, @CookieValue(name = "token") UUID token) {
    try {
      validateUser(mashovId, token, Role.TEACHER);
    } catch (ResponseStatusException e) {
      if (e.getStatus() != HttpStatus.FORBIDDEN)
        return "teacher_login";
      throw e;
    }
    return "teacher";
  }
  
  @GetMapping(path = "/admin/requests")
  public String getAdminRequests(@CookieValue(name = "admin") UUID token) {
    System.out.println(token);
    System.out.println(admin.findAll());
    if (!admin.existsById(token)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    return "admin_requests";
  }
  
  @GetMapping(path = "/admin/users")
  public String getAdminUsers(@CookieValue(name = "admin") UUID token) {
    if (!admin.existsById(token)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    return "admin_users";
  }
  
  @GetMapping(path = "/privacy")
  public String getPrivacyPage() {
    return "privacy";
  }
}
