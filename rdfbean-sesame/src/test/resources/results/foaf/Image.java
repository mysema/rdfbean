package foaf;

import com.mysema.rdfbean.annotations.*;

@ClassMapping(ns="http://xmlns.com/foaf/0.1/")
public class Image extends wordnet.Document {

    @Predicate(ln="depicts")
    private owl.Thing foafDepicts;

    @Predicate(ln="thumbnail")
    private Image foafThumbnail;

    public owl.Thing getFoafDepicts(){
        return foafDepicts;
    }

    public void setFoafDepicts(owl.Thing foafDepicts){
        this.foafDepicts = foafDepicts;
    }

    public Image getFoafThumbnail(){
        return foafThumbnail;
    }

    public void setFoafThumbnail(Image foafThumbnail){
        this.foafThumbnail = foafThumbnail;
    }

}
