package org.superbiz.moviefun.blobstore;

import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

@Component
public class FileStore implements BlobStore {

    @Override
    public void put(Blob blob) throws IOException {
        // ...
        File file = new File(blob.name);
        saveUploadToFile(blob, file);

    }

    @Override
    public Optional<Blob> get(String name) throws IOException, URISyntaxException {
        Path path = getPath(name);
        InputStream inputStream = Files.newInputStream(path);

        return Optional.ofNullable(new Blob(name, inputStream, new Tika().detect(name)));
    }

    @Override
    public void deleteAll() {
        // ...
    }

    private void saveUploadToFile(Blob blob, File targetFile) throws IOException {
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(IOUtils.toByteArray(blob.inputStream));
        }
    }



    private Path getPath(String coverFileName) throws URISyntaxException {
        File coverFile = new File(coverFileName);

        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            URL systemResource = getClass().getClassLoader().getResource("default-cover.jpg");
            System.out.println("url: " + systemResource);


            coverFilePath = Paths.get(systemResource.toURI());
        }

        return coverFilePath;
    }



}
