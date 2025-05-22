package com.threeamigos.pixelpeeper.interfaces.filters.flavors;

import com.threeamigos.pixelpeeper.interfaces.filters.Filter;

/**
 * An interface exposing all parameters the fake RomyJona algorithm accepts.
 * Used to test the hosting capabilities of the filter preferences window.
 *
 * @author Stefano Reksten
 */
public interface RomyJonaFilter extends Filter {

    void setPuppamento(int puppamento);

    int getPuppamento();

    void setANastro(boolean aNastro);

    boolean isANastro();

}
