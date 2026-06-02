package com.niit.memory.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.niit.memory.config.Result;
import com.niit.memory.service.MusicSongService;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/music")
public class MusicController {

    private final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private final ObjectMapper mapper = new ObjectMapper();
    private final MusicSongService musicSongService;

    public MusicController(MusicSongService musicSongService) {
        this.musicSongService = musicSongService;
    }

    @GetMapping("/search")
    public Result search(@RequestParam String keyword, @RequestParam(defaultValue = "20") int limit) {
        List<Map<String, Object>> list = trySearch(keyword, limit);
        if (list != null) return Result.success(list);
        return Result.error("搜索失败，请稍后重试");
    }

    private List<Map<String, Object>> trySearch(String keyword, int limit) {
        // Try HTTP first (old API often works better on HTTP)
        String[] urls = {
            "http://music.163.com/api/search/get?s=" + encode(keyword) + "&type=1&limit=" + limit,
            "https://music.163.com/api/search/get?s=" + encode(keyword) + "&type=1&limit=" + limit,
        };
        for (String url : urls) {
            try {
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .header("Referer", "https://music.163.com/")
                        .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .header("Accept", "application/json, text/plain, */*")
                        .timeout(Duration.ofSeconds(8))
                        .GET()
                        .build();
                HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
                JsonNode root = mapper.readTree(resp.body());
                JsonNode songs = root.path("result").path("songs");
                if (songs.isArray() && songs.size() > 0) {
                    return parseSongs(songs);
                }
            } catch (Exception ignored) {
                // try next URL
            }
        }
        return null;
    }

    private List<Map<String, Object>> parseSongs(JsonNode songs) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (JsonNode song : songs) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", song.path("id").asLong());
            item.put("name", song.path("name").asText());
            item.put("duration", song.path("duration").asLong());
            JsonNode artists = song.path("artists");
            StringBuilder artistNames = new StringBuilder();
            if (artists.isArray()) {
                for (int i = 0; i < artists.size(); i++) {
                    if (i > 0) artistNames.append("/");
                    artistNames.append(artists.get(i).path("name").asText());
                }
            }
            item.put("artist", artistNames.toString());
            JsonNode album = song.path("album");
            item.put("album", album.path("name").asText(""));
            item.put("picUrl", album.path("picUrl").asText(""));
            list.add(item);
        }
        return list;
    }

    private String encode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }

    @GetMapping("/playlist")
    public Result getPlaylist() {
        try {
            List<Map<String, Object>> playlist = musicSongService.getPlaylist();
            return Result.success(playlist);
        } catch (Exception e) {
            return Result.error("获取歌单失败: " + e.getMessage());
        }
    }

    @PutMapping("/playlist")
    public Result savePlaylist(@RequestBody List<Map<String, Object>> songs) {
        try {
            musicSongService.savePlaylist(songs);
            return Result.success(null);
        } catch (Exception e) {
            return Result.error("保存歌单失败: " + e.getMessage());
        }
    }
}
