package net.de1mos.felix_ai.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MusicAssistantService {

    private final OpenAiChatModel chatModel;

    void playMusicRequest(String msg) {
        var userMessage = new UserMessage(msg);
        ChatResponse response = chatModel.call(new Prompt(List.of(userMessage), OpenAiChatOptions.builder().withFunction("playMusicFunction").build()));
        String content = response.getResult().getOutput().getContent();
        log.info("Got response from assistant: {}", content);
    }
}
