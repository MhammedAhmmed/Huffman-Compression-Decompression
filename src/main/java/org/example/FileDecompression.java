package org.example;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FileDecompression {

    Map<String, String> codeword = new HashMap<>();
    private int paddingBits;
    private byte[] byteArray;
    private int bytePointer = 0;
    private final int threshold = 800000000; // Write byteArray in the file if number of bytes in it larger than the threshold

    public FileDecompression(){
    }

    public void decompress(String inputPath) throws IOException {

        try (FileInputStream fis = new FileInputStream(inputPath)) {

            // Read the size of the map (4 bytes)
            byte[] sizeBytes = new byte[4];
            fis.read(sizeBytes);
            int mapSize = bytesToInt(sizeBytes);

            paddingBits = fis.read();

            // Read each key-value pair
            for (int i = 0; i < mapSize; i++) {
                String key = readString(fis);  // Read the key
                String value = readString(fis);  // Read the value
                codeword.put(value, key);
            }

            int size = 1000000000;
            byteArray = new byte[size];

            Path path = Paths.get(inputPath);

            String directoryPath = path.getParent().toString();
            String fileName = path.getFileName().toString();
            String newFileName = "extracted." + fileName.substring(0, fileName.length() - 3); // Add extracted. and remove .hc

            String newPath = Paths.get(directoryPath, newFileName).toString();

            saveFile(fis, newPath);
        }
    }

    private void saveFile(FileInputStream fis, String filePath){
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            byte[] chunk = new byte[102400]; // Chunk size
            StringBuilder temp = new StringBuilder();
            StringBuilder chunkBinary = new StringBuilder();

            int bytesRead;
            StringBuilder binaryString = new StringBuilder(8);
            while ((bytesRead = fis.read(chunk)) != -1) {
                for (int i = 0; i < bytesRead; i++) {
                    binaryString.setLength(0);
                    for(int j = 0; j < 8; j++){ // Convert byte to binary string ex -> "01010011"
                        binaryString.append((chunk[i] & (1 << (7 - j))) == 0 ? '0' : '1');
                    }
                    chunkBinary.append(binaryString);
                }

                int chunkLen = chunkBinary.length();
                int p = -1;
                for (int i = 0; i < chunkLen - paddingBits; i++) {
                    temp.append(chunkBinary.charAt(i));
                    if (codeword.containsKey(temp.toString())) {
                        saveBytes(codeword.get(temp.toString()));
                        temp.setLength(0);
                        p = i;
                    }
                }
                if(bytePointer > threshold){ // Save each 800000000 byte
                    fos.write(byteArray, 0, bytePointer);
                    bytePointer = 0;
                }
                temp.setLength(0);
                chunkBinary.delete(0, p + 1);
            }
            fos.write(byteArray, 0, bytePointer);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void saveBytes(String key) {
        for (int i = 0; i < key.length(); i++) {
            byteArray[bytePointer++] = (byte) key.charAt(i);  // Convert to byte (assuming char values)
        }
    }

    // Helper method to read a string (assuming length is stored in 4 bytes before the string)
    private String readString(InputStream is) throws IOException {
        byte[] lengthBytes = new byte[4];
        is.read(lengthBytes);  // Read the 4 bytes representing the length
        int length = bytesToInt(lengthBytes);

        byte[] stringBytes = new byte[length];
        is.read(stringBytes);  // Read the string data
        return new String(stringBytes);
    }

    // Helper method to convert 4 bytes to an integer
    private int bytesToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) | ((bytes[1] & 0xFF) << 16) | ((bytes[2] & 0xFF) << 8) | (bytes[3] & 0xFF);
    }
}