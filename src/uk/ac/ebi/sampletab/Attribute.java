package uk.ac.ebi.sampletab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class Attribute extends AnnotatedObject
{
 private String name; 
 private List<Attribute> vals;
 private int order;
 
 public Attribute( String name, String val, int ord )
 {
  setID(val);
  
  this.name = name;
  order=ord;
 }
 
 public Attribute addValue( String v, int ord )
 {
  if( vals == null )
  {
   vals = new ArrayList<Attribute>();
   
   Attribute thisAt = new Attribute(getName(), super.getID(), getOrder());
   
   vals.add(thisAt);
  }
  
  Attribute nattr = new Attribute(getName(), v, ord);
  
  vals.add(nattr);
  
  return nattr;
 }
 
// public String getID()
// {
//  if( vals == null )
//   return super.getID();
//  
//  return vals.get(0).getID();
// }
 
 public List<Attribute> getValues()
 {
  if( vals != null )
   return vals;
   
  return vals=Collections.singletonList( this );
 }
 
 public int getValuesNumber()
 {
  return vals == null? 1: vals.size();
 }

 public String getName()
 {
  return name;
 }

// public void setName(String name)
// {
//  this.name = name;
// }
 
 public String toString()
 {
  if( vals == null )
   return getID();
  
  StringBuilder sb = new StringBuilder();
  
  sb.append('[');
  
  for( Attribute a : vals)
   sb.append(a.getID()).append(',');

  sb.setCharAt(sb.length()-1, ']');
  
  return sb.toString();
 }

 public int getOrder()
 {
  return order;
 }

 public void setOrder(int order)
 {
  this.order = order;
 }
 
 public boolean equals( Object o )
 {
  Attribute othat = (Attribute) o;
  
//  if( getValuesNumber() != othat.getValuesNumber() )
//   return false;
  
  if( getValuesNumber() == 1  )
  {
   if( othat.getValuesNumber() == 1 )
     return singleEquals(othat);
   else
    return cmpSingleMultiple(this, othat.getValues() );
  }
  else
  {
   if( othat.getValuesNumber() == 1 )
    return cmpSingleMultiple( othat, this.getValues() );
   else
    return cmpMultipleMultiple( this.getValues(), othat.getValues() );
  }

 }

 private boolean singleEquals(  Attribute a2 )
 {
  if( getID() == null || getID().length() == 0 )
   return a2.getID() == null || a2.getID().length() == 0;
  else
   return getID().equals( a2.getID() ) && super.equals(a2);
 }
 
 private static boolean cmpSingleMultiple(  Attribute a1, Collection<Attribute> mul )
 {
  boolean found = false;
  
  for( Attribute a : mul )
  {
   if( a.isEmpty() )
    continue;
   
   if( a1.singleEquals(a) )
   {
    if( found )
     return false;
    
    found = true;
   }
   return false;
  }
  
  return found;
 }

 private static boolean cmpMultipleMultiple(  Collection<Attribute> mul1, Collection<Attribute> mul2 )
 {
  List<Attribute> lst1 = new ArrayList<Attribute>( mul1.size() );
  lst1.addAll(mul1);

  List<Attribute> lst2 = new ArrayList<Attribute>( mul2.size() );
  lst2.addAll(mul2);
  
  Comparator<Attribute> cmp = new Comparator<Attribute>(){

   @Override
   public int compare(Attribute o1, Attribute o2)
   {
    if( o1.isEmpty() )
    {
     if( o2.isEmpty() )
      return 0;
     else
      return -1;
    }
    else
    {
     if( o2.isEmpty() )
      return 1;
     else
      return o1.getID().compareTo(o2.getID());
    }
   }};
   
   Collections.sort(lst1, cmp);
   Collections.sort(lst2, cmp);
   
   int ptr1 = 0;
   int ptr2 = 0;
   
   for(; ptr1 < lst1.size() && lst1.get(ptr1).isEmpty(); ptr1++);

   for(; ptr2 < lst2.size() && lst2.get(ptr2).isEmpty(); ptr2++);
   
   if( (lst1.size() - ptr1) != (lst2.size() - ptr2) )
    return false;
  
   for(; ptr1 < lst1.size(); ptr1++)
    if( ! lst1.get(ptr1).singleEquals( lst2.get(ptr2++) ))
     return false;
   
   return true;

}

 
 public boolean isEmpty()
 {
  if( vals == null )
  {
   if( getID() == null || getID().length() == 0 )
    return true;
   else
    return false;
  }
  
  for( Attribute a : vals )
  {
   if( ! a.isEmpty() )
    return false;
  }
  
  return true;
 }
}

