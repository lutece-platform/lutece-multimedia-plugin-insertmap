/*
 * Copyright (c) 2002-2009, Mairie de Paris
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
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.web.insert.InsertServiceJspBean;
import fr.paris.lutece.portal.web.insert.InsertServiceSelectionBean;
import fr.paris.lutece.util.html.HtmlTemplate;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


/**
 *
 */
public final class InsertMapJspBean extends InsertServiceJspBean implements InsertServiceSelectionBean
{
    private static final long serialVersionUID = 8529163405839151344L;
    private static final String TEMPLATE_SELECTOR_PAGE = "admin/plugins/insertmap/insertmap_selector.html";
    private static final String PARAMETER_INPUT = "input";
    private static final String BASE_URL = "baseUrl";
    private static final String WIDTH = "width";
    private static final String ZOOM = "zoom";
    private static final String HEIDHT = "height";
    private static final String ADDRESS = "address";
    private String input;

    private void init( HttpServletRequest request )
    {
        input = request.getParameter( PARAMETER_INPUT ); //for the html popup to go in the WYSYWYG editor
    }

    /**
     * Give the user the choose of the workspace
     *
     * @param request The Http Request
     * @return The html form.
     */
    public String getInsertServiceSelectorUI( HttpServletRequest request )
    {
        init( request );

        Map<String, Object> model = new HashMap<String, Object>(  );
        String strBaseUrl = AppPathService.getBaseUrl( request );
        model.put( BASE_URL, strBaseUrl );
        model.put( PARAMETER_INPUT, input );

        Locale locale = AdminUserService.getLocale( request );
        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_SELECTOR_PAGE, locale, model );

        return template.getHtml(  );
    }

    public String doInsertLink( HttpServletRequest request )
    {
        String strTemplateName = request.getParameter( "mapName" );

        //get the name of the html tag to return to the WYNGZIG editor
        String strInput = request.getParameter( PARAMETER_INPUT );

        Map<String, Object> model = new HashMap<String, Object>(  );
        String strBaseUrl = AppPathService.getBaseUrl( request );
        model.put( BASE_URL, strBaseUrl );
        model.put( ADDRESS, request.getParameter( ADDRESS ) );
        model.put( WIDTH, request.getParameter( WIDTH ) );
        model.put( ZOOM, request.getParameter( ZOOM ) );
        model.put( HEIDHT, request.getParameter( HEIDHT ) );
        model.put( ADDRESS, request.getParameter( ADDRESS ) );

        Locale locale = AdminUserService.getLocale( request );
        String strTemplateAddress = "admin/plugins/insertmap/insertmap_" + strTemplateName + "_default.html";
        HtmlTemplate template = AppTemplateService.getTemplate( strTemplateAddress, locale, model );

        return insertUrlWithoutEscape( request, strInput, template.getHtml(  ) );
    }
}
