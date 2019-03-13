package org.superbiz.moviefun.blobstore;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Repository
public class MySQLStore implements BlobStore {

    private JdbcTemplate jdbcTemplate;

    public MySQLStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void put(Blob blob) throws IOException {
        String name = blob.name;
        final int albumId = resolveAlbumId(name);

        String createSql = "update album set cover = ? WHERE id=?";

        System.out.println("album id: " + albumId);
        jdbcTemplate.update(connection -> {
            PreparedStatement preparedStatement = connection.prepareStatement(createSql);
            try {
                preparedStatement.setBinaryStream(1, blob.inputStream);
                preparedStatement.setInt(2, albumId);
            } catch (SQLException e) {
                System.out.println(e.getMessage());
            }

            return preparedStatement;
        });
    }

    @Override
    public Optional<Blob> get(final String name) {

        String retrievSql = "SELECT cover FROM album WHERE id=?";
        AtomicReference<Blob> blob = new AtomicReference<>();
        int albumId = resolveAlbumId(name);

        jdbcTemplate.query(retrievSql, Arrays.copyOf(new Object[]{albumId}, 1), rs -> {
            java.sql.Blob cover = rs.getBlob("cover");
            InputStream binaryStream = cover.getBinaryStream();
            blob.set(new Blob(name, binaryStream, "image/jpeg"));
        });

        return Optional.ofNullable(blob.get());
    }

    private int resolveAlbumId(String name) {
        if (name != null && name.startsWith("covers/")) {
            name = name.replace("covers/", "");
        }
        for (Character c : name.toCharArray()) {
            if (!Character.isDigit(c)) {
                throw new IllegalArgumentException("id is not digit");
            }

        }
        return Integer.valueOf(name);
    }

    @Override
    public void deleteAll() {

        System.out.println("NOT YET IMPLEMENTED!");
    }
}
