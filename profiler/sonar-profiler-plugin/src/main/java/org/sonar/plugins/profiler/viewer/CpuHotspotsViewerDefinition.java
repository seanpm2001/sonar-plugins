package org.sonar.plugins.profiler.viewer;

import org.sonar.api.resources.Resource;
import org.sonar.api.web.*;
import org.sonar.plugins.profiler.viewer.client.CpuHotspotsViewer;

/**
 * @author Evgeny Mandrikov
 */
@ResourceQualifier(Resource.QUALIFIER_UNIT_TEST_CLASS)
@NavigationSection(NavigationSection.RESOURCE_TAB)
@DefaultTab(metrics={
    // TODO
})
@UserRole(UserRole.CODEVIEWER)
public class CpuHotspotsViewerDefinition extends GwtPage {
  public String getGwtId() {
    return CpuHotspotsViewer.GWT_ID;
  }

  public String getTitle() {
    return "CPU Hotspots";
  }
}
