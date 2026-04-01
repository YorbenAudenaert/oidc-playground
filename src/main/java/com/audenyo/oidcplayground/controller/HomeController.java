package com.audenyo.oidcplayground.controller;

import com.audenyo.oidcplayground.model.dto.UserInfoDto;
import com.audenyo.oidcplayground.service.AuthenticationService;
import com.audenyo.oidcplayground.service.RefreshTokenStorageService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final AuthenticationService authenticationService;
    private final RefreshTokenStorageService refreshTokenStorageService;

    public HomeController(
            AuthenticationService authenticationService,
            RefreshTokenStorageService refreshTokenStorageService
    ) {
        this.authenticationService = authenticationService;
        this.refreshTokenStorageService = refreshTokenStorageService;
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

        if (authentication != null && authentication.getPrincipal() instanceof OidcUser oidcUser) {
            refreshTokenStorageService.findBySubject(oidcUser.getSubject())
                    .ifPresent(token -> model.addAttribute("storedToken", token));
        }

        return "user-info";
    }
}
