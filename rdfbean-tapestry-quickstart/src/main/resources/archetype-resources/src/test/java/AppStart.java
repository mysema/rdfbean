#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )

package ${package};

import com.mysema.commons.jetty.JettyHelper;

public class AppStart {

    public static void main(String[] args) throws Exception{
        System.setProperty("org.mortbay.jetty.webapp.parentLoaderPriority", "true");
        System.setProperty("production.mode", "true");
        JettyHelper.startJetty("src/main/webapp", "/", 8080, 8443);
    }

}
