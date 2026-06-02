package com.niit.memory.repository;

import com.niit.memory.entity.MusicSong;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MusicSongRepository {

    private final JdbcTemplate jdbc;

    public MusicSongRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<MusicSong> findAll() {
        String sql = "SELECT * FROM music_playlist ORDER BY sort_order";
        return jdbc.query(sql, new BeanPropertyRowMapper<>(MusicSong.class));
    }

    public int insert(MusicSong song) {
        String sql = "INSERT INTO music_playlist (song_id, name, artist, album, duration, pic_url, sort_order) VALUES (?,?,?,?,?,?,?)";
        return jdbc.update(sql, song.getSongId(), song.getName(),
                song.getArtist(), song.getAlbum(), song.getDuration(), song.getPicUrl(), song.getSortOrder());
    }

    public int deleteAll() {
        String sql = "DELETE FROM music_playlist";
        return jdbc.update(sql);
    }
}
