package uk.ac.ebi.sampletab;

import java.util.ArrayList;
import java.util.List;

public class ContainerObjectExact extends AnnotatedObject
{
 private List<TermSource>   termSources;
 private List<Person>       persons;
 private List<Organization> organizations;
 private List<Database>     databases;

 public TermSource addTermSource( TermSource ts )
 {
  if( termSources == null )
  {
   termSources = new ArrayList<TermSource>();
   
   termSources.add(ts);
   
   return ts;
  }
  
  for( TermSource ets : termSources )
   if( ets.equals( ts ) )
    return ets;
  
  termSources.add(ts);
  
  return ts;
 }

 
 public List<TermSource> getTermSources()
 {
  return termSources;
 }

 
 public Person addPerson( Person ts )
 {
  if( persons == null )
  {
   persons = new ArrayList<Person>();
   
   persons.add(ts);
   
   return ts;
  }
  
  for( Person ets : persons )
   if( ets.equals( ts ) )
    return ets;
  
  persons.add(ts);
  
  return ts;
 }

 public List<Person> getPersons()
 {
  return persons;
 }

 public Organization addOrganization( Organization ts )
 {
  if( organizations == null )
  {
   organizations = new ArrayList<Organization>();
   
   organizations.add(ts);
   
   return ts;
  }
  
  for( Organization ets : organizations )
   if( ets.equals( ts ) )
    return ets;
  
  organizations.add(ts);
  
  return ts;
 }

 public List<Organization> getOrganizations()
 {
  return organizations;
 }

 public Database addOrganization( Database ts )
 {
  if( databases == null )
  {
   databases = new ArrayList<Database>();
   
   databases.add(ts);
   
   return ts;
  }
  
  for( Database ets : databases )
   if( ets.equals( ts ) )
    return ets;
  
  databases.add(ts);
  
  return ts;
 }
 
 public List<Database> getDatabases()
 {
  return databases;
 }
}
