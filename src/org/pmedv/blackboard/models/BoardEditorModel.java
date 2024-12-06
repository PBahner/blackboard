/**

	BlackBoard breadboard designer
	Written and maintained by Matthias Pueski 
	
	Copyright (c) 2010-2011 Matthias Pueski
	
	This program is free software; you can redistribute it and/or
	modify it under the terms of the GNU General Public License
	as published by the Free Software Foundation; either version 2
	of the License, or (at your option) any later version.
	
	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.
	
	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.

 */
package org.pmedv.blackboard.models;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pmedv.blackboard.components.Item;
import org.pmedv.blackboard.components.Layer;
import org.pmedv.blackboard.components.Line;
import org.pmedv.blackboard.components.Part;
import org.pmedv.blackboard.components.TextPart;

/**
 * The {@link BoardEditorModel} contains all elements of
 * a board.
 * 
 * @see Item
 * @see Line
 * @see Part
 * @see TextPart
 * 
 * @author Matthias Pueski (18.06.2011)
 *
 */
public class BoardEditorModel {
	
	public static final Log log = LogFactory.getLog(BoardEditorModel.class);

	/*
	WARNING
	Do not use XX_LAYER_INDEX for identifying layers.
	This will cause problems because it can be changed through the user (by changing the layer hierarchy).
	Use it only on initialization!
	 */
	protected static final int TOP_LAYER_INDEX = 0;
	public static final String TOP_LAYER_NAME = "Top";
	protected static final int BOTTOM_LAYER = 1;
	public static final String BOTTOM_LAYER_NAME = "Bottom";
	public static final int PART_LAYER_INDEX = 2;
	public static final String PART_LAYER_NAME = "Parts";
	protected static final int BOARD_LAYER = 3;
	public static final String BOARD_LAYER_NAME = "Board";
	
	public static enum BoardType {
		HOLES,
		STRIPES,
		BLANK,
		SCHEMATICS,
		CUSTOM
	}
	
	private Layer currentLayer;
	private ArrayList<Layer> layers;
	private int width;
	private int height;
	private BoardType type;
	private BufferedImage backgroundImage;
	private String backgroundImageLocation;
	
	public BoardEditorModel() {
		layers = new ArrayList<Layer>();
		type = BoardType.BLANK;
	}

	public void addDefaultLayers() {
		layers.add(new Layer(TOP_LAYER_INDEX, TOP_LAYER_NAME, Color.RED));
		layers.add(new Layer(BOTTOM_LAYER, BOTTOM_LAYER_NAME, Color.BLUE));
		layers.add(new Layer(PART_LAYER_INDEX, PART_LAYER_NAME));
		layers.add(new Layer(BOARD_LAYER, BOARD_LAYER_NAME));
	}
	
	/**
	 * @return the layers
	 */
	public ArrayList<Layer> getLayers() {
		return layers;
	}

	/**
	 * @param layers the layers to set
	 */
	public void setLayers(ArrayList<Layer> layers) {
		this.layers = layers;
	}

	public Layer getLayer(int layer) {
		
		for (Layer l : layers) {
			if (l.getIndex() == layer)
				return l;
		}
			
		
		return null;
	}

	public Layer getLayer(String name) {
		for (Layer l : layers) {
			if (l.getName().equals(name))
				return l;
		}
		return null;
	}

	public int getLayerIndexByName(String name) {
		for (Layer l : layers) {
			if (l.getName().equals(name))
				return  l.getIndex();
		}
		return -1;
	}

	/**
	 * @return the currentLayer
	 */
	public Layer getCurrentLayer() {
		return currentLayer;
	}

	/**
	 * @param currentLayer the currentLayer to set
	 */
	public void setCurrentLayer(Layer currentLayer) {
		log.info("current layer "+currentLayer);
		this.currentLayer = currentLayer;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return the type
	 */
	public BoardType getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(BoardType type) {
		this.type = type;
	}

	/**
	 * @return the backgroundImage
	 */
	public BufferedImage getBackgroundImage() {
		return backgroundImage;
	}

	/**
	 * @param backgroundImage the backgroundImage to set
	 */
	public void setBackgroundImage(BufferedImage backgroundImage) {
		this.backgroundImage = backgroundImage;
	}

	/**
	 * @return the backgroundImageLocation
	 */
	public String getBackgroundImageLocation() {
		return backgroundImageLocation;
	}

	/**
	 * @param backgroundImageLocation the backgroundImageLocation to set
	 */
	public void setBackgroundImageLocation(String backgroundImageLocation) {
		this.backgroundImageLocation = backgroundImageLocation;
	}
	
	
}
