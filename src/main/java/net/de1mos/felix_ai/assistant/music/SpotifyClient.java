package net.de1mos.felix_ai.assistant.music;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import se.michaelthelin.spotify.model_objects.miscellaneous.Device;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;

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
        Recommendations recommendations = spotifyApi.getRecommendations().seed_tracks(trackId).limit(10).build().execute();
        String[] recommendationTracks = Arrays.stream(recommendations.getTracks()).map(Track::getUri).toArray(String[]::new);
        String[] tracks = ArrayUtils.addFirst(recommendationTracks, trackUrl);
        spotifyApi.replacePlaylistsItems(playlist.id, tracks).build().execute();

        var deviceId = device.getId();
        JsonObject trackOffset = new JsonObject();
        trackOffset.add("uri", new JsonPrimitive(trackUrl));
        spotifyApi.startResumeUsersPlayback().device_id(deviceId).context_uri(playlist.uri()).offset(trackOffset).build().execute();
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
