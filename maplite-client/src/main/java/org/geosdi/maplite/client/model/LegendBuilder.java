/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geosdi.maplite.client.model;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.VerticalPanel;
import org.geosdi.maplite.shared.ClientRasterInfo;
import org.geosdi.maplite.shared.GPFolderClientInfo;
import org.geosdi.maplite.shared.IGPFolderElements;
import org.gwtopenmaps.openlayers.client.Map;

/**
 * @author Nazzareno Sileno - CNR IMAA geoSDI Group
 * @email nazzareno.sileno@geosdi.org
 */
public class LegendBuilder {

    private static final String GET_LEGEND_REQUEST = "?REQUEST=GetLegendGraphic"
            + "&VERSION=1.0.0&FORMAT=image/png&LAYER=";

    private LegendBuilder() {
    }

    public static Image generateLegendImage(ClientRasterInfo raster, Map map, boolean visible) {
        StringBuilder imageURL = new StringBuilder();
        imageURL.append(raster.getDataSource()).append(GET_LEGEND_REQUEST)
                .append(raster.getLayerName()).append("&scale=").append(map.getScale())
                .append("&service=WMS&style=")
                .append(raster.getStyles().size() > 0 ? raster.getStyles().get(0).getStyleString() : "")
                .append("&LEGEND_OPTIONS=forceRule:True;forceLabels=on");

        final Image legendImage = new Image(imageURL.toString());
        legendImage.setVisible(visible);
        return legendImage;
    }

    public static void rebuildLegend(java.util.Map<GPFolderClientInfo, VerticalPanel> legendPanels,
            Map map) {
        for (GPFolderClientInfo folder : legendPanels.keySet()) {
            VerticalPanel legendPanel = legendPanels.get(folder);
            rebuildLegend(folder, legendPanel, map);
        }
    }

    public static void rebuildLegend(GPFolderClientInfo folder, VerticalPanel legendPanel, Map map) {
        legendPanel.clear();
        for (IGPFolderElements folderElement : folder.getFolderElements()) {
            if (folderElement instanceof ClientRasterInfo) {
                ClientRasterInfo raster = (ClientRasterInfo) folderElement;
                legendPanel.add(generateLegendImage(raster, map,
                        folder.isChecked() ? raster.isChecked() : false));
            }
        }
    }

}
