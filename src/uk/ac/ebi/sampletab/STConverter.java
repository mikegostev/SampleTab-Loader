package uk.ac.ebi.sampletab;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import com.pri.util.StringUtils;

public class STConverter
{
 static final String SAMPLETAB = "sampletab.toload.txt";
 
 static class InputFiles
 {
  File dir;
  File sampletab;
 }
 
 static final String        usage = "java -jar STConverter.jar <input file/dir> [ ... <input file/dir> ]";

 static private Queue<InputFiles> infiles;
 static private STConverterOptions options;
 
 static Log stdout = new PrintStreamLog(System.out, false);
 
 public static void main(String[] args)
 {
  options = new STConverterOptions();
  CmdLineParser parser = new CmdLineParser(options);

  try
  {
   parser.parseArgument(args);
  }
  catch(CmdLineException e)
  {
   System.err.println(e.getMessage());
   System.err.println(usage);
   parser.printUsage(System.err);
   return;
  }

  if(options.getDirs() == null || options.getDirs().size() == 0)
  {
   System.err.println(usage);
   parser.printUsage(System.err);
   return;
  }

  infiles = new LinkedBlockingQueue<InputFiles>();


  
  Set<String> processedDirs = new HashSet<String>();
  
  for(String outf : options.getDirs())
  {
   File in = new File(outf);

   if( ! in.exists() )
   {
    System.err.println("Input directory '" + outf + "' doesn't exist");
    System.exit(1);
   }
   else if( ! in.isDirectory() )
   {
    System.err.println("'" + outf + "' is not a directory");
    System.exit(1);
   }
   
   collectInput( in, infiles, processedDirs ) ;

  }

  if(infiles.size() == 0)
  {
   System.err.println("No files to process");
   return;
  }
  
  File outDir=null;
  
  if( options.getOutputDir() != null )
  {
   if( options.getDirs().size() != 1 )
   {
    System.err.println("Only one input directory is allowed if output directory specified");
    System.exit(1);
   }
   
   outDir = new File( options.getOutputDir() );
   
   if( outDir.isFile() )
   {
    System.err.println("Output path should point to some directory");
    System.exit(1);
   }
   
   if( ! outDir.exists() && ! outDir.mkdirs() )
   {
    System.err.println("Can't create output direcory");
    System.exit(1);
   }
  }
//  else
//   outDir = new File( options.getDirs().get(0) );

//  if(options.getOutDir() == null)
//  {
//   System.err.println("Output directory is not specified");
//   return;
//  }

//  final File outDir = new File(options.getOutDir());

//  if(outDir.isFile())
//  {
//   System.err.println("Output path should point to a directory");
//   return;
//  }
//
//  if(!outDir.exists() && !outDir.mkdirs())
//  {
//   System.err.println("Can't create output directory");
//   return;
//  }

  final Log log;
  final Log failedLog;

  try
  {
   if( options.getLogFileName() != null )
    log = new PrintStreamLog(new PrintStream( new File(options.getLogFileName()) ), true);
   else
    log = new PrintStreamLog( System.err , true);
  }
  catch(IOException e1)
  {
   System.err.println("Can't create log file: " + options.getLogFileName());
   return;
  }

  try
  {
   if( options.getFailedFileName() != null )
    failedLog = new PrintStreamLog(new PrintStream( new File(options.getFailedFileName()) ), false);
   else
    failedLog = new NullLog();
  }
  catch(IOException e1)
  {
   System.err.println("Can't create failed log file: " + options.getFailedFileName());
   return;
  }

  if( infiles.size() == 1 )
   new Converter(outDir!=null?new File(options.getDirs().get(0)):null, outDir, "Main", log, failedLog).run();
  else
  {
   int nTheads = Runtime.getRuntime().availableProcessors();
   
   if( infiles.size() < nTheads )
    nTheads = infiles.size();
   
   log.write("Starting " + nTheads + " threads");
   
   ExecutorService exec = Executors.newFixedThreadPool(nTheads);
   
   File bDir = outDir!=null?new File(options.getDirs().get(0)):null;
   
   for(int i = 1; i <= nTheads; i++)
    exec.execute(new Converter(bDir,outDir,"Thr"+i, log, failedLog));
   
   try
   {
    exec.shutdown();
    
    exec.awaitTermination(72, TimeUnit.HOURS);
   }
   catch(InterruptedException e)
   {
   }
  }
  
  
  log.shutdown();
  failedLog.shutdown();
 }

 static class Converter implements Runnable
 {
  private String threadName;
  
  private Log log;
  private Log failedLog;
  
  File baseDir;
  File outDir;
  
  
  private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
  
  public Converter(File bDir, File oDir, String threadName, Log log, Log failedLog)
  {
   super();
   this.threadName = threadName;
   this.log = log;
   this.failedLog = failedLog;
   
   baseDir = bDir;
   outDir = oDir;
  }

  @Override
  public void run()
  {
   Thread.currentThread().setName(threadName);

   InputFiles f;

   while((f = infiles.poll()) != null)
   {
    String dirName = " ("+f.dir.getName()+")";

    try
    {

     Submission s = null;

     long time = System.currentTimeMillis();

     File subOutDir;
     
     if( outDir != null )
     {
      String relPath = f.dir.getAbsolutePath().substring( baseDir.getAbsolutePath().length() );
      
      if( relPath.startsWith("/") )
       relPath=relPath.substring(1);
      
      subOutDir = new File(outDir, relPath+"/age");
       
     }
     else
      subOutDir = new File(f.dir, "age");
     
     if( !subOutDir.isDirectory() && !subOutDir.mkdirs() )
     {
      log.write("ERROR: Can't create output directory: " + subOutDir.getAbsolutePath()+dirName);
      System.exit(1);
     }

     File ageFile = new File(subOutDir, f.dir.getName() + ".age.txt");

     if( options.isUpdate() && ageFile.exists() && ageFile.lastModified() >= f.sampletab.lastModified() )
     {
      log.write("File '"+ageFile+"' is up-to-date"+dirName);
      continue;
     }
     
     //     System.out.println("Parsing file: " + f);
     log.write("Parsing file: " + f.sampletab.getAbsolutePath());
    

     String stContent = null;
     try
     {
      stContent = StringUtils.readUnicodeFile(f.sampletab);
      
      int l = stContent.length();
      int p=0;
      
      for( p=0; p < l; p++ )
       if( stContent.charAt(p) == '[' )
        break;
      
      if( p == l )
       throw new STParseException("No [MSI] section"+dirName);
      else if( p > 0 )
       stContent=stContent.substring(p);
       
      s = STParser3.readST(stContent);
     }
     catch(Exception e)
     {
      failedLog.write(f.dir.getAbsolutePath());
      
      log.write("ERROR: File parsing error: " + e.getMessage()+dirName);
      log.printStackTrace( e );
      continue;
     }

     String sbmId = s.getAnnotation(Definitions.SUBMISSIONIDENTIFIER).getValue();

     if(sbmId == null)
     {
      log.write("ERROR: Can't retrieve submission identifier"+dirName);
      failedLog.write(f.dir.getAbsolutePath());
      continue;
     }
     
     
     log.write("Parsing success. " + (System.currentTimeMillis() - time) + "ms"+dirName);

     log.write("Converting to AGE-TAB"+dirName);
     time=System.currentTimeMillis();
     
     
     FileOutputStream fos = new FileOutputStream(ageFile);

     ATWriter.writeAgeTab(s, fos);

     fos.close();
     
     log.write("Converting success. " + (System.currentTimeMillis() - time) + "ms"+dirName);

      PrintWriter stOut = new PrintWriter(new File(subOutDir, "source.sampletab.txt"), "UTF-8");
      stOut.write(stContent);
      stOut.close();

     
     File idFile = new File(subOutDir,".id");
     PrintWriter metaOut = new PrintWriter( idFile, "UTF-8" );
     metaOut.print(sbmId);
     metaOut.close();

     idFile = new File(subOutDir,".id."+ageFile.getName());
     metaOut = new PrintWriter( idFile, "UTF-8" );
     metaOut.print(sbmId+":module1");
     metaOut.close();


     String descr = s.getAnnotation(Definitions.SUBMISSIONDESCRIPTION).getValue();

     if(descr == null)
      descr = "Submission "+sbmId;

     File descFile = new File(subOutDir,".description");
     metaOut = new PrintWriter( descFile, "UTF-8" );
     metaOut.print(descr);
     metaOut.close();

     
     File modDescFile = new File(subOutDir,".description."+ageFile.getName());
     metaOut = new PrintWriter( modDescFile, "UTF-8" );
     metaOut.print("Data module for submisson '"+sbmId+"'. Converted from Sample-Tab at "+dateFormat.format(new Date()));
     metaOut.close();

     modDescFile = new File(subOutDir,".description."+f.sampletab.getName());
     metaOut = new PrintWriter( modDescFile, "UTF-8" );
     metaOut.print("Sample-Tab file'. Last mofified at "+dateFormat.format( new Date(f.sampletab.lastModified())));
     metaOut.close();

     stdout.write(f.dir.getAbsolutePath());
    }
    catch(IOException e)
    {
     log.write("ERROR: IOException. "+e.getMessage()+dirName);
     failedLog.write(f.sampletab.getAbsolutePath());

     log.printStackTrace( e );
    }
    catch (Exception e) 
    {
     log.write("ERROR: Unknown Exception. "+e.getClass().getName()+" "+e.getMessage()+dirName);
     failedLog.write(f.sampletab.getAbsolutePath());
     
     log.printStackTrace( e );
    }
   }
  }  
 }
 
 static void collectInput( File in, Collection<InputFiles> infiles, Set<String> processedDirs )
 {
  File[] files = in.listFiles();
  
  for( File f : files )
  {
   if( f.isDirectory() && ! processedDirs.contains(f.getAbsolutePath()) && options.isRecursive() )
   {
    collectInput(f, infiles, processedDirs);
    processedDirs.add(f.getAbsolutePath());
   }
   else if( options.getStFileName().equals(f.getName()) && f.isFile() )
   {
    InputFiles ifls = new InputFiles();
    
    ifls.dir = in;
    ifls.sampletab = f;
    
    infiles.add( ifls );
   }
    
  }
 }
 
 
 
 static class NullLog implements Log
 {
  @Override
  public void shutdown()
  {
  }

  @Override
  public void write(String msg)
  {
  }

  @Override
  public void printStackTrace(Exception e)
  {
  }
 }
 
 static class PrintStreamLog implements Log
 {
  private PrintStream log;
  private Lock        lock = new ReentrantLock();
  
  private boolean showThreads;

  PrintStreamLog(PrintStream l, boolean th)
  {
   log = l;
   showThreads = th;
  }

  public void shutdown()
  {
   log.close();
  }

  public void write(String msg)
  {
   lock.lock();

   try
   {
    if( showThreads )
     log.print("[" + Thread.currentThread().getName() + "] ");
    
    log.println(msg);
   }
   finally
   {
    lock.unlock();
   }
  }
  
  @Override
  public void printStackTrace(Exception e)
  {
   lock.lock();

   try
   {
    e.printStackTrace(log);
   }
   finally
   {
    lock.unlock();
   }
  }

 }

}
