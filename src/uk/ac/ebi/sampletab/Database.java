package uk.ac.ebi.sampletab;

import com.pri.util.StringUtils;

public class Database implements Comparable<Database>
{
 private String id;
 private String name;
 private String uri;

 public String getId()
 {
  return id;
 }

 public void setId(String id)
 {
  this.id = id;
 }

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


 @Override
 public int compareTo(Database o)
 {
  int res = StringUtils.compareStrings(name, o.getName());
    
  if( res != 0 )
   return res;
 
  res = StringUtils.compareStrings(id, o.getId() );
  
  if( res != 0 )
   return res;

  res = StringUtils.compareStrings(uri, o.getUri());
  
  if( res != 0 )
   return res;

  return 0;
 }

 public boolean equals( Object o )
 {
  return compareTo( (Database)o ) == 0;
 }
}
