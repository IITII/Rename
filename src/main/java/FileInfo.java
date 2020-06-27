import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

//@TODO 文件列表的自定义排序

/**
 * @author IITII
 */
public class FileInfo implements Comparable<FileInfo>, Cloneable, Serializable {
    String fileName;
    String fileSize;
    String modifyDate;

    public FileInfo() {
    }

    public FileInfo(String fileName, String fileSize, String modifyDate) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.modifyDate = modifyDate;
    }

    public String getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(String modifyDate) {
        this.modifyDate = modifyDate;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    // Custom compare
    @Override
    public int compareTo(FileInfo o) {
        Map<String, Integer> map = new HashMap<>();
        String[] mark = {"PB", "GB", "MB", "KB", "B"};
        int index = 5;
        for (String temp : mark) {
            map.put(temp, index);
            index--;
        }
        String[] string = this.getFileSize().split(" ");
        String[] string1 = o.getFileSize().split(" ");
        if (map.get(string[1]) > map.get(string1[1])) {
            return 1;
        } else if (map.get(string[1]) < map.get(string1[1])) {
            return -1;
        } else if (map.get(string[1]).equals(map.get(string1[1]))) {
            if (Integer.parseInt(string[0]) > Integer.parseInt(string1[0])) {
                return 1;
            } else if (Integer.parseInt(string[0]) < Integer.parseInt(string1[0])) {
                return -1;
            }
        }
        return 0;
    }
}
