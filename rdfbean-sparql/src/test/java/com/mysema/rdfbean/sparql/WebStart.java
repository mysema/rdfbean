package com.mysema.rdfbean.sparql;

import com.mysema.commons.jetty.JettyHelper;


public class WebStart {
    
    public static void main(String[] args) throws Exception{
        JettyHelper.startJetty("src/test/webapp", "/sparql", 8080, 8443);
    }

}
