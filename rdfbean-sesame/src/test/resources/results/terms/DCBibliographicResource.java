package terms;

import com.mysema.rdfbean.annotations.*;

@ClassMapping(ns="http://purl.org/dc/terms/",ln="BibliographicResource")
public class DCBibliographicResource {

    @Predicate
    private String bibliographicCitation;

    public String getBibliographicCitation(){
        return bibliographicCitation;
    }

    public void setBibliographicCitation(String bibliographicCitation){
        this.bibliographicCitation = bibliographicCitation;
    }

}
