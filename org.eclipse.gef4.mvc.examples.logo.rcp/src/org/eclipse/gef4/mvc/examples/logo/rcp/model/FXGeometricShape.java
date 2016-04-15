/*******************************************************************************
 * Copyright (c) 2014, 2015 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alexander Nyßen (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.mvc.examples.logo.rcp.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.gef4.geometry.planar.AffineTransform;
import org.eclipse.gef4.geometry.planar.IGeometry;
import org.eclipse.gef4.geometry.planar.IShape;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

public class FXGeometricShape extends AbstractFXGeometricElement<IShape> {

	public static final String FILL_PROPERTY = "fill";

	private final Set<AbstractFXGeometricElement<? extends IGeometry>> anchorages = new HashSet<>();

	private final ObjectProperty<Paint> fillProperty = new SimpleObjectProperty<>(
			this, FILL_PROPERTY);

	public FXGeometricShape(IShape shape, AffineTransform transform,
			Color stroke, double strokeWidth, Paint fill, Effect effect) {
		super(shape, transform, stroke, strokeWidth, effect);
		setFill(fill);
	}

	public FXGeometricShape(IShape shape, AffineTransform transform, Paint fill,
			Effect effect) {
		this(shape, transform, new Color(0, 0, 0, 1), 1.0, fill, effect);
	}

	public void addAnchorage(
			AbstractFXGeometricElement<? extends IGeometry> anchorage) {
		this.anchorages.add(anchorage);
	}

	public ObjectProperty<Paint> fillProperty() {
		return fillProperty;
	}

	public Set<AbstractFXGeometricElement<? extends IGeometry>> getAnchorages() {
		return anchorages;
	}

	public Paint getFill() {
		return fillProperty.get();
	}

	public void setFill(Paint fill) {
		fillProperty.set(fill);
	}

}
