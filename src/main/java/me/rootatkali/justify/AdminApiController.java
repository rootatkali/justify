package me.rootatkali.justify;

import me.rootatkali.justify.model.*;
import me.rootatkali.justify.repo.AdminRepository;
import me.rootatkali.justify.repo.RequestRepository;
import me.rootatkali.justify.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping(path = "/api/admin")
public class AdminApiController {
  @Autowired
  private AdminRepository adminRepo;
  @Autowired
  private UserRepository userRepo;
  @Autowired
  private RequestRepository requestRepo;
  
  private static final Supplier<ResponseStatusException> NOT_FOUND =
      () -> new ResponseStatusException(HttpStatus.NOT_FOUND);
  
  private void verify(UUID token) {
    adminRepo.findById(token).orElseThrow(
        () -> new ResponseStatusException(HttpStatus.FORBIDDEN)
    );
  }
  
  @GetMapping(path = "/requests")
  public Iterable<Request> getRequests(@CookieValue(name = "admin") UUID token) {
    verify(token);
    
    return requestRepo.findAll();
  }
  
  @GetMapping(path = "/requests/{id}")
  public Request getRequest(@CookieValue(name = "admin") UUID token, @PathVariable Integer id) {
    verify(token);
    
    return requestRepo.findById(id).orElseThrow(NOT_FOUND);
  }
  
  @GetMapping(path = "/users/{id}/count")
  public Integer getUserRequestCount(@CookieValue(name = "admin") UUID token, @PathVariable String id) {
    verify(token);
    userRepo.findById(id).orElseThrow(NOT_FOUND);
  
    return StreamSupport.stream(requestRepo.findAll().spliterator(), false)
        .filter(r -> r.getMashovId().equals(id))
        .mapToInt(r -> 1).sum();
  }
  
  @GetMapping(path = "/users")
  public Iterable<User> getUsers(@CookieValue(name = "admin") UUID token) {
    verify(token);
    
    return userRepo.findAll();
  }
  
  @GetMapping(path = "/users/{id}")
  public User getUser(@CookieValue(name = "admin") UUID token, @PathVariable String id) {
    verify(token);
    
    return userRepo.findById(id).orElseThrow(NOT_FOUND);
  }
  
  private Request setStatus(Integer id, RequestStatus status) {
    Request r = requestRepo.findById(id).orElseThrow(NOT_FOUND);
    r.setStatus(status);
    return requestRepo.save(r);
  }
  
  @PostMapping(path = "/requests/{id}/approve")
  public Request setApproved(@CookieValue(name = "admin") UUID token, @PathVariable Integer id) {
    verify(token);
    
    return setStatus(id, RequestStatus.APPROVED);
  }
  
  
  @PostMapping(path = "/requests/{id}/reject")
  public Request setRejected(@CookieValue(name = "admin") UUID token, @PathVariable Integer id) {
    verify(token);
    
    return setStatus(id, RequestStatus.REJECTED);
  }
  
  @PostMapping(path = "/requests/{id}/cancel")
  public Request setCancelled(@CookieValue(name = "admin") UUID token, @PathVariable Integer id) {
    verify(token);
    
    return setStatus(id, RequestStatus.CANCELLED);
  }
  
  @PostMapping(path = "/requests/{id}/reset")
  public Request setUnanswered(@CookieValue(name = "admin") UUID token, @PathVariable Integer id) {
    verify(token);
    
    return setStatus(id, RequestStatus.UNANSWERED);
  }
  
  @DeleteMapping(path = "/requests/{id}")
  public void deleteRequest(@CookieValue(name = "admin") UUID token, @PathVariable Integer id) {
    verify(token);
    
    requestRepo.deleteById(id);
  }
  
  @PutMapping(path = "/requests/{id}")
  public Request editRequest(@CookieValue(name = "admin") UUID token, @PathVariable Integer id, @RequestBody RequestTemplate template) {
    verify(token);
    
    Request r = requestRepo.findById(id).orElseThrow(NOT_FOUND);
    
    if (template.getDateStart() != null) r.setDateStart(template.getDateStart());
    if (template.getPeriodStart() != null) r.setPeriodStart(template.getPeriodStart());
    if (template.getDateEnd() != null) r.setDateEnd(template.getDateEnd());
    if (template.getPeriodEnd() != null) r.setPeriodEnd(template.getPeriodEnd());
    if (template.getJustificationCode() != null) r.setJustificationCode(template.getJustificationCode());
    if (template.getEventCode() != null) r.setEventCode(template.getEventCode());
    if (template.getNote() != null) r.setNote(template.getNote());
    
    return requestRepo.save(r);
  }
  
  @PostMapping(path = "/users/{id}/name")
  public User setUserName(@CookieValue(name = "admin") UUID token,
                          @PathVariable String id,
                          @RequestParam String first,
                          @RequestParam String last) {
    verify(token);
    
    User u = userRepo.findById(id).orElseThrow(NOT_FOUND);
    u.setFirstName(Xss.deXss(first));
    u.setLastName(Xss.deXss(last));
    return userRepo.save(u);
  }
  
  @GetMapping(path = "/")
  public Iterable<Admin> getAdmin() {
    return adminRepo.findAll();
  }
}
