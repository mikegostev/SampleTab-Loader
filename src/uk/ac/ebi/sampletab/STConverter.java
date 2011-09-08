package uk.ac.ebi.sampletab;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Queue;
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
 static final String        usage = "java -jar STConverter.jar -o outdir <input file/dir> [ ... <input file/dir> ]";

 static private Queue<File> infiles;

 public static void main(String[] args)
 {
  STConverterOptions options = new STConverterOptions();
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

  infiles = new LinkedBlockingQueue<File>();

  for(String outf : options.getDirs())
  {
   File in = new File(outf);

   if(in.isDirectory())
    infiles.addAll(Arrays.asList(in.listFiles()));
   else if(in.isFile())
    infiles.add(in);
   else
   {
    System.err.println("Input file/directory '" + outf + "' doesn't exist");
    return;
   }
  }

  if(infiles.size() == 0)
  {
   System.err.println("No files to process");
   return;
  }

  if(options.getOutDir() == null)
  {
   System.err.println("Output directory is not specified");
   return;
  }

  final File outDir = new File(options.getOutDir());

  if(outDir.isFile())
  {
   System.err.println("Output path should point to a directory");
   return;
  }

  if(!outDir.exists() && !outDir.mkdirs())
  {
   System.err.println("Can't create output directory");
   return;
  }

  final Log log;
  final Log failedLog;

  try
  {
   log = new Log(new PrintWriter(new File(outDir, "log.txt")), true);
  }
  catch(FileNotFoundException e1)
  {
   System.err.println("Can't create log file: " + new File(outDir, "log.txt").getAbsolutePath());
   return;
  }

  try
  {
   failedLog = new Log(new PrintWriter(new File(outDir, "failed.txt")),false);
  }
  catch(FileNotFoundException e1)
  {
   System.err.println("Can't create log file: " + new File(outDir, "failed.txt").getAbsolutePath());
   return;
  }

  if( infiles.size() == 1 )
   new Converter("Main", outDir, log, failedLog).run();
  else
  {
   int nTheads = Runtime.getRuntime().availableProcessors();
   
   if( infiles.size() < nTheads )
    nTheads = infiles.size();
   
   log.write("Starting " + nTheads + " threads");
   
   ExecutorService exec = Executors.newFixedThreadPool(nTheads);
   
   for(int i = 1; i <= nTheads; i++)
    exec.execute(new Converter("Thr"+i, outDir, log, failedLog));
   
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
  
  private File outDir;

  public Converter(String threadName, File outDir, Log log, Log failedLog)
  {
   super();
   this.threadName = threadName;
   this.log = log;
   this.failedLog = failedLog;
   this.outDir = outDir;
  }

  @Override
  public void run()
  {
   Thread.currentThread().setName(threadName);

   File f;

   while((f = infiles.poll()) != null)
   {
    try
    {

     Submission s = null;

     long time = System.currentTimeMillis();

     System.out.println("Parsing file: " + f);
     log.write("Parsing file: " + f);

     String stContent = null;
     try
     {
      stContent = StringUtils.readUnicodeFile(f);
      s = STParser3.readST(stContent);
     }
     catch(Exception e)
     {
      failedLog.write(f.getAbsolutePath());
      
      System.out.println("ERROR. See log file for details");
      log.write("ERROR: File parsing error: " + e.getMessage());
      continue;
     }

     String sbmId = s.getAnnotation(Definitions.SUBMISSIONIDENTIFIER).getValue();

     if(sbmId == null)
     {
      log.write("ERROR: Can't retrieve submission identifier");
      continue;
     }

     log.write("Parsing success. " + (System.currentTimeMillis() - time) + "ms");

     log.write("Converting to AGE-TAB");
     time=System.currentTimeMillis();
     
     File subOutDir = new File(outDir, sbmId);

     if(!subOutDir.isDirectory() && !subOutDir.mkdir())
     {
      log.write("ERROR: Can't create output directory: " + subOutDir.getAbsolutePath());
      System.exit(1);
     }

     File ageFile = new File(subOutDir, f.getName() + ".age.txt");
     
     FileOutputStream fos = new FileOutputStream(ageFile);

     try
     {
      ATWriter.writeAgeTab(s, fos);

      fos.close();
     }
     catch(IOException e)
     {
     }
     
     log.write("Converting success. " + (System.currentTimeMillis() - time) + "ms");

     try
     {
      PrintWriter stOut = new PrintWriter(new File(subOutDir, f.getName()), "UTF-8");
      stOut.write(stContent);
      stOut.close();
     }
     catch(UnsupportedEncodingException e)
     {
      e.printStackTrace();
     }

    }
    catch(IOException e)
    {
     log.write("ERROR: IOException. "+e.getMessage());
    }
    catch (Exception e) 
    {
     log.write("ERROR: Unknown Exception. "+e.getClass().getName()+" "+e.getMessage());
    }
   }
  }  
 }
 
 static class Log
 {
  private PrintWriter log;
  private Lock        lock = new ReentrantLock();
  
  private boolean showThreads;

  Log(PrintWriter l, boolean th)
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
 }

}
