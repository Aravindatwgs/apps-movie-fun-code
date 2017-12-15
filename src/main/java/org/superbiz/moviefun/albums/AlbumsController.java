package org.superbiz.moviefun.albums;


import org.apache.tika.Tika;
import org.apache.tika.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sun.nio.ch.IOUtil;

import javax.print.DocFlavor;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static java.lang.ClassLoader.getSystemResource;
import static java.lang.String.format;
import static java.nio.file.Files.readAllBytes;

@Controller
@RequestMapping("/albums")
public class AlbumsController {

    private final AlbumsBean albumsBean;

    @Autowired
    private BlobStore blobStore;

    public AlbumsController(AlbumsBean albumsBean) {
        this.albumsBean = albumsBean;
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


        Blob blob   = new Blob(format("covers/%d", albumId),uploadedFile.getInputStream(),uploadedFile.getContentType());
        this.blobStore.put(blob);

        return format("redirect:/albums/%d", albumId);
    }

    @GetMapping("/{albumId}/cover")
    public HttpEntity<byte[]> getCover(@PathVariable long albumId) throws IOException, URISyntaxException {

        //byte[] imageBytes = readAllBytes(coverFilePath);

        Optional<Blob> blob = this.blobStore.get(format("covers/%d", albumId));

        System.out.println(blob.get().inputStream);

        byte[] inputStream = IOUtils.toByteArray(blob.get().inputStream);

        HttpHeaders headers = createImageHttpHeaders(blob.get().contentType, inputStream);

        return new HttpEntity<>(inputStream,headers);
    }


    private void saveUploadToFile(@RequestParam("file") MultipartFile uploadedFile, String targetFile) throws IOException {

        Blob blob   = new Blob(targetFile,uploadedFile.getInputStream(),uploadedFile.getContentType());
        this.blobStore.put(blob);


    }

    private HttpHeaders createImageHttpHeaders(String contentType, byte[] imageBytes) throws IOException {
       // String contentType = new Tika().detect(coverFilePath);

        HttpHeaders headers = new HttpHeaders();
       // headers.setContentType("/img/png");
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.setContentLength(imageBytes.length);
        System.out.println(imageBytes.length);
        //System.out.println(MediaType.parseMediaType(contentType));
        return headers;
    }

    private  File getCoverFile(@PathVariable long albumId) {
        String coverFileName = format("covers/%d", albumId);

//        try {
//          Optional<Blob> blob =  this.blobStore.get(coverFileName);
//          blob.
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        return  new File(coverFileName);

    }

    private Path getExistingCoverPath(@PathVariable long albumId) throws URISyntaxException {
        File coverFile = getCoverFile(albumId);
        Path coverFilePath;

        if (coverFile.exists()) {
            coverFilePath = coverFile.toPath();
        } else {
            coverFilePath = Paths.get(getSystemResource("default-cover.jpg").toURI());
        }

        return coverFilePath;
    }
}
