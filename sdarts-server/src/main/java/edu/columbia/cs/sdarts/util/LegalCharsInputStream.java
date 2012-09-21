package edu.columbia.cs.sdarts.util;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * LegalCharsInputStream.java
 * Filters out all characters not found on a regular keyboard
 * @author <a href="mailto:ss1792@cs.columbia.edu">Sergey Sigelman</a>
 */


public class LegalCharsInputStream extends FilterInputStream
{
 private static final String legalChars = 
" abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789`~!@#$%^&*()-_=+\\|,<.>/?;:'\"[{]}\n\r\t";

 public static void main(String args[]) throws Exception
 {
  BufferedReader br = new BufferedReader(new InputStreamReader(new LegalCharsInputStream(new FileInputStream(args[0]))));
  String line;
  while((line=br.readLine())!=null)
   System.out.println(line);
 }

 public LegalCharsInputStream(InputStream isr){super(isr);}

 public int read() throws IOException
 {
  int c;
  do
  {
   c = super.read();
   if(legalChars.indexOf(c)!=-1)
    return c;
  } while(c!=-1);
  return -1;
 }

 public int read(byte[] b, int off, int len) throws IOException
 {
  int c,i=0;
  while(i<len)
  {
   c = super.read();
   if(legalChars.indexOf(c)!=-1)
   {
    b[off+i]=(byte)c;
    i++;
   }
   else if(c==-1){return (i==0)? -1 : i;} 
  }
  return i;
 }
}

