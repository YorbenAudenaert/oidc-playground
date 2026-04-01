package com.audenyo.oidcplayground.controller;

import com.audenyo.oidcplayground.model.dto.UserInfoDto;
import com.audenyo.oidcplayground.service.AuthenticationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final AuthenticationService authenticationService;

    public HomeController(
            AuthenticationService authenticationService
    ) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/")
    public String home(Authentication authentication, Model model) {
        boolean authenticated = authentication != null && authentication.isAuthenticated();
        model.addAttribute("authenticated", authenticated);
        if (authenticated) {
            model.addAttribute("username", authentication.getName());
        }
        return "index";
    }

    @GetMapping("/user-info")
    public String userInfo(Authentication authentication, Model model) {
        UserInfoDto userInfo = authenticationService.extractUserInfo(authentication);
        model.addAttribute("userInfo", userInfo);
        return "user-info";
    }
}
