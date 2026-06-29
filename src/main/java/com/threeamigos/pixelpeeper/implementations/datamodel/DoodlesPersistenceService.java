package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.google.gson.Gson;
import com.threeamigos.pixelpeeper.data.ImageDoodlesData;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class DoodlesPersistenceService {

    private static final String SIDECAR_EXTENSION = ".doodles";
    private final Gson gson = new Gson();

    public File getSidecarFile(File imageFile) {
        return new File(imageFile.getAbsolutePath() + SIDECAR_EXTENSION);
    }

    public boolean sidecarExists(File imageFile) {
        return getSidecarFile(imageFile).exists();
    }

    public void saveDoodles(File imageFile, ImageDoodlesData doodlesData) {
        File sidecar = getSidecarFile(imageFile);
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(sidecar), StandardCharsets.UTF_8)) {
            gson.toJson(doodlesData, writer);
        } catch (IOException e) {
            // doodles are non-critical data; silently ignore IO failures
        }
    }

    public ImageDoodlesData loadDoodles(File imageFile) {
        File sidecar = getSidecarFile(imageFile);
        if (!sidecar.exists() || !sidecar.canRead()) {
            return null;
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(sidecar), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, ImageDoodlesData.class);
        } catch (IOException e) {
            return null;
        }
    }

    public void deleteSidecar(File imageFile) {
        File sidecar = getSidecarFile(imageFile);
        if (sidecar.exists()) {
            sidecar.delete();
        }
    }
}
