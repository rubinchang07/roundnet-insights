package com.roundnet.insights.ingest.analyzer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

/** One roundnet-match.json file from Roundnet Analyzer, exactly as exported. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AnalyzerExport(
        Integer formatVersion,
        String exportedAt,
        MatchInfo match,
        List<Player> players,
        List<List<String>> teams,
        List<Serve> serves,
        List<Point> points) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Player(String id, String name) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record MatchInfo(String id, String playedOn, Integer pointsTo, Integer hardCap) {}
        
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Point(String id, Integer gameNumber, String serverId, List<Serve> serves, String winningTeam,
        String outcome, String responsiblePlayerId, String violationType, List<Touch> rally
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Serve(String id, Double timestamp, String playerId, String receiverId, String hand,
        String type, Integer serveAttempt, String legality, String faultType, Boolean playedThrough,
        String result, Boolean replayed, String replayReason
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Touch(String id, Double timestamp, String playerId, String type, Boolean controlled,
        Boolean weak
    ) {}
}