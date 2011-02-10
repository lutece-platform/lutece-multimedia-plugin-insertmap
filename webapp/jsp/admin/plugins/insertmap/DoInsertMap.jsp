<%@ page errorPage="ErrorPage.jsp" %>

<jsp:useBean id="mapajax" scope="session" class="fr.paris.lutece.plugins.insertmap.web.InsertMapJspBean" />

<% 
     response.sendRedirect( mapajax.doInsertLink( request ) );
%>
