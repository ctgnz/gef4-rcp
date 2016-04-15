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

import org.eclipse.gef4.geometry.planar.AffineTransform;
import org.eclipse.gef4.geometry.planar.IGeometry;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;

abstract public class AbstractFXGeometricElement<G extends IGeometry> {

	public static final String GEOMETRY_PROPERTY = "geometry";
	public static final String TRANSFORM_PROPERTY = "transform";
	public static final String STROKE_WIDTH_PROPERTY = "strokeWidth";

	private final ObjectProperty<G> geometryProperty = new SimpleObjectProperty<>(
			this, GEOMETRY_PROPERTY);
	private final ObjectProperty<AffineTransform> transformProperty = new SimpleObjectProperty<>(
			this, TRANSFORM_PROPERTY);
	private Paint stroke = new Color(0, 0, 0, 1);
	private Effect effect;
	private final DoubleProperty strokeWidthProperty = new SimpleDoubleProperty(
			this, STROKE_WIDTH_PROPERTY, 0.5);

	public AbstractFXGeometricElement(G geometry) {
		setGeometry(geometry);
	}

	public AbstractFXGeometricElement(G geometry, AffineTransform transform,
			Paint stroke, double strokeWidth, Effect effect) {
		this(geometry);
		setTransform(transform);
		setEffect(effect);
		setStroke(stroke);
		setStrokeWidth(strokeWidth);
	}

	public AbstractFXGeometricElement(G geometry, Paint stroke,
			double strokeWidth, Effect effect) {
		setGeometry(geometry);
		setEffect(effect);
		setStroke(stroke);
		setStrokeWidth(strokeWidth);
	}

	public Effect getEffect() {
		return effect;
	}

	public G getGeometry() {
		return geometryProperty.get();
	}

	public Paint getStroke() {
		return stroke;
	}

	public double getStrokeWidth() {
		return strokeWidthProperty.get();
	}

	public AffineTransform getTransform() {
		return transformProperty.get();
	}

	public void setEffect(Effect effect) {
		this.effect = effect;
	}

	public void setGeometry(G geometry) {
		geometryProperty.set(geometry);
	}

	public void setStroke(Paint stroke) {
		this.stroke = stroke;
	}

	public void setStrokeWidth(double strokeWidth) {
		strokeWidthProperty.set(strokeWidth);
	}

	public void setTransform(AffineTransform transform) {
		transformProperty.set(transform);
	}

}