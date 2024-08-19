package net.de1mos.felix_ai.assistant.music;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.miscellaneous.CurrentlyPlayingContext;
import se.michaelthelin.spotify.model_objects.miscellaneous.Device;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Service
public class SpotifyClient {

    record Playlist(String id, String uri) {}

    public static final String FELIX_PLAYLIST_NAME = "FelixAI";
    @Value("${spotify-client-id}")
    private String spotifyClientId;

    @Value("${spotify-client-secret}")
    private String spotifyClientSecret;

    @Value("${spotify-client-url}")
    private URI spotifyRedirectUrl;

    @Value("${spotify-refresh-token}")
    private String spotifyRefreshToken;

    @Value("${spotify-device-name}")
    private String deviceName;

    @SneakyThrows
    public void playTrack(String trackUrl) {
        SpotifyApi spotifyApi = getSpotifyApi();
        Device device = findDevice(spotifyApi);

        var playlist = assumeFelixPlaylist(spotifyApi);

        var trackId = Arrays.stream(trackUrl.split(":")).reduce((a, b) -> b).orElseThrow();
        Recommendations recommendations = spotifyApi.getRecommendations().seed_tracks(trackId).limit(20).build().execute();
        String[] recommendationTracks = Arrays.stream(recommendations.getTracks()).filter(t -> !t.getIsExplicit()).map(Track::getUri).toArray(String[]::new);
        String[] tracks = ArrayUtils.addFirst(recommendationTracks, trackUrl);
        log.info("current playlist1: {}", spotifyApi.getPlaylist(playlist.id).build().execute());
        spotifyApi.replacePlaylistsItems(playlist.id, tracks).build().execute();
        log.info("current playlist2: {}", spotifyApi.getPlaylist(playlist.id).build().execute());


        var deviceId = device.getId();
        JsonObject trackOffset = new JsonObject();
        trackOffset.add("uri", new JsonPrimitive(trackUrl));
        CurrentlyPlayingContext playingContext = spotifyApi.getInformationAboutUsersCurrentPlayback().build().execute();
        log.info("Current playing state: {}", playingContext);
        if (playingContext.getIs_playing()) {
            spotifyApi.pauseUsersPlayback().device_id(deviceId).build().execute();
        }
        log.info("current playlist3: {}", spotifyApi.getPlaylist(playlist.id).build().execute());

        spotifyApi.startResumeUsersPlayback().device_id(deviceId).context_uri(playlist.uri()).offset(trackOffset).build().execute();
        log.info("current playlist4: {}", spotifyApi.getPlaylist(playlist.id).build().execute());

        CurrentlyPlayingContext playingContextAfter = spotifyApi.getInformationAboutUsersCurrentPlayback().build().execute();
        log.info("Current playing state after run: {}", playingContext);
        if (!playingContextAfter.getIs_playing()) {
            spotifyApi.startResumeUsersPlayback().device_id(deviceId).context_uri(playlist.uri()).build().execute();
        }
        log.info("current playlist5: {}", spotifyApi.getPlaylist(playlist.id).build().execute());


//        spotifyApi.startResumeUsersPlayback().device_id(deviceId).context_uri(playlist.uri()).build().execute();
//        addToQueue(spotifyApi, trackUrl, deviceId);
//        spotifyApi.skipUsersPlaybackToNextTrack().device_id(deviceId).build().execute();
//        Arrays.stream(recommendations.getTracks()).forEach(t-> addToQueue(spotifyApi, t.getUri(), deviceId));
    }

    @SneakyThrows
    private static void addToQueue(SpotifyApi spotifyApi, String t, String deviceId) {
        spotifyApi.addItemToUsersPlaybackQueue(t).device_id(deviceId).build().execute();
    }

    @SneakyThrows
    public Playlist assumeFelixPlaylist(SpotifyApi spotifyApi) {
        Paging<PlaylistSimplified> playlists = spotifyApi.getListOfCurrentUsersPlaylists().limit(50).build().execute();
        Optional<Playlist> felixAI = Arrays.stream(playlists.getItems()).filter(p -> p.getName().equals(FELIX_PLAYLIST_NAME)).findFirst().map(p -> new Playlist(p.getId(), p.getUri()));
        return felixAI.orElseGet(() -> createFelixPlaylist(spotifyApi));
    }

    @SneakyThrows
    private static Playlist createFelixPlaylist(SpotifyApi spotifyApi) {
        User user = spotifyApi.getCurrentUsersProfile().build().execute();
        var executed = spotifyApi.createPlaylist(user.getId(), FELIX_PLAYLIST_NAME).build().execute();
        return new Playlist(executed.getId(), executed.getUri());
    }

    @SneakyThrows
    private SpotifyApi getSpotifyApi() {
        SpotifyApi spotifyApi = SpotifyApi.builder()
                .setClientId(spotifyClientId)
                .setClientSecret(spotifyClientSecret)
                .setRefreshToken(spotifyRefreshToken)
                .build();

        AuthorizationCodeCredentials codeCredentials = spotifyApi.authorizationCodeRefresh().build().execute();
        spotifyApi.setAccessToken(codeCredentials.getAccessToken());
        return spotifyApi;
    }

    @SneakyThrows
    public void stop() {
        SpotifyApi spotifyApi = getSpotifyApi();
        Device device = findDevice(spotifyApi);
        spotifyApi.pauseUsersPlayback().device_id(device.getId()).build().execute();
    }

    @SneakyThrows
    private Device findDevice(SpotifyApi spotifyApi) {
        Device[] devices = spotifyApi.getUsersAvailableDevices().build().execute();
        return Arrays.stream(devices).filter(d -> d.getName().toLowerCase().contains(deviceName)).findFirst().orElseThrow();
    }
}
