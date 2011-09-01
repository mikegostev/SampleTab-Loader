package uk.ac.ebi.sampletab;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ContainerObject extends AnnotatedObject
{
 private Map<String, List<WellDefinedObject>> attachedObjects = new HashMap<String, List<WellDefinedObject>>();
 
 public void setAttachedObjects( String s, List<WellDefinedObject> list )
 {
  attachedObjects.put( s, list );
 }
 
 public List<WellDefinedObject> getAttachedObjects(String s)
 {
  return attachedObjects.get(s);
 }
 
 public Collection<String> getAttachedClasses()
 {
  return attachedObjects.keySet();
 }
}
