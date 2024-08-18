package net.de1mos.felix_ai.controllers;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;

import java.net.URI;

@RestController
@RequestMapping("spotify")
public class SpotifyLoginController {

    @Value("${spotify-client-id}")
    private String spotifyClientId;

    @Value("${spotify-client-secret}")
    private String spotifyClientSecret;

    @Value("${spotify-client-url}")
    private URI spotifyRedirectUrl;


    @SneakyThrows
    @GetMapping("/code")
    public ResponseEntity<String> getConfirmationCode(@RequestParam("code") String code) {
        System.out.println("hey, my code is: " + code);

        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(spotifyClientId)
                .setClientSecret(spotifyClientSecret)
                .setRedirectUri(spotifyRedirectUrl)
                .build();
        AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
        AuthorizationCodeCredentials codeCredentials = authorizationCodeRequest.execute();
        System.out.println("access token: " + codeCredentials.getAccessToken());
        System.out.println("refresh token: " + codeCredentials.getRefreshToken());
        System.out.println("expires in : " + codeCredentials.getExpiresIn());


        return ResponseEntity.ok(code);
    }
}
