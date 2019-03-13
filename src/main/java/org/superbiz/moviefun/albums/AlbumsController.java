package org.superbiz.moviefun.albums;

import com.amazonaws.util.IOUtils;
import org.apache.tika.Tika;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.superbiz.moviefun.blobstore.Blob;
import org.superbiz.moviefun.blobstore.BlobStore;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;
    private final BlobStore blobStore;

/*
    public AlbumsController(AlbumsBean albumsBean, BlobStore blobStore) {
        this.albumsBean = albumsBean;
        this.blobStore = blobStore;
    }
*/

    public AlbumsController(AlbumsBean albumsBean, BlobStore dbStore) {
        this.albumsBean = albumsBean;
        this.blobStore = dbStore;
    }

    @GetMapping
    public String index(Map<String, Object> model) {
        model.put("albums", albumsBean.getAlbums());
        return "albums";
    }

    @GetMapping("/{albumId}")
    public String details(@PathVariable long albumId, Map<String, Object> model) {
        model.put("album", albumsBean.find(albumId));
        return "albumDetails";
    }

    @PostMapping("/{albumId}/cover")
    public String uploadCover(@PathVariable long albumId, @RequestParam("file") MultipartFile uploadedFile) throws IOException {

//        File coverFile = getCoverFile(albumId);
        InputStream inputStream = uploadedFile.getInputStream();
        String contentType = uploadedFile.getContentType();
        String name = getCoverFileName(albumId);
        Blob blob = new Blob(name, inputStream, contentType);
        blobStore.put(blob);

        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {

        String coverFileName = getCoverFileName(albumId);
        Path coverFilePath = getExistingCoverPath(albumId);
        Optional<Blob> optionalBlob = blobStore.get(coverFileName);

        byte[] imageBytes = (optionalBlob.isPresent())
                ? IOUtils.toByteArray(optionalBlob.get().inputStream)
                : readAllBytes(coverFilePath);

        HttpHeaders headers = createImageHttpHeaders(coverFilePath, imageBytes);

        return new HttpEntity<>(imageBytes, headers);
    }

    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, File targetFile) throws IOException {
        targetFile.delete();
        targetFile.getParentFile().mkdirs();
        targetFile.createNewFile();

        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
            outputStream.write(uploadedFile.getBytes());
        }
    }

    private HttpHeaders createImageHttpHeaders(Path coverFilePath, byte[] imageBytes) throws IOException {
        String contentType = new Tika().detect(coverFilePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        return headers;
    }


    private File getCoverFile(@PathVariable long albumId) {
        String coverFileName = getCoverFileName(albumId);
        return new File(coverFileName);
    }

    private Path getExistingCoverPath(@PathVariable long albumId) throws URISyntaxException {
        String coverFileName = getCoverFileName(albumId);
        return getPath(coverFileName);
    }

    private String getCoverFileName(@PathVariable long albumId) {
        return format("covers/%d", albumId);
    }

    private Path getPath(String coverFileName) throws URISyntaxException {
        File coverFile = new File(coverFileName);

        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            coverFilePath = Paths.get(getClass().getClassLoader().getResource("default-cover.jpg").toURI());
        }

        return coverFilePath;
    }

}
