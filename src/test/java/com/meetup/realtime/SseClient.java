package com.meetup.realtime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/** 실서버 SSE 구독 (테스트용). 받은 줄을 큐에 쌓음 */
final class SseClient implements AutoCloseable {

    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private final BlockingQueue<String> lines = new LinkedBlockingQueue<>();
    private final InputStream body;

    SseClient(int port, String code, String token) throws Exception {
        HttpRequest req = HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/rooms/" + code
                + "/events")).header("Accept", "text/event-stream").header("X-Participant-Token", token).build();
        body = HTTP.send(req, HttpResponse.BodyHandlers.ofInputStream()).body();
        Thread reader = new Thread(this::read);
        reader.setDaemon(true);
        reader.start();
    }

    private void read() {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(body, StandardCharsets.UTF_8))) {
            for (String l = r.readLine(); l != null; l = r.readLine()) {
                lines.add(l);
            }
        } catch (IOException ignored) {
            // 닫으면 끝
        }
    }

    // text가 든 줄이 올 때까지. 시간 안에 없으면 null
    String await(String text, Duration timeout) throws InterruptedException {
        long end = System.nanoTime() + timeout.toNanos();
        while (true) {
            String l = lines.poll(end - System.nanoTime(), TimeUnit.NANOSECONDS);
            if (l == null || l.contains(text)) {
                return l;
            }
        }
    }

    // 강제 단절
    @Override
    public void close() throws IOException {
        body.close();
    }
}
