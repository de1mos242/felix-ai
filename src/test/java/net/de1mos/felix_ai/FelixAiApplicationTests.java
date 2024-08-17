package net.de1mos.felix_ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Platform;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.function.FunctionCallbackWrapper;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.*;
import java.util.List;

@ActiveProfiles({"local", "test", "default"})
@SpringBootTest
class FelixAiApplicationTests {

    @Autowired
    OpenAiChatModel chatModel;

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void playMusicRequest() {
        var userMessage = new UserMessage("Play lieb mich");
        ChatResponse response = chatModel.call(new Prompt(List.of(userMessage), OpenAiChatOptions.builder().withFunction("playMusicFunction").build()));
        System.out.println(response.getResult().getOutput().getContent());
        Assertions.assertThat(response.getResult().getOutput().getContent()).containsIgnoringCase("scooter");
    }

    @Test
    void playMusicRequestAudio() {
        this.getClass().getClassLoader().getResource("myfile2.wav");
        var userMessage = new UserMessage("Play lieb mich");
        ChatResponse response = chatModel.call(new Prompt(List.of(userMessage), OpenAiChatOptions.builder().withFunction("playMusicFunction").build()));
        System.out.println(response.getResult().getOutput().getContent());
        Assertions.assertThat(response.getResult().getOutput().getContent()).containsIgnoringCase("scooter");
    }

    @Test
    void parseSpeech() throws Exception {
        if (Platform.isMac()) {
            var res = this.getClass().getClassLoader().getResource("vosk/darwin/libvosk.dylib");
            System.load(res.getPath());
        }
        String modelName = "vosk-model-small-ru-0.22";
//        String modelName = "vosk-model-en-us-0.22-lgraph";
//        String modelName = "vosk-model-small-en-us-0.15";
        String modelPath = this.getClass().getClassLoader().getResource("models/" + modelName).getPath();
        try (Model model = new Model(modelPath)) {

//            AudioFormat format = buildAudioFormatInstance();
//            int frameSizeInBytes = format.getFrameSize();
//            var line = getTargetDataLineForRecord();
//            int bufferLengthInFrames = line.getBufferSize() / 8;
//            final int bufferLengthInBytes = bufferLengthInFrames * frameSizeInBytes;

            int sampleRate = 48000;
            var format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, sampleRate, 16, 1, 2, sampleRate, false);
            TargetDataLine line;
            DataLine.Info info = new DataLine.Info(TargetDataLine.class,
                    format); // format is an AudioFormat object
            if (!AudioSystem.isLineSupported(info)) {
                // Handle the error ...
                System.out.println("oopsie  mic ");
            }
// Obtain and open the line.
            try {
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);

            } catch (LineUnavailableException ex) {
                // Handle the error ...
                System.out.println("oopsie " + ex.getMessage());
                throw ex;
            }
            LibVosk.setLogLevel(LogLevel.DEBUG);

            line.start();
//            InputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream("../../python/example/test.wav")));
            Recognizer recognizer = new Recognizer(model, sampleRate);
//            recognizer.setMaxAlternatives(10);
//            recognizer.setWords(true);
//            recognizer.setPartialWords(true);
            var ais = new AudioInputStream(line);
            int df = 0;

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            int nbytes;
            byte[] b = new byte[4096];
            while ((nbytes = ais.read(b)) >= 0) {
                if (recognizer.acceptWaveForm(b, nbytes)) {
                    String recognizerResult = recognizer.getResult();
                    System.out.println(recognizerResult);
                    String text = objectMapper.readTree(recognizerResult).get("text").asText();
                    out.writeBytes(b);
                    if (text.toLowerCase().contains("феликс")) {
                        System.out.println("WOOOHOOO " + text);
                        FileOutputStream fileOutputStream = new FileOutputStream("/Users/denis/IdeaProjects/felix-ai/src/test/resources/myfile" + df + ".wav");
                        out.writeTo(fileOutputStream);
                        df++;
                    } else {
                    }
                    out = new ByteArrayOutputStream();
                } else {
                    String partialResult = recognizer.getPartialResult();
                    var partText = objectMapper.readTree(partialResult).get("partial").asText();
                    if (!partText.isBlank()) {
                        out.writeBytes(b);
//                        System.out.println("Wrote partial data: " + partText);
                    }
//                        recognizer.
//                        System.out.println(recognizer.getPartialResult());
                }
            }

            System.out.println(recognizer.getFinalResult());
        }
    }

    public static AudioFormat buildAudioFormatInstance() {
        ApplicationProperties aConstants = new ApplicationProperties();
        AudioFormat.Encoding encoding = aConstants.ENCODING;
        float rate = aConstants.RATE;
        int channels = aConstants.CHANNELS;
        int sampleSize = aConstants.SAMPLE_SIZE;
        boolean bigEndian = aConstants.BIG_ENDIAN;

        return new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8) * channels, rate, bigEndian);
    }

    @SneakyThrows
    private TargetDataLine getTargetDataLineForRecord() {
        TargetDataLine line;
        AudioFormat format = buildAudioFormatInstance();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            return null;
        }
        line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format, line.getBufferSize());
        return line;
    }


}
