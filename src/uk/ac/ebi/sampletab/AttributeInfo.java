package uk.ac.ebi.sampletab;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class AttributeInfo
{
 private String name;
 private int valuesNumber;
 
 private Map<String, AttributeInfo> qualifiers;

 public AttributeInfo(String name2)
 {
  name=name2;
 }

 public AttributeInfo getInfo(String name)
 {
  if( qualifiers == null )
   return null;
  
  return qualifiers.get(name);
 }

 public void addAttributeInfo(AttributeInfo atInf)
 {
  if( qualifiers == null )
   qualifiers = new LinkedHashMap<String, AttributeInfo>();
  
  qualifiers.put( atInf.getName(), atInf );
 }

 public int getValuesNumber()
 {
  return valuesNumber;
 }

 public int getQualifiersNumber()
 {
  return qualifiers==null ? 0 : qualifiers.size();
 }

 public Collection<AttributeInfo> getQualifiers()
 {
  if( qualifiers == null )
   return null;
  
  return qualifiers.values();
 }
 
 public void setValuesNumber(int valuesNumber)
 {
  this.valuesNumber = valuesNumber;
 }

 public String getName()
 {
  return name;
 }

 public void setName(String name)
 {
  this.name = name;
 }
}
