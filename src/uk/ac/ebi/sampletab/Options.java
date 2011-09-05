package uk.ac.ebi.sampletab;

import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class Options
{
 @Option( name = "-l", usage = "Load files to database. (Require -d)")
 private boolean load;
 
 @Option( name = "-a", usage="Save converted AGE files")
 private boolean save;
 
 @Option( name = "-r", usage="Save server response. (Assumes -l)")
 private boolean saveResponse;

 @Option( name = "-s", usage="Store files in database (assumes -l). Otherwise they will be verified but not stored")
 private boolean store;
 
 @Option( name = "-h", usage="Database base URL", metaVar="URL")
 private String databaseURL;
 
 @Option( name = "-u", usage="User name", metaVar="USER")
 private String user;

 @Option( name = "-p", usage="User password", metaVar="PASS")
 private String password;
 
 @Option( name = "-update", usage="Update submissions")
 private boolean update;
 
 @Option( name = "-new", usage="Load new submissions")
 private boolean newSub;

 @Option( name = "-o", usage="Output directory")
 private String outDir;
 
 @Argument
 private List<String> dirs;

 public boolean isLoad()
 {
  return load;
 }

 public boolean isSave()
 {
  return save;
 }

 public boolean isStore()
 {
  return store;
 }

 public String getDatabaseURL()
 {
  return databaseURL;
 }

 public String getUser()
 {
  return user;
 }

 public String getPassword()
 {
  return password;
 }

 public List<String> getDirs()
 {
  return dirs;
 }

 public void setDatabaseURI(String databaseURI)
 {
  this.databaseURL = databaseURI;
 }

 public boolean isSaveResponse()
 {
  return saveResponse;
 }

 public boolean isUpdateSubmissions()
 {
  return update;
 }

 public boolean isNewSubmissions()
 {
  return newSub;
 }

 public String getOutDir()
 {
  return outDir;
 }

}
