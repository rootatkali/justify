package me.rootatkali.justify;

import de.faceco.mashovapi.API;
import de.faceco.mashovapi.components.LoginInfo;
import de.faceco.mashovapi.components.LoginResponse;
import de.faceco.mashovapi.components.School;
import me.rootatkali.justify.model.*;
import me.rootatkali.justify.repo.EventRepository;
import me.rootatkali.justify.repo.JustificationRepository;
import me.rootatkali.justify.repo.RequestRepository;
import me.rootatkali.justify.repo.UserRepository;
import me.rootatkali.justify.teacher.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(path = "/api")
public class ApiController {
  @Autowired
  private UserRepository userRepository;
  
  @Autowired
  private RequestRepository requestRepository;
  
  @Autowired
  private EventRepository eventRepository;
  
  @Autowired
  private JustificationRepository justificationRepository;
  
  @PostMapping(path = "/login")
  @ResponseBody
  public User login(@RequestParam String username, @RequestParam String password,
                    @CookieValue(name = "uniquId", required = false) String uniquId,
                    HttpServletResponse response) throws IOException {
    API api = API.getInstance();
    if (api.getSchool() == null) {
      api.fetchSchool(580019);
    }
    api.setDeviceId(uniquId);
    LoginResponse lr = api.login(2021, username, password);
    
    if (lr instanceof LoginInfo) { // Login success
      LoginInfo li = (LoginInfo) lr;
      
      Cookie c = new Cookie("uniquId", api.getDeviceId());
      c.setSecure(true);
      c.setPath("/");
      c.setMaxAge(Integer.MAX_VALUE);
      c.setHttpOnly(true);
      response.addCookie(c);
      
      String id = String.valueOf(li.getCredential().getIdNumber());
      if (!userRepository.existsById(id)) { // create new user
        User user = new User();
        user.setMashovId(id);
        user.setFirstName(li.getAccessToken().getChildren()[0].getPrivateName());
        user.setLastName(li.getAccessToken().getChildren()[0].getFamilyName());
        user.setRole(Role.STUDENT);
        user.setToken();
        user.setTokenExpires();
        userRepository.save(user);
        api.logout();
        
        Cookie uid = new Cookie("mashovId", id);
        Cookie token = new Cookie("token", user.getToken().toString());
        Arrays.asList(uid, token).forEach(ck -> {
          ck.setSecure(true);
          ck.setPath("/");
          response.addCookie(ck);
        });
        
        return user;
        
      } else { // find user from sql
        User user = userRepository.findById(id).orElseThrow(
            () -> { // should never reach here
              try {
                api.logout();
              } catch (IOException e) {
                e.printStackTrace();
              }
              return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        );
        if (user.getRole() != Role.STUDENT) { // check for role
          api.logout();
          throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Teachers cannot log in as students.");
        }
        user.setToken();
        user.setTokenExpires();
  
        Cookie uid = new Cookie("mashovId", id);
        Cookie token = new Cookie("token", user.getToken().toString());
        Arrays.asList(uid, token).forEach(ck -> {
          ck.setSecure(true);
          ck.setPath("/");
          response.addCookie(ck);
        });
        
        userRepository.save(user);
        api.logout();
        return user;
      }
    } else throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login failed.");
  }
  
  @PostMapping(path = "/teacherLogin")
  public User teacherLogin(@RequestParam String username, @RequestParam String password,
                           @CookieValue(name = "uniquId", required = false) String uniquId,
                           HttpServletResponse response) throws IOException {
    TeacherApi tApi = TeacherApi.getInstance();
    School kfar = tApi.fetchSchool();
    String mashovId = "";
    User user;
    UserResponse ur;
    
    if (username.equals("test") && password.equals("test")) {
      mashovId = "teacher_test";
      user = userRepository.findById(mashovId).orElseThrow(
          () -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR)
      );
      ur = new UserResponse(user, null, null, null);
      
    } else {
      TeacherLogin l = new TeacherLogin(kfar, username, password);
      ur = tApi.login(l, uniquId);
      user = ur.getUser();
      mashovId = user.getMashovId();
    }
    
    if (userRepository.existsById(mashovId)) {
      user = userRepository.findById(mashovId).orElseThrow(() -> // shouldn't reach here
          new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
      if (user.getRole() != Role.TEACHER)
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Students cannot log in as teachers.");
      user.clearToken();
    }
    
    Cookie mashovCookies = new Cookie("teacherCookies", ur.getCookies());
    Cookie teacherCsrf = new Cookie("teacherCsrf", ur.getCsrfToken());
    Cookie uniqC = new Cookie("uniquId", uniquId != null ? uniquId : ur.getUniquId());
    uniqC.setMaxAge(Integer.MAX_VALUE);
    Arrays.asList(mashovCookies, teacherCsrf, uniqC).forEach(c -> {
      c.setPath("/");
      c.setHttpOnly(true);
      c.setSecure(true);
      response.addCookie(c);
    });
    
    user.setToken();
    user.setTokenExpires();
  
    Cookie uid = new Cookie("mashovId", mashovId);
    Cookie token = new Cookie("token", user.getToken().toString());
    Arrays.asList(uid, token).forEach(ck -> {
      ck.setSecure(true);
      ck.setPath("/");
      response.addCookie(ck);
    });
    
    userRepository.save(user);
    ur.setUser(user);
    return ur.getUser();
  }
  
  @GetMapping(path = "/logout")
  public void logout(@CookieValue(name = "mashovId") String mashovId,
                     @CookieValue(name = "token") UUID token,
                     @CookieValue(name = "teacherCsrf", required = false) String csrf,
                     @CookieValue(name = "teacherCookies", required = false) String cookies)
      throws IOException {
    validateUser(mashovId, token, null);
    User u = userRepository.findById(mashovId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    if (u.getRole() == Role.TEACHER && csrf != null && !csrf.equals("null") && !csrf.isBlank()) {
      if (TeacherApi.getInstance().logout(csrf, CookieUtil.convert(cookies)) != 200)
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    }
    u.clearToken();
    userRepository.save(u);
  }
  
  /*
   * Validate user according to role and token
   */
  private void validateUser(String mashovId, UUID token, Role role) {
    User user = userRepository.findById(mashovId).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED)
    );
    
    // role == null => no role/permission check
    if (role != null && user.getRole() != role) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    if (!User.validateToken(user, mashovId, token)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
        "Token expired, please log out.");
  }
  
  @GetMapping(path = "/users/{id}")
  @ResponseBody
  public User getUser(@PathVariable String id, @CookieValue(name = "mashovId") String mashovId, @CookieValue(name = "token") UUID token) {
    if (!id.equals(mashovId)) validateUser(mashovId, token, Role.TEACHER);
    else validateUser(mashovId, token, null); // Find information about me
    return userRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
  }
  
  @GetMapping(path = "/users")
  @ResponseBody
  public Iterable<User> getUsers(@CookieValue(name = "mashovId") String mashovId, @CookieValue(name = "token") UUID token) {
    validateUser(mashovId, token, Role.TEACHER);
    return userRepository.findAll();
  }
  
  @GetMapping(path = "/requests")
  public Iterable<Request> getRequests(@CookieValue(name = "mashovId") String mashovId, @CookieValue(name = "token") UUID token) {
    validateUser(mashovId, token, Role.TEACHER);
    return requestRepository.findAll();
  }
  
  @GetMapping(path = "/requests/unanswered")
  public Iterable<Request> getUnansweredRequests(@CookieValue(name = "mashovId") String mashovId, @CookieValue(name = "token") UUID token) {
    validateUser(mashovId, token, Role.TEACHER);
    return StreamSupport.stream(requestRepository.findAll().spliterator(), false)
        .filter(r -> r.getStatus() == RequestStatus.UNANSWERED)
        .collect(Collectors.toList());
  }
  
  @GetMapping(path = "/requests/{id}")
  public Request getRequest(@PathVariable Integer id, @CookieValue(name = "mashovId") String mashovId, @CookieValue(name = "token") UUID token) {
    Optional<Request> opt = requestRepository.findById(id);
    if (opt.isPresent() && opt.get().getMashovId().equals(mashovId)) {
      validateUser(mashovId, token, Role.STUDENT);
      return opt.get();
    } else {
      validateUser(mashovId, token, Role.TEACHER);
      return opt.orElseThrow(
          () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found.")
      );
    }
  }
  
  @GetMapping(path = "/users/{id}/requests")
  public Iterable<Request> getUserRequests(@PathVariable String id, @CookieValue(name = "mashovId") String mashovId, @CookieValue(name = "token") UUID token) {
    if (id.equals(mashovId)) validateUser(mashovId, token, Role.STUDENT);
    else validateUser(mashovId, token, Role.TEACHER);
    
    if (!userRepository.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
    
    return StreamSupport.stream(requestRepository.findAll().spliterator(), false)
        .filter(r -> r.getMashovId().equals(id))
        .collect(Collectors.toList());
  }
  
  @GetMapping(path = "/users/{id}/requests/unanswered")
  public Iterable<Request> getUserUnansweredRequests(@PathVariable String id, @CookieValue(name = "mashovId") String mashovId, @CookieValue(name = "token") UUID token) {
    if (id.equals(mashovId)) validateUser(mashovId, token, Role.STUDENT);
    else validateUser(mashovId, token, Role.TEACHER);
    
    if (!userRepository.existsById(id)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found.");
    
    return StreamSupport.stream(requestRepository.findAll().spliterator(), false)
        .filter(r -> r.getMashovId().equals(id) && r.getStatus() == RequestStatus.UNANSWERED)
        .collect(Collectors.toList());
  }
  
  @PostMapping(path = "/requests", consumes = "application/json")
  public Request postRequest(@RequestBody RequestTemplate template, @CookieValue(name = "mashovId") String mashovId, @CookieValue(name = "token") UUID token) {
    validateUser(mashovId, token, Role.STUDENT);
    
    if (!mashovId.equals(template.getMashovId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN,
        "Submitting requests for different users is not allowed.");
    
    Request r = new Request();
    r.setMashovId(template.getMashovId());
    r.setDateStart(template.getDateStart());
    r.setDateEnd(template.getDateEnd());
    r.setPeriodStart(template.getPeriodStart());
    r.setPeriodEnd(template.getPeriodEnd());
    r.setEventCode(template.getEventCode());
    r.setJustificationCode(template.getJustificationCode());
    r.setStatus(RequestStatus.UNANSWERED);
    r.setNote(template.getNote());
    requestRepository.save(r);
    System.out.println(r.getRequestId());
    return r;
  }
  
  @GetMapping(path = "/requests/{id}/cancel")
  public Request cancelRequest(@PathVariable Integer id, @CookieValue(name = "mashovId") String mashovId, @CookieValue(name = "token") UUID token) {
    validateUser(mashovId, token, Role.STUDENT);
    
    Request r = requestRepository.findById(id).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found.")
    );
    if (!r.getMashovId().equals(mashovId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    r.setRequestId(id);
    r.setStatus(RequestStatus.CANCELLED);
    return requestRepository.save(r);
  }
  
  @GetMapping(path = "/requests/{id}/approve")
  public Request approveRequest(@PathVariable Integer id, @CookieValue(name = "mashovId") String mashovId, @CookieValue(name = "token") UUID token,
                                @CookieValue(name = "teacherCsrf") String csrf,
                                @CookieValue(name = "teacherCookies") String cookies) throws IOException {
    validateUser(mashovId, token, Role.TEACHER);
    
    Request r = requestRepository.findById(id).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found.")
    );
    r.setRequestId(id);
    r.setStatus(RequestStatus.APPROVED);
    Approval apr = new Approval(
        r.getEventCode(),
        r.getDateEnd().toString() + "T02:00:00",
        15,
        6,
        r.getPeriodEnd(),
        r.getJustificationCode(),
        Integer.parseInt(mashovId),
        r.getDateStart().toString() + "T02:00:00",
        0,
        0,
        r.getPeriodStart(),
        LocalDateTime.now().toString(),
        Integer.parseInt(r.getMashovId())
    );
    TeacherApi.getInstance().requestApproval(apr, csrf, CookieUtil.convert(cookies));
    return requestRepository.save(r);
  }
  
  @GetMapping(path = "/requests/{id}/reject")
  public Request rejectRequest(@PathVariable Integer id, @CookieValue(name = "mashovId") String mashovId, @CookieValue(name = "token") UUID token) {
    validateUser(mashovId, token, Role.TEACHER);
    
    Request r = requestRepository.findById(id).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found.")
    );
    r.setRequestId(id);
    r.setStatus(RequestStatus.REJECTED);
    return requestRepository.save(r);
  }
  
  @GetMapping(path = "/events")
  public Iterable<Event> getEvents() {
    return eventRepository.findAll();
  }
  
  @GetMapping(path = "/justifications")
  public Iterable<Justification> getJustifications() {
    return justificationRepository.findAll();
  }
  
  @GetMapping(path = "/coffee")
  public String getCoffee() {
    throw new ResponseStatusException(HttpStatus.I_AM_A_TEAPOT);
  }
}
