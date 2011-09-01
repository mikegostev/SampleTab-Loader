package uk.ac.ebi.sampletab;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TAB2AGE
{

 static class ColumnDescriptor
 {
  String kind;
  int    linkedColumnNumber;
  String nodeType;
  String attClass;
  String attType;
  String attSubtype;
  String unitType;
  String commentType;
 }

 static class IDFObject
 {
  String   attributeName;
  String[] attributeValues;
 }

 static Set<String> nodeTypes                 = new HashSet<String>(Arrays.asList("Sample Name", "Group Name"));
 static Set<String> specialNodeTypes          = new HashSet<String>(Arrays.asList("Group Name"));
 static Set<String> attributeTypes            = new HashSet<String>(Arrays.asList("Characteristics", "Provider",
                                                "Material"));
 static Set<String> attributesCommentsAllowed = new HashSet<String>(Arrays.asList("Provider"));

 public static void main(String[] arg) throws IOException
 {
  File dir = new File(arg[0]); // parameter - directory where all .txt files
                               // need to be converted
  if(dir.isDirectory())
  {
   String[] children = dir.list();
   for(int i = 0; i < children.length; i++)
   {
    if(children[i].endsWith(".txt"))
    {
     try
     {
      convert(arg[0] + File.separator + children[i]);
     }
     catch(Exception e)
     {
      e.printStackTrace(System.out);
     }
    }
   }
  }
 }

 public static void convert(String path) throws IOException
 {

  System.out.println(path);
  String submissionName = path.substring(path.lastIndexOf("\\") + 1, path.lastIndexOf("."));
  TreeSet<String> groupSet = new TreeSet<String>();
  String prefix = "XX-X-";

  BufferedReader in = new BufferedReader(new FileReader(path));
  String str;
  ColumnDescriptor[] colDefs = null;
  int colNumber = 0;
  String oldCat = "";
  Vector<IDFObject> v = new Vector<IDFObject>();
  BufferedWriter out = new BufferedWriter(new FileWriter(path + ".out"));
  int blockColNum = 0;
  boolean lastBlock = false;
  boolean hasGroups = false;
  boolean headerLine = false;
  while((str = in.readLine()) != null)
  { // first pass through the file - find if there are groups defined; if not, a
    // default will be created
   if(str.startsWith("[SCD]"))
   {
    headerLine = true;
    continue;
   }
   if(headerLine && str.indexOf("Group Name") != -1)
   {
    hasGroups = true;
    break;
   }
   headerLine = false;
  }
  in.close(); // end of first pass

  in = new BufferedReader(new FileReader(path)); // 2nd pass - write MSI section
                                                 // info, determine SCD section
                                                 // structure
  while((str = in.readLine()) != null)
  {
   if(str.startsWith("[SCD]"))
    lastBlock = true;
   else if(str.startsWith("#") || str.startsWith("[") || str.contains("[MSI]") || str.trim().compareTo("") == 0)
    continue;
   String[] strArr = str.split("[\\t]");
   colNumber = strArr.length;
   String newCat = "";
   if(!lastBlock)
   {
    newCat = str.substring(0, str.indexOf(" "));
    if(strArr[0].startsWith("Term Source "))
     newCat = "Term Source";
   }
   else
    newCat = "end";

   if(oldCat.compareTo(newCat) != 0)
   { // MSI section processed in blocks, e.g. "Submission" block and
     // "Organization" block
    if(oldCat.compareTo("") != 0 && blockColNum > 1 && oldCat.compareTo("Publication") != 0
      && oldCat.compareTo("Organization") != 0 && oldCat.compareTo("Person") != 0)
    {
     if(oldCat.compareTo("Submission") == 0)
      out.write(oldCat + "\t");
     for(int i = 0; i < v.size(); i++)
     {
      String attName = v.get(i).attributeName;
      out.write(translate(attName, false) + "\t");
      if(attName.compareTo("Database Name") == 0)
      {
       String db = v.get(i).attributeValues[1].substring(0, 2); // trying to
                                                                // infer group
                                                                // names from
                                                                // database ids
       prefix = db + prefix.substring(2);
      }
      if(attName.compareTo("Database ID") == 0)
      {
       String dbid = v.get(i).attributeValues[1];
       prefix = prefix.substring(0, 3) + dbid + "-"; // trying to infer group
                                                     // names from database ids
      }
     }
     out.write("\n");

     // now we will process only Submission, Term Source and Database objects.
     // Publications, Persons and Organizations go to the end of the file, after
     // all groups have been defined,
     // because we will want to link these objects to groups.
     if(oldCat.compareTo("Submission") == 0 || oldCat.compareTo("Term Source") == 0
       || oldCat.compareTo("Database") == 0)
     {
      for(int j = 1; j < blockColNum; j++)
      {
       if(oldCat.compareTo("Submission") == 0)
        out.write("*\t");
       for(int i = 0; i < v.size(); i++)
       {
        String token = "";
        if(v.get(i).attributeValues.length > j)
         token = v.get(i).attributeValues[j];
        if(i == 0 && (oldCat.compareTo("Term Source") == 0 || oldCat.compareTo("Database") == 0))
         token = "$" + token;
        out.write(token + "\t");
       }
       out.write("\n");
      }
     }
     out.write("\n");
    }
    blockColNum = colNumber;
    oldCat = newCat;
    v = new Vector<IDFObject>();
   }
   if(lastBlock)
    break;
   IDFObject idfo = new IDFObject();
   idfo.attributeName = strArr[0];
   idfo.attributeValues = strArr;
   v.add(idfo);
  }

  if(!hasGroups)
  { // generated group
   String groupId = "G" + prefix + "xxx";
   groupSet.add(groupId);

   out.write("Group\n");
   out.write(groupId + "\n");
   out.write("\n");
   out.write("Sample\tbelongsTo\n");
   out.write("*\t" + groupId + "\n"); // all samples belong to this one
   out.write("\n");
  }

  Pattern pattern = Pattern.compile("([^\\[]+)(\\[(.*)\\])?\\s*(\\((.+)\\))?");

  while((str = in.readLine()) != null)
  {
   if(str.startsWith("#") || str.startsWith("["))
    continue; // finding SCD section's structure (generic code, also good for
              // SDRF..)
   String[] strArr = str.split("[\\t]");
   colNumber = strArr.length;
   colDefs = new ColumnDescriptor[strArr.length];
   int curNodeIdx = -1;
   int curAttIdx = -1;
   int curUnitIdx = -1;
   int prevNodeIdx = -1;
   for(int curColIdx = 0; curColIdx < strArr.length; curColIdx++)
   {
    colDefs[curColIdx] = new ColumnDescriptor();
    Matcher matcher = pattern.matcher(strArr[curColIdx]);
    matcher.find();
    String header = matcher.group(1).trim();
    String inBrackets = (matcher.group(3) != null) ? matcher.group(3).trim() : null;
    if(nodeTypes.contains(header))
    {
     colDefs[curColIdx].kind = "node";
     colDefs[curColIdx].linkedColumnNumber = prevNodeIdx;
     if(!specialNodeTypes.contains(header))
      prevNodeIdx = curColIdx;
     curNodeIdx = curColIdx;
     colDefs[curColIdx].nodeType = header;
     curUnitIdx = -1;
     curAttIdx = -1;
    }
    else if(attributeTypes.contains(header) || curAttIdx == -1
      && (header.compareTo("Term Source REF") == 0 || header.compareTo("Comment") == 0)
      || (header.compareTo("Comment") == 0 && !attributesCommentsAllowed.contains(colDefs[curAttIdx].attClass)))
    {
     colDefs[curColIdx].kind = "attribute";
     colDefs[curColIdx].linkedColumnNumber = curNodeIdx;
     curAttIdx = curColIdx;
     colDefs[curColIdx].attClass = header;
     colDefs[curColIdx].attType = inBrackets;
     if(matcher.group(5) != null)
      colDefs[curColIdx].attSubtype = matcher.group(5).trim();
     curUnitIdx = -1;
    }
    else if(header.compareTo("Unit") == 0)
    {
     colDefs[curColIdx].kind = "unit";
     colDefs[curColIdx].linkedColumnNumber = curAttIdx;
     curUnitIdx = curColIdx;
     colDefs[curColIdx].unitType = inBrackets;
    }
    else if(header.compareTo("Term Source REF") == 0)
    {
     if(curUnitIdx != -1)
      colDefs[curColIdx].linkedColumnNumber = curUnitIdx;
     else
      colDefs[curColIdx].linkedColumnNumber = curAttIdx;
     curUnitIdx = -1;
     colDefs[curColIdx].kind = "tsr";
    }
    else if(header.compareTo("Comment") == 0)
    {
     colDefs[curColIdx].kind = "comment";
     colDefs[curColIdx].linkedColumnNumber = curAttIdx;
     curUnitIdx = -1;
     colDefs[curColIdx].commentType = inBrackets;
    }
    else
    {
     colDefs[curColIdx].kind = "other";
     colDefs[curColIdx].linkedColumnNumber = curNodeIdx;
    }
   }
   break;
  }
  in.close();
  for(int pivotColIdx = 0; pivotColIdx < colNumber; pivotColIdx++)
  { // reads file as many times as there are "pivot" columns in SCD (i.e.,
    // defining objects)
   if(colDefs[pivotColIdx] == null || colDefs[pivotColIdx].kind == null
     || colDefs[pivotColIdx].kind.compareTo("node") != 0)
    continue;
   out.write("\n");
   in = new BufferedReader(new FileReader(path));
   while((str = in.readLine()) != null)
    if(str.startsWith("[SCD]"))
     break;
   boolean header = true;
   int counter = 0;
   while((str = in.readLine()) != null)
   {
    if(str.startsWith("#") || str.startsWith("["))
     continue;
    if(!header)
     counter++;

    String[] strArr = str.split("[\\t]");
    int curAttIdx = -1;
    int curUnitIdx = -1;
    if(strArr.length <= pivotColIdx)
     continue;
    String toWrite = strArr[pivotColIdx];
    if(header)
     toWrite = translate(toWrite, false);

    if(header)
     out.write(colDefs[pivotColIdx].nodeType.substring(0, colDefs[pivotColIdx].nodeType.indexOf(" ")) + "\t");
    else
    {
     boolean isGroup = false;
     if(colDefs[pivotColIdx].nodeType.startsWith("Group"))
      isGroup = true;
     String id = (isGroup ? "G" : "S") + prefix + strArr[pivotColIdx];
     if(isGroup)
     {
      if(groupSet.contains(id))
       continue;
      groupSet.add(id);
     }
     out.write(id + "\t");
    }
    out.write(toWrite);
    for(int curColIdx = pivotColIdx + 1; curColIdx < strArr.length; curColIdx++)
    {
     if(colDefs[curColIdx].linkedColumnNumber == pivotColIdx || colDefs[curColIdx].linkedColumnNumber == curAttIdx
       || colDefs[curColIdx].linkedColumnNumber == curUnitIdx)
     {
      toWrite = strArr[curColIdx];
      boolean linkedColumn = false;
      if(colDefs[curColIdx].kind.compareTo("node") == 0)
       linkedColumn = true;
      if(header)
       toWrite = translate(toWrite, linkedColumn);
      else
      {
       if(colDefs[curColIdx].kind.compareTo("node") == 0)
       {
        boolean isGroup = false;
        if(colDefs[pivotColIdx].nodeType.startsWith("Group"))
         isGroup = true;
        toWrite = (isGroup ? "G" : "S") + prefix + toWrite;
       }
      }
      out.write("\t" + toWrite);
      if(colDefs[curColIdx].kind == "unit")
       curUnitIdx = curColIdx;
      else
       curUnitIdx = -1;
      if(colDefs[curColIdx].kind == "node")
       curAttIdx = -1;
      if(colDefs[curColIdx].kind == "attribute")
       curAttIdx = curColIdx;
     }
    }
    out.write("\n");
    header = false;
   }
   out.write("\n");
   in.close();
  }

  in = new BufferedReader(new FileReader(path));
  colDefs = null;
  colNumber = 0; // read again - populate group information from Submission
                 // fields
  oldCat = "";
  v = new Vector<IDFObject>();
  blockColNum = 0;
  lastBlock = false;
  while((str = in.readLine()) != null)
  {
   if(str.startsWith("[SCD]"))
    lastBlock = true;
   else if(str.startsWith("#") || str.startsWith("[") || str.contains("[MSI]") || str.trim().compareTo("") == 0)
    continue;
   String[] strArr = str.split("[\\t]");
   colNumber = strArr.length;
   String newCat = "";
   if(!lastBlock)
   {
    newCat = str.substring(0, str.indexOf(" "));
    if(strArr[0].startsWith("Term Source "))
     newCat = "Term Source";
   }
   else
    newCat = "end";
   if(oldCat.compareTo(newCat) != 0)
   {
    // now we will output Publication, Organization and Person objects only
    if(oldCat.compareTo("") != 0
      && blockColNum > 1
      && (oldCat.compareTo("Publication") == 0 || oldCat.compareTo("Organization") == 0 || oldCat.compareTo("Person") == 0))
    {
     out.write(oldCat + "\t");
     for(int i = 0; i < v.size(); i++)
      out.write(translate(v.get(i).attributeName, false) + "\t");
     if(oldCat.compareTo("Person") == 0 || oldCat.compareTo("Organization") == 0)
      out.write("contactOf");
     if(oldCat.compareTo("Publication") == 0)
      out.write("publicationAbout");
     out.write("\n");
     for(int j = 1; j < blockColNum; j++)
     {
      out.write("?\t");
      for(int i = 0; i < v.size(); i++)
      {
       String token = "";
       if(v.get(i).attributeValues.length > j)
        token = v.get(i).attributeValues[j];
       out.write(token + "\t");
      }
      // link to all groups
      if(groupSet.size() > 0)
      {
       out.write(groupSet.first() + "\n");
       Iterator<String> i = groupSet.iterator();
       i.next();
       while(i.hasNext())
       {
        String s = i.next();
        for(int ii = -1; ii < v.size(); ii++)
        {
         out.write("\t");
        }
        out.write(s + "\n");
       }
      }
      else
       out.write("\n");
     }
     out.write("\n");
    }
    blockColNum = colNumber;
    oldCat = newCat;
    v = new Vector<IDFObject>();
   }
   if(lastBlock)
    break;
   IDFObject idfo = new IDFObject();
   idfo.attributeName = strArr[0];
   idfo.attributeValues = strArr;
   v.add(idfo);
  }

  out.close();
 }

 // This translation business needs a better thinking through - very ad hoc at
 // the moment

 static String previousSource = ""; // we remember the previous value, so that
                                    // Term Source Refs could be translated into
                                    // qualifiers

 static String translate(String source, boolean linkedColumn)
 {

  if(source.compareTo("Term Source REF") == 0)
   return previousSource + "[Term Source]";
  if(source.compareTo("Term Source ID") == 0)
   return previousSource + "[" + source + "]";

  String toReturn = "";
  if(source.endsWith("Term Source REF") && source.length() > "Term Source REF".length())
   source = source.substring(0, source.length() - "Term Source REF".length() - 1) + "[Term Source]";
  if(source.startsWith("Person") || source.startsWith("Publication"))
   toReturn = source.substring(source.indexOf(" ") + 1);
  else if(source.startsWith("Organization") && !source.endsWith("Email"))
  {
   if(source.endsWith("[Term Source]"))
    toReturn = "Organization Roles[Term Source]";
   else
    toReturn = source;
  }
  else if(source.compareTo("Term Source Name") == 0 || source.compareTo("Database Name") == 0)
   toReturn = source.substring(0, source.lastIndexOf(" "));
  else if(source.startsWith("Term Source"))
   toReturn = source.substring("Term Source".length() + 1);
  else if(source.startsWith("Database") && !source.equals("Database REF"))
   toReturn = source.substring("Database".length() + 1);
  else if(source.compareTo("Sample Name") == 0 && linkedColumn)
   toReturn = "derivedFrom";
  else if(source.compareTo("Group Name") == 0 && linkedColumn)
   toReturn = "belongsTo";
  else if(source.endsWith("Name"))
   toReturn = "Name";
  else if(source.endsWith("Description"))
   toReturn = "Description";
  else if(source.startsWith("Characteristics") || source.startsWith("Comment"))
   toReturn = source.replace('[', '{').replace(']', '}');
  else
   toReturn = "Comment{" + source + "}";

  previousSource = toReturn;
  return toReturn;
 }
}
