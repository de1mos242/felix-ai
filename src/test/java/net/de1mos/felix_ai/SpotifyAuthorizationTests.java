package net.de1mos.felix_ai;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.SpotifyHttpManager;
import se.michaelthelin.spotify.enums.AuthorizationScope;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import se.michaelthelin.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import java.net.URI;
import java.util.Scanner;

@ActiveProfiles({"local", "test", "default"})
@SpringBootTest
class SpotifyAuthorizationTests {

    @Value("${spotify-client-id}")
    private String spotifyClientId;

    @Value("${spotify-client-secret}")
    private String spotifyClientSecret;

    @Value("${scode")
    private String scode;

    private static final URI redirectUri = SpotifyHttpManager.makeUri("http://localhost:8080/spotify/code");

    @SneakyThrows
    @Test
    void authorizeUsingCode() {
        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(spotifyClientId)
                .setClientSecret(spotifyClientSecret)
                .setRedirectUri(redirectUri)
                .build();

        AuthorizationCodeUriRequest uriRequest = spotifyApi.authorizationCodeUri()
                .scope(AuthorizationScope.USER_MODIFY_PLAYBACK_STATE, AuthorizationScope.USER_READ_PLAYBACK_STATE, AuthorizationScope.APP_REMOTE_CONTROL, AuthorizationScope.STREAMING, AuthorizationScope.USER_READ_CURRENTLY_PLAYING, AuthorizationScope.USER_READ_PLAYBACK_POSITION, AuthorizationScope.PLAYLIST_MODIFY_PUBLIC, AuthorizationScope.PLAYLIST_MODIFY_PRIVATE)
                .build();
        URI codeUri = uriRequest.execute();
        System.out.println(codeUri);

        Scanner in = new Scanner(System.in);

        String code = in.nextLine();

        AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
        AuthorizationCodeCredentials codeCredentials = authorizationCodeRequest.execute();
        System.out.println("access token: " + codeCredentials.getAccessToken());
        System.out.println("refresh token: " + codeCredentials.getRefreshToken());
        System.out.println("expires in : " + codeCredentials.getExpiresIn());
    }

}
