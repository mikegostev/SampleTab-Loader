package uk.ac.ebi.sampletab;

import com.pri.util.StringUtils;

public class Organization implements Comparable<Organization>
{
 private String name;
 private String address;
 private String uri;
 private String email;
 private String role;

 public String getName()
 {
  return name;
 }

 public void setName(String name)
 {
  this.name = name;
 }

 public String getAddress()
 {
  return address;
 }

 public void setAddress(String address)
 {
  this.address = address;
 }

 public String getUri()
 {
  return uri;
 }

 public void setUri(String uri)
 {
  this.uri = uri;
 }

 public String getEmail()
 {
  return email;
 }

 public void setEmail(String emale)
 {
  this.email = emale;
 }

 public String getRole()
 {
  return role;
 }

 public void setRole(String role)
 {
  this.role = role;
 }


 @Override
 public int compareTo(Organization o)
 {
  int res = StringUtils.compareStrings(name, o.getName());
    
  if( res != 0 )
   return res;
 
  res = StringUtils.compareStrings(address, o.getAddress());
  
  if( res != 0 )
   return res;

  res = StringUtils.compareStrings(uri, o.getUri());
  
  if( res != 0 )
   return res;

  res = StringUtils.compareStrings(email, o.getEmail());
  
  if( res != 0 )
   return res;

  res = StringUtils.compareStrings(role, o.getRole());
  
  if( res != 0 )
   return res;

  return 0;
 }

 public boolean equals( Object o )
 {
  return compareTo( (Organization)o ) == 0;
 }
}
