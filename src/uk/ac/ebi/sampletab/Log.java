package uk.ac.ebi.sampletab;

public interface Log
{
 void shutdown();
 void write(String msg);
 void printStackTrace(Exception e);
}
