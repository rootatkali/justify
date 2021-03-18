package me.rootatkali.justify.teacher;

import de.faceco.mashovapi.API;
import de.faceco.mashovapi.components.School;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.stream.IntStream;

@Getter @Setter @ToString @EqualsAndHashCode
public class TeacherLogin {
  private int semel;
  private String username;
  private String password;
  private int year;
  private String appName;
  private String apiVersion;
  private String appVersion;
  private String appBuild;
  private String deviceUuid;
  private String devicePlatform;
  private String deviceManufacturer;
  private String deviceModel;
  private String deviceVersion;
  
  public TeacherLogin(School school, int year, String username, String password) {
    // Check if the requested year is present in Mashov's database for this school.
    if (IntStream.of(school.getYears()).noneMatch(x -> x == year)) {
      throw new IllegalArgumentException("Year not in school!");
    }
    this.year = year;
    
    semel = school.getId();
    this.username = username;
    this.password = password;
    appName = "info.mashov.teachers";
    appBuild = apiVersion = appVersion = "3.20200528";
    devicePlatform = deviceUuid = "chrome";
    deviceManufacturer = "windows";
    deviceModel = "desktop";
    deviceVersion = "83.0.4103.61";
  }

  public TeacherLogin(School school, String username, String password) {
    this(school, school.getCurrentYear(), username, password);
  }
}
