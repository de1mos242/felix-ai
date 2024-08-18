package net.de1mos.felix_ai.assistant.music;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles({"local", "test", "default"})
@SpringBootTest
class SpotifyClientTest {

    @Autowired SpotifyClient spotifyClient;

    @Test
    void playTrack() {
//        spotifyClient.playTrack("spotify:track:7uBeD5cr7NkqJUh6eJMVVk");
        spotifyClient.playTrack("spotify:track:0XyN1YW1uBEoOxrljLEYCJ");
    }

    @Test
    void stop() {
        spotifyClient.stop();
    }
}