package org.superbiz.moviefun.albums;

import org.junit.Test;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class AlbumTest {

    @Test
    public void testIsEquivalent() {
        Album persisted = new Album("Radiohead", "OK Computer", 1997, 8);
        persisted.setId(10);

        Album sameFromCsv = new Album("Radiohead", "OK Computer", 1997, 9);
        assertThat(persisted.isEquivalent(sameFromCsv), is(true));

        Album otherFromCsv = new Album("Radiohead", "Kid A", 2000, 9);
        assertThat(persisted.isEquivalent(otherFromCsv), is(false));
    }

    @Test
    public void testDateFormatting() {
        String dateFormat = "YYYY-MM-dd HH:mm:ss.SS";
        Date date = new Date();
        System.out.println(date);
        String formattedDate = new SimpleDateFormat(dateFormat).format(date);
        System.out.println(formattedDate);

        java.sql.Date date1 = new java.sql.Date(date.getTime());
        System.out.println(date1);
        Timestamp timestamp = new Timestamp(date1.getTime());
        System.out.println(timestamp);
        System.out.println(new Timestamp(date.getTime()));

        System.out.println(new Timestamp(new java.sql.Date(date.getTime()).getTime()));
    }
}
