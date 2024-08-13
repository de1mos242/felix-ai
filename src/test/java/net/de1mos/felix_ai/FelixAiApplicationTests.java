package net.de1mos.felix_ai;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@ActiveProfiles({"local", "test", "default"})
@SpringBootTest
class FelixAiApplicationTests {

	@Autowired OpenAiChatModel chatModel;

	@Test
	void playMusicRequest() {
		var userMessage = new UserMessage("Play lieb mich");
		ChatResponse response = chatModel.call(new Prompt(List.of(userMessage), OpenAiChatOptions.builder().withFunction("playMusicFunction").build()));
		System.out.println(response.getResult().getOutput().getContent());
		Assertions.assertThat(response.getResult().getOutput().getContent()).containsIgnoringCase("scooter");
	}

}
