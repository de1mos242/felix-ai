package net.de1mos.felix_ai.assistant;

import net.de1mos.felix_ai.assistant.music.MusicService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
public class AssistantConfig {

    @Bean
    public Function<MusicService.PlayMusicRequest, MusicService.PlayMusicResponse> playMusicFunction() {
        return new MusicService();
    }
}
