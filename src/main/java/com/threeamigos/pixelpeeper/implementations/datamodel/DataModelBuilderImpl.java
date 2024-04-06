package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.common.util.interfaces.messagehandler.MessageHandler;
import com.threeamigos.pixelpeeper.interfaces.datamodel.*;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.EdgesDetectorPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ImageHandlingPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.SessionPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.ExifTagsFilter;

public class DataModelBuilderImpl implements DataModelBuilder,
        DataModelBuilder.StepMessageHandler,
        DataModelBuilder.StepSessionPreferences,
        DataModelBuilder.StepImageSlices,
        DataModelBuilder.StepImageHandlingPreferences,
        DataModelBuilder.StepEdgesDetectorPreferences,
        DataModelBuilder.StepExifCache,
        DataModelBuilder.StepExifImageReader,
        DataModelBuilder.StepExifTagsClassifier,
        DataModelBuilder.StepExifTagsFilter,
        DataModelBuilder.StepBuild {

    public static StepMessageHandler builder() {
        return new DataModelBuilderImpl();
    }

    private MessageHandler messageHandler;
    private SessionPreferences sessionPreferences;
    private ImageSlices imageSlices;
    private ImageHandlingPreferences imageHandlingPreferences;
    private EdgesDetectorPreferences edgesDetectorPreferences;
    private ExifCache exifCache;
    private ExifImageReader exifImageReader;
    private ExifTagsClassifier exifTagsClassifier;
    private ExifTagsFilter exifTagsFilter;

    @Override
    public StepSessionPreferences withMessageHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
        return this;
    }

    @Override
    public StepImageSlices withSessionPreferences(SessionPreferences sessionPreferences) {
        this.sessionPreferences = sessionPreferences;
        return this;
    }

    @Override
    public StepImageHandlingPreferences withImageSlices(ImageSlices imageSlices) {
        this.imageSlices = imageSlices;
        return this;
    }

    @Override
    public StepEdgesDetectorPreferences withImageHandlingPreferences(ImageHandlingPreferences imageHandlingPreferences) {
        this.imageHandlingPreferences = imageHandlingPreferences;
        return this;
    }

    @Override
    public StepExifCache withEdgesDetectorPreferences(EdgesDetectorPreferences edgesDetectorPreferences) {
        this.edgesDetectorPreferences = edgesDetectorPreferences;
        return this;
    }

    @Override
    public StepExifImageReader withExifCache(ExifCache exifCache) {
        this.exifCache = exifCache;
        return this;
    }

    @Override
    public StepExifTagsClassifier withExifImageReader(ExifImageReader exifImageReader) {
        this.exifImageReader = exifImageReader;
        return this;
    }

    @Override
    public StepExifTagsFilter withExifTagsClassifier(ExifTagsClassifier exifTagsClassifier) {
        this.exifTagsClassifier = exifTagsClassifier;
        return this;
    }


    @Override
    public StepBuild withExifTagsFilter(ExifTagsFilter exifTagsFilter) {
        this.exifTagsFilter = exifTagsFilter;
        return this;
    }

    @Override
    public DataModel build() {
        return new DataModelImpl(this);
    }

    public MessageHandler getMessageHandler() {
        return messageHandler;
    }

    public SessionPreferences getSessionPreferences() {
        return sessionPreferences;
    }

    public ImageSlices getImageSlices() {
        return imageSlices;
    }

    public ImageHandlingPreferences getImageHandlingPreferences() {
        return imageHandlingPreferences;
    }

    public EdgesDetectorPreferences getEdgesDetectorPreferences() {
        return edgesDetectorPreferences;
    }

    public ExifCache getExifCache() {
        return exifCache;
    }

    public ExifImageReader getExifImageReader() {
        return exifImageReader;
    }

    public ExifTagsClassifier getExifTagsClassifier() {
        return exifTagsClassifier;
    }

    public ExifTagsFilter getExifTagsFilter() {
        return exifTagsFilter;
    }
}
