package net.de1mos.felix_ai.services;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.task.VirtualThreadTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class RecognitionExecutor implements ApplicationRunner {

    private  final LocalVoiceRecognitionService localVoiceRecognitionService;

    private final TranscribeService transcribeService;
    private final MusicAssistantService musicAssistantService;

    private final VirtualThreadTaskExecutor threadTaskExecutor = new VirtualThreadTaskExecutor();

    @Override
    public void run(ApplicationArguments args) {
        localVoiceRecognitionService.startRecognition(this::onNewUtterance);
    }

    private void onNewUtterance(ByteArrayOutputStream wavFile) {
        CompletableFuture<String> transcribeFuture = CompletableFuture.supplyAsync(() -> transcribeService.trascribe(wavFile), threadTaskExecutor);
        transcribeFuture.thenAccept(musicAssistantService::playMusicRequest);
    }
}
