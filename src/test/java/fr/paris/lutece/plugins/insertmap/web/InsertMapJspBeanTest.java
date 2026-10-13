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

import org.junit.jupiter.api.Test;

import fr.paris.lutece.portal.web.insert.InsertServiceSelectorJspBean;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.mocks.MockHttpServletRequest;

/**
 * Tests the parameter checks and the rendering of the map insert service.
 */
public class InsertMapJspBeanTest extends LuteceTestCase
{
    /**
     * Builds a request carrying the map parameters.
     *
     * @param strLat
     *            the latitude
     * @param strZoom
     *            the zoom
     * @return the request
     */
    private static MockHttpServletRequest newRequest( String strLat, String strZoom )
    {
        MockHttpServletRequest request = new MockHttpServletRequest( );
        request.addParameter( "lat", strLat );
        request.addParameter( "lng", "2.36973" );
        request.addParameter( "zoom", strZoom );
        request.addParameter( "width", "400" );
        request.addParameter( "height", "300" );
        request.addParameter( "input", "tinymce" );

        return request;
    }

    /**
     * A valid point renders the map image centered on it, in Web Mercator, linked to the OpenStreetMap page of the point, with
     * the marker drawn at the center of the image.
     */
    @Test
    public void testMapHtml( )
    {
        String strHtml = new InsertMapJspBean( ).getMapHtml( newRequest( "48.84652", "16" ) );

        assertTrue( strHtml.contains( "BBOX=263319.4,6248500.8,264274.9,6249217.4" ) );
        assertTrue( strHtml.contains( "WIDTH=400&amp;HEIGHT=300" ) );
        assertTrue( strHtml.contains( "<div style=\"padding-top:130px\"><img src=\"images/local/skin/plugins/insertmap/marker.png\"" ) );
        assertTrue( strHtml.contains( "mlat=48.84652&amp;mlon=2.36973" ) );
        assertTrue( strHtml.contains( "#map=16/48.84652/2.36973" ) );
    }

    /**
     * A coordinate or a zoom that is not a number or out of range renders nothing.
     */
    @Test
    public void testInvalidParametersRenderNothing( )
    {
        assertEquals( "", new InsertMapJspBean( ).getMapHtml( newRequest( "", "16" ) ) );
        assertEquals( "", new InsertMapJspBean( ).getMapHtml( newRequest( "48.8\" onload=\"x", "16" ) ) );
        assertEquals( "", new InsertMapJspBean( ).getMapHtml( newRequest( "91", "16" ) ) );
        assertEquals( "", new InsertMapJspBean( ).getMapHtml( newRequest( "48.84652", "25" ) ) );
    }

    /**
     * The insertion url hands the map to the core insert page through the session.
     */
    @Test
    public void testInsertMapUrl( )
    {
        MockHttpServletRequest request = newRequest( "48.84652", "16" );

        String strUrl = new InsertMapJspBean( ).getInsertMapUrl( request );

        assertTrue( strUrl.contains( "jsp/admin/insert/DoInsertIntoElement.jsp" ) );
        String strInsert = (String) request.getSession( ).getAttribute( InsertServiceSelectorJspBean.SESSION_INSERT );
        assertTrue( strInsert.contains( "data.geopf.fr\\/wms-r" ) );
    }
}
