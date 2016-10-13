import java.io.*;

public class FileEdit {

    private String filename;

    public FileEdit(String filename){
        this.filename = filename;
    }

    private int findLineNo(String targetStr){
        int i = 0;
        boolean foundIt = false;

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        {
            try {
                for(String line; (line = br.readLine()) != null; ) {
                    if(line.compareTo(targetStr) == 1){
                        System.out.println("YES FOUND IT!");
                        foundIt = true;
                        break;
                    }
                    i++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(foundIt){
            return i;
        } else {
            return -1;
        }
    }

    public void removeLine(String targetLine) throws IOException {
        File tmp = null;
        try {
            tmp = File.createTempFile("tmp", "");
        } catch (IOException e) {
            e.printStackTrace();
        }

        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(tmp));
        } catch (IOException e) {
            e.printStackTrace();
        }

        int toRemove = findLineNo(targetLine);

        for (int i = 0; i < toRemove; i++)
            bw.write(String.format("%s%n", br.readLine()));

        br.readLine();

        String l;
        while (null != (l = br.readLine()))
            bw.write(String.format("%s%n", l));

        br.close();
        bw.close();

        File oldFile = new File(filename);
        if (oldFile.delete())
            tmp.renameTo(oldFile);
    }


    public void appendLine(String line) throws IOException {
        BufferedWriter writer = null;
        writer = new BufferedWriter(new FileWriter(filename, true));
        writer.newLine();
        writer.write(line);
        writer.flush();
        writer.close();
    }


    public static void main (String[] args){
//        FileEdit file = new FileEdit("test.txt");
//
//        try {
//            file.appendLine("Testing. This should be the last line");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }
}
