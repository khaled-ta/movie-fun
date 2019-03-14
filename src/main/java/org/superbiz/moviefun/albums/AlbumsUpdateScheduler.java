package org.superbiz.moviefun.albums;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.sql.Timestamp;

@Configuration
@EnableAsync
@EnableScheduling
public class AlbumsUpdateScheduler {

    private static final long SECONDS = 1000;
    private static final long MINUTES = 60 * SECONDS;

    private final AlbumsUpdater albumsUpdater;
    private final JdbcTemplate jdbcTemplate;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public AlbumsUpdateScheduler(AlbumsUpdater albumsUpdater, DataSource dataSource) {
        this.albumsUpdater = albumsUpdater;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }


    @Scheduled(initialDelay = 15 * SECONDS, fixedRate = 2 * MINUTES)
    public void run() {
        try {
            if (startAlbumSchedulerTask()) {
                logger.debug("Starting albums update");
                updateSchedulerTimestamp();
                albumsUpdater.update();
                logger.debug("Finished albums update");

            } else {
                logger.debug("Nothing to start");
            }

        } catch (Throwable e) {
            logger.error("Error while updating albums", e);
        }
    }

    private void updateSchedulerTimestamp() {
        String updateSql = "update album_scheduler_task set started_at = ?";
        jdbcTemplate.update(connection -> {

            PreparedStatement preparedStatement = connection.prepareStatement(updateSql);
            preparedStatement.setTimestamp(1, new Timestamp(new Date().getTime()));

            return preparedStatement;
        });
    }

    private boolean startAlbumSchedulerTask() {

        String sqlQuery = "select started_at from album_scheduler_task";
        final boolean[] startAlbumUpdate = new boolean[1];
        jdbcTemplate.query(sqlQuery, rs -> {
            Timestamp timestamp = rs.getTimestamp(1);
            if (timestamp == null) {
                startAlbumUpdate[0] = Boolean.TRUE;
                return;
            }
            long timeDiff = new Date().getTime() - timestamp.getTime();
            startAlbumUpdate[0] =  (timeDiff >= (60 * 2 * 1000));
        });

        return startAlbumUpdate[0];
    }
}
