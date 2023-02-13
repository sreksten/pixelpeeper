package com.threeamigos.pixelpeeper.interfaces.edgedetect.flavours;

import com.threeamigos.pixelpeeper.interfaces.edgedetect.EdgesDetector;

public interface RomyJonaEdgesDetector extends EdgesDetector {

	public void setPuppamento(int puppamento);

	public int getPuppamento();

	public void setANastro(boolean aNastro);

	public boolean isANastro();

}
