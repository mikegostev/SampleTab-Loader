package uk.ac.ebi.sampletab;

import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class STConverterOptions
{
 @Option( name = "-r", usage="Process recursively")
 private boolean recursive;
 
 @Argument
 private List<String> dirs;
 
 @Option( name = "-u", usage="Update files")
 private boolean update;

 @Option( name = "-o", usage="Output directory. (Default is the same as input)")
 private String outDir;

 @Option( name = "-s", usage="SampleTab file name. (Default is "+STConverter.SAMPLETAB+")")
 private String stFileName;

 @Option( name = "-l", usage="Log file", metaVar="LOGFILE")
 private String logFileName;

 @Option( name = "-f", usage="Fail log file", metaVar="LOGFILE")
 private String failedFileName;

 
 public List<String> getDirs()
 {
  return dirs;
 }

 public boolean isUpdate()
 {
  return update;
 }

 public boolean isRecursive()
 {
  return recursive;
 }

 public String getStFileName()
 {
  return stFileName!=null?stFileName:STConverter.SAMPLETAB;
 }

 public String getLogFileName()
 {
  return logFileName;
 }

 public String getFailedFileName()
 {
  return failedFileName;
 }

 public String getOutputDir()
 {
  return outDir;
 }


}
