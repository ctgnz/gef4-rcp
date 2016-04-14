
package org.eclipse.gef4.mvc.examples.logo.rcp;

import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.gef4.fx.nodes.InfiniteCanvas;
import org.eclipse.gef4.mvc.examples.logo.rcp.model.FXGeometricModel;
import org.eclipse.gef4.mvc.fx.domain.FXDomain;
import org.eclipse.gef4.mvc.fx.viewer.FXViewer;
import org.eclipse.gef4.mvc.models.ContentModel;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javafx.scene.layout.BorderPane;

public class MvcLogoExamplePart {
  @Inject
  public MvcLogoExamplePart() {

  }

  @PostConstruct
  public void postConstruct(BorderPane container) {
    Injector injector = Guice.createInjector(new MvcLogoExampleModule());
    FXDomain domain = injector.getInstance(FXDomain.class);
    FXViewer viewer = domain.getAdapter(FXViewer.class);
    InfiniteCanvas canvas = viewer.getCanvas();
    container.setCenter(canvas);
    canvas.sceneProperty().addListener((observable, oldValue, newValue) -> {
      if (canvas.getScene() != null) {
        domain.activate();
        try {
          viewer.getAdapter(ContentModel.class).getContents().setAll(createContents());
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    });
  }

  public static List<FXGeometricModel> createContents() {
    return Collections.singletonList(new FXGeometricModel());
  }

}