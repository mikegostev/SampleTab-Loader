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

  try
  {
   log = new Log(new PrintWriter(new File(outDir, ".log")));
  }
  catch(FileNotFoundException e1)
  {
   System.err.println("Can't create log file: " + new File(outDir, ".log").getAbsolutePath());
   return;
  }

  int nCores = Runtime.getRuntime().availableProcessors();

  log.write("Starting " + nCores + " thread" + (nCores > 1 ? "s" : ""));

  ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

  for(int i = 0; i < nCores; i++)
  {

   final int thrdNum = i + 1;

   exec.execute(new Runnable()
   {

    @Override
    public void run()
    {
     Thread.currentThread().setName("Th" + thrdNum);

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
        System.out.println("ERROR. See log file for details");
        log.write("File parsing error: " + e.getMessage());
        e.printStackTrace();
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

       File subOutDir = new File(outDir, sbmId);

       if(!subOutDir.isDirectory() && !subOutDir.mkdir())
       {
        log.write("ERROR: Can't create output directory: " + subOutDir.getAbsolutePath());
        System.exit(1);
       }

       FileOutputStream fos = new FileOutputStream(new File(subOutDir, f.getName() + ".age.txt"));

       try
       {
        ATWriter.writeAgeTab(s, fos);

        fos.close();
       }
       catch(IOException e)
       {
       }

       try
       {
        PrintWriter stOut = new PrintWriter(new File(subOutDir, f.getName()), "UTF-8");
        stOut.write(stContent);
        stOut.close();
       }
       catch(UnsupportedEncodingException e)
       {
        // TODO Auto-generated catch block
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
   });
  }
  
  try
  {
   exec.shutdown();
   
   exec.awaitTermination(24, TimeUnit.HOURS);
  }
  catch(InterruptedException e)
  {
  }
  
  log.shutdown();
 }

 static class Log
 {
  private PrintWriter log;
  private Lock        lock = new ReentrantLock();

  Log(PrintWriter l)
  {
   log = l;
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
    log.println("[" + Thread.currentThread().getName() + "] " + msg);
   }
   finally
   {
    lock.unlock();
   }
  }
 }

}
