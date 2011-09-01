package uk.ac.ebi.sampletab;


public class QualifierExtractor extends QualifiedAttributeExtractor
{

 public QualifierExtractor( String name, String hdr )
 {
  super(name, hdr);
 }

 
 @Override
 public void setSample(Sample obj)
 {}
 
 public void setAttribute(Attribute attr)
 {
  if( attr == null)
   obj = null;
  else
   obj = attr.getAnnotation(name);

  pos = 0 ;
  delivered = false;
 }
}

