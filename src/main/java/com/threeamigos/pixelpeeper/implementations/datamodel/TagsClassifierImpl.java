package com.threeamigos.pixelpeeper.implementations.datamodel;

import com.threeamigos.pixelpeeper.data.ExifMap;
import com.threeamigos.pixelpeeper.data.ExifTag;
import com.threeamigos.pixelpeeper.data.ExifValue;
import com.threeamigos.pixelpeeper.interfaces.datamodel.TagsClassifier;

import java.util.*;
import java.util.stream.Collectors;

public class TagsClassifierImpl implements TagsClassifier {

    private Collection<ExifTag> commonTags;
    private Map<ExifTag, Collection<ExifValue>> uncommonTagsToValues;
    private int mappedPictures;

    @Override
    public void classifyTags(Collection<ExifMap> exifMaps) {
        commonTags = new ArrayList<>();
        uncommonTagsToValues = new EnumMap<>(ExifTag.class);
        mappedPictures = exifMaps.size();

        Set<ExifTag> allTags = new HashSet<>();
        exifMaps.forEach(map -> allTags.addAll(map.getKeys()));

        for (ExifTag tag : allTags) {
            Collection<ExifValue> values = exifMaps.stream().map(exifMap -> exifMap.getExifValue(tag))
                    .collect(Collectors.toSet());
            if (values.size() == 1) {
                commonTags.add(tag);
            } else {
                uncommonTagsToValues.put(tag, values);
            }
        }
    }

    @Override
    public int getTotalMappedPictures() {
        return mappedPictures;
    }

    @Override
    public boolean isCommonTag(ExifTag exifTag) {
        return commonTags.contains(exifTag) || !getUncommonTagsToValues().containsKey(exifTag);
    }

    @Override
    public Collection<ExifTag> getCommonTags() {
        return Collections.unmodifiableCollection(commonTags);
    }

    @Override
    public Map<ExifTag, Collection<ExifValue>> getUncommonTagsToValues() {
        return Collections.unmodifiableMap(uncommonTagsToValues);
    }

    @Override
    public Map<ExifTag, Collection<ExifValue>> getUncommonTagsToValues(Collection<ExifTag> tagsToReturn) {
        Map<ExifTag, Collection<ExifValue>> filteredMap = new EnumMap<>(ExifTag.class);
        for (ExifTag tag : tagsToReturn) {
            Collection<ExifValue> values = uncommonTagsToValues.get(tag);
            if (values != null && !values.isEmpty()) {
                filteredMap.put(tag, values);
            }
        }
        return Collections.unmodifiableMap(filteredMap);
    }

}
