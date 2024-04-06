package com.threeamigos.pixelpeeper.interfaces.datamodel;

import com.threeamigos.common.util.interfaces.messagehandler.MessageHandler;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.ImageHandlingPreferences;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavours.SessionPreferences;
import com.threeamigos.pixelpeeper.interfaces.ui.ExifTagsFilter;

public interface DataModelBuilder {

    interface StepMessageHandler {
        StepSessionPreferences withMessageHandler(MessageHandler messageHandler);
    }

    interface StepSessionPreferences {
        StepImageSlices withSessionPreferences(SessionPreferences sessionPreferences);
    }

    interface StepImageSlices {
        StepImageHandlingPreferences withImageSlices(ImageSlices imageSlices);
    }

    interface StepImageHandlingPreferences {
        StepExifCache withImageHandlingPreferences(ImageHandlingPreferences imageHandlingPreferences);
    }

    interface StepExifCache {
        StepExifImageReader withExifCache(ExifCache exifCache);
    }

    interface StepExifImageReader {
        StepExifTagsClassifier withExifImageReader(ExifImageReader exifImageReader);
    }

    interface StepExifTagsClassifier {
        StepExifTagsFilter withExifTagsClassifier(ExifTagsClassifier exifTagsClassifier);
    }

    interface StepExifTagsFilter {
        StepBuild withExifTagsFilter(ExifTagsFilter exifTagsFilter);
    }

    interface StepBuild {
        DataModel build();
    }

    MessageHandler getMessageHandler();

    SessionPreferences getSessionPreferences();

    ImageSlices getImageSlices();

    ImageHandlingPreferences getImageHandlingPreferences();

    ExifCache getExifCache();

    ExifImageReader getExifImageReader();

    ExifTagsClassifier getExifTagsClassifier();

    ExifTagsFilter getExifTagsFilter();
}
