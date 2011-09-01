package uk.ac.ebi.sampletab;

public interface ValueExtractor
{
 String getHeader();
 
 void setSample( Sample obj );
 
 String extract();
 
 boolean hasValue();
}
