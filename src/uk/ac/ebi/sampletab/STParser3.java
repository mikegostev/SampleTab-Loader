package uk.ac.ebi.sampletab;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pri.util.SpreadsheetReader;
import com.pri.util.StringUtils;


public class STParser3
{
 private static int counter;
 
 public static Submission readST( File modFile ) throws IOException
 {
  return readST( StringUtils.readUnicodeFile( modFile ) );
 }
 
 public static Submission readST( String text ) throws IOException
 {
  Submission sub = new Submission();
  
  SpreadsheetReader reader = new SpreadsheetReader( text );
  
  List<String> parts = new ArrayList<String>(100);
  
  boolean sampleSection = false;
  List<String> headerLine = null; 
  List<String> prevLine = null; 
  
  int lineNum = 0;
  while( reader.readLine(parts) != null )
  {
   lineNum++;
   
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
   
   if( p0.length() == 0 || p0.startsWith(Definitions.COMMENTCHAR) || p0.equals(Definitions.MSIBLOCK) )
    continue;
   
   if( p0.equals(Definitions.SCDBLOCK) )
   {
    sampleSection = true;
    continue;
   }
   
   if( ! sampleSection )
   {
    if( Definitions.submissionProperties.containsKey(p0))
    {
     if(parts.size() != 2)
      throw new STParseException("Invalid number of values for tag: '" + p0 + "' Expected: 1");

     sub.addAnnotation(new Attribute(p0, parts.get(1), reader.getLineNumber()) );
     
     if( Definitions.SUBMISSIONIDENTIFIER.equals(p0) )
      sub.setID(parts.get(1));
    }
    else if( ! Definitions.propertyToObject.containsKey(p0))
     throw new STParseException("Unknown tag: '" + p0 + "' Line: " + reader.getLineNumber());
    else
    {
     String objName = Definitions.propertyToObject.get(p0);

     List<WellDefinedObject> objs = sub.getAttachedObjects(objName);
     
     if( objs == null )
      sub.setAttachedObjects(objName, objs= new ArrayList<WellDefinedObject>());
     
     if( objs.size() < parts.size()-1 )
      for( int k=parts.size()-1-objs.size(); k > 0 ; k-- )
       objs.add(null);
     
     
     for( int i=0; i < parts.size()-1; i++ )
     {
      WellDefinedObject a = objs.get(i);
      
      if( a == null )
       objs.set(i, a=new WellDefinedObject(objName));
      else
      {
       for( Attribute attr : a.getAnnotations() )
       {
        if( p0.equals(attr.getName()) )
         throw new STParseException("Repeting field: '"+p0+"' Line: "+reader.getLineNumber() );
       }
      }
      
      a.addAnnotation(new Attribute(p0, parts.get(i+1), reader.getLineNumber() ) );
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
     
     if( ! headerLine.get(0).equals(Definitions.SAMPLENAME) )
      throw new STParseException("The first column should be "+Definitions.SAMPLENAME+" Line: "+reader.getLineNumber());
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
     
     boolean firstDefLine=true;
    
     for( int i=0; i < runlen; i++ )
     {
      String hdr = headerLine.get(i);
      String cellVal = parts.get(i).trim();
      
      if( cellVal.length() == 0 )
       continue;
      
      if( Definitions.SAMPLENAME.equals(hdr) )
      {
       blockNum++;
       
       
       sample = sub.getSample( cellVal );
       
       if( sample == null )
       {
        sample = new Sample();
        sample.setBlock(blockNum);
       
        sample.setID(cellVal);
        sub.addSample(sample);
        
        sample.addAnnotation( new Attribute(Definitions.SAMPLENAME_AGE, cellVal, i));
        
        firstDefLine = true;
       }
       else
        firstDefLine = false;
 
       if( lastSample != null )
       {
        lastSample.addDerivative( sample );
        sample.addDerivedFrom( lastSample );
       }
       
       lastSample = sample;
      
       group = null;
       attribute = null;

      }
      else if( Definitions.GROUPNAME.equals(hdr) )
      {
       blockNum++;

       group = sub.getGroup( cellVal );
       
       if( group == null )
       {
        group = new Group();
        group.setBlock(blockNum);
       
        group.setID(cellVal);
        sub.addGroup(group);

        group.addAnnotation( new Attribute(Definitions.GROUPNAME_AGE, cellVal, i));

        firstDefLine = true;
       }
       else
        firstDefLine = false;
       
       group.addSample(sample);
       sample.addGroup( group );

       attribute = null;
      }
      else if( Definitions.propertyToObject.containsKey(hdr) && group != null )
      {
       if( firstDefLine )
       {
        String clsName = Definitions.propertyToObject.get(hdr);
        
        List<WellDefinedObject> oLst = group.getAttachedObjects( clsName );
        
        if( oLst == null )
         group.setAttachedObjects(clsName, oLst= new ArrayList<WellDefinedObject>());

        WellDefinedObject obj = null;
        
        if( oLst.size() == 0  )
         oLst.add( obj = new WellDefinedObject(clsName) );
        else
         obj = oLst.get(oLst.size()-1);

        Attribute cAttr = obj.getAnnotation(hdr);

        if( cAttr != null )
         oLst.add( obj = new WellDefinedObject(clsName) );
        
        obj.addAnnotation( new Attribute( hdr, cellVal, i) );
       }
//       else if( ! cellVal.equals( prevLine.get(i) ) )
//        throw new STParseException("Object redefinition. Line: "+reader.getLineNumber()+" Col: "+(i+1));
       
       attribute = null;
      }
      else if( Definitions.UNIT.equals( hdr ) )
      {
       if( firstDefLine )
       {
        if( attribute == null )
         throw new STParseException("Invalid tag position. Line: "+reader.getLineNumber()+" Col: "+(i+1));
        
        
        if( cellVal.length() > 0 )
         attribute.addAnnotation(attribute=new Attribute(hdr,cellVal,i));
       }
//       else if( ! cellVal.equals( prevLine.get(i) ) )
//        throw new STParseException("Object redefinition. Line: "+reader.getLineNumber()+" Col: "+(i+1));

      }
      else if( Definitions.TERMSOURCEREF.equals( hdr ) ||  Definitions.TERMSOURCEID.equals( hdr ) )
      {
       if( firstDefLine )
       {
        if( attribute == null )
         throw new STParseException("Invalid tag position. Line: "+reader.getLineNumber()+" Col: "+(i+1));
        
        
        if( cellVal.length() > 0 )
         attribute.addAnnotation(new Attribute(hdr,cellVal,i));
       }
//       else if( ! cellVal.equals( prevLine.get(i) ) )
//        throw new STParseException("Object redefinition. Line: "+reader.getLineNumber()+" Col: "+(i+1));

      }
      else
      {
       if( firstDefLine )
       {
        AnnotatedObject host = group != null ? group : sample;

        attribute = host.getAnnotation(hdr);
 
        if( attribute != null )
         attribute = attribute.addValue(cellVal, i);
        else
         host.addAnnotation( attribute = new Attribute(hdr, cellVal, i) );
       
       }
//       else if( ! cellVal.equals( prevLine.get(i) ) )
//        throw new STParseException("Object redefinition. Line: "+reader.getLineNumber()+" Col: "+(i+1));

      }
     }
     
//     TimeLog.reportEvent("Line read "+p0);

     
     prevLine = parts;
    }
    
   }
  }
  
  
  List<WellDefinedObject> tsrs = sub.getAttachedObjects( Definitions.TERMSOURCE );
  
  if( tsrs != null )
   for( WellDefinedObject ts : tsrs )
    validateTermSource( ts );
  
  for( Group g : sub.getGroups() )
  {
   Attribute accss = g.getAnnotation(Definitions.GROUPACCESSION);
   
   if( accss == null )
    throw new STParseException("No accession is defined for group: "+g.getID());

   if( accss.getValuesNumber() != 1 )
    throw new STParseException("Multiple accessions are defined for group: "+g.getID());

   g.setID(accss.getID());
   
   tsrs = g.getAttachedObjects(Definitions.TERMSOURCE);

   if(tsrs == null)
    continue;

   for(WellDefinedObject ts : tsrs)
   {
    validateTermSource(ts);

    boolean found = false;

    List<WellDefinedObject> subTsrs = sub.getAttachedObjects(Definitions.TERMSOURCE);

    if(subTsrs != null)
    {
     for(WellDefinedObject subts : subTsrs)
     {
      if(subts.getID().equals(ts.getID()))
      {
       if(subts.equals(ts))
       {
        found = true;
        break;
       }
       else
        throw new STParseException("Term Source conflict: " + subts.getID());
      }
     }
    }
    else
     sub.setAttachedObjects(Definitions.TERMSOURCE, subTsrs=new ArrayList<WellDefinedObject>());

    if(!found)
     subTsrs.add(ts);
   }

  }
  
  counter = 1;
 
  for( String atClName : Definitions.object2Properties.keySet() )
   if( ! atClName.equals( Definitions.TERMSOURCE ) )
    mergeObjects( sub, atClName );

  for( List<Sample> sBlk : sub.getSampleBlocks() )
  {
   for( Sample s : sBlk)
   {
    Attribute accss = s.getAnnotation(Definitions.SAMPLEACCESSION);
    
    if( accss == null )
     throw new STParseException("No accession is defined for sample: "+s.getID());

    if( accss.getValuesNumber() != 1 )
     throw new STParseException("Multiple accessions are defined for sample: "+s.getID());
    
    s.setID(accss.getID());
   }
  }
  
  return sub;
 }

 private static void mergeObjects(Submission sub, String atClName)
 {
  List<WellDefinedObject> subos = sub.getAttachedObjects( atClName );
  
  if( subos == null )
   subos = new ArrayList<WellDefinedObject>();
  
  for( WellDefinedObject obj : subos )
  {
   if( obj.getID() == null )
    obj.setID(atClName.substring(0,3)+(counter++));
  }
  
  for( Group g : sub.getGroups() )
  {
   List<WellDefinedObject> grpos = g.getAttachedObjects( atClName );

   if( grpos == null )
    continue;
   
   
   for( WellDefinedObject obj : grpos )
   {

    boolean found = false;
    for( WellDefinedObject subobj  : subos)
    {
     if( obj.equals(subobj) )
     {
      found=true;
      obj.setID(subobj.getID());
      break;
     }
    }
    
    if( ! found )
    {
     obj.setID(atClName.substring(0,3)+(counter++));
     subos.add(obj);
    }
   }
  }
  
  if( subos.size() > 0 )
   sub.setAttachedObjects(atClName, subos);
 }

 private static void validateTermSource(WellDefinedObject ts)
 {
  Attribute attr = ts.getAnnotation( Definitions.TERMSOURCENAME );
  
  if( attr == null || attr.getID().length() == 0 )
   throw new STParseException("Term Source has no name");
  
  ts.setID(attr.getID());
  
  attr = ts.getAnnotation( Definitions.TERMSOURCEURI );
  
  if( attr == null || attr.getID().length() == 0 )
   throw new STParseException("Term Source has no URI");

 }
}
