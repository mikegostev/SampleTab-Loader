package uk.ac.ebi.sampletab;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.pri.util.collection.Collections;


public class Sample extends AnnotatedObject
{
 private int block;

 private Map<String,Sample> derivatives = Collections.emptyMap();
 private Map<String,Sample> derivedFrom = Collections.emptyMap();
 private Map<String,Group> groups = Collections.emptyMap();
 
 
 public Sample addDerivative(Sample sample)
 {
  if( derivatives == Collections.<String,Sample>emptyMap() )
  {
   derivatives = new LinkedHashMap<String,Sample>();
   
   derivatives.put(sample.getID(), sample);
   
   return sample;
  }
  
  Sample ds = derivatives.get(sample.getID());
  
  if( ds != null )
   return ds;
  
  
  derivatives.put(sample.getID(), sample);
  
  return sample;
 }

 public Sample addDerivedFrom(Sample sample)
 {
  if( derivedFrom == Collections.<String,Sample>emptyMap() )
  {
   derivedFrom =  new LinkedHashMap<String,Sample>();
   
   derivedFrom.put(sample.getID(), sample);
   
   return sample;
  }
  
  Sample ds = derivedFrom.get(sample.getID());

  if( ds != null )
   return ds;
  
  derivedFrom.put(sample.getID(), sample);
  
  return sample;
 }

 public Group addGroup(Group group)
 {
  if( groups == Collections.<String,Group>emptyMap() )
  {
   groups = new LinkedHashMap<String,Group>();
   
   groups.put(group.getID(), group);
   
   return group;
  }
  
  Group ds = groups.get(group.getID());

  if( ds != null )
   return ds;

  
  groups.put(group.getID(), group);
  
  return group;
  
 }
 
 public Group getGroup( String gId )
 {
  return groups.get(gId);
 }
 
 public Collection<? extends Group> getGroups()
 {
  return groups.values();
 }

 public Sample getDeriverFromSample( String sId )
 {
  return derivedFrom.get(sId);
 }
 
 public Collection<? extends Sample> getDeriverFromSamples()
 {
  return derivedFrom.values();
 }

 
 public int getBlock()
 {
  return block;
 }

 public void setBlock(int block)
 {
  this.block = block;
 }

}
