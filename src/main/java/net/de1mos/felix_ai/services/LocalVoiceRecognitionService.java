package net.de1mos.felix_ai.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jna.Platform;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.vosk.Model;
import org.vosk.Recognizer;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalVoiceRecognitionService {


    // "vosk-model-en-us-0.22-lgraph";
    // "vosk-model-small-en-us-0.15";
    public static final String MODEL_NAME = "vosk-model-small-ru-0.22";
    public static final int SAMPLE_RATE = 48000;

    private final ObjectMapper objectMapper;

    @SneakyThrows
    public void startRecognition(Consumer<ByteArrayOutputStream> consumer) {


        if (Platform.isMac()) {
            var res = this.getClass().getClassLoader().getResource("vosk/darwin/libvosk.dylib");
            System.load(res.getPath());
        }

        String modelPath = this.getClass().getClassLoader().getResource("models/" + MODEL_NAME).getPath();
        try (Model model = new Model(modelPath)) {

            var format = getAudioFormat();
            TargetDataLine line;
            DataLine.Info info = new DataLine.Info(TargetDataLine.class,
                    format); // format is an AudioFormat object
            if (!AudioSystem.isLineSupported(info)) {
                throw new IllegalStateException("Mic is not supported");
            }

            try {
                line = (TargetDataLine) AudioSystem.getLine(info);
                line.open(format);
                line.start();
            } catch (LineUnavailableException ex) {
                log.error("Line error", ex);
                throw ex;
            }

            Recognizer recognizer = new Recognizer(model, SAMPLE_RATE);
            var ais = new AudioInputStream(line);

            ByteArrayOutputStream out = new ByteArrayOutputStream();

            log.info("------------------------------------------------------------------");
            log.info("----------- Felix AI ready to listen your commands ---------------");
            log.info("------------------------------------------------------------------");

            int nbytes;
            byte[] b = new byte[4096];
            while ((nbytes = ais.read(b)) >= 0) {
                if (recognizer.acceptWaveForm(b, nbytes)) {
                    String recognizerResult = recognizer.getResult();
                    String text = objectMapper.readTree(recognizerResult).get("text").asText();
                    out.writeBytes(b);
                    if (text.toLowerCase().contains("феликс")) {
                        log.info("Got a new input portion: {}", text);
                        var wavStream = writeWavFile(out, format);
                        consumer.accept(wavStream);
                    }
                    out = new ByteArrayOutputStream();
                } else {
                    String partialResult = recognizer.getPartialResult();
                    var partText = objectMapper.readTree(partialResult).get("partial").asText();
                    if (!partText.isBlank()) {
                        out.writeBytes(b);
                    }
                }
            }
        }

    }
    private static AudioFormat getAudioFormat () {
        return new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, SAMPLE_RATE, 16, 1, 2, SAMPLE_RATE, false);
    }

    private static ByteArrayOutputStream writeWavFile(ByteArrayOutputStream out, AudioFormat audioFormat) throws IOException {
        byte[] data = out.toByteArray();
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
        AudioInputStream audioInputStream = new AudioInputStream(byteArrayInputStream, audioFormat, data.length / audioFormat.getFrameSize());
        ByteArrayOutputStream wavOutputStream = new ByteArrayOutputStream();
        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, wavOutputStream);
        return wavOutputStream;
    }
}