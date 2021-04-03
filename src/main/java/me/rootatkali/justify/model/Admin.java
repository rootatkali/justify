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
  private UUID token;
  private String representation;
  
  public Admin() {
  }
  
  public UUID getToken() {
    return token;
  }
  
  public void setToken(UUID token) {
    this.token = token;
  }
  
  public String getRepresentation() {
    return representation;
  }
  
  public void setRepresentation(String representation) {
    this.representation = representation;
  }
}
