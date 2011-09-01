package uk.ac.ebi.sampletab;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class GroupRelationExtractor implements ValueExtractor
{
 private int order;
 private Set<String> blacklist;
 private Sample obj;
 private boolean delivered;
 private String header;

 public GroupRelationExtractor( String hdr, Collection<? extends Group> bl, int ord )
 {
  order = ord;

  if( bl != null )
  {
   blacklist = new HashSet<String>();
   
   for( Group bs : bl)
    blacklist.add(bs.getID());
  }
  
  header=hdr;
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
  
  for( Group g : obj.getGroups() )
  {
   if( blacklist != null && blacklist.contains(g.getID()) )
    continue;
   
   if( i == order )
    return g.getID();
   
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
