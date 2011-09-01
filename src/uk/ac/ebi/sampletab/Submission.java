package uk.ac.ebi.sampletab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pri.util.collection.IntHashMap;
import com.pri.util.collection.IntMap;
import com.pri.util.collection.IntMap.Entry;

public class Submission extends ContainerObject
{
 private IntMap<List<Sample>> sampleBlocks = new IntHashMap<List<Sample>>();
 private Map<String,Sample> sampleMap = new HashMap<String,Sample>();

 private IntMap<List<Group>> groupBlocks = new IntHashMap<List<Group>>();
 private Map<String,Group> groupMap = new HashMap<String,Group>();
 
 public Sample addSample(Sample sample)
 {
  Sample s = sampleMap.get(sample.getID());
  
  if( s != null )
  {
   if( s.getBlock() != sample.getBlock() )
     throw new RuntimeException("Sample accession redefinition: "+sample.getID());
  
   return s;
  } 
  
  sampleMap.put( sample.getID(), sample );
  
  List<Sample> blkList = sampleBlocks.get(sample.getBlock());
  
  if( blkList == null )
   sampleBlocks.put(sample.getBlock(), blkList = new ArrayList<Sample>(100) );
  
  blkList.add(sample);
  
  return sample;
 }
 
 public List<List<Sample>> getSampleBlocks()
 {
  ArrayList<IntMap.Entry<List<Sample>>> meList = new ArrayList<IntMap.Entry<List<Sample>>>( sampleBlocks.entrySet() );
  
  Collections.sort(meList, new Comparator<IntMap.Entry<List<Sample>>>()
  {
   @Override
   public int compare(Entry<List<Sample>> o1, Entry<List<Sample>> o2)
   {
    return o1.getKey()-o2.getKey();
   }
  });
  
  ArrayList< List<Sample> > lst = new ArrayList<List<Sample>>( meList.size() );
  
  for( IntMap.Entry<List<Sample>> blk : meList)
   lst.add( blk.getValue() );
  
  return lst;
 }

 public Group addGroup(Group group)
 {
  Group g = groupMap.get(group.getID());
  
  if( g != null )
  {
   if( g.getBlock() != group.getBlock() )
     throw new RuntimeException("Group accession redefinition: "+group.getID());
  
   return g;
  } 
  
  groupMap.put( group.getID(), group );
  
  List<Group> blkList = groupBlocks.get(group.getBlock());
  
  if( blkList == null )
   groupBlocks.put(group.getBlock(), blkList = new ArrayList<Group>(100) );
  
  blkList.add(group);
  
  return group;
 }

 public Sample getSample(String sname)
 {
  return sampleMap.get(sname);
 }

 public Group getGroup(String gname)
 {
  return groupMap.get(gname);
 }

 public Collection<Group> getGroups()
 {
  return groupMap.values();
 }

}
