package uk.ac.ebi.sampletab;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class DerivedFromRelationExtractor implements ValueExtractor
{
 private int order;
 private Set<String> blacklist;
 private Sample obj;
 private boolean delivered;
 private String header;

 public DerivedFromRelationExtractor( String hdr, Collection<? extends Sample> bl, int ord )
 {
  order = ord;
  
  if( bl != null )
  {
   blacklist = new HashSet<String>();
   
   for( Sample bs : bl)
    blacklist.add(bs.getID());
  }
  
  header = hdr;
 }
 
 @Override
 public void setSample(Sample sample)
 {
  obj = sample;
  delivered=false;
 }

 @Override
 public String extract()
 {
  if( delivered )
   return "";
  
  delivered = true;

  int i=0;
  
  for( Sample dfs : obj.getDeriverFromSamples() )
  {
   if( blacklist != null && blacklist.contains(dfs.getID()) )
    continue;
   
   if( i == order )
    return dfs.getID();
   
   i++;
  }
  
  return "";

 }

 @Override
 public boolean hasValue()
 {
  return ! delivered;
 }

 public String getHeader()
 {
  return header;
 }
}
