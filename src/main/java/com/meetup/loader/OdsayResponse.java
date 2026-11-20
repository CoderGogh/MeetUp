package com.meetup.loader;

import com.fasterxml.jackson.databind.JsonNode;

/** ODsay 대중교통 경로 응답 해석 */
final class OdsayResponse {

    private OdsayResponse() {
    }

    static Route parse(JsonNode root) {
        JsonNode error = root.path("error");
        if (!error.isMissingNode()) {
            JsonNode e = error.isArray() ? error.path(0) : error;
            throw new RouteException("ODsay 오류 코드 " + e.path("code").asText("?"));
        }
        JsonNode best = null;
        for (JsonNode path : root.path("result").path("path")) {
            if (best == null || totalTime(path) < totalTime(best)) {
                best = path;
            }
        }
        if (best == null) {
            throw new RouteException("ODsay 경로 없음");
        }
        JsonNode info = best.path("info");
        int rides = info.path("busTransitCount").asInt() + info.path("subwayTransitCount").asInt();
        return new Route(totalTime(best), Math.max(0, rides - 1), Route.Source.ODSAY);
    }

    private static int totalTime(JsonNode path) {
        return path.path("info").path("totalTime").asInt();
    }
}
