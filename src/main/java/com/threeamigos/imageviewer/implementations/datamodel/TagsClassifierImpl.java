package com.threeamigos.imageviewer.implementations.datamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.threeamigos.imageviewer.data.ExifMap;
import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.interfaces.datamodel.TagsClassifier;

public class TagsClassifierImpl implements TagsClassifier {

	private Collection<ExifTag> commonTags;
	private Map<ExifTag, Collection<String>> uncommonTagsToValues;
	private int mappedPictures;

	@Override
	public void classifyTags(Collection<ExifMap> exifMaps) {
		commonTags = new ArrayList<>();
		uncommonTagsToValues = new EnumMap<>(ExifTag.class);
		mappedPictures = exifMaps.size();

		Set<ExifTag> allTags = new HashSet<>();
		exifMaps.forEach(map -> allTags.addAll(map.getTags()));

		for (ExifTag tag : allTags) {
			Collection<String> values = exifMaps.stream().map(exifMap -> exifMap.getTagDescriptive(tag))
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
		return commonTags.contains(exifTag) || !getUncommonTagsToValues().keySet().contains(exifTag);
	}

	@Override
	public Collection<ExifTag> getCommonTags() {
		return Collections.unmodifiableCollection(commonTags);
	}

	@Override
	public Map<ExifTag, Collection<String>> getUncommonTagsToValues() {
		return Collections.unmodifiableMap(uncommonTagsToValues);
	}

}
