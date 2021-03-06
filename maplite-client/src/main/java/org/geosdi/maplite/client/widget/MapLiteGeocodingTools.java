/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geosdi.maplite.client.widget;

import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import java.util.List;
import java.util.logging.Logger;
import org.geosdi.geoplatform.gui.shared.util.GPSharedUtils;
import org.geosdi.maplite.client.map.VectorFeatureStyle;
import org.geosdi.maplite.client.model.CoordinateReferenceSystem;
import org.geosdi.maplite.client.service.MapLiteServiceRemote;
import org.geosdi.maplite.shared.geocoding.MapLiteGeocodingResult;
import org.gwtopenmaps.openlayers.client.Bounds;
import org.gwtopenmaps.openlayers.client.LonLat;
import org.gwtopenmaps.openlayers.client.Map;
import org.gwtopenmaps.openlayers.client.feature.VectorFeature;
import org.gwtopenmaps.openlayers.client.geometry.Point;
import org.gwtopenmaps.openlayers.client.layer.RendererOptions;
import org.gwtopenmaps.openlayers.client.layer.Vector;
import org.gwtopenmaps.openlayers.client.layer.VectorOptions;

/**
 * @author Nazzareno Sileno - CNR IMAA geoSDI Group
 * @email nazzareno.sileno@geosdi.org
 */
public class MapLiteGeocodingTools {

    private final static Logger logger = Logger.getLogger("");

    private final static String GEOCODING_VECTOR_FEATURE = "geocodingVectorFeature";
    private Vector geocodingVectorLayer;
    //Coming parameters
    private final Map map;
    private final ListBox geocodingListBox;
    private final TextBox geocodingTextBox;

    public MapLiteGeocodingTools(Map map, ListBox geocodingListBox, TextBox geocodingTextBox) {
        this.map = map;
        this.geocodingListBox = geocodingListBox;
        this.geocodingTextBox = geocodingTextBox;
        this.initGeocodingTools();
    }

    private void initGeocodingTools() {
        final List<MapLiteGeocodingResult> geocodingResults = Lists.<MapLiteGeocodingResult>newArrayList();

        this.geocodingListBox.setVisible(false);
        this.geocodingListBox.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                MapLiteGeocodingResult selectedItem = geocodingResults.get(geocodingListBox.getSelectedIndex());

                LonLat lower = new LonLat(selectedItem.geometry.bounds.southwest.lng,
                        selectedItem.geometry.bounds.southwest.lat);
                lower.transform(CoordinateReferenceSystem.WGS_84.getCode(), map.getProjection());
                LonLat upper = new LonLat(selectedItem.geometry.bounds.northeast.lng,
                        selectedItem.geometry.bounds.northeast.lat);
                upper.transform(CoordinateReferenceSystem.WGS_84.getCode(), map.getProjection());

                map.zoomToExtent(new Bounds(lower.lon(), lower.lat(), upper.lon(), upper.lat()));
                geocodingListBox.setVisible(false);
                geocodingTextBox.setText(selectedItem.formattedAddress);

                LonLat geometryLocation = new LonLat(selectedItem.geometry.location.lng,
                        selectedItem.geometry.location.lat);
                geometryLocation.transform(CoordinateReferenceSystem.WGS_84.getCode(), map.getProjection());

                Point geom = new Point(geometryLocation.lon(), geometryLocation.lat());

                VectorFeature vectorFeature = new VectorFeature(geom);
                vectorFeature.setFeatureId(GEOCODING_VECTOR_FEATURE);
                vectorFeature.setStyle(VectorFeatureStyle.generateGeocodingStyle());

                VectorFeature vectorFeatureInMap = geocodingVectorLayer.getFeatureById(
                        GEOCODING_VECTOR_FEATURE);
                if (vectorFeatureInMap == null) {
                    geocodingVectorLayer.addFeature(vectorFeature);
                } else {
                    vectorFeatureInMap.move(geometryLocation);
                }
            }

        });

        this.geocodingTextBox.setTitle("Search address");
        this.geocodingTextBox.getElement().setPropertyString("placeholder", "Insert the address to search...");
        this.geocodingTextBox.addKeyDownHandler(new KeyDownHandler() {

            private Timer suggestTimer;

            @Override
            public void onKeyDown(KeyDownEvent event) {
                final String addressToSearch = geocodingTextBox.getValue();
                if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER
                        && GPSharedUtils.isNotEmpty(addressToSearch)) {
                    if (suggestTimer != null) {
                        suggestTimer.cancel();
                    }
                    MapLiteServiceRemote.Util.getInstance().executeGeocoding(
                            addressToSearch, new AsyncCallback<List<MapLiteGeocodingResult>>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    logger.warning("Error on retrieving geocode address: " + caught);
                                }

                                @Override
                                public void onSuccess(List<MapLiteGeocodingResult> result) {
                                    showAddresses(result);
                                }
                            });
                } else if ((event.getNativeKeyCode() == KeyCodes.KEY_DELETE
                        || event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE)
                        && GPSharedUtils.isEmpty(addressToSearch)) {
                    clearGeocodingListBox();
                    geocodingListBox.setVisible(false);
                } else if (GPSharedUtils.isNotEmpty(addressToSearch)
                        && addressToSearch.length() > 2) {
                    suggestTimer = new Timer() {
                        @Override
                        public void run() {
                            MapLiteServiceRemote.Util.getInstance().suggestGeocoding(addressToSearch, new AsyncCallback<List<MapLiteGeocodingResult>>() {

                                @Override
                                public void onFailure(Throwable caught) {
                                    logger.warning("Error on retrieving geocode address: " + caught);
                                }

                                @Override
                                public void onSuccess(List<MapLiteGeocodingResult> result) {
                                    showAddresses(result);
                                }
                            });
                        }
                    };
                    // Schedule the timer to run once in 1,5 seconds.
                    suggestTimer.schedule(1500);
                } else {
                    if (suggestTimer != null) {
                        suggestTimer.cancel();
                    }
                }
            }

            private void showAddresses(List<MapLiteGeocodingResult> addresses) {
                if (GPSharedUtils.isNotEmpty(addresses)) {
                    logger.finest("Addresses received: " + addresses.toString());
                    this.clearGeocodingListBox();
                    geocodingResults.addAll(addresses);
                    for (MapLiteGeocodingResult singleresult : GPSharedUtils.safeList(addresses)) {
                        geocodingListBox.addItem(singleresult.formattedAddress);
                    }
                    geocodingListBox.setVisibleItemCount(5);
                    geocodingListBox.setVisible(true);
                }
            }

            private void clearGeocodingListBox() {
                geocodingResults.clear();
                geocodingListBox.clear();
            }
        }
        );

        VectorOptions vectorOption = new VectorOptions();
        VectorFeatureStyle style = new VectorFeatureStyle();
        vectorOption.setStyle(style.getVectorStyle());
        vectorOption.setDisplayInLayerSwitcher(false);
        RendererOptions rendererOptions = new RendererOptions();
        rendererOptions.setZIndexing(Boolean.TRUE);
        vectorOption.setRendererOptions(rendererOptions);

        this.geocodingVectorLayer = new Vector("geocoding-vector", vectorOption);
        map.addLayer(geocodingVectorLayer);
        map.setLayerZIndex(geocodingVectorLayer, 99999);
    }

}
