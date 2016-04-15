/*******************************************************************************
 * Copyright (c) 2015 itemis AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Matthias Wienand (itemis AG) - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.gef4.mvc.examples.logo.rcp.policies;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.gef4.fx.nodes.Connection;
import org.eclipse.gef4.geometry.convert.fx.FX2Geometry;
import org.eclipse.gef4.geometry.planar.AffineTransform;
import org.eclipse.gef4.mvc.fx.policies.FXTransformPolicy;
import org.eclipse.gef4.mvc.fx.policies.IFXOnClickPolicy;
import org.eclipse.gef4.mvc.models.SelectionModel;
import org.eclipse.gef4.mvc.operations.DeselectOperation;
import org.eclipse.gef4.mvc.parts.IContentPart;
import org.eclipse.gef4.mvc.parts.IRootPart;
import org.eclipse.gef4.mvc.policies.AbstractInteractionPolicy;
import org.eclipse.gef4.mvc.policies.CreationPolicy;
import org.eclipse.gef4.mvc.viewer.IViewer;

import com.google.common.collect.HashMultimap;
import com.google.common.reflect.TypeToken;

import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

public class FXCloneOnClickPolicy extends AbstractInteractionPolicy<Node>
		implements IFXOnClickPolicy {

	@SuppressWarnings("serial")
	@Override
	public void click(MouseEvent event) {
		if (!isCloneModifierDown(event)) {
			return;
		}

		// clone content
		Object cloneContent = getHost()
				.getAdapter(AbstractCloneContentPolicy.class).cloneContent();

		// create the clone content part
		IRootPart<Node, ? extends Node> root = getHost().getRoot();
		CreationPolicy<Node> creationPolicy = root
				.getAdapter(new TypeToken<CreationPolicy<Node>>() {
				});
		init(creationPolicy);
		IContentPart<Node, ? extends Node> clonedContentPart = creationPolicy
				.create(cloneContent,
						(IContentPart<Node, ? extends Node>) getHost()
								.getParent(),
						HashMultimap
								.<IContentPart<Node, ? extends Node>, String> create());
		commit(creationPolicy);

		// XXX: Ensure start and end anchor are set for connections, so that
		// they can be interacted with (transform/bend).
		if (clonedContentPart.getVisual() instanceof Connection) {
			Connection connection = (Connection) clonedContentPart.getVisual();
			if (connection.getStartAnchor() == null) {
				connection.setStartPoint(
						((Connection) getHost().getVisual()).getStartPoint());
			}
			if (connection.getEndAnchor() == null) {
				connection.setEndPoint(
						((Connection) getHost().getVisual()).getEndPoint());
			}
		}

		// deselect all but the clone
		IViewer<Node> viewer = getHost().getRoot().getViewer();
		List<? extends IContentPart<Node, ? extends Node>> toBeDeselected = new ArrayList<>(
				viewer.getAdapter(new TypeToken<SelectionModel<Node>>() {
				}).getSelectionUnmodifiable());
		toBeDeselected.remove(clonedContentPart);
		try {
			viewer.getDomain().execute(new DeselectOperation<>(
					getHost().getRoot().getViewer(), toBeDeselected));
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}

		// copy the transformation
		AffineTransform originalTransform = FX2Geometry.toAffineTransform(
				getHost().getAdapter(FXTransformPolicy.TRANSFORM_PROVIDER_KEY)
						.get());
		FXTransformPolicy transformPolicy = clonedContentPart
				.getAdapter(FXTransformPolicy.class);
		init(transformPolicy);
		transformPolicy.setTransform(originalTransform);
		commit(transformPolicy);
	}

	protected boolean isCloneModifierDown(MouseEvent e) {
		return e.isAltDown() || e.isShiftDown();
	}

}
