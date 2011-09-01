package uk.ac.ebi.sampletab;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Definitions
{
 public static final String        MODIDPREFIX                = "@M:";
 public static final char[]        CUSTIOMCLASSBRACKETS       = new char[]{'{','}'};
 public static final char[]        QUALIFIERBRACKETS          = new char[]{'[',']'};
 public static final String        PROTOTYPEID                = "<<ALL>>";

 public static final String        SAMPLE                     = "Sample";
 public static final String        SAMPLENAME                 = "Sample Name";
 public static final String        SAMPLENAME_AGE             = "Name";
 public static final String        BELONGSTO                  = "belongsTo";
 public static final String        DERIVEDFROM                = "derivedFrom";
 public static final String        SAMPLEACCESSION            = "Sample Accession";

 public static final String        GROUP                      = "Group";
 public static final String        GROUPNAME                  = "Group Name";
 public static final String        GROUPNAME_AGE              = "Name";
 public static final String        GROUPACCESSION             = "Group Accession";

 public static final String        TERMSOURCE                 = "Term Source";
 public static final String        TERMSOURCEREF              = "Term Source REF";
 public static final String        TERMSOURCEID               = "Term Source ID";
 public static final String        TERMSOURCENAME             = "Term Source Name";
 public static final String        TERMSOURCEURI              = "Term Source URI";
 public static final String        TERMSOURCEVER              = "Term Source Version";

 public static final String        ORGANIZATION               = "Organization";
 public static final String        ORGANIZATIONNAME           = "Organization Name";
 public static final String        ORGANIZATIONADDR           = "Organization Address";
 public static final String        ORGANIZATIONURI            = "Organization URI";
 public static final String        ORGANIZATIONEMAIL          = "Organization Email";
 public static final String        ORGANIZATIONROLE           = "Organization Role";

 public static final String        DATABASE                   = "Database";
 public static final String        DATABASEID                 = "Database ID";
 public static final String        DATABASEURI                = "Database URI";
 public static final String        DATABASENAME               = "Database Name";

 public static final String        PUBLICATION                = "Publication";
 public static final String        PUBLICATIONDOI             = "Publication DOI";
 public static final String        PUBLICATIONPUBMED          = "Publication PubMed ID";

 public static final String        PERSON                     = "Person";
 public static final String        PERSONLASTNAME             = "Person Last Name";
 public static final String        PERSONMIDINIT              = "Person Mid Initials";
 public static final String        PERSONFIRSTNAME            = "Person First Name";
 public static final String        PERSONEMAIL                = "Person Email";
 public static final String        PERSONROLE                 = "Person Role";

 public static final String        SUBMISSION                 = "Submission";
 public static final String        SUBMISSIONTITLE            = "Submission Title";
 public static final String        SUBMISSIONDESCRIPTION      = "Submission Description";
 public static final String        SUBMISSIONVERSION          = "Submission Version";
 public static final String        SUBMISSIONABSTRACT         = "Submission Abstract";
 public static final String        SUBMISSIONRELEASEDATE      = "Submission Release Date";
 public static final String        SUBMISSIONMODIFICATIONDATE = "Submission Modification Date";
 public static final String        SUBMISSIONIDENTIFIER       = "Submission Identifier";
 public static final String        SUBMISSIONREFERENCELAYER   = "Submission Reference Layer";

 public static final String        UNIT                       = "Unit";

 public static final String        COMMENTCHAR                = "#";
 
 public static final String        MSIBLOCK                   = "[MSI]";
 public static final String        SCDBLOCK                   = "[SCD]";
 
 public static Map<String, String> submissionProperties = new HashMap<String, String>()
 {{
  put(SUBMISSIONTITLE,SUBMISSION);
  put(SUBMISSIONDESCRIPTION,SUBMISSION);
  put(SUBMISSIONVERSION,SUBMISSION);
  put(SUBMISSIONABSTRACT,SUBMISSION);
  put(SUBMISSIONRELEASEDATE,SUBMISSION);
  put(SUBMISSIONMODIFICATIONDATE,SUBMISSION);
  put(SUBMISSIONIDENTIFIER,SUBMISSION);
  put(SUBMISSIONREFERENCELAYER,SUBMISSION);
 }};

 public static Map<String, String> propertyToObject = new HashMap<String, String>()
 {{
  put(PERSONLASTNAME,PERSON);
  put(PERSONMIDINIT,PERSON);
  put(PERSONFIRSTNAME,PERSON);
  put(PERSONEMAIL,PERSON);
  put(PERSONROLE,PERSON);

  put(PUBLICATIONDOI,PUBLICATION);
  put(PUBLICATIONPUBMED,PUBLICATION);
  
  put(DATABASEID,DATABASE);
  put(DATABASEURI,DATABASE);
  put(DATABASENAME,DATABASE);
  
  put(ORGANIZATIONNAME,ORGANIZATION);
  put(ORGANIZATIONADDR,ORGANIZATION);
  put(ORGANIZATIONURI,ORGANIZATION);
  put(ORGANIZATIONEMAIL,ORGANIZATION);
  put(ORGANIZATIONROLE,ORGANIZATION);
  
  put(TERMSOURCENAME,TERMSOURCE);
  put(TERMSOURCEURI,TERMSOURCE);
  put(TERMSOURCEVER,TERMSOURCE);
 }};
 
 public static Map<String, Set<String> > object2Properties = new HashMap<String, Set<String>>();
 
 static
 {
  for( Map.Entry<String, String> me : propertyToObject.entrySet() )
  {
   Set<String> props =  object2Properties.get(me.getValue());
   
   if( props == null )
    object2Properties.put(me.getValue(), props = new HashSet<String>() );
  
   props.add(me.getKey());
  }
 }
}
