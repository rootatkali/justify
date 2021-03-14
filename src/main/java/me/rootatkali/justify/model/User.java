package me.rootatkali.justify.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Time;
import java.time.LocalTime;
import java.util.Date;
import java.util.UUID;

@Entity
public class User {
  @Id
  private String mashovId;
  private String firstName;
  private String lastName;
  private Role role;
  private UUID token;
  private Time tokenExpires;
  
  
  public String getMashovId() {
    return mashovId;
  }
  
  public void setMashovId(String mashovId) {
    this.mashovId = mashovId;
  }
  
  public String getFirstName() {
    return firstName;
  }
  
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }
  
  public String getLastName() {
    return lastName;
  }
  
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }
  
  public Role getRole() {
    return role;
  }
  
  public void setRole(Role role) {
    this.role = role;
  }
  
  public UUID getToken() {
    return token;
  }
  
  public void setToken() {
    this.token = UUID.randomUUID();
  }
  
  public Time getTokenExpires() {
    return tokenExpires;
  }
  
  public void setTokenExpires() {
    this.tokenExpires = Time.valueOf(LocalTime.now().plusMinutes(90));
  }
  
  public static boolean validateToken(User user,String mashovId, UUID token) {
    if (user.token == null || user.tokenExpires == null) return false;
    
    return user.mashovId.equals(mashovId) &&
        user.token.equals(token) &&
        user.tokenExpires.toLocalTime().isAfter(LocalTime.now());
  }
  
  public void clearToken() {
    token = null;
    tokenExpires = null;
  }
}
