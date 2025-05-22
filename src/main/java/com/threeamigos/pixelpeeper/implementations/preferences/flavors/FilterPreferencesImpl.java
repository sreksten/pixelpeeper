package com.threeamigos.pixelpeeper.implementations.preferences.flavors;

import com.threeamigos.common.util.implementations.BasicPropertyChangeAware;
import com.threeamigos.pixelpeeper.interfaces.datamodel.CommunicationMessages;
import com.threeamigos.pixelpeeper.interfaces.filters.FilterFlavor;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.FilterPreferences;

public class FilterPreferencesImpl extends BasicPropertyChangeAware implements FilterPreferences {

	private boolean showFilterResults;
	private int transparency;
	private FilterFlavor filterFlavor;

	@Override
	public void setShowResults(boolean showFilterResults) {
		boolean oldShowEdges = this.showFilterResults;
		this.showFilterResults = showFilterResults;
		firePropertyChange(CommunicationMessages.FILTER_VISIBILITY_CHANGED, oldShowEdges, showFilterResults);
	}

	@Override
	public boolean isShowResults() {
		return showFilterResults;
	}

	@Override
	public void setTransparency(int transparency) {
		int oldTransparency = this.transparency;
		this.transparency = transparency;
		firePropertyChange(CommunicationMessages.FILTER_TRANSPARENCY_CHANGED, oldTransparency, transparency);
	}

	@Override
	public int getTransparency() {
		return transparency;
	}

	@Override
	public void setFilterFlavor(FilterFlavor filterFlavor) {
		FilterFlavor oldFilterFlavor = this.filterFlavor;
		this.filterFlavor = filterFlavor;
		firePropertyChange(CommunicationMessages.FILTER_FLAVOR_CHANGED, oldFilterFlavor,
				filterFlavor);
	}

	@Override
	public FilterFlavor getFilterFlavor() {
		return filterFlavor;
	}

	@Override
	public void loadDefaultValues() {
		showFilterResults = FilterPreferences.SHOW_RESULT_DEFAULT;
		transparency = FilterPreferences.TRANSPARENCY_DEFAULT;
		filterFlavor = FilterPreferences.FILTER_FLAVOR_DEFAULT;
	}

	@Override
	public void validate() {
		if (transparency < NO_TRANSPARENCY || transparency > TOTAL_TRANSPARENCY) {
			throw new IllegalArgumentException(String.format("Invalid edges transparency: %d", transparency));
		}
	}
}
