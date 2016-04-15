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

import org.eclipse.gef4.mvc.examples.logo.rcp.model.AbstractFXGeometricElement;
import org.eclipse.gef4.mvc.fx.parts.AbstractFXContentPart;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;

public abstract class AbstractFXGeometricElementPart<N extends Node>
		extends AbstractFXContentPart<N> {

	private final ChangeListener<Object> contentObserver = new ChangeListener<Object>() {

		@Override
		public void changed(ObservableValue<? extends Object> observable,
				Object oldValue, Object newValue) {
			refreshVisual();
		}
	};

	@Override
	protected void doActivate() {
		super.doActivate();
		contentProperty().addListener(contentObserver);
	}

	@Override
	protected void doDeactivate() {
		contentProperty().removeListener(contentObserver);
		super.doDeactivate();
	}

	@Override
	protected void doRefreshVisual(N visual) {
		AbstractFXGeometricElement<?> content = getContent();
		if (visual.getEffect() != content.getEffect()) {
			visual.setEffect(content.getEffect());
		}
	}

	@Override
	public AbstractFXGeometricElement<?> getContent() {
		return (AbstractFXGeometricElement<?>) super.getContent();
	}

}
