package org.example;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class FileCompression {
    private final HuffmanCode huffmanCode;
    private Map<String, String> codeword;
    private byte[] dataBytes;
    private int dataPointer = 0;
    private final int threshold = 800000000; // Write in file evey 1 billion bytes
    private int[] byteFrequencies;



    public FileCompression() {
        huffmanCode = new HuffmanCode();
    }

    public void compress(String filepath, int n) throws IOException {
        Map<String, Integer> frequencies;
        if (n == 1){
            byteFrequencies = new int[256];
            frequencies = generateFrequenciesArray(filepath);
        }else{
            frequencies = generateFrequencies(filepath, n);
        }

        this.codeword = huffmanCode.generateCodeword(frequencies);

        int maxSize = 1000000000;
        dataBytes = new byte[maxSize];

        Path path = Paths.get(filepath);

        String directoryPath = path.getParent().toString();
        String newFileName = "21011064."+ n + "." + path.getFileName().toString() + ".hc";

        String newPath = Paths.get(directoryPath, newFileName).toString();
        saveBinaryDataWithMetadata(n, filepath, newPath);
    }

    private Map<String, Integer> generateFrequenciesArray(String filePath) throws IOException {
        int chunkSize = 1024000;

        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[chunkSize];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                processFrequenciesArray(buffer, bytesRead);
            }

            Map<String, Integer> frequencies = new HashMap<>();
            for (int i = 0; i < 256; i++){
                if(byteFrequencies[i] != 0){
                    frequencies.put(Character.toString((char) (byte)i), byteFrequencies[i]);
                }
            }
            return frequencies;
        }
    }

    private void processFrequenciesArray(byte[] bytes, int bytesRead) {
        for (int i = 0; i < bytesRead; i ++) {
            byteFrequencies[bytes[i] & 0xFF]++;
        }
    }

    private Map<String, Integer> generateFrequencies(String filePath, int n) throws IOException {
        int chunkSize = 102400; // Chunk size
        chunkSize = n > chunkSize ? n : chunkSize - (chunkSize % n); // Make chunk size divisible by n

        Map<String, Integer> frequencies = new HashMap<>();
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[chunkSize];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                processFrequencies(buffer, n, bytesRead, frequencies);
            }
        }
        return frequencies;
    }
    // Fill the frequencies map
    private void processFrequencies(byte[] bytes, int n, int bytesRead, Map<String, Integer> frequencies) {
        StringBuilder block = new StringBuilder();

        for (int i = 0; i < bytesRead; i += n) {
            int len = Math.min(i + n, bytesRead);
            block.setLength(0);
            for(int j = i; j < len; j++){
                block.append((char)bytes[j]);
            }
            frequencies.compute(block.toString(), (key, value) -> value == null ? 1 : value + 1);
        }
    }


    private void saveBinaryDataWithMetadata(int n, String inputPath, String outputPath) throws IOException {

        try (FileOutputStream fos = new FileOutputStream(outputPath)) {

            fos.write(intToBytes(codeword.size()));  // 4 bytes for the map size

            int paddingLength = 0;
            fos.write(paddingLength);  // 1 byte for the padding information, initially equal 0 then it will be overwritten.

            for (Map.Entry<String, String> entry : codeword.entrySet()) {
                writeString(fos, entry.getKey());  // Write the key
                writeString(fos, entry.getValue());  // Write the value
            }

            int chunkSize = 102400; // chunk size
            chunkSize = n > chunkSize ? n : chunkSize - (chunkSize % n); // Make chunk size divisible by n

            StringBuilder binaryString = new StringBuilder();
            try (FileInputStream fis = new FileInputStream(inputPath)) {
                byte[] buffer = new byte[chunkSize];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    generateBinaryString(buffer, n, bytesRead, binaryString);

                    saveBytes(binaryString);

                    int paddingBits = binaryString.length() % 8;
                    binaryString.delete(0, binaryString.length() - paddingBits);

                    if(dataPointer > threshold){ // // Save each 800000000 byte
                        fos.write(dataBytes, 0, dataPointer);
                        dataPointer = 0;
                    }
                }
                fos.write(dataBytes, 0, dataPointer);
            }



            if (binaryString.length() != 0) {
                paddingLength = (8 - binaryString.length()) % 8;

                binaryString.append("0".repeat(Math.max(0, paddingLength))); // Add the padding bits

                byte paddingByte = (byte) Integer.parseInt(binaryString.toString(), 2);  // Convert binary string to byte
                fos.write(paddingByte);

                try (RandomAccessFile raf = new RandomAccessFile(outputPath, "rw")) { // overwrite  paddingLength which is the 5th byte in the file
                    raf.seek(4);
                    raf.write((byte) paddingLength);
                }
            }
        }
    }
    // Method that save the bytes of binary string to be written in the file later
    private void saveBytes(StringBuilder binaryStr) {
        int len = binaryStr.length();
        int bytesLen = len / 8;

        for (int i = 0; i < bytesLen; i++) {
            String byteStr = binaryStr.substring(i * 8, (i + 1) * 8);  // Extract each 8-bit chunk
            dataBytes[dataPointer++] = (byte) Integer.parseInt(byteStr, 2);
        }
    }
    // Method that convert bytes to binary string -> ("010010110110")
    private void generateBinaryString(byte[] bytes, int n, int bytesRead, StringBuilder binaryString) {
        StringBuilder block = new StringBuilder();
        for (int i = 0; i < bytesRead; i += n) {
            int len = Math.min(i + n, bytesRead);
            block.setLength(0);
            for(int j = i; j < len; j++){
                block.append((char)bytes[j]);
            }
            binaryString.append(codeword.get(block.toString()));
        }
    }

    // Helper method to write length of string then string itself
    private static void writeString(OutputStream os, String str) throws IOException {
        byte[] strBytes = str.getBytes("UTF-8");
        os.write(intToBytes(strBytes.length));  // Length of the string (4 bytes)
        os.write(strBytes);  // Write the string bytes
    }

    private static byte[] intToBytes(int value) {
        return new byte[]{
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
    }
}