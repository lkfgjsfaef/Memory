package com.niit.memory.controller;

import com.niit.memory.config.Result;
import com.niit.memory.entity.MemoryAlbum;
import com.niit.memory.entity.MemoryMoment;
import com.niit.memory.entity.VisitedLocation;
import com.niit.memory.service.MemoryAlbumService;
import com.niit.memory.service.MemoryMomentService;
import com.niit.memory.service.VisitedLocationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MemoryController {

    private static final Logger log = LoggerFactory.getLogger(MemoryController.class);

    private final MemoryAlbumService albumService;
    private final MemoryMomentService momentService;
    private final VisitedLocationService locationService;

    public MemoryController(MemoryAlbumService albumService,
                            MemoryMomentService momentService,
                            VisitedLocationService locationService) {
        this.albumService = albumService;
        this.momentService = momentService;
        this.locationService = locationService;
    }

    // === 时光相册 ===
    @GetMapping("/albums")
    public Result listAlbums() {
        try {
            List<MemoryAlbum> list = albumService.findAll();
            log.info("GET /api/albums: returning {} records", list.size());
            for (MemoryAlbum a : list) {
                log.info("  album id={} location={} coverUrl={} emoji={}",
                    a.getId(), a.getLocation(), a.getCoverUrl(), a.getEmoji());
            }
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/albums/{id}")
    public Result getAlbum(@PathVariable Long id) {
        try {
            return Result.success(albumService.findById(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/albums")
    public Result createAlbum(@RequestBody MemoryAlbum album) {
        try {
            log.info("POST /api/albums: location={} coverUrl={} emoji={} photoUrls={}",
                album.getLocation(), album.getCoverUrl(), album.getEmoji(), album.getPhotoUrls());
            albumService.create(album);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/albums/{id}")
    public Result updateAlbum(@PathVariable Long id, @RequestBody MemoryAlbum album) {
        try {
            log.info("PUT /api/albums/{}: location={} coverUrl={} emoji={} photoUrls={} isPrivate={}",
                id, album.getLocation(), album.getCoverUrl(), album.getEmoji(),
                album.getPhotoUrls(), album.getIsPrivate());
            album.setId(id);
            return Result.success(albumService.update(album));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/albums/{id}")
    public Result deleteAlbum(@PathVariable Long id) {
        try {
            albumService.delete(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // === 记忆长河 ===
    @GetMapping("/moments")
    public Result getTimeline() {
        try {
            return Result.success(momentService.getTimeline());
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/moments/{id}")
    public Result getMoment(@PathVariable Long id) {
        try {
            return Result.success(momentService.findById(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/moments")
    public Result createMoment(@RequestBody MemoryMoment moment) {
        try {
            momentService.create(moment);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/moments/{id}")
    public Result updateMoment(@PathVariable Long id, @RequestBody MemoryMoment moment) {
        try {
            moment.setId(id);
            return Result.success(momentService.update(moment));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/moments/{id}")
    public Result deleteMoment(@PathVariable Long id) {
        try {
            momentService.delete(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // === 足迹地图 ===
    @GetMapping("/locations")
    public Result listLocations() {
        try {
            List<VisitedLocation> list = locationService.findAll();
            log.info("GET /api/locations: returning {} records", list.size());
            for (VisitedLocation loc : list) {
                log.info("  loc id={} name={} imageUrl={} lat={} lng={} mapX={} mapY={}",
                    loc.getId(), loc.getName(), loc.getImageUrl(), loc.getLat(), loc.getLng(),
                    loc.getMapX(), loc.getMapY());
            }
            return Result.success(list);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/locations")
    public Result createLocation(@RequestBody VisitedLocation location) {
        try {
            log.info("POST /api/locations: name={} imageUrl={} lat={} lng={}",
                location.getName(), location.getImageUrl(), location.getLat(), location.getLng());
            return Result.success(locationService.create(location));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/locations/{id}")
    public Result updateLocation(@PathVariable Long id, @RequestBody VisitedLocation location) {
        try {
            log.info("PUT /api/locations/{}: name={} imageUrl={} lat={} lng={}",
                id, location.getName(), location.getImageUrl(), location.getLat(), location.getLng());
            location.setId(id);
            return Result.success(locationService.update(location));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/locations/{id}")
    public Result deleteLocation(@PathVariable Long id) {
        try {
            locationService.delete(id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
