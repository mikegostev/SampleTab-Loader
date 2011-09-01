package uk.ac.ebi.sampletab;

import java.util.ArrayList;
import java.util.List;

public class Group extends ContainerObject
{
 private int block;
 private List<Sample> samples;
 
 public Sample addSample(Sample sample)
 {
  if( samples == null )
  {
   samples = new ArrayList<Sample>();
   
   samples.add(sample);
   
   return sample;
  }
  
  for( Sample ds : samples )
   if( ds.getID().equals(sample.getID()) )
    return ds;
  
  samples.add(sample);
  
  return sample;
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
