package com.threeamigos.pixelpeeper.interfaces.edgedetect.flavours;

import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetector;

public interface CannyEdgesDetector extends EdgesDetector {

    float getLowThreshold();

    void setLowThreshold(float lowThreshold);

    float getHighThreshold();

    void setHighThreshold(float highThreshold);

    float getGaussianKernelRadius();

    void setGaussianKernelRadius(float gaussianKernelRadius);

    int getGaussianKernelWidth();

    void setGaussianKernelWidth(int gaussianKernelWidth);

    boolean isContrastNormalized();

    void setContrastNormalized(boolean contrastNormalized);

}
