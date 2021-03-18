package me.rootatkali.justify.model;

import javax.persistence.*;
import java.sql.Date;

@Entity
public class Request {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer requestId;
  private String mashovId;
  private Date dateStart;
  private Date dateEnd;
  private Integer periodStart;
  private Integer periodEnd;
  private Integer eventCode;
  private Integer justificationCode;
  private String note;
  private RequestStatus status;
  
  public Integer getRequestId() {
    return requestId;
  }
  
  public void setRequestId(Integer requestId) {
    this.requestId = requestId;
  }
  
  public String getMashovId() {
    return mashovId;
  }
  
  public void setMashovId(String mashovId) {
    this.mashovId = mashovId;
  }
  
  public Date getDateStart() {
    return dateStart;
  }
  
  public void setDateStart(Date dateStart) {
    this.dateStart = dateStart;
  }
  
  public Date getDateEnd() {
    return dateEnd;
  }
  
  public void setDateEnd(Date dateEnd) {
    this.dateEnd = dateEnd;
  }
  
  public Integer getPeriodStart() {
    return periodStart;
  }
  
  public void setPeriodStart(Integer periodStart) {
    this.periodStart = periodStart;
  }
  
  public Integer getPeriodEnd() {
    return periodEnd;
  }
  
  public void setPeriodEnd(Integer periodEnd) {
    this.periodEnd = periodEnd;
  }
  
  public Integer getEventCode() {
    return eventCode;
  }
  
  public void setEventCode(Integer eventCode) {
    this.eventCode = eventCode;
  }
  
  public Integer getJustificationCode() {
    return justificationCode;
  }
  
  public void setJustificationCode(Integer justificationCode) {
    this.justificationCode = justificationCode;
  }
  
  public String getNote() {
    return note;
  }
  
  public void setNote(String note) {
    this.note = note.replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll("\"", "&quot;")
        .replaceAll("'", "&#x27;");
    // XSS prevention
  }
  
  public RequestStatus getStatus() {
    return status;
  }
  
  public void setStatus(RequestStatus status) {
    this.status = status;
  }
}
