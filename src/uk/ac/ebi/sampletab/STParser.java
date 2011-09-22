package uk.ac.ebi.sampletab;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.pri.util.SpreadsheetReader;
import com.pri.util.collection.IntHashMap;
import com.pri.util.collection.IntMap;
import com.pri.util.stream.StreamPump;


public class STParser
{
 public static final String SAMPLENAME      = "Sample Name";
 public static final String SAMPLENAME_AGE  = "Name";

 public static final String GROUPNAME       = "Group Name";
 public static final String GROUPNAME_AGE   = "Name";

 public static final String ACCESSIONSUFFIX = "Accession";

 public static final String TERMSOURCE      = "Term Source";
 public static final String TERMSOURCEREF   = "Term Source REF";
 public static final String TERMSOURCENAME  = "Term Source Name";
 public static final String UNIT            = "Unit";
 
 static Map<String, String> field2Object = new HashMap<String, String>()
 {{
  put("Submission Title", null);
  put("Submission Description", null);
  put("Submission Version", null);
  put("Submission Abstract", null);
  put("Submission Release Date", null);
  put("Submission Modification Date", null);
  put("Submission Identifier", null);
  put("Submission Reference Layer", null);
  
  put("Database ID", "Database");
  put("Database URI", "Database");
  put("Database Name", "Database");

  put("Publication DOI", "Publication");
  put("Publication PubMed ID", "Publication");

  put("Person Last Name", "Person");
  put("Person First Name", "Person");
  put("Person Mid Initials", "Person");
  put("Person Email", "Person");
  put("Person Role", "Person");

  put("Organization Name", "Organization");
  put("Organization Address", "Organization");
  put("Organization URI", "Organization");
  put("Organization Email", "Organization");
  put("Organization Role", "Organization");

  put(TERMSOURCENAME, TERMSOURCE);
  put("Term Source URI", TERMSOURCE);
  put("Term Source Version", TERMSOURCE);
 }};
 
 public static class AnnotatedObject
 {
  private String value;

  Map<String,AnnotatedObject> annotations = null;

  public String getValue()
  {
   return value;
  }

  public void setValue(String accession)
  {
   this.value = accession;
  }
  
  void addAnnotation( String name, AnnotatedObject value )
  {
   if( annotations == null )
    annotations = new LinkedHashMap<String, STParser.AnnotatedObject>();
   
   annotations.put(name, value);
  }
  
  public AnnotatedObject getAnnotation( String key )
  {
   return annotations.get(key);
  }
  
  public Collection<AnnotatedObject> getAnnotations()
  {
   if( annotations != null )
    return annotations.values();
   
   return null;
  }
 }

 
 public static class Attribute extends AnnotatedObject
 {
  private String name; 
  private List<String> vals;
  
  public Attribute( String name, String val )
  {
   setValue(val);
   
   this.name = name;
  }
  
  public void addValue( String v )
  {
   if( vals == null )
   {
    vals = new ArrayList<String>();
    
    vals.add(super.getValue());
   }
   
   vals.add(v);
  }
  
  public String getValue()
  {
   if( vals == null )
    return super.getValue();
   
   return vals.get(0);
  }
  
  public int getValueNumber()
  {
   return vals == null? 1: vals.size();
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
 
 public  static class Submission extends AnnotatedObject
 {
  private Map<String, List<AnnotatedObject>> objects= new HashMap<String, List<AnnotatedObject>>();

  private IntMap<List<Sample>> sampleBlocks = new IntHashMap<List<Sample>>();
  private Map<String,Sample> sampleMap = new HashMap<String, STParser.Sample>();

  private IntMap<List<Group>> groupBlocks = new IntHashMap<List<Group>>();
  private Map<String,Group> groupMap = new HashMap<String, STParser.Group>();
  
  public Sample addSample(Sample sample)
  {
   Sample s = sampleMap.get(sample.getValue());
   
   if( s != null )
   {
    if( s.getBlock() != sample.getBlock() )
      throw new RuntimeException("Sample accession redefinition: "+sample.getValue());
   
    return s;
   } 
   
   sampleMap.put( sample.getValue(), sample );
   
   List<Sample> blkList = sampleBlocks.get(sample.getBlock());
   
   if( blkList == null )
    sampleBlocks.put(sample.getBlock(), blkList = new ArrayList<STParser.Sample>(100) );
   
   blkList.add(sample);
   
   return sample;
  }

  public Group addGroup(Group group)
  {
   Group g = groupMap.get(group.getValue());
   
   if( g != null )
   {
    if( g.getBlock() != group.getBlock() )
      throw new RuntimeException("Group accession redefinition: "+group.getValue());
   
    return g;
   } 
   
   groupMap.put( group.getValue(), group );
   
   List<Group> blkList = groupBlocks.get(group.getBlock());
   
   if( blkList == null )
    groupBlocks.put(group.getBlock(), blkList = new ArrayList<STParser.Group>(100) );
   
   blkList.add(group);
   
   return group;
  }
  
  public List<AnnotatedObject> getAttachedObjects( String objName )
  {
   List<AnnotatedObject> obl = objects.get( objName ); 
   
   if( obl == null )
    objects.put(objName, obl = new ArrayList<AnnotatedObject>() );
   
   return obl;
  }
 }
 
 public static class Sample extends AnnotatedObject
 {
  private int block;

  private List<Sample> derivatives;
  private List<Sample> derivedFrom;
  private List<Group> groups;
  
  
  public Sample addDerivative(Sample sample)
  {
   if( derivatives == null )
   {
    derivatives = new ArrayList<STParser.Sample>();
    
    derivatives.add(sample);
    
    return sample;
   }
   
   for( Sample ds : derivatives )
    if( ds.getValue().equals(sample.getValue()) )
     return ds;
   
   derivatives.add(sample);
   
   return sample;
  }

  public Sample addDerivedFrom(Sample sample)
  {
   if( derivedFrom == null )
   {
    derivedFrom = new ArrayList<STParser.Sample>();
    
    derivedFrom.add(sample);
    
    return sample;
   }
   
   for( Sample ds : derivedFrom )
    if( ds.getValue().equals(sample.getValue()) )
     return ds;
   
   derivedFrom.add(sample);
   
   return sample;
  }

  public Group addGroup(Group group)
  {
   if( groups == null )
   {
    groups = new ArrayList<Group>();
    
    groups.add(group);
    
    return group;
   }
   
   for( Group ds : groups )
    if( ds.getValue().equals(group.getValue()) )
     return ds;
   
   groups.add(group);
   
   return group;
   
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
 
 public static class Group extends AnnotatedObject
 {
  private int block;
  private List<Sample> samples;
  
  public Sample addSample(Sample sample)
  {
   if( samples == null )
   {
    samples = new ArrayList<STParser.Sample>();
    
    samples.add(sample);
    
    return sample;
   }
   
   for( Sample ds : samples )
    if( ds.getValue().equals(sample.getValue()) )
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
 
 public static Submission readST( File modFile ) throws IOException
 {
  ByteArrayOutputStream bais = new ByteArrayOutputStream();

  FileInputStream fis = new FileInputStream(modFile);
  StreamPump.doPump(fis, bais, false);
  fis.close();

  bais.close();

  byte[] barr = bais.toByteArray();
  String enc = "UTF-8";

  if(barr.length >= 2 && (barr[0] == -1 && barr[1] == -2) || (barr[0] == -2 && barr[1] == -1))
   enc = "UTF-16";

  String text = new String(bais.toByteArray(), enc);
  
  Submission sub = new Submission();
  
  SpreadsheetReader reader = new SpreadsheetReader( text );
  
  List<String> parts = new ArrayList<String>(100);
  
  boolean sampleSection = false;
  List<String> headerLine = null; 
  
  while( reader.readRow(parts) != null )
  {
   int emp=0;
   for( int k=parts.size()-1; k>=0 ; k-- )
    if( parts.get(k).trim().length() == 0 )
     emp++;
    else
     break;
    
   if( emp == parts.size() )
    continue;
   
   parts = parts.subList(0, parts.size()-emp);
   
   String p0 = parts.get(0).trim();
   
   if( p0.length() == 0 || p0.startsWith("#") || p0.equals("[MSI]") )
    continue;
   
   if( p0.equals("[SCD]") )
   {
    sampleSection = true;
    continue;
   }
   
   if( ! sampleSection )
   {
    if(!field2Object.containsKey(p0))
     throw new STParseException("Unknown tag: '" + p0 + "' Line: " + reader.getLineNumber());

    String objName = field2Object.get(p0);

    if(objName == null)
    {
     if(parts.size() != 2)
      throw new STParseException("Invalid number of values for tag: '" + p0 + "' Expected: 1");

//     {
//      for( int j=2; j < parts.size(); j++ )
//       if( parts.get(j).trim().length() > 0 )
//     }
     sub.addAnnotation(p0, new Attribute(p0, parts.get(1)) );
    }
    else
    {
     List<AnnotatedObject> objs = sub.getAttachedObjects(objName);
     
     if( objs.size() < parts.size()-1 )
      for( int k=0; k < parts.size()-1-objs.size(); k++ )
       objs.add(null);
     
     
     for( int i=0; i < parts.size()-1; i++ )
     {
      AnnotatedObject a = objs.get(i);
      
      if( a == null )
       objs.set(i, a=new AnnotatedObject());
      
      String nm = p0.substring(objName.length()+1);
      
      a.addAnnotation(nm, new Attribute(nm, parts.get(i+1) ) );
     }
    } 
   }
   else
   {
    if( headerLine == null )
    {
     headerLine = new ArrayList<String>( parts.size() );
     
     for( String p : parts )
      headerLine.add( p.trim() );
     
     if( ! headerLine.get(0).equals(SAMPLENAME) )
      throw new STParseException("The first column should be "+SAMPLENAME+" Line: "+reader.getLineNumber());
    }
    else
    {
     if( parts.size() > headerLine.size() )
      throw new STParseException("Some values are beyond the annotation. Line: "+reader.getLineNumber());
     
     Sample lastSample = null;
     Sample sample = null;
     Group group = null;
     Attribute attribute = null;
     
     int blockNum=0;
     
     int runlen = parts.size();
    
     for( int i=0; i < runlen; i++ )
     {
      String hdr = headerLine.get(i);
      
      if( SAMPLENAME.equals(hdr) )
      {
       blockNum++;
       
       
       if( group != null )
       {
        if( group.getValue() == null )
         throw new STParseException("Group has no accession. Line: "+reader.getLineNumber());
        
        group = sub.addGroup( group );
        
        group.addSample(sample);
        sample.addGroup( group );
        
        group = null;
       }
       else if( sample != null )
       {
        if( sample.getValue() == null )
         throw new STParseException("Sample has no accession. Line: "+reader.getLineNumber());

        sample = sub.addSample( sample );
        
        if( lastSample != null )
        {
         lastSample.addDerivative( sample );
         sample.addDerivedFrom( lastSample );
        }
        
        lastSample = sample;
       }
       
       sample = new Sample();
       sample.setBlock(blockNum);
       
       sample.addAnnotation(SAMPLENAME_AGE, attribute = new Attribute(SAMPLENAME_AGE, parts.get(i) ) );
      }
      if( GROUPNAME.equals(hdr) )
      {
       blockNum++;
       
       if( sample != null )
       {
        if( sample.getValue() == null )
         throw new STParseException("Sample has no accession. Line: "+reader.getLineNumber());

        sample = sub.addSample( sample );
        
        if( lastSample != null )
        {
         lastSample.addDerivative( sample );
         sample.addDerivedFrom( lastSample );
        }
        
        lastSample = sample;
       }
       
       group = new Group();
       group.setBlock(blockNum);
       
       group.addAnnotation(GROUPNAME_AGE, attribute = new Attribute(GROUPNAME_AGE, parts.get(i) ) );
      }
      else if( TERMSOURCEREF.equals(hdr) )
      {
       List<AnnotatedObject> tss = sub.getAttachedObjects( TERMSOURCE );
       
       if( tss == null )
        throw new STParseException("No term sources defined. Line: "+reader.getLineNumber() );
       
       String tsRef = parts.get(i).trim();
       
       if( tsRef.length() == 0 )
        continue;
       
       boolean found = false;
       
       for( AnnotatedObject a : tss )
       {
        if( tsRef.equals( a.getAnnotation( "Name" ).getValue() ) )
        {
         found = true;
         break;
        }
       }
       
       if( ! found )
        throw new STParseException("No such term source defined: '"+tsRef+"'. Line: "+reader.getLineNumber() );
       
       attribute.addAnnotation(TERMSOURCEREF, new Attribute(TERMSOURCEREF,tsRef));
      }
      else if( UNIT.equals( hdr ))
      {
       String value = parts.get(i).trim();
       
       if( value.length() > 0 )
        attribute.addAnnotation(UNIT, new Attribute(UNIT,value));
      }
      else if( hdr.endsWith(ACCESSIONSUFFIX) )
      {
       if( group != null )
        group.setValue(parts.get(i).trim());
       else
        sample.setValue(parts.get(i).trim());
      }
      else
      {
       String value = parts.get(i).trim();

       AnnotatedObject host = group != null ? group : sample;

       Attribute attr = (Attribute) host.getAnnotation(hdr);

       if(attr != null)
        attr.addValue(value);
       else
        host.addAnnotation(hdr, new Attribute(hdr, value));
      }
     }
     
     
     if( group != null )
     {
      if( group.getValue() == null )
       throw new STParseException("Group has no accession. Line: "+reader.getLineNumber());
      
      group = sub.addGroup( group );
      
      group.addSample(sample);
      sample.addGroup( group );
     }
     else if( sample != null )
     {
      if( sample.getValue() == null )
       throw new STParseException("Sample has no accession. Line: "+reader.getLineNumber());

      sample = sub.addSample( sample );
      
      if( lastSample != null )
      {
       lastSample.addDerivative( sample );
       sample.addDerivedFrom( lastSample );
      }
     }
     
     
     
    }
    
   }
  }
  
  return sub;
 }
}
