package net.de1mos.felix_ai.assistant.music;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Description;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchTracksRequest;

import java.util.Arrays;
import java.util.function.Function;

@Slf4j
public class MusicService implements Function<MusicService.PlayMusicRequest, MusicService.PlayMusicResponse> {

    @Value("${spotify-client-id}")
    private String spotifyClientId;

    @Value("${spotify-client-secret}")
    private String spotifyClientSecret;

    @JsonClassDescription("User request to search a song in spotify and play it")
    public record PlayMusicRequest(String searchString) { }

    public record PlayMusicResponse(String id, String artist, String title) { }
    @SneakyThrows
    @Override
    public PlayMusicResponse apply(PlayMusicRequest playMusicRequest) {
        log.info("Search for song " + playMusicRequest.searchString);

        SpotifyApi spotifyApi = new SpotifyApi.Builder()
                .setClientId(spotifyClientId)
                .setClientSecret(spotifyClientSecret)
                .build();

        ClientCredentialsRequest credentialsRequest = spotifyApi.clientCredentials().build();
        ClientCredentials clientCredentials = credentialsRequest.execute();
        spotifyApi.setAccessToken(clientCredentials.getAccessToken());


        var searchTracks = spotifyApi.searchTracks(playMusicRequest.searchString).limit(3).build();
        Paging<Track> trackPaging = searchTracks.execute();
        Arrays.stream(trackPaging.getItems()).forEach(i -> log.info("Found tracK: " + i.getName() + " from " + getArtists(i)));

        var track = trackPaging.getItems()[0];
        return new PlayMusicResponse(track.getId(), getArtists(track), track.getName());
    }

    private static String getArtists(Track i) {
        return String.join(", ", Arrays.stream(i.getArtists()).map(ArtistSimplified::getName).toList());
    }
}
