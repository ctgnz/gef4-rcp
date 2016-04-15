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
package org.eclipse.gef4.mvc.examples.logo.rcp.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.gef4.fx.anchors.DynamicAnchor;
import org.eclipse.gef4.fx.anchors.IAnchor;
import org.eclipse.gef4.fx.nodes.Connection;
import org.eclipse.gef4.fx.nodes.OrthogonalRouter;
import org.eclipse.gef4.fx.nodes.PolyBezierInterpolator;
import org.eclipse.gef4.fx.nodes.PolylineInterpolator;
import org.eclipse.gef4.fx.nodes.StraightRouter;
import org.eclipse.gef4.geometry.planar.AffineTransform;
import org.eclipse.gef4.geometry.planar.IGeometry;
import org.eclipse.gef4.geometry.planar.Point;
import org.eclipse.gef4.mvc.examples.logo.rcp.model.AbstractFXGeometricElement;
import org.eclipse.gef4.mvc.examples.logo.rcp.model.FXGeometricCurve;
import org.eclipse.gef4.mvc.examples.logo.rcp.model.FXGeometricCurve.Decoration;
import org.eclipse.gef4.mvc.examples.logo.rcp.model.FXGeometricCurve.RoutingStyle;
import org.eclipse.gef4.mvc.parts.IBendableContentPart;
import org.eclipse.gef4.mvc.parts.IContentPart;
import org.eclipse.gef4.mvc.parts.ITransformableContentPart;
import org.eclipse.gef4.mvc.parts.IVisualPart;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import com.google.common.reflect.TypeToken;
import com.google.inject.Provider;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;

public class FXGeometricCurvePart
extends AbstractFXGeometricElementPart<Connection>
implements ITransformableContentPart<Node, Connection>,
IBendableContentPart<Node, Connection> {

  public static class ArrowHead extends Polygon {
    public ArrowHead() {
      super(0, 0, 10, 3, 10, -3);
      setFill(Color.TRANSPARENT);
    }
  }

  public static class CircleHead extends Circle {
    public CircleHead() {
      super(5);
      setFill(Color.TRANSPARENT);
    }
  }

  private static final String END_ROLE = "END";

  private static final String START_ROLE = "START";

  private final CircleHead START_CIRCLE_HEAD = new CircleHead();

  private final CircleHead END_CIRCLE_HEAD = new CircleHead();

  private final ArrowHead START_ARROW_HEAD = new ArrowHead();
  private final ArrowHead END_ARROW_HEAD = new ArrowHead();
  private FXGeometricCurve previousContent;

  // refresh visual upon model property changes
  private final ListChangeListener<Point> wayPointsChangeListener = new ListChangeListener<Point>() {
    @Override
    public void onChanged(
      javafx.collections.ListChangeListener.Change<? extends Point> c) {
      refreshVisual();
    }
  };
  private final ListChangeListener<Double> dashesChangeListener = new ListChangeListener<Double>() {
    @Override
    public void onChanged(
      javafx.collections.ListChangeListener.Change<? extends Double> c) {
      refreshVisual();
    }
  };
  private final ChangeListener<RoutingStyle> routingStyleChangeListener = new ChangeListener<RoutingStyle>() {
    @Override
    public void changed(ObservableValue<? extends RoutingStyle> observable,
      RoutingStyle oldValue, RoutingStyle newValue) {
      refreshVisual();
    }
  };
  private final ChangeListener<Decoration> decorationChangeListener = new ChangeListener<Decoration>() {
    @Override
    public void changed(ObservableValue<? extends Decoration> observable,
      Decoration oldValue, Decoration newValue) {
      refreshVisual();
    }
  };

  @SuppressWarnings("serial")
  @Override
  protected void attachToAnchorageVisual(
    IVisualPart<Node, ? extends Node> anchorage, String role) {
    Provider<? extends IAnchor> anchorProvider = anchorage
        .getAdapter(new TypeToken<Provider<? extends IAnchor>>() {
        });
    if (anchorProvider == null) {
      throw new IllegalStateException(
                                      "Require <Provider<IAnchor>> adapter at <"
                                          + anchorage.getClass() + ">.");
    }
    IAnchor anchor = anchorProvider.get();
    if (role.equals(START_ROLE)) {
      // System.out.println(
      // "Setting start anchor of curve " + this + " to " + anchor);
      getVisual().setStartAnchor(anchor);
    } else if (role.equals(END_ROLE)) {
      // System.out.println(
      // "Setting end anchor of curve " + this + " to " + anchor);
      getVisual().setEndAnchor(anchor);
    } else {
      throw new IllegalStateException(
                                      "Cannot attach to anchor with role <" + role + ">.");
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void bendContent(List<BendPoint> bendPoints) {
    getContent().getSourceAnchorages().clear();
    getContent().getTargetAnchorages().clear();
    List<Point> waypoints = new ArrayList<>();
    for (int i = 0; i < bendPoints.size(); i++) {
      BendPoint bp = bendPoints.get(i);
      if (bp.isAttached()) {
        if (i == 0) {
          // update start anchorage
          // TODO: introduce setter so this is more concise
          getContent().addSourceAnchorage(
                                          (AbstractFXGeometricElement<? extends IGeometry>) bp
                                          .getContentAnchorage());
        }
        if (i == bendPoints.size() - 1) {
          // update end anchorage
          // TODO: introduce setter so this is more concise
          getContent().addTargetAnchorage(
                                          (AbstractFXGeometricElement<? extends IGeometry>) bp
                                          .getContentAnchorage());
        }
      } else {
        waypoints.add(bp.getPosition());
      }
    }
    refreshContentAnchorages();
    getContent().setWayPoints(waypoints.toArray(new Point[] {}));
  }

  @Override
  protected Connection createVisual() {
    Connection visual = new Connection();
    visual.setInterpolator(new PolyBezierInterpolator());
    visual.getCurveNode().setStrokeLineCap(StrokeLineCap.BUTT);
    return visual;
  }

  @Override
  protected void detachFromAnchorageVisual(
    IVisualPart<Node, ? extends Node> anchorage, String role) {
    if (role.equals(START_ROLE)) {
      // System.out.println("Unsetting start anchor of curve.");
      getVisual().setStartPoint(getVisual().getStartPoint());
    } else if (role.equals(END_ROLE)) {
      // System.out.println("Unsetting end anchor of curve.");
      getVisual().setEndPoint(getVisual().getEndPoint());
    } else {
      throw new IllegalStateException(
                                      "Cannot detach from anchor with role <" + role + ">.");
    }
  }

  @Override
  protected void doAttachToContentAnchorage(Object contentAnchorage,
    String role) {
    if (!(contentAnchorage instanceof AbstractFXGeometricElement)) {
      throw new IllegalArgumentException(
          "Inappropriate content anchorage: wrong type.");
    }
    AbstractFXGeometricElement<?> geom = (AbstractFXGeometricElement<?>) contentAnchorage;
    if (START_ROLE.equals(role)) {
      getContent().getSourceAnchorages().add(geom);
    } else if (END_ROLE.equals(role)) {
      getContent().getTargetAnchorages().add(geom);
    }
  }

  @Override
  protected void doDetachFromContentAnchorage(Object contentAnchorage,
    String role) {
    if (START_ROLE.equals(role)) {
      getContent().getSourceAnchorages().remove(contentAnchorage);
    } else if (END_ROLE.equals(role)) {
      getContent().getTargetAnchorages().remove(contentAnchorage);
    }
  }

  @Override
  protected SetMultimap<Object, String> doGetContentAnchorages() {
    SetMultimap<Object, String> anchorages = HashMultimap.create();

    Set<AbstractFXGeometricElement<? extends IGeometry>> sourceAnchorages = getContent()
        .getSourceAnchorages();
    for (Object src : sourceAnchorages) {
      anchorages.put(src, START_ROLE);
    }
    Set<AbstractFXGeometricElement<? extends IGeometry>> targetAnchorages = getContent()
        .getTargetAnchorages();
    for (Object dst : targetAnchorages) {
      anchorages.put(dst, END_ROLE);
    }
    return anchorages;
  }

  @Override
  protected List<? extends Object> doGetContentChildren() {
    return Collections.emptyList();
  }

  @SuppressWarnings("serial")
  @Override
  protected void doRefreshVisual(Connection visual) {
    FXGeometricCurve content = getContent();

    List<Point> wayPoints = content.getWayPointsCopy();

    // TODO: why is this needed??
    AffineTransform transform = content.getTransform();
    if (previousContent == null || transform != null
        && !transform.equals(previousContent.getTransform())
        || transform == null
        && previousContent.getTransform() != null) {
      if (transform != null) {
        Point[] transformedWayPoints = transform
            .getTransformed(wayPoints.toArray(new Point[] {}));
        wayPoints = Arrays.asList(transformedWayPoints);
      }
    }

    if (!getContentAnchoragesUnmodifiable().containsValue(START_ROLE)) {
      if (!wayPoints.isEmpty()) {
        visual.setStartPoint(wayPoints.remove(0));
      } else {
        visual.setStartPoint(new Point());
      }
    }
    if (!getContentAnchoragesUnmodifiable().containsValue(END_ROLE)) {
      if (!wayPoints.isEmpty()) {
        visual.setEndPoint(wayPoints.remove(wayPoints.size() - 1));
      } else {
        visual.setEndPoint(new Point());
      }
    }
    if (!visual.getControlPoints().equals(wayPoints)) {
      visual.setControlPoints(wayPoints);
    }

    // decorations
    switch (content.getSourceDecoration())

    {
      case NONE:
        if (visual.getStartDecoration() != null) {
          visual.setStartDecoration(null);
        }
        break;
      case CIRCLE:
        if (visual.getStartDecoration() == null
        || !(visual.getStartDecoration() instanceof CircleHead)) {
          visual.setStartDecoration(START_CIRCLE_HEAD);
        }
        break;
      case ARROW:
        if (visual.getStartDecoration() == null
        || !(visual.getStartDecoration() instanceof ArrowHead)) {
          visual.setStartDecoration(START_ARROW_HEAD);
        }
        break;
    }
    switch (content.getTargetDecoration())

    {
      case NONE:
        if (visual.getEndDecoration() != null) {
          visual.setEndDecoration(null);
        }
        break;
      case CIRCLE:
        if (visual.getEndDecoration() == null
        || !(visual.getEndDecoration() instanceof CircleHead)) {
          visual.setEndDecoration(END_CIRCLE_HEAD);
        }
        break;
      case ARROW:
        if (visual.getEndDecoration() == null
        || !(visual.getEndDecoration() instanceof ArrowHead)) {
          visual.setEndDecoration(END_ARROW_HEAD);
        }
        break;
    }

    Shape startDecorationVisual = (Shape) visual.getStartDecoration();
    Shape endDecorationVisual = (Shape) visual.getEndDecoration();

    // stroke paint
    if (visual.getCurveNode().getStroke() != content.getStroke())

    {
      visual.getCurveNode().setStroke(content.getStroke());
    }
    if (startDecorationVisual != null
        && startDecorationVisual.getStroke() != content.getStroke())

    {
      startDecorationVisual.setStroke(content.getStroke());
    }
    if (endDecorationVisual != null
        && endDecorationVisual.getStroke() != content.getStroke())

    {
      endDecorationVisual.setStroke(content.getStroke());
    }

    // stroke width
    if (visual.getCurveNode().getStrokeWidth() != content.getStrokeWidth())

    {
      visual.getCurveNode().setStrokeWidth(content.getStrokeWidth());
    }
    if (startDecorationVisual != null && startDecorationVisual
        .getStrokeWidth() != content.getStrokeWidth())

    {
      startDecorationVisual.setStrokeWidth(content.getStrokeWidth());
    }
    if (endDecorationVisual != null && endDecorationVisual
        .getStrokeWidth() != content.getStrokeWidth())

    {
      endDecorationVisual.setStrokeWidth(content.getStrokeWidth());
    }

    // dashes
    List<Double> dashList = new ArrayList<>(content.getDashes().length);
    for (double d : content.getDashes())

    {
      dashList.add(d);
    }
    if (!visual.getCurveNode().getStrokeDashArray().equals(dashList))

    {
      visual.getCurveNode().getStrokeDashArray().setAll(dashList);
    }

    // connection router
    if (content.getRoutingStyle().equals(RoutingStyle.ORTHOGONAL))

    {
      // FIXME: Change the computation strategy from the operation that
      // changes the curve's isSegmentBased flag.
      Set<AbstractFXGeometricElement<? extends IGeometry>> sourceAnchorages = getContent()
          .getSourceAnchorages();
      if (!sourceAnchorages.isEmpty()) {
        AbstractFXGeometricElement<? extends IGeometry> source = sourceAnchorages
            .iterator().next();
        IContentPart<Node, ? extends Node> sourcePart = getViewer()
            .getContentPartMap().get(source);
        IAnchor sourceAnchor = sourcePart
            .getAdapter(new TypeToken<Provider<IAnchor>>() {
            }).get();
        ((DynamicAnchor) sourceAnchor).setComputationStrategy(
                                                              visual.getStartAnchorKey(),
                                                              new DynamicAnchor.OrthogonalProjectionStrategy());
      }

      Set<AbstractFXGeometricElement<? extends IGeometry>> targetAnchorages = getContent()
          .getTargetAnchorages();
      if (!targetAnchorages.isEmpty()) {
        AbstractFXGeometricElement<? extends IGeometry> target = targetAnchorages
            .iterator().next();
        IContentPart<Node, ? extends Node> targetPart = getViewer()
            .getContentPartMap().get(target);
        IAnchor targetAnchor = targetPart
            .getAdapter(new TypeToken<Provider<IAnchor>>() {
            }).get();
        ((DynamicAnchor) targetAnchor).setComputationStrategy(
                                                              visual.getEndAnchorKey(),
                                                              new DynamicAnchor.OrthogonalProjectionStrategy());
      }

      visual.setInterpolator(new PolylineInterpolator());
      visual.setRouter(new OrthogonalRouter());
    } else

    {
      // FIXME: Restore the computation strategy from the operation that
      // changes the curve's isSegmentBased flag.
      Set<AbstractFXGeometricElement<? extends IGeometry>> sourceAnchorages = getContent()
          .getSourceAnchorages();
      if (!sourceAnchorages.isEmpty()) {
        AbstractFXGeometricElement<? extends IGeometry> source = sourceAnchorages
            .iterator().next();
        IContentPart<Node, ? extends Node> sourcePart = getViewer()
            .getContentPartMap().get(source);
        if (sourcePart != null) {
          IAnchor sourceAnchor = sourcePart
              .getAdapter(new TypeToken<Provider<IAnchor>>() {
              }).get();
          ((DynamicAnchor) sourceAnchor).setComputationStrategy(
                                                                visual.getStartAnchorKey(), new DynamicAnchor.OrthogonalProjectionStrategy());
        }
      }

      Set<AbstractFXGeometricElement<? extends IGeometry>> targetAnchorages = getContent()
          .getTargetAnchorages();
      if (!targetAnchorages.isEmpty()) {
        AbstractFXGeometricElement<? extends IGeometry> target = targetAnchorages
            .iterator().next();
        IContentPart<Node, ? extends Node> targetPart = getViewer()
            .getContentPartMap().get(target);
        if (targetPart != null) {
          IAnchor targetAnchor = targetPart
              .getAdapter(new TypeToken<Provider<IAnchor>>() {
              }).get();
          ((DynamicAnchor) targetAnchor).setComputationStrategy(
                                                                visual.getEndAnchorKey(), new DynamicAnchor.OrthogonalProjectionStrategy());
        }
      }

      visual.setRouter(new StraightRouter());
      visual.setInterpolator(new PolyBezierInterpolator());
    }

    previousContent = content;

    // apply effect
    super.doRefreshVisual(visual);

  }

  @Override
  public FXGeometricCurve getContent() {
    return (FXGeometricCurve) super.getContent();
  }

  @Override
  public void setContent(Object model) {
    if (model != null && !(model instanceof FXGeometricCurve)) {
      throw new IllegalArgumentException(
          "Only ICurve models are supported.");
    }
    if (getContent() != null) {
      // remove property change listeners from model
      getContent().wayPointsProperty()
      .removeListener(wayPointsChangeListener);
      getContent().dashesProperty().removeListener(dashesChangeListener);
      getContent().routingStyleProperty()
      .removeListener(routingStyleChangeListener);
      getContent().sourceDecorationProperty()
      .removeListener(decorationChangeListener);
      getContent().targetDecorationProperty()
      .removeListener(decorationChangeListener);
    }
    super.setContent(model);
    if (getContent() != null) {
      // add property change listeners to model
      getContent().wayPointsProperty()
      .addListener(wayPointsChangeListener);
      getContent().dashesProperty().addListener(dashesChangeListener);
      getContent().routingStyleProperty()
      .addListener(routingStyleChangeListener);
      getContent().sourceDecorationProperty()
      .addListener(decorationChangeListener);
      getContent().targetDecorationProperty()
      .addListener(decorationChangeListener);
    }
  }

  @Override
  public void transformContent(AffineTransform transform) {
    // applying transform to content is done by transforming waypoints
    getContent().setWayPoints(transform.getTransformed(
                                                       getContent().getWayPoints().toArray(new Point[] {})));
  }

}