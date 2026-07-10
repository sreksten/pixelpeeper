package com.threeamigos.pixelpeeper.implementations.eventbus.events;

import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifTagVisibility;

public class TagVisibilityChangedEvent {
    public final ExifTag tag;
    public final ExifTagVisibility visibility;

    public TagVisibilityChangedEvent(ExifTag tag, ExifTagVisibility visibility) {
        this.tag = tag;
        this.visibility = visibility;
    }
}
