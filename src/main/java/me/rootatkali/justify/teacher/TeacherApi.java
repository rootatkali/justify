package me.rootatkali.justify.teacher;

import com.google.gson.Gson;
import com.jayway.jsonpath.JsonPath;
import de.faceco.mashovapi.API;
import de.faceco.mashovapi.components.School;
import me.rootatkali.justify.model.Role;
import me.rootatkali.justify.model.User;
import okhttp3.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeacherApi {
  private static final String BASE_URL = "https://web.mashov.info/api";
  private static final int SCHOOL = 580019;
  private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) " +
      "Chrome/81.0.4044.122 Safari/537.36";
  private School school;
  private OkHttpClient http;
  private Gson gson;
  
  private static TeacherApi singleton;
  
  private TeacherApi() {
    http = new OkHttpClient.Builder().build();
    gson = new Gson();
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
  
  public School fetchSchool() throws IOException {
    Request req = new Request.Builder()
        .url(BASE_URL + "/schools")
        .method("GET", null)
        .build();
    
    Response res = http.newCall(req).execute();
    School[] sls = gson.fromJson(res.body().string(), School[].class);
    school = Arrays.stream(sls).filter(s -> s.getId() == SCHOOL).findAny().get();
    return school;
  }
  
  public UserResponse login(TeacherLogin l, String uniquId) throws IOException {
    MediaType json = MediaType.parse("application/json");
    RequestBody body = RequestBody.create(json, gson.toJson(l));
    
    Request.Builder builder = new Request.Builder()
        .url(BASE_URL + "/login")
        .method("POST", body)
        .addHeader("User-Agent", USER_AGENT)
        .addHeader("Content-Type", "application/json");
    if (uniquId != null && !uniquId.equals("")) {
      builder.addHeader("Cookie", "uniquId=" + uniquId);
    }
    Request req = builder.build();
    
    Response res = http.newCall(req).execute();
    if (!res.isSuccessful()) {
      System.err.println(res.body().string());
      throw new ResponseStatusException(HttpStatus.valueOf(res.code()));
    }
    
    HashMap<String, String> cookies = new HashMap<>();
    
    Headers headers = res.headers();
    String csrfToken = Cookie.parse(req.url(), headers.get("Set-Cookie")).value();
    List<String> rawCookies = headers.values("Set-Cookie");
    for (String c : rawCookies) {
      String[] split = c.trim().split("=", 2);
      if (!cookies.containsKey(split[0])) {
        cookies.put(split[0], split[1].substring(0, split[1].indexOf(";")));
      }
    }
    
    String rb = res.body().string();

    System.err.println("SUCCESS\n\n" + rb);
    
    User u = new User();
    u.setMashovId(String.valueOf((Integer) JsonPath.read(rb, "$.credential.idNumber")));
    String name = JsonPath.read(rb, "$.accessToken.displayName");
    String[] names = name.split(" ", 2);
    u.setFirstName(names[1]);
    u.setLastName(names[0]);
    u.setRole(Role.TEACHER);
    String uniqRet = cookies.getOrDefault("uniquId", null);
    return new UserResponse(u, CookieUtil.convert(cookies, "~"), csrfToken, uniqRet);
  }
  
  private String cookieHeader(HashMap<String, String> cookies) {
    return CookieUtil.convert(cookies, ";");
  }
  
  public ApprovalResponse requestApproval(Approval apr, String csrf, HashMap<String, String> cookies) throws IOException {
    MediaType json = MediaType.parse("application/json");
    RequestBody body = RequestBody.create(json, gson.toJson(apr));
    Request req = new Request.Builder()
        .url(BASE_URL + "/futureJustifications")
        .method("POST", body)
        .addHeader("User-Agent", USER_AGENT)
        .addHeader("x-csrf-token", csrf)
        .addHeader("Cookie", cookieHeader(cookies))
        .build();
    Response res = http.newCall(req).execute();
  
    String rb = res.body().string();
    if (!res.isSuccessful()) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
    System.err.println(res.code());
    System.err.println(rb);
    return gson.fromJson(rb, ApprovalResponse.class);
  }
  
  public int logout(String csrf, HashMap<String, String> cookies) throws IOException {
    Request req = new Request.Builder()
        .url(BASE_URL + "/logout")
        .method("GET", null)
        .addHeader("User-Agent", USER_AGENT)
        .addHeader("Cookie", cookieHeader(cookies))
        .addHeader("X-Csrf-Token", csrf)
        .build();
    return http.newCall(req).execute().code();
  }
}
