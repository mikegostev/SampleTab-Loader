package uk.ac.ebi.sampletab;


public class WellDefinedObject extends AnnotatedObject
{
 private String className;
// private Map<String, Attribute>
 
 public WellDefinedObject( String cls ) 
 {
  className = cls;
  
  if( ! Definitions.object2Properties.containsKey(cls) )
   throw new STParseException("Invalid class name");
 }
 
 public void addAnnotation( Attribute attr )
 {
  if( ! className.equals( Definitions.propertyToObject.get(attr.getName()) ) )
   throw new STParseException("Invalid property '"+attr.getName()+"' for class '"+className+"'");

  super.addAnnotation(attr);
 }

}
