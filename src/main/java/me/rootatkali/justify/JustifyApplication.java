package me.rootatkali.justify;

import de.faceco.mashovapi.API;
import de.faceco.mashovapi.components.Achva;
import me.rootatkali.justify.model.Admin;
import me.rootatkali.justify.model.Event;
import me.rootatkali.justify.model.Justification;
import me.rootatkali.justify.repo.AdminRepository;
import me.rootatkali.justify.repo.EventRepository;
import me.rootatkali.justify.repo.JustificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;

@SpringBootApplication
public class JustifyApplication {

  public static void main(String[] args) {
    SpringApplication.run(JustifyApplication.class, args);
  }
  
  @Autowired
  private EventRepository eventRepository;
  @Autowired
  private JustificationRepository justificationRepository;
  @Autowired
  private AdminRepository adminRepository;
  
  @PostConstruct
  private void initSql() throws IOException {
    if (adminRepository.count() < 1) {
      Admin admin = new Admin();
      admin.setToken(UUID.randomUUID().toString());
      admin.setRepresentation(admin.getToken());
      adminRepository.save(admin);
    }
    
    API api = API.getInstance();
    if (api.getSchool() == null || api.getSchool().getId() != 580019) api.fetchSchool(580019);
    api.login(2021, "rotemoses", "mashov2021");
    
    for (Achva a : api.getAchvas()) {
      // add event codes to sql
      if (!eventRepository.existsById(a.getCode()) && !a.isHidden()) {
        Event e = new Event();
        e.setCode(a.getCode());
        e.setName(a.getName());
        eventRepository.save(e);
      }
    }
    
    for (de.faceco.mashovapi.components.Justification jm : api.getJustifications()) {
      // add justifications to sql
      if (!justificationRepository.existsById(jm.getJustificationId()) && !jm.isHidden() && jm.getJustificationId() > 0) {
        Justification j = new Justification();
        j.setCode(jm.getJustificationId());
        j.setName(jm.getJustification());
        justificationRepository.save(j);
      }
    }
    
    api.logout();
  }
}
