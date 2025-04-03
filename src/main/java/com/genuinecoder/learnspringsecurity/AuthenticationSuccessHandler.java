package com.genuinecoder.learnspringsecurity;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;

public class AuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        String targetUrl = determineTargetUrl(authentication);
        setDefaultTargetUrl(targetUrl);
        super.onAuthenticationSuccess(request, response, authentication);
    }

    private String determineTargetUrl(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .findFirst()
                .map(role -> {
                    switch (role) {
                        case "ROLE_CHEF_PROJET":
                            return "/chef-projet/home";
                        case "ROLE_TEST_LEADER":
                            return "/test-leader/home";
                        case "ROLE_TESTEUR":
                            return "/testeur/home";
                        default:
                            return "/user/home";
                    }
                })
                .orElse("/home");
    }
}