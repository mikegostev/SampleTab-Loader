package uk.ac.ebi.sampletab;

import java.util.List;

public class SimpleAttributeExtractor implements ValueExtractor
{
 private String name;
 private String header;
 private int order;
 private Attribute obj;
 private boolean delivered;
 
 public SimpleAttributeExtractor( String name, String hdr, int ord )
 {
  this.name=name;
  order = ord;
  header = hdr;
 }

 @Override
 public void setSample(Sample obj)
 {
  this.obj = obj.getAnnotation(name);
  delivered=false;
 }

 @Override
 public String extract()
 {
  if( delivered || obj == null )
  {
   delivered = true;
   return "";
  }
  
  delivered = true;

  List<Attribute> vals = obj.getValues();
  
  if( vals.size() <= order )
   return "";
  
  return vals.get(order).getID();
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
