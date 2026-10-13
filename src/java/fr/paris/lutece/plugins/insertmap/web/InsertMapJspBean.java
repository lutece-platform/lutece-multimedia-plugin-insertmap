/*
 * Copyright (c) 2002-2014, Mairie de Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.insertmap.web;

import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.security.ISecurityTokenService;
import fr.paris.lutece.portal.service.security.SecurityTokenHandler;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.web.insert.InsertServiceJspBean;
import fr.paris.lutece.portal.web.insert.InsertServiceSelectionBean;

import org.apache.commons.text.StringEscapeUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Insert service that inserts into a rich text editor a static map image (Plan IGN of the Geoplateforme) centered on a
 * point, linked to the OpenStreetMap page of that point.
 */
@RequestScoped
public class InsertMapJspBean extends InsertServiceJspBean implements InsertServiceSelectionBean
{
    private static final long serialVersionUID = 8529163405839151344L;

    private static final String TEMPLATE_SELECTOR = "admin/plugins/insertmap/insertmap_selector.html";
    private static final String TEMPLATE_MAP = "admin/plugins/insertmap/insertmap_map.html";
    private static final String TEMPLATE_PREVIEW = "admin/plugins/insertmap/insertmap_preview.html";

    private static final String PARAMETER_INPUT = "input";
    private static final String PARAMETER_LAT = "lat";
    private static final String PARAMETER_LNG = "lng";
    private static final String PARAMETER_ZOOM = "zoom";
    private static final String PARAMETER_WIDTH = "width";
    private static final String PARAMETER_HEIGHT = "height";

    private static final String MARK_INPUT = "input";
    private static final String MARK_LAT = "lat";
    private static final String MARK_LNG = "lng";
    private static final String MARK_ZOOM = "zoom";
    private static final String MARK_WIDTH = "width";
    private static final String MARK_HEIGHT = "height";
    private static final String MARK_BBOX = "bbox";

    private static final String FORMAT_COORDINATE = "%.5f";
    private static final String FORMAT_BBOX = "%.1f,%.1f,%.1f,%.1f";

    private static final double MAX_LAT = 85;
    private static final double MAX_LNG = 180;
    private static final int MIN_ZOOM = 1;
    private static final int MAX_ZOOM = 18;
    private static final int MIN_SIZE = 100;
    private static final int MAX_SIZE = 2000;
    private static final double TILE_SIZE = 256;
    private static final double EARTH_RADIUS = 6378137;

    /**
     * Returns the map selector: address search, size and zoom, preview and insertion. The core instantiates this class by
     * reflection, so the token service is looked up rather than injected.
     *
     * @param request
     *            the HTTP request
     * @return the HTML of the selector
     */
    @Override
    public String getInsertServiceSelectorUI( HttpServletRequest request )
    {
        Map<String, Object> model = new HashMap<>( );
        model.put( MARK_INPUT, request.getParameter( PARAMETER_INPUT ) );
        model.put( SecurityTokenHandler.MARK_CSRF_TOKEN, CDI.current( ).select( ISecurityTokenService.class ).get( ).getToken( request, InsertMapSelectorJspBean.ACTION_INSERT_MAP ) );

        return AppTemplateService.getTemplate( TEMPLATE_SELECTOR, AdminUserService.getLocale( request ), model ).getHtml( );
    }

    /**
     * Renders the map described by the request parameters.
     *
     * @param request
     *            the HTTP request
     * @return the HTML of the map, or an empty string when a parameter is missing or out of range
     */
    public String getMapHtml( HttpServletRequest request )
    {
        Map<String, Object> model = getMapModel( request );

        if ( model == null )
        {
            return "";
        }

        return AppTemplateService.getTemplate( TEMPLATE_MAP, AdminUserService.getLocale( request ), model ).getHtml( );
    }

    /**
     * Renders the preview of the map described by the request parameters.
     *
     * @param request
     *            the HTTP request
     * @return the HTML of the map, or a warning when a parameter is missing or out of range
     */
    public String getPreviewHtml( HttpServletRequest request )
    {
        Map<String, Object> model = getMapModel( request );

        return AppTemplateService.getTemplate( TEMPLATE_PREVIEW, AdminUserService.getLocale( request ), model == null ? new HashMap<>( ) : model )
                .getHtml( );
    }

    /**
     * Builds the url that inserts the map into the calling editor field.
     *
     * @param request
     *            the HTTP request
     * @return the insertion url
     */
    public String getInsertMapUrl( HttpServletRequest request )
    {
        return insertUrl( request, request.getParameter( PARAMETER_INPUT ), StringEscapeUtils.escapeEcmaScript( getMapHtml( request ) ) );
    }

    /**
     * Reads and checks the map parameters and computes, in Web Mercator (EPSG:3857), the bounding box shown at the
     * requested zoom and size.
     *
     * @param request
     *            the HTTP request
     * @return the model of the map template, or null when a parameter is missing or out of range
     */
    private static Map<String, Object> getMapModel( HttpServletRequest request )
    {
        Double lat = parseDouble( request.getParameter( PARAMETER_LAT ), MAX_LAT );
        Double lng = parseDouble( request.getParameter( PARAMETER_LNG ), MAX_LNG );
        Integer zoom = parseInt( request.getParameter( PARAMETER_ZOOM ), MIN_ZOOM, MAX_ZOOM );
        Integer width = parseInt( request.getParameter( PARAMETER_WIDTH ), MIN_SIZE, MAX_SIZE );
        Integer height = parseInt( request.getParameter( PARAMETER_HEIGHT ), MIN_SIZE, MAX_SIZE );

        if ( lat == null || lng == null || zoom == null || width == null || height == null )
        {
            return null;
        }

        double metersPerPixel = 2 * Math.PI * EARTH_RADIUS / ( TILE_SIZE * Math.pow( 2, zoom ) );
        double x = Math.toRadians( lng ) * EARTH_RADIUS;
        double y = Math.log( Math.tan( Math.PI / 4 + Math.toRadians( lat ) / 2 ) ) * EARTH_RADIUS;
        double halfWidth = metersPerPixel * width / 2;
        double halfHeight = metersPerPixel * height / 2;

        Map<String, Object> model = new HashMap<>( );
        model.put( MARK_LAT, String.format( Locale.ROOT, FORMAT_COORDINATE, lat ) );
        model.put( MARK_LNG, String.format( Locale.ROOT, FORMAT_COORDINATE, lng ) );
        model.put( MARK_ZOOM, zoom );
        model.put( MARK_WIDTH, width );
        model.put( MARK_HEIGHT, height );
        model.put( MARK_BBOX, String.format( Locale.ROOT, FORMAT_BBOX, x - halfWidth, y - halfHeight, x + halfWidth, y + halfHeight ) );

        return model;
    }

    /**
     * Parses a coordinate.
     *
     * @param strValue
     *            the value
     * @param dMax
     *            the largest absolute value accepted
     * @return the coordinate, or null when it is not a number or out of range
     */
    private static Double parseDouble( String strValue, double dMax )
    {
        if ( strValue == null )
        {
            return null;
        }

        try
        {
            double dValue = Double.parseDouble( strValue );

            return Math.abs( dValue ) <= dMax ? dValue : null;
        }
        catch( NumberFormatException e )
        {
            return null;
        }
    }

    /**
     * Parses a bounded integer.
     *
     * @param strValue
     *            the value
     * @param nMin
     *            the smallest value accepted
     * @param nMax
     *            the largest value accepted
     * @return the integer, or null when it is not an integer or out of range
     */
    private static Integer parseInt( String strValue, int nMin, int nMax )
    {
        try
        {
            int nValue = Integer.parseInt( strValue );

            return nValue >= nMin && nValue <= nMax ? nValue : null;
        }
        catch( NumberFormatException e )
        {
            return null;
        }
    }
}
