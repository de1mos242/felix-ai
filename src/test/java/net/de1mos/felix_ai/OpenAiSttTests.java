package net.de1mos.felix_ai;

import org.junit.jupiter.api.Test;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.openai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles({"local", "test", "default"})
@SpringBootTest
class OpenAiSttTests {

    @Autowired
    OpenAiAudioTranscriptionModel transcriptionModel;

    @Test
    void speechToText() {
        var transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
                .withResponseFormat(OpenAiAudioApi.TranscriptResponseFormat.JSON)
                .withTemperature(0f)
                .build();

        var audioFile = new FileSystemResource("src/test/resources/liebMichRequest.wav");

        AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioFile, transcriptionOptions);
        AudioTranscriptionResponse response = transcriptionModel.call(transcriptionRequest);


        assertThat(response.getResult().getOutput()).isEqualTo("Включи, пожалуйста, песню Либ Мих группы Mono Inc.");
    }

}
