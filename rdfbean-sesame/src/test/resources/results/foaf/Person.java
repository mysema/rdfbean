package foaf;

import com.mysema.rdfbean.annotations.*;

@ClassMapping(ns="http://xmlns.com/foaf/0.1/")
public class Person extends wordnet.Person {

    @Predicate(ln="currentProject")
    private owl.Thing foafCurrentProject;

    @Predicate(ln="family_name")
    private String foafFamily_name;

    @Predicate(ln="firstName")
    private String foafFirstName;

    @Predicate(ln="geekcode")
    private String foafGeekcode;

    @Predicate(ln="img")
    private Image foafImg;

    @Predicate(ln="interest")
    private Document foafInterest;

    @Predicate(ln="knows")
    private Person foafKnows;

    @Predicate(ln="myersBriggs")
    private String foafMyersBriggs;

    @Predicate(ln="pastProject")
    private owl.Thing foafPastProject;

    @Predicate(ln="plan")
    private String foafPlan;

    @Predicate(ln="publications")
    private Document foafPublications;

    @Predicate(ln="schoolHomepage")
    private Document foafSchoolHomepage;

    @Predicate(ln="surname")
    private String foafSurname;

    @Predicate(ln="topic_interest")
    private owl.Thing foafTopic_interest;

    @Predicate(ln="workInfoHomepage")
    private Document foafWorkInfoHomepage;

    @Predicate(ln="workplaceHomepage")
    private Document foafWorkplaceHomepage;

    public owl.Thing getFoafCurrentProject(){
        return foafCurrentProject;
    }

    public void setFoafCurrentProject(owl.Thing foafCurrentProject){
        this.foafCurrentProject = foafCurrentProject;
    }

    public String getFoafFamily_name(){
        return foafFamily_name;
    }

    public void setFoafFamily_name(String foafFamily_name){
        this.foafFamily_name = foafFamily_name;
    }

    public String getFoafFirstName(){
        return foafFirstName;
    }

    public void setFoafFirstName(String foafFirstName){
        this.foafFirstName = foafFirstName;
    }

    public String getFoafGeekcode(){
        return foafGeekcode;
    }

    public void setFoafGeekcode(String foafGeekcode){
        this.foafGeekcode = foafGeekcode;
    }

    public Image getFoafImg(){
        return foafImg;
    }

    public void setFoafImg(Image foafImg){
        this.foafImg = foafImg;
    }

    public Document getFoafInterest(){
        return foafInterest;
    }

    public void setFoafInterest(Document foafInterest){
        this.foafInterest = foafInterest;
    }

    public Person getFoafKnows(){
        return foafKnows;
    }

    public void setFoafKnows(Person foafKnows){
        this.foafKnows = foafKnows;
    }

    public String getFoafMyersBriggs(){
        return foafMyersBriggs;
    }

    public void setFoafMyersBriggs(String foafMyersBriggs){
        this.foafMyersBriggs = foafMyersBriggs;
    }

    public owl.Thing getFoafPastProject(){
        return foafPastProject;
    }

    public void setFoafPastProject(owl.Thing foafPastProject){
        this.foafPastProject = foafPastProject;
    }

    public String getFoafPlan(){
        return foafPlan;
    }

    public void setFoafPlan(String foafPlan){
        this.foafPlan = foafPlan;
    }

    public Document getFoafPublications(){
        return foafPublications;
    }

    public void setFoafPublications(Document foafPublications){
        this.foafPublications = foafPublications;
    }

    public Document getFoafSchoolHomepage(){
        return foafSchoolHomepage;
    }

    public void setFoafSchoolHomepage(Document foafSchoolHomepage){
        this.foafSchoolHomepage = foafSchoolHomepage;
    }

    public String getFoafSurname(){
        return foafSurname;
    }

    public void setFoafSurname(String foafSurname){
        this.foafSurname = foafSurname;
    }

    public owl.Thing getFoafTopic_interest(){
        return foafTopic_interest;
    }

    public void setFoafTopic_interest(owl.Thing foafTopic_interest){
        this.foafTopic_interest = foafTopic_interest;
    }

    public Document getFoafWorkInfoHomepage(){
        return foafWorkInfoHomepage;
    }

    public void setFoafWorkInfoHomepage(Document foafWorkInfoHomepage){
        this.foafWorkInfoHomepage = foafWorkInfoHomepage;
    }

    public Document getFoafWorkplaceHomepage(){
        return foafWorkplaceHomepage;
    }

    public void setFoafWorkplaceHomepage(Document foafWorkplaceHomepage){
        this.foafWorkplaceHomepage = foafWorkplaceHomepage;
    }

}
