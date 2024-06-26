package com.threeamigos.pixelpeeper.implementations.datamodel.metadatareaders;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifDirectoryBase;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.makernotes.CanonMakernoteDirectory;
import com.drew.metadata.exif.makernotes.NikonType1MakernoteDirectory;
import com.drew.metadata.exif.makernotes.NikonType2MakernoteDirectory;
import com.drew.metadata.exif.makernotes.PanasonicMakernoteDirectory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.threeamigos.common.util.implementations.messagehandler.ConsoleMessageHandler;
import com.threeamigos.common.util.interfaces.messagehandler.MessageHandler;
import com.threeamigos.pixelpeeper.data.ExifMap;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.interfaces.datamodel.ExifReader;

import java.io.File;
import java.util.Optional;

/**
 * An implementation of the {@link ExifReader} that uses Drew Noakes' library.
 * Visit {@link <a href="https://github.com/drewnoakes/metadata-extractor">Drew Noakes' GitHub project</a>} for more information.
 *
 * @author Stefano Reksten
 */
public class DrewNoakesExifReader implements ExifReader {

    private File file;
    private ExifMap exifMap;
    private final MessageHandler messageHandler = new ConsoleMessageHandler();

    @Override
    public Optional<ExifMap> readMetadata(File file) {
        this.file = file;
        exifMap = new ExifMap();
        if (consume()) {
            return Optional.of(exifMap);
        } else {
            return Optional.empty();
        }
    }

    private boolean consume() {

        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);

            // printAllTags(metadata);

            for (Directory directory : metadata.getDirectories()) {
                Class<? extends Directory> directoryClass = directory.getClass();
                if (directoryClass.equals(JpegDirectory.class)) {
                    consumeJpeg((JpegDirectory) directory);
                } else if (directoryClass.equals(ExifIFD0Directory.class)) {
                    consumeCamera((ExifIFD0Directory) directory);
                } else if (directoryClass.equals(ExifSubIFDDirectory.class)) {
                    consumeImage((ExifSubIFDDirectory) directory);
                } else if (directoryClass.equals(PanasonicMakernoteDirectory.class)) {
                    consumePanasonic((PanasonicMakernoteDirectory) directory);
                } else if (directoryClass.equals(NikonType1MakernoteDirectory.class)) {
                    consumeNikon1((NikonType1MakernoteDirectory) directory);
                } else if (directoryClass.equals(NikonType2MakernoteDirectory.class)) {
                    consumeNikon2((NikonType2MakernoteDirectory) directory);
                } else if (directoryClass.equals(CanonMakernoteDirectory.class)) {
                    consumeCanon((CanonMakernoteDirectory) directory);
                }
            }

            return true;

        } catch (Exception e) {

            return false;

        }

    }

    private void consumeJpeg(JpegDirectory jpegDirectory) throws MetadataException {
        int pictureWidth = jpegDirectory.getInt(JpegDirectory.TAG_IMAGE_WIDTH);
        int pictureHeight = jpegDirectory.getInt(JpegDirectory.TAG_IMAGE_HEIGHT);
        String dimensions = pictureWidth + "x" + pictureHeight;
        exifMap.add(ExifTag.IMAGE_DIMENSIONS, dimensions, dimensions);
    }

    private void consumeCamera(ExifIFD0Directory directory) {
        consume(directory, ExifTag.CAMERA_MANUFACTURER, ExifDirectoryBase.TAG_MAKE);
        consume(directory, ExifTag.CAMERA_MODEL, ExifDirectoryBase.TAG_MODEL);
        consume(directory, ExifTag.CAMERA_FIRMWARE, ExifDirectoryBase.TAG_SOFTWARE);
        consume(directory, ExifTag.LENS_MANUFACTURER, ExifDirectoryBase.TAG_LENS_MAKE);
        consume(directory, ExifTag.LENS_MODEL, ExifDirectoryBase.TAG_LENS_MODEL);
        consume(directory, ExifTag.IMAGE_ORIENTATION, ExifDirectoryBase.TAG_ORIENTATION);
        try {
            exifMap.setPictureOrientation(directory.getInt(ExifDirectoryBase.TAG_ORIENTATION));
        } catch (MetadataException e) {
            // Let's just not make any orientation correction
        }
    }

    private void consumeImage(ExifSubIFDDirectory directory) {
        consume(directory, ExifTag.LENS_MODEL, ExifDirectoryBase.TAG_LENS_MODEL);
        consume(directory, ExifTag.LENS_MAXIMUM_APERTURE, ExifDirectoryBase.TAG_MAX_APERTURE);
        consume(directory, ExifTag.PICTURE_DATE, ExifDirectoryBase.TAG_DATETIME_ORIGINAL);
        consume(directory, ExifTag.FOCAL_LENGTH, ExifDirectoryBase.TAG_FOCAL_LENGTH);
        consume(directory, ExifTag.FOCAL_LENGTH_35MM_EQUIVALENT, ExifDirectoryBase.TAG_35MM_FILM_EQUIV_FOCAL_LENGTH);
        consume(directory, ExifTag.APERTURE, ExifDirectoryBase.TAG_FNUMBER);
        consume(directory, ExifTag.ISO, ExifDirectoryBase.TAG_ISO_EQUIVALENT);
        consume(directory, ExifTag.EXPOSURE_TIME, ExifDirectoryBase.TAG_EXPOSURE_TIME);
        consume(directory, ExifTag.EXPOSURE_PROGRAM, ExifDirectoryBase.TAG_EXPOSURE_PROGRAM);
        consume(directory, ExifTag.DISTANCE_FROM_SUBJECT, ExifDirectoryBase.TAG_SUBJECT_DISTANCE);
        consume(directory, ExifTag.METERING_MODE, ExifDirectoryBase.TAG_METERING_MODE);
        consume(directory, ExifTag.WHITE_BALANCE, ExifDirectoryBase.TAG_WHITE_BALANCE);
        consume(directory, ExifTag.WHITE_BALANCE_MODE, ExifDirectoryBase.TAG_WHITE_BALANCE_MODE);
        consume(directory, ExifTag.FLASH, ExifDirectoryBase.TAG_FLASH);
        consume(directory, ExifTag.COLOR_SPACE, ExifDirectoryBase.TAG_COLOR_SPACE);
        consume(directory, ExifTag.EXPOSURE_MODE, ExifDirectoryBase.TAG_EXPOSURE_MODE);
        consume(directory, ExifTag.DIGITAL_ZOOM_RATIO, ExifDirectoryBase.TAG_DIGITAL_ZOOM_RATIO);
        consume(directory, ExifTag.GAIN_CONTROL, ExifDirectoryBase.TAG_GAIN_CONTROL);
        consume(directory, ExifTag.CONTRAST, ExifDirectoryBase.TAG_CONTRAST);
        consume(directory, ExifTag.SATURATION, ExifDirectoryBase.TAG_SATURATION);
        consume(directory, ExifTag.SHARPNESS, ExifDirectoryBase.TAG_SHARPNESS);
    }

    private void consumePanasonic(PanasonicMakernoteDirectory directory) {
        consume(directory, ExifTag.CAMERA_FIRMWARE, PanasonicMakernoteDirectory.TAG_FIRMWARE_VERSION);
        consume(directory, ExifTag.LENS_MODEL, PanasonicMakernoteDirectory.TAG_LENS_TYPE);
        consume(directory, ExifTag.LENS_FIRMWARE, PanasonicMakernoteDirectory.TAG_LENS_FIRMWARE_VERSION);
        consume(directory, ExifTag.WHITE_BALANCE, PanasonicMakernoteDirectory.TAG_WHITE_BALANCE);
        consume(directory, ExifTag.FOCUS_MODE, PanasonicMakernoteDirectory.TAG_FOCUS_MODE);
        consume(directory, ExifTag.COLOR_TEMPERATURE, PanasonicMakernoteDirectory.TAG_COLOR_TEMP_KELVIN);
        consume(directory, ExifTag.HDR, PanasonicMakernoteDirectory.TAG_HDR);

        // Other infos:
        // https://exiftool.org/TagNames/Panasonic.html
        // https://exiv2.org/tags-panasonic.html
    }

    private void consumeNikon1(NikonType1MakernoteDirectory directory) {
        // To be implemented - I don't have access to a Nikon camera
    }

    private void consumeNikon2(NikonType2MakernoteDirectory directory) {
        consume(directory, ExifTag.LENS_MODEL, NikonType2MakernoteDirectory.TAG_LENS);
        consume(directory, ExifTag.LENS_FIRMWARE, NikonType2MakernoteDirectory.TAG_FIRMWARE_VERSION);
        consume(directory, ExifTag.FOCUS_MODE, NikonType2MakernoteDirectory.TAG_AF_TYPE);
    }

    private void consumeCanon(CanonMakernoteDirectory directory) {
        // It seems that Canon (full frame?) cameras does not provide the FOCAL_LENGTH_35MM_EQUIVALENT
        // https://exiftool.org/TagNames/Canon.html#SensorInfo
    }

    private void consume(Directory directory, ExifTag exifTag, int tag) {
        exifMap.add(exifTag, directory.getDescription(tag), directory.getObject(tag));
    }

    private void printAllTags(Metadata metadata) {
        for (Directory directory : metadata.getDirectories()) {
            for (Tag tag : directory.getTags()) {
                messageHandler.handleInfoMessage(String.format("[%s] - %s [%s] = %s%n", directory.getName(), tag.getTagName(), tag.getTagTypeHex(),
                        tag.getDescription()));
            }
            if (directory.hasErrors()) {
                for (String error : directory.getErrors()) {
                    messageHandler.handleErrorMessage(String.format("ERROR: %s%n", error));
                }
            }
        }
    }

}
