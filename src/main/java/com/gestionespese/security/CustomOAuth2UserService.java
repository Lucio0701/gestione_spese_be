package com.gestionespese.security;

import com.gestionespese.model.User;
import com.gestionespese.repository.UserRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String googleId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");

        userRepository.findByEmail(email).ifPresentOrElse(
            user -> {
                if (user.getGoogleId() == null) {
                    user.setGoogleId(googleId);
                    userRepository.save(user);
                }
            },
            () -> {
                User newUser = new User(
                    email,
                    UUID.randomUUID().toString(),
                    firstName != null ? firstName : "",
                    lastName != null ? lastName : ""
                );
                newUser.setGoogleId(googleId);
                userRepository.save(newUser);
            }
        );
        return oAuth2User;
    }
}
