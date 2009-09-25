package foaf;

import com.mysema.rdfbean.annotations.*;

@ClassMapping(ns="http://xmlns.com/foaf/0.1/")
public class Document extends wordnet.Document {

    @Predicate(ln="primaryTopic")
    private owl.Thing foafPrimaryTopic;

    @Predicate(ln="sha1")
    private Object foafSha1;

    @Predicate(ln="topic")
    private owl.Thing foafTopic;

    public owl.Thing getFoafPrimaryTopic(){
        return foafPrimaryTopic;
    }

    public void setFoafPrimaryTopic(owl.Thing foafPrimaryTopic){
        this.foafPrimaryTopic = foafPrimaryTopic;
    }

    public Object getFoafSha1(){
        return foafSha1;
    }

    public void setFoafSha1(Object foafSha1){
        this.foafSha1 = foafSha1;
    }

    public owl.Thing getFoafTopic(){
        return foafTopic;
    }

    public void setFoafTopic(owl.Thing foafTopic){
        this.foafTopic = foafTopic;
    }

}
