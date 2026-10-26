/**
* @(#)LoadLog4jConfiguration.java
* @copyright@
*/

package com.svb.common;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.xml.DOMConfigurator;

/**
* This servlet's purpose is to initialize the logging schema for log4j. It expects that the
* log4j.xml file will be in the WEB-INF folder.<p>
*/
public class LoadLog4jConfiguration extends HttpServlet
{
    /** key to the init param of the log4j file */
    public static final String LOG4J_FILE = "/WEB-INF/log4j.xml";

    /**
    * load the log4j.xml file
    */
    public void init()
    {
        try
        {
            DOMConfigurator.configure(getServletContext().getResource(LOG4J_FILE));
            log("logging configured from dom file: " + LOG4J_FILE + ", successfully.");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            //try to just run the basic configurator instead
            BasicConfigurator.configure();
            log("can't seem to run the DOMConfigurator. starting with BasicConfigurator instead");
        }
    }

    /**
        * Implemented do get that informs user that this servlet is not meant for
        * prime time interactions, it is to be preloaded and that's it, except
        * for when it gets destroyed.
        * @param request
        * @param response
        * @exception <{ServletException}>
        * @exception <{IOException}>
        */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
            //set the content type
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        out.println("This servlet does not service requests. It only loads the log4j system");
        out.close();
    }

    /**
        * Do the post
        * @param request
        * @param response
        * @exception <{ServletException}>
        * @exception <{IOException}>
        */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        doGet(request, response);
    }

    /**
        * Information about this servlet
        * @return info
        */
    public String getServletInfo()
    {
        return LoadLog4jConfiguration.class.getName();
    }
}


