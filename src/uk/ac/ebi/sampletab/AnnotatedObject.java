package uk.ac.ebi.sampletab;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class AnnotatedObject
{
 private String value;

 Map<String,Attribute> annotations = null;

 public String getID()
 {
  return value;
 }

 public String getValue()
 {
  return value;
 }

 public void setValue(String accession)
 {
  this.value = accession;
 }

 public void setID(String accession)
 {
  this.value = accession;
 }
 
 void addAnnotation( Attribute value )
 {
  if( annotations == null )
   annotations = new LinkedHashMap<String, Attribute>();
  
  annotations.put(value.getName(), value);
 }
 
 public Attribute getAnnotation( String key )
 {
  if( annotations == null )
   return null;
  
  return annotations.get(key);
 }
 
 public Collection<Attribute> getAnnotations()
 {
  if( annotations != null )
   return annotations.values();
  
  return null;
 }
 
 public boolean equals( Object o )
 {
  AnnotatedObject othObj = (AnnotatedObject)o;
  
  if( othObj.getAnnotations() == null )
   return annotations == null;

  if( annotations == null )
   return false;

//  if( annotations.size() != othObj.getAnnotations().size() )
//   return false;
  
  for( Attribute myat : annotations.values() )
  {
   Attribute othAttr = othObj.getAnnotation( myat.getName() );
   
   if( othAttr == null )
   {
    if( myat != null && ! myat.isEmpty() )
     return false;
   }
   else if( ! othAttr.equals(myat) )
    return false;
  }
  
  for( Attribute othAttr : othObj.getAnnotations() )
  {
   Attribute myat = getAnnotation( othAttr.getName() );
   
   if( myat == null )
   {
    if( othAttr != null && ! othAttr.isEmpty() )
     return false;
   }
   else if( ! myat.equals(othAttr) )
    return false;
  }
  
  return true;
 }
}
