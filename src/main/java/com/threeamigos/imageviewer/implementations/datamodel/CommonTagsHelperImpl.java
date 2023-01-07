package com.threeamigos.imageviewer.implementations.datamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.threeamigos.imageviewer.data.ExifTag;
import com.threeamigos.imageviewer.data.PictureData;
import com.threeamigos.imageviewer.interfaces.datamodel.CommonTagsHelper;

public class CommonTagsHelperImpl implements CommonTagsHelper {

	private Collection<ExifTag> commonTags;
	private Map<ExifTag, Collection<String>> uncommonTagsToValues;
	private int mappedPictures;

	@Override
	public void updateCommonTags(Collection<PictureData> pictureData) {
		commonTags = new ArrayList<>();
		uncommonTagsToValues = new EnumMap<>(ExifTag.class);
		mappedPictures = pictureData.size();

		Set<ExifTag> allTags = new HashSet<>();
		pictureData.forEach(p -> allTags.addAll(p.getAllTags()));

		for (ExifTag tag : allTags) {
			Collection<String> values = pictureData.stream().map(picture -> picture.getTagDescriptive(tag))
					.collect(Collectors.toSet());
			if (values.size() == 1) {
				commonTags.add(tag);
			} else {
				uncommonTagsToValues.put(tag, values);
			}
		}
	}

	@Override
	public int getMappedPictures() {
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
