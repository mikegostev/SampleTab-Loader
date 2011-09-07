package uk.ac.ebi.sampletab;

import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class STConverterOptions
{
 @Option( name = "-o", usage="Output directory")
 private String outDir;
 
 @Argument
 private List<String> dirs;

 
 public String getOutDir()
 {
  return outDir;
 }
 
 public List<String> getDirs()
 {
  return dirs;
 }

}
