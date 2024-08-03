package com.rspell.sites;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SessionRecorderInterceptor implements HandlerInterceptor {

    // logger

    // TODO REDO
    @Autowired
    private HttpSession session;

    // public boolean preHandle
    //      start time

    // private boolean shouldIgnore
    // requestURI.contains("images")

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView model) {
        // if shouldIgnore return

        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        // pragma no cache
        // expires 0
        // xrobotstag, none, noarchive

        // execution time
    }
}
