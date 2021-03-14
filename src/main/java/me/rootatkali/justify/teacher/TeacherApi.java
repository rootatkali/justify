package me.rootatkali.justify.teacher;

import de.faceco.mashovapi.API;
import de.faceco.mashovapi.components.School;

import java.io.IOException;
import java.util.Arrays;

public class TeacherApi {
  private static final String BASE_URL = "https://web.mashov.info/api";
  private static final int SCHOOL = 580019;
  private School school;
  
  private static TeacherApi singleton;
  
  private TeacherApi() {
  
  }
  
  public static TeacherApi getInstance() {
    if (singleton == null) {
      singleton = new TeacherApi();
    }
    
    return singleton;
  }
  
  public School getSchool() throws IOException {
    School s = Arrays.stream(API.getInstance().getAllSchools()).filter(sc -> sc.getId() == SCHOOL).findFirst().get();
    school = s;
    return s;
  }
}
