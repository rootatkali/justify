package me.rootatkali.justify;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;

@Controller
public class ErrorContentController implements ErrorController {
  @RequestMapping("/error")
  public String handleError(HttpServletRequest request) {
    Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
    if (status != null) {
      int code = Integer.parseInt(status.toString());
      switch (code) {
        case 400:
          return "400";
        case 401:
          return "401";
        case 403:
          return "403";
        case 404:
          return "404";
        case 500:
          return "500";
      }
    }
    return "error";
  }
  
  @Override
  public String getErrorPath() {
    return null;
  }
}
