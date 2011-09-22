package uk.ac.ebi.sampletab;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.pri.util.SpreadsheetReader;
import com.pri.util.stream.StreamPump;


public class STParser2
{
 
 
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
    if(! Definitions.propertyToObject.containsKey(p0))
     throw new STParseException("Unknown tag: '" + p0 + "' Line: " + reader.getLineNumber());

    String objName = Definitions.propertyToObject.get(p0);

    if(objName.equals(Definitions.SUBMISSION))
    {
     if(parts.size() != 2)
      throw new STParseException("Invalid number of values for tag: '" + p0 + "' Expected: 1");

     sub.addAnnotation(new Attribute(p0, parts.get(1), reader.getLineNumber()) );
    }
    else
    {
     List<WellDefinedObject> objs = sub.getAttachedObjects(objName);
     
     if( objs.size() < parts.size()-1 )
      for( int k=0; k < parts.size()-1-objs.size(); k++ )
       objs.add(null);
     
     
     for( int i=0; i < parts.size()-1; i++ )
     {
      WellDefinedObject a = objs.get(i);
      
      if( a == null )
       objs.set(i, a=new WellDefinedObject(objName));
      
//      String nm = p0.substring(objName.length()+1);
      
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
     int atObjNum=0;
     
     int runlen = parts.size();
    
     for( int i=0; i < runlen; i++ )
     {
      String hdr = headerLine.get(i);
      
      if( Definitions.SAMPLENAME.equals(hdr) )
      {
       blockNum++;
       
       
       if( group != null )
       {
        if( group.getID() == null )
         throw new STParseException("Group has no accession. Line: "+reader.getLineNumber());
        
        group = sub.addGroup( group );
        
        group.addSample(sample);
        sample.addGroup( group );
        
        group = null;
       }
       else if( sample != null )
       {
        if( sample.getID() == null )
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
       
       sample.addAnnotation(attribute = new Attribute(Definitions.SAMPLENAME_AGE, parts.get(i), i ) );
      }
      if( Definitions.GROUPNAME.equals(hdr) )
      {
       blockNum++;
       atObjNum=0;
       
       if( sample != null )
       {
        if( sample.getID() == null )
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
       
       group.addAnnotation(attribute = new Attribute(Definitions.GROUPNAME_AGE, parts.get(i), i ) );
      }
      else if( Definitions.propertyToObject.containsKey(hdr) && group != null )
      {
       String clsName = Definitions.propertyToObject.get(hdr);
       
       List<WellDefinedObject> oLst = group.getAttachedObjects( clsName );
       
       WellDefinedObject obj = null;
       
       if( oLst.size() <= atObjNum )
        oLst.add( obj = new WellDefinedObject(clsName) );
       else
       {
        obj = oLst.get(atObjNum);
        
        Attribute cAttr = obj.getAnnotation(hdr);
        
        if( cAttr != null )
        {
         if( cAttr.getOrder() == i )
         {
          if( ! cAttr.getID().equals( parts.get(i) ) )
           throw new STParseException("Attached object redefinition. Line: "+reader.getLineNumber()+" Col: "+(i+1));
         }
         else
         {
          oLst.add( obj = new WellDefinedObject(clsName) );
          
          atObjNum++;
         }
        }
       }
       
       obj.addAnnotation( new Attribute( hdr, parts.get(i), i) );

      }
      else if( Definitions.UNIT.equals( hdr ) ||  Definitions.TERMSOURCEREF.equals( hdr ) ||  Definitions.TERMSOURCEID.equals( hdr ) )
      {
       String value = parts.get(i).trim();
       
       if( value.length() > 0 )
        attribute.addAnnotation(new Attribute(hdr,value,i));
      }
      else if( hdr.endsWith("Accession") )
      {
       if( group != null )
        group.setID(parts.get(i).trim());
       else
        sample.setID(parts.get(i).trim());
      }
      else
      {
       String value = parts.get(i).trim();

       AnnotatedObject host = group != null ? group : sample;

       Attribute attr = (Attribute) host.getAnnotation(hdr);

       if(attr != null)
        attr.addValue(value,i);
       else
        host.addAnnotation( new Attribute(hdr, value, i) );
      }
     }
     
     
     if( group != null )
     {
      if( group.getID() == null )
       throw new STParseException("Group has no accession. Line: "+reader.getLineNumber());
      
      group = sub.addGroup( group );
      
      group.addSample(sample);
      sample.addGroup( group );
     }
     else if( sample != null )
     {
      if( sample.getID() == null )
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
