package com.example.sites.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;

@Controller
public class ErrorController {

    private static final Logger log = LoggerFactory.getLogger(ErrorController.class);

    @RequestMapping(value="/error", produces="application/json")
    @ResponseBody
    public Map<String, Object> handle(final HttpServletRequest request) {
        Map<String, Object> errorData = new LinkedHashMap<>();
        errorData.put("status", request.getAttribute("javax.servlet.error.status_code"));
        errorData.put("reaspm", request.getAttribute("javax.servlet.error.message"));
        log.error(String.valueOf(errorData));
        return errorData;
    }
}
