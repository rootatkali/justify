package me.rootatkali.justify.model;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
@ToString
@EqualsAndHashCode
public class Admin {
  @Id
  private String  token;
  private String representation;
  
  public Admin() {
  }
  
  public String getToken() {
    return token;
  }
  
  public void setToken(String token) {
    this.token = token;
  }
  
  public String getRepresentation() {
    return representation;
  }
  
  public void setRepresentation(String representation) {
    this.representation = representation;
  }
}
