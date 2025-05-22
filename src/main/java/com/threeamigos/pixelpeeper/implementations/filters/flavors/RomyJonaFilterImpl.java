package com.threeamigos.pixelpeeper.implementations.filters.flavors;

import com.threeamigos.common.util.ui.effects.text.BorderedStringRenderer;
import com.threeamigos.pixelpeeper.interfaces.filters.flavors.RomyJonaFilter;
import com.threeamigos.pixelpeeper.interfaces.preferences.flavors.RomyJonaFilterPreferences;

import java.awt.*;
import java.awt.image.BufferedImage;

public class RomyJonaFilterImpl implements RomyJonaFilter {

    private int puppamento;
    private boolean aNastro;
    private BufferedImage sourceImage;
    private BufferedImage filteredImage;

    public RomyJonaFilterImpl(RomyJonaFilterPreferences romyJonaFilterPreferences) {
        this.puppamento = romyJonaFilterPreferences.getPuppamento();
        this.aNastro = romyJonaFilterPreferences.isANastro();
    }

    @Override
    public void setSourceImage(BufferedImage sourceImage) {
        this.sourceImage = sourceImage;
    }

    @Override
    public void process() {
        int width = sourceImage.getWidth();
        int height = sourceImage.getHeight();
        BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = newImage.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, width / 2, height / 2);
        g2d.fillRect(width / 2, height / 2, width, height);
        int fontHeight = height / 10;
        Font font = new Font("Arial", Font.BOLD, fontHeight);
        g2d.setFont(font);
        Color color;
        if (puppamento == 1) {
            color = Color.WHITE;
        } else if (puppamento == 2) {
            color = Color.YELLOW;
        } else {
            color = Color.RED;
        }
        int y = (height - fontHeight) / 2;
        BorderedStringRenderer.drawString(g2d, "PUPPI", 10, y, Color.BLACK, color);
        if (aNastro) {
            y += fontHeight + 5;
            BorderedStringRenderer.drawString(g2d, "a nastro", 10, y, Color.BLACK, color);
        }
        g2d.dispose();
        filteredImage = newImage;
    }

    public void abort() {
        // This is just a test class and makes no calculations
    }

    @Override
    public BufferedImage getResultingImage() {
        return filteredImage;
    }

    @Override
    public void setPuppamento(int puppamento) {
        this.puppamento = puppamento;
    }

    @Override
    public int getPuppamento() {
        return puppamento;
    }

    @Override
    public void setANastro(boolean aNastro) {
        this.aNastro = aNastro;
    }

    @Override
    public boolean isANastro() {
        return aNastro;
    }

}
