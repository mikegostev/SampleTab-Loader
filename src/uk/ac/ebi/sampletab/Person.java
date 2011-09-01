package uk.ac.ebi.sampletab;

import com.pri.util.StringUtils;

public class Person implements Comparable<Person>
{
 private String lastName;
 private String firstName;
 private String midInitials;

 private String email;
 private String role;

 public String getLastName()
 {
  return lastName;
 }

 public void setLastName(String lastName)
 {
  this.lastName = lastName;
 }

 public String getFirstName()
 {
  return firstName;
 }

 public void setFirstName(String firstName)
 {
  this.firstName = firstName;
 }

 public String getMidInitials()
 {
  return midInitials;
 }

 public void setMidInitials(String midInitials)
 {
  this.midInitials = midInitials;
 }

 public String getEmail()
 {
  return email;
 }

 public void setEmail(String email)
 {
  this.email = email;
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
 public int compareTo(Person o)
 {
  int res = StringUtils.compareStrings(lastName, o.getLastName());
    
  if( res != 0 )
   return res;
 
  res = StringUtils.compareStrings(firstName, o.getFirstName());
  
  if( res != 0 )
   return res;

  res = StringUtils.compareStrings(midInitials, o.getMidInitials());
  
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
  return compareTo( (Person)o ) == 0;
 }
}
