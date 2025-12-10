package it.unisa.c02.rently.rently_application.commons.services.storageService;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import lombok.NoArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@NoArgsConstructor
public class FilesStorageServiceImpl implements FilesStorageService {

    private Path root;

    @Override
    public void init(final String basePath) {
        try {

            final Path targetDirectory = Paths.get(basePath).toAbsolutePath().normalize();

            // Assicurati che la directory di destinazione esista
            if (!targetDirectory.toFile().exists()) {
                targetDirectory.toFile().mkdirs();
            }


           
            root = targetDirectory;
            Files.createDirectories(root);
        } catch (final IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }

    @Override
    public void save(final MultipartFile file, final String fileName) {
        try {
            Files.copy(file.getInputStream(), this.root.resolve(fileName));
        } catch (final Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    @Override
    public Resource load(final String filename) {
        try {
            final Path file = root.resolve(filename);
            final Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (final MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.root, 1).filter((final var path) -> !path.equals(this.root)).map(this.root::relativize);
        } catch (final IOException e) {
            throw new RuntimeException("Could not load the files!");
        }
    }

    @Override
    public String generateRandomFileName() {
        final int n = 12;
        final String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        final StringBuilder sb = new StringBuilder(n);

        for (int i = 0; i < n; ++i) {
            final int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());

            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();
    }
}