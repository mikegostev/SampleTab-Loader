package uk.ac.ebi.sampletab;

public class TermSource
{
 private String name;
 private String uri;
 private String version;

 public String getName()
 {
  return name;
 }

 public void setName(String name)
 {
  this.name = name;
 }

 public String getUri()
 {
  return uri;
 }

 public void setUri(String uri)
 {
  this.uri = uri;
 }

 public String getVersion()
 {
  return version;
 }

 public void setVersion(String version)
 {
  this.version = version;
 }
 
 public boolean equals( Object cs )
 {
  return ((TermSource)cs).getName().equals(name);
 }
}
