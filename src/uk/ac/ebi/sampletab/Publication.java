package uk.ac.ebi.sampletab;

import com.pri.util.StringUtils;

public class Publication implements Comparable<Publication>
{
 private String DOI;
 private String pubMedID;

 public String getDOI()
 {
  return DOI;
 }

 public void setDOI(String dOI)
 {
  DOI = dOI;
 }

 public String getPubMedID()
 {
  return pubMedID;
 }

 public void setPubMedID(String pubMedID)
 {
  this.pubMedID = pubMedID;
 }
 

 @Override
 public int compareTo(Publication o)
 {
  int res = StringUtils.compareStrings(DOI, o.getDOI());
    
  if( res != 0 )
   return res;
 
  res = StringUtils.compareStrings(pubMedID, o.getPubMedID());
  
  if( res != 0 )
   return res;

  return 0;
 }

 public boolean equals( Object o )
 {
  return compareTo( (Publication)o ) == 0;
 }
}
