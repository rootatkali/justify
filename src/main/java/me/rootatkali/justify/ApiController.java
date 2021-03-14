package me.rootatkali.justify;

import de.faceco.mashovapi.API;
import de.faceco.mashovapi.components.LoginInfo;
import de.faceco.mashovapi.components.LoginResponse;
import me.rootatkali.justify.model.*;
import me.rootatkali.justify.repo.EventRepository;
import me.rootatkali.justify.repo.JustificationRepository;
import me.rootatkali.justify.repo.RequestRepository;
import me.rootatkali.justify.repo.UserRepository;
import me.rootatkali.justify.teacher.TeacherApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
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
  public User login(@RequestParam String username, @RequestParam String password) throws IOException {
    API api = API.getInstance();
    if (api.getSchool() == null) {
      api.fetchSchool(580019);
    }
    LoginResponse lr = api.login(2021, username, password);
    
    if (lr instanceof LoginInfo) { // Login success
      LoginInfo li = (LoginInfo) lr;
      
      if (!userRepository.existsById(li.getCredential().getUserId())) { // create new user
        User user = new User();
        user.setMashovId(li.getCredential().getUserId());
        user.setFirstName(li.getAccessToken().getChildren()[0].getPrivateName());
        user.setLastName(li.getAccessToken().getChildren()[0].getFamilyName());
        user.setRole(Role.STUDENT);
        user.setToken();
        user.setTokenExpires();
        userRepository.save(user);
        api.logout();
        return user;
        
      } else { // find user from sql
        User user = userRepository.findById(li.getCredential().getUserId()).orElseThrow(
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
        userRepository.save(user);
        api.logout();
        return user;
      }
    } else throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login failed.");
  }
  
  @PostMapping(path = "/teacherLogin")
  public User teacherLogin(@RequestParam String username, @RequestParam String password) {
    TeacherApi tApi = TeacherApi.getInstance();
    
    String mashovId = "";
    
    // TODO insert teacher login code
    
    if (username.equals("test") && password.equals("test")) { // TODO remove after logic
      mashovId = "teacher_test";
    }
    
    User user;
    if (!userRepository.existsById(mashovId)) {
      user = new User();
      // TODO set user params
      user.setRole(Role.TEACHER);
    } else {
      user = userRepository.findById(mashovId).orElseThrow(() -> // shouldn't reach here
          new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
      if (user.getRole() != Role.TEACHER)
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Students cannot log in as teachers.");
      user.clearToken();
    }
    user.setToken();
    user.setTokenExpires();
    return userRepository.save(user);
  }
  
  @GetMapping(path = "/logout")
  public void logout(@CookieValue(name = "mashovId") String mashovId, @CookieValue(name = "token") UUID token) {
    User u = userRepository.findById(mashovId).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
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
    
    if (user.getRole() != role) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
    if (!User.validateToken(user, mashovId, token)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
        "Token expired, please log out.");
  }
  
  @GetMapping(path = "/users/{id}")
  @ResponseBody
  public User getUser(@PathVariable String id, @CookieValue(name = "mashovId") String mashovId, @CookieValue(name = "token") UUID token) {
    if (!id.equals(mashovId)) validateUser(mashovId, token, Role.TEACHER);
    else validateUser(mashovId, token, Role.STUDENT); // Find information about me
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
  
  @GetMapping(path = "/requests/{id}/approve")
  public Request approveRequest(@PathVariable Integer id, @CookieValue(name = "mashovId") String mashovId, @CookieValue(name = "token") UUID token) {
    validateUser(mashovId, token, Role.TEACHER);
    
    Request r = requestRepository.findById(id).orElseThrow();
    r.setRequestId(id);
    r.setStatus(RequestStatus.APPROVED);
    // TODO insert mashov approval logic
    return requestRepository.save(r);
  }
  
  @GetMapping(path = "/requests/{id}/reject")
  public Request rejectRequest(@PathVariable Integer id, @CookieValue(name = "mashovId") String mashovId, @CookieValue(name = "token") UUID token) {
    validateUser(mashovId, token, Role.TEACHER);
    
    Request r = requestRepository.findById(id).orElseThrow();
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
