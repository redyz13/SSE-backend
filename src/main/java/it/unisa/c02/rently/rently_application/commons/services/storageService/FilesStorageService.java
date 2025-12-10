package it.unisa.c02.rently.rently_application.commons.services.storageService;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FilesStorageService {
    void init(final String id);

    void save(final MultipartFile file, final String fileName);

    Resource load(final String filename);

    void deleteAll();

    Stream<Path> loadAll();

    String generateRandomFileName();
}
