package uk.ac.ebi.sampletab;

import java.io.File;

public class InFile
{
 private File file;
 private int  blockNo;

 public InFile(File file, int blockNo)
 {
  super();
  this.file = file;
  this.blockNo = blockNo;
 }
 
 public InFile()
 {}

 public File getFile()
 {
  return file;
 }

 public void setFile(File file)
 {
  this.file = file;
 }

 public int getBlockNo()
 {
  return blockNo;
 }

 public void setBlockNo(int blockNo)
 {
  this.blockNo = blockNo;
 }
}
