package org.example;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
          String operation = args[0];
          String filePath = args[1];

          double startTime, endTime;
          if(operation.equals("c")){
              int n = Integer.parseInt(args[2]);
              FileCompression fileCompression = new FileCompression();

              startTime = System.currentTimeMillis();
              fileCompression.compress(filePath, n);
              endTime = System.currentTimeMillis();

              System.out.println("Time for compression: " + (endTime - startTime) / 1000.0 + " sec");
              File file = new File(filePath);

              double originalFileLength = file.length();

              Path path = Paths.get(filePath);

              String directoryPath = path.getParent().toString();
              String newFileName = "21011064."+ n + "." + path.getFileName().toString() + ".hc";

              String newPath = Paths.get(directoryPath, newFileName).toString();

              file = new File(newPath);

              double compressedFileLength = file.length();

              System.out.println("Compression ratio is: "+ compressedFileLength / originalFileLength);
          } else if (operation.equals("d")) {
              FileDecompression fileDecompression = new FileDecompression();

              startTime = System.currentTimeMillis();
              fileDecompression.decompress(filePath);
              endTime = System.currentTimeMillis();

              System.out.println("Time for decompression: " + (endTime - startTime) / 1000 + " sec");
          }
    }
}