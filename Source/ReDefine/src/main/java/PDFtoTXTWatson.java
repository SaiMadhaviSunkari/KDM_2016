import com.ibm.watson.developer_cloud.document_conversion.v1.DocumentConversion;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


//
public class PDFtoTXTWatson {
   public static void main(String[] args) throws IOException {
        DocumentConversion service = new DocumentConversion(DocumentConversion.VERSION_DATE_2015_12_01);
        service.setUsernameAndPassword("26008258-bc7c-4639-bfbe-e04cfe9eeb40", "jYpdOjxBEjmu");

// code to read all filepaths in a folder in string format
       Files.walk(Paths.get("data/researchArticles")).forEach(filePath -> {
           if (Files.isRegularFile(filePath)) {
               System.out.println(filePath);
               String inputFilePath=filePath.toString();
               File pdf = new File(inputFilePath);
               System.out.println("Converting pdf document to Text\n");
               String normalizedtext = service.convertDocumentToText(pdf).execute();
               System.out.println(normalizedtext);
               BufferedWriter output = null;
               try {
                   String researchArticleName = FilenameUtils.getBaseName(inputFilePath);
                   //System.out.println(researchArticleName);
                   String outputFilePath="output/researchArticlesTXT/"+researchArticleName+".txt";
                   File file = new File(outputFilePath);
                   output = new BufferedWriter(new FileWriter(file));
                   output.write(normalizedtext);
               } catch (IOException e) {
                   e.printStackTrace();
               } finally {
                   if (output != null) {
                       try {
                           output.close();
                       } catch (IOException e) {
                           e.printStackTrace();
                       }
                   }
               }
           }
       });

       //File pdf = new File("data/sparkpaper");


       //code to read all the text documents in a folder into a string
       /*
       File folder = new File("data/researchArticles");
       File[] listOfFiles = folder.listFiles();

       for (int i = 0; i < listOfFiles.length; i++) {
           File file = listOfFiles[i];
           if (file.isFile() && file.getName().endsWith(".txt")) {
               String content = FileUtils.readFileToString(file);
               }
       }
       */


       // code to read all the files and print the file paths in a directory
       /*
       Files.walk(Paths.get("data/researchArticles")).forEach(filePath -> {
           if (Files.isRegularFile(filePath)) {
               System.out.println(filePath);
           }
       });
      */
   }
    private static String nameOf(Object o) {
        return o.getClass().getSimpleName();
    }


}