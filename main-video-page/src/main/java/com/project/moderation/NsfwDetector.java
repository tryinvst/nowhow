package com.project.moderation;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * NSFW-классификатор кадров: ViT (AdamCodd/vit-base-nsfw-detector, Apache-2.0) в ONNX, классы [sfw, nsfw].
 */
@Component
public class NsfwDetector {
    private static final int SIZE = 384;

    private final String modelPath;
    private OrtEnvironment environment;
    private OrtSession session;

    public NsfwDetector(@Value("${app.moderation.nsfw.model}") String modelPath) {
        this.modelPath = modelPath;
    }

    public boolean isAvailable() {
        return modelPath != null && !modelPath.isBlank() && Files.isRegularFile(Path.of(modelPath));
    }

    /**
     * Вероятность 0..1, что кадр откровенный.
     * <p>Softmax по двум классам.
     */
    public synchronized float nsfwProbability(Path jpg) throws IOException, OrtException {
        OrtSession ortSession = session();
        BufferedImage image = ImageIO.read(jpg.toFile());
        if (image == null) {
            throw new IOException("Кадр не читается: " + jpg.getFileName());
        }
        try (OnnxTensor input = OnnxTensor.createTensor(environment, toChw(image), new long[]{1, 3, SIZE, SIZE});
             OrtSession.Result result = ortSession.run(Map.of(ortSession.getInputNames().iterator().next(), input))) {
            float[] logits = ((float[][]) result.get(0).getValue())[0];
            double max = Math.max(logits[0], logits[1]);
            double sfw = Math.exp(logits[0] - max);
            double nsfw = Math.exp(logits[1] - max);
            return (float) (nsfw / (sfw + nsfw));
        }
    }

    /**
     * Как в preprocessor_config модели: 384x384, пиксель/255, затем (x - 0.5) / 0.5.
     */
    private FloatBuffer toChw(BufferedImage source) {
        BufferedImage image = source;
        if (source.getWidth() != SIZE || source.getHeight() != SIZE) {
            image = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_RGB);
            Graphics2D graphics = image.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            graphics.drawImage(source, 0, 0, SIZE, SIZE, null);
            graphics.dispose();
        }
        FloatBuffer buffer = FloatBuffer.allocate(3 * SIZE * SIZE);
        int plane = SIZE * SIZE;
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int rgb = image.getRGB(x, y);
                int index = y * SIZE + x;
                buffer.put(index, (((rgb >> 16) & 0xFF) / 255f - 0.5f) / 0.5f);
                buffer.put(plane + index, (((rgb >> 8) & 0xFF) / 255f - 0.5f) / 0.5f);
                buffer.put(2 * plane + index, ((rgb & 0xFF) / 255f - 0.5f) / 0.5f);
            }
        }
        return buffer;
    }

    /**
     * Модель ~90 МБ: грузим при первом видео, а не при старте сервиса.
     */
    private OrtSession session() throws OrtException {
        if (session == null) {
            environment = OrtEnvironment.getEnvironment();
            session = environment.createSession(modelPath, new OrtSession.SessionOptions());
        }
        return session;
    }

    @PreDestroy
    public synchronized void close() throws OrtException {
        if (session != null) {
            session.close();
        }
    }
}
