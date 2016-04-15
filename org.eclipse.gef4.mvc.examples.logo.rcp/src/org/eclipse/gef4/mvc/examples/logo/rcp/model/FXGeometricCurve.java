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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.gef4.common.beans.property.ReadOnlyListWrapperEx;
import org.eclipse.gef4.common.collections.CollectionUtils;
import org.eclipse.gef4.geometry.planar.ICurve;
import org.eclipse.gef4.geometry.planar.IGeometry;
import org.eclipse.gef4.geometry.planar.Point;
import org.eclipse.gef4.geometry.planar.PolyBezier;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.effect.Effect;
import javafx.scene.paint.Paint;

// TODO: parameterize with concrete ICurve and encapsulate construction of geometry; limit the number of waypoints if needed
public class FXGeometricCurve extends AbstractFXGeometricElement<ICurve> {

	public enum Decoration {
		NONE, ARROW, CIRCLE
	}

	public enum RoutingStyle {
		STRAIGHT, ORTHOGONAL
	}

	public static final String SOURCE_DECORATION_PROPERTY = "sourceDecoration";
	public static final String TARGET_DECORATION_PROPERTY = "targetDecoration";
	public static final String ROUTING_STYLE_PROPERTY = "routingStyle";
	public static final String WAY_POINTS_PROPERTY = "wayPoints";
	public static final String DASHES_PROPERTY = "dashes";

	public static ICurve constructCurveFromWayPoints(Point... waypoints) {
		if (waypoints == null || waypoints.length == 0) {
			waypoints = new Point[] { new Point(), new Point() };
		} else if (waypoints.length == 1) {
			waypoints = new Point[] { new Point(), waypoints[0] };
		}
		return PolyBezier.interpolateCubic(waypoints);
	}

	private final ReadOnlyListWrapperEx<Point> wayPointsProperty = new ReadOnlyListWrapperEx<>(
			this, WAY_POINTS_PROPERTY,
			CollectionUtils.<Point> observableArrayList());
	private final ObjectProperty<Decoration> sourceDecorationProperty = new SimpleObjectProperty<>(
			this, SOURCE_DECORATION_PROPERTY, Decoration.NONE);
	private final ObjectProperty<Decoration> targetDecorationProperty = new SimpleObjectProperty<>(
			this, TARGET_DECORATION_PROPERTY, Decoration.NONE);
	private final ObjectProperty<RoutingStyle> routingStyleProperty = new SimpleObjectProperty<>(
			this, ROUTING_STYLE_PROPERTY, RoutingStyle.STRAIGHT);
	private final ReadOnlyListWrapperEx<Double> dashesProperty = new ReadOnlyListWrapperEx<>(
			this, DASHES_PROPERTY,
			CollectionUtils.<Double> observableArrayList());
	private final Set<AbstractFXGeometricElement<? extends IGeometry>> sourceAnchorages = new HashSet<>();
	private final Set<AbstractFXGeometricElement<? extends IGeometry>> targetAnchorages = new HashSet<>();

	public FXGeometricCurve(Point[] waypoints, Paint stroke, double strokeWidth,
			Double[] dashes, Effect effect) {
		super(constructCurveFromWayPoints(waypoints), stroke, strokeWidth,
				effect);
		wayPointsProperty.addAll(Arrays.asList(waypoints));
		dashesProperty.addAll(dashes);
	}

	public void addSourceAnchorage(
			AbstractFXGeometricElement<? extends IGeometry> anchored) {
		sourceAnchorages.add(anchored);
	}

	public void addTargetAnchorage(
			AbstractFXGeometricElement<? extends IGeometry> anchored) {
		targetAnchorages.add(anchored);
	}

	public void addWayPoint(int i, Point p) {
		// TODO: check index != 0 && index != end
		List<Point> points = getWayPointsCopy();
		points.add(i, p);
		setWayPoints(points.toArray(new Point[] {}));
	}

	public ReadOnlyListProperty<Double> dashesProperty() {
		return dashesProperty.getReadOnlyProperty();
	}

	public Double[] getDashes() {
		return dashesProperty.get().toArray(new Double[] {});
	}

	public RoutingStyle getRoutingStyle() {
		return routingStyleProperty.get();
	}

	public Set<AbstractFXGeometricElement<? extends IGeometry>> getSourceAnchorages() {
		return sourceAnchorages;
	}

	public Decoration getSourceDecoration() {
		return sourceDecorationProperty.get();
	}

	public Set<AbstractFXGeometricElement<? extends IGeometry>> getTargetAnchorages() {
		return targetAnchorages;
	}

	public Decoration getTargetDecoration() {
		return targetDecorationProperty.get();
	}

	public List<Point> getWayPoints() {
		return Collections.unmodifiableList(wayPointsProperty.get());
	}

	public List<Point> getWayPointsCopy() {
		return new ArrayList<>(getWayPoints());
	}

	public void removeWayPoint(int i) {
		// TODO: check index
		List<Point> points = getWayPointsCopy();
		points.remove(i);
		setWayPoints(points.toArray(new Point[] {}));
	}

	/**
	 * Returns the {@link ObjectProperty} for the {@link RoutingStyle} of this
	 * {@link FXGeometricCurve}.
	 *
	 * @return The {@link ObjectProperty} for the {@link RoutingStyle} of this
	 *         {@link FXGeometricCurve}.
	 */
	public ObjectProperty<RoutingStyle> routingStyleProperty() {
		return routingStyleProperty;
	}

	public void setRoutingStyle(RoutingStyle routingStyle) {
		routingStyleProperty.set(routingStyle);
	}

	public void setSourceDecoration(Decoration sourceDecoration) {
		sourceDecorationProperty.set(sourceDecoration);
	}

	public void setTargetDecoration(Decoration targetDecoration) {
		targetDecorationProperty.set(targetDecoration);
	}

	public void setWayPoint(int i, Point p) {
		List<Point> points = getWayPointsCopy();
		points.set(i, p);
		setWayPoints(points.toArray(new Point[] {}));
	}

	public void setWayPoints(Point... waypoints) {
		// cache waypoints and polybezier
		this.wayPointsProperty.setAll(Arrays.asList(waypoints));
		setGeometry(constructCurveFromWayPoints(waypoints));
	}

	/**
	 * Returns the {@link ObjectProperty} for the source {@link Decoration} of
	 * this {@link FXGeometricCurve}.
	 *
	 * @return The {@link ObjectProperty} for the source {@link Decoration} of
	 *         this {@link FXGeometricCurve}.
	 */
	public ObjectProperty<Decoration> sourceDecorationProperty() {
		return sourceDecorationProperty;
	}

	/**
	 * Returns the {@link ObjectProperty} for the target {@link Decoration} of
	 * this {@link FXGeometricCurve}.
	 *
	 * @return The {@link ObjectProperty} for the target {@link Decoration} of
	 *         this {@link FXGeometricCurve}.
	 */
	public ObjectProperty<Decoration> targetDecorationProperty() {
		return targetDecorationProperty;
	}

	public ReadOnlyListProperty<Point> wayPointsProperty() {
		return wayPointsProperty.getReadOnlyProperty();
	}

}
