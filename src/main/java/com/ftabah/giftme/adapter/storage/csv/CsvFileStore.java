package com.ftabah.giftme.adapter.storage.csv;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class CsvFileStore {

    private final Path dataDirectory;

    public CsvFileStore(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public List<List<String>> read(String fileName) throws IOException {
        Path target = dataDirectory.resolve(fileName);
        if (!Files.exists(target)) {
            return List.of();
        }
        List<List<String>> rows = new ArrayList<>();
        try (Reader reader = Files.newBufferedReader(target, StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {
            parser.forEach(record -> {
                List<String> row = new ArrayList<>();
                record.forEach(row::add);
                rows.add(List.copyOf(row));
            });
        }
        return List.copyOf(rows);
    }

    public void replace(String fileName, List<? extends List<String>> rows) throws IOException {
        Files.createDirectories(dataDirectory);
        Path target = dataDirectory.resolve(fileName);
        Path temporary = Files.createTempFile(dataDirectory, fileName, ".tmp");
        try {
            try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8);
                 CSVPrinter printer = CSVFormat.DEFAULT.print(writer)) {
                for (List<String> row : rows) {
                    printer.printRecord(row);
                }
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException atomicMoveFailure) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}