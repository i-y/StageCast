package stagecast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

import data.ImportationObject;
import java.io.IOException;

/**
 * Loads input data files and store them in a database.
 * This class provides two methods for loading an input data file and one for 
 * data file output. A file could be loaded using a previously defined 
 * ImportationObject or the class could be passed a target file and asked to 
 * automatically parse and load it. Automatically loading the file is preferred 
 * as a convenience to the user; however, since a wide variety of input types 
 * are allowed, success can not be guaranteed in all circumstances.
 * @note It is legal to use date or column separator characters in other 
 * parts of the data. For example, a dash (-) could also be used in the name 
 * of an organism. However, depending on the specifics of the file, such use 
 * could prevent the file from being automatically loaded.
 * @author Ian Yocum
 * @date 6/3/2013
 * @copyright United States Department of Agriculture Agricultural Research 
 * Service (US Government Public Domain)
 */
public class Importer {
    /**
     * Automatically finds the column separator character.
     * This method finds the character delineating the columns by splitting the 
     * string by each valid character and counting which character produces a 
     * larger array.
     * @param line Text string containing one row of data from the input file.
     * @param io The ImportationObject holding all format information already 
     * derived from the file.
     */
    private static ImportationObject autoColSep(String line, ImportationObject io) {
        int tab = line.split("\t").length;
        int space = line.split(" ").length;
        int comma = line.split(",").length;
        if((tab > space) && (tab > comma)) {
            io.columnSeparator = "\t";
        } else if((space > tab) && (space > comma)) {
            io.columnSeparator = " ";
        } else if((comma > tab) && (comma > space)) {
            io.columnSeparator =  ",";
        } else {
            return null;
        }
        return io;
    }
    
    /**
     * Determines the date format.
     * This method attempts to determine the date format based on the 
     * information in the file. The date must be numeric but the constituent 
     * parts of the date can come in any order. It finds this by measuring the 
     * rate of change of the three elements. Based on these rates of change as 
     * well as being able to eliminate impossible options (e.g. 2013 can't be 
     * the month or day), it can be easily discovered if the year is at the 
     * front of the date or the end of the date. From there, it uses the same 
     * process to determine which one of the remaining two values is the day and 
     * which is the month. 
     * @note Julian dates have already been noted in a previous method and are 
     * of no concern here.
     * @param io ImportationObject containing data to be parsed.
     */
    private static ImportationObject autoDateFormat(ImportationObject io) {
        ImportationObject ret;
        if(io.dateSeparator.equals("Julian")) {
            io.dateFormat = "Julian";
            ret = io;
        } else {
            int maxX = 0;
            int maxY = 0;
            int maxZ = 0;
            int lastX = 0;
            int lastY = 0;
            int lastZ = 0;
            int deltaX = -1;
            int deltaY = -1;
            int deltaZ = -1;
            String t[] = new String[io.dates.keySet().size()];
            io.dates.keySet().toArray(t);
            String dateList[] = new String[io.dates.get(t[0]).size()];
            io.dates.get(t[0]).toArray(dateList);
            for (String dateList1 : dateList) {
                String[] temp = dateList1.split(io.dateSeparator);
                int x = Integer.parseInt(temp[0]);
                int y = Integer.parseInt(temp[1]);
                int z = Integer.parseInt(temp[2]);
                if(x > maxX) {
                    maxX = x;
                }
                if(y > maxY) {
                    maxY = y;
                }
                if(z > maxZ) {
                    maxZ = z;
                }
                if(x != lastX) {
                    lastX = x;
                    deltaX++;
                }
                if(y != lastY) {
                    lastY = y;
                    deltaY++;
                }
                if(z != lastZ) {
                    lastZ = z;
                    deltaZ++;
                }
            }
            Boolean yearFront = ( (maxX > 31) || ((deltaX < deltaY)&&(deltaX < deltaZ)) );
            Boolean yearEnd = ( (maxZ > 31) || ((deltaZ < deltaY)&&(deltaZ < deltaX)) );
            Boolean yearFrontEdge = ((!yearEnd)&&((maxX > maxZ)||(maxX > maxY)));
            Boolean yearEndEdge = ((!yearFront)&&((maxZ > maxX)||(maxZ > maxY)));
            if( yearFront || yearFrontEdge ) {
                if( (maxY > 12) || (deltaY > deltaZ) || (maxY > maxZ) ) {
                    io.dateFormat = "yyyy" +"/dd/mm";
                } else {
                    io.dateFormat = "yyyy" +"/mm/dd";
                }
            } else if( (yearEnd) || yearEndEdge ) {
                if( (maxX > 12) || (deltaX > deltaY) || (maxX > maxY) ) {
                    io.dateFormat = "dd/mm/" + "yyyy";
                } else {
                    io.dateFormat = "mm/dd/" + "yyyy";
                }
            }
            ret = io;
        }
        return ret;
    } 

    /**
     * Automatically finds the date separator character.
     * This method finds the character delineating the date elements by 
     * splitting the string by each valid character and counting which character 
     * produces a larger array.
     * @param line Text string containing one row of data from the input file.
     * @param io The ImportationObject holding all format information already 
     * derived from the file.
     */
    private static ImportationObject autoDateSep(String line,ImportationObject io) {
        int dash = line.split(io.columnSeparator)[0].split("-").length;
        int slash = line.split(io.columnSeparator)[0].split("/").length;
        int dot = line.split(io.columnSeparator)[0].split("\\.").length;
        if((dash > slash)&&(dash > dot)) {
            io.dateSeparator = "-";
        } else if((slash > dash)&&(slash > dot)) {
            io.dateSeparator = "/";
        } else if((dot > dash)&&(dot > slash)) {
            io.dateSeparator = "\\.";
        } else if((dash == slash)&&(dash == dot)&&(dot == slash)) {
            io.dateSeparator = "Julian";
        } else {
            return null;
        }
        return io;
    } 

    /**
     * Automatically parses and loads an input file.
     * This method attempts to parse a target file to determine its format. If 
     * it believes it has successfully made this determination it will try to 
     * load the file into memory. Under certain circumstances this method could 
     * fail. Unless this failure crashes the program it is not considered a bug. 
     * This method exists only as a convenience and at the present time makes no 
     * attempt to cover any of the numerous edge cases that exist within the 
     * file specification.
     * @param target Target file to load.
     * @param organism If true, the input file describes an organism dataset.
     * @return ImportationObject describing the dataset
     */
    public static ImportationObject autoLoad(String target, Boolean organism) {
        ImportationObject importer = new ImportationObject();
        importer.source = target;
        importer.species = organism;
        importer.data = new HashMap<>();
        importer.dates = new HashMap<>();
        String line;
        try {
            importer.datasetName = (new File(importer.source)).getName().split("\\.")[0];
            importer.destination = (new File(importer.source)).getAbsolutePath();
            try (BufferedReader inputStream = new BufferedReader(new FileReader(importer.source))) {
                line = inputStream.readLine();
                while( (line != null) && ((line.isEmpty()) || (line.startsWith("#"))) ) {
                    line = inputStream.readLine();
                }
            }
            if( (line == null) || (line.isEmpty()) ) {
                ErrorManager.error("Could not automatically read file.", "Importer.autoLoad failed to find a valid data string.", null);
                return null;
            }
        } catch(IOException e) {
            ErrorManager.error("Could not automatically read file.", "Importer.autoLoad has encountered an error.", e);
            return null;
        }
        importer = autoColSep(line, importer);
        if(importer == null) {
            ErrorManager.error("Could not detect file format. Program will choose the defaults for all settings.", "Importer.autoLoad could not identify the column separator character.", null);
            return null;
        }
        if(importer.species) {
            importer.stages = line.split(importer.columnSeparator).length - 2;
        }
        importer = autoDateSep(line, importer);
        if(importer == null) {
            ErrorManager.error("Could not detect file format. Program will choose the defaults for all settings.", "Importer.autoLoad could not identify the date separator character.", null);
            return null;
        }
        importer = load(importer);
        if(importer == null) {
            ErrorManager.error("Could not open data file.","Importer.autoLoad failed to load the file using the automatically detected format.", null);
            importer = new ImportationObject();
            importer.species = organism;
            return importer;
        }
        importer = autoDateFormat(importer);
        if(importer == null) {
            ErrorManager.error("Could not detect file format. Program will choose the defaults for all settings.", "Importer.autoLoad could not identify the date format.", null);
            return null;
        }
        importer = convertDate(importer);
        return importer;
    }

    /**
     * Converts dates to the Julian equivalent.
     * The program specification calls for all dates to be in Julian format 
     * before a database can be created from the input file. Since the 
     * specification also allows for the input dates to not be in the Julian 
     * format, this method becomes a necessary intermediate step.
     * @param io ImportationObject containing the data to be converted.
     */
    private static ImportationObject convertDate(ImportationObject io) {
        if(io.dateFormat.equals("Julian")) {
            return io;
        }
        int day = 0;
        int month = 0;
        int year = 0;
        switch (io.dateFormat) {
            case "mm/dd/yyyy":
                month = 0;
                day = 1;
                year = 2;
                break;
            case "dd/mm/yyyy":
                day = 0;
                month = 1;
                year = 2;
                break;
            case "yyyy/mm/dd":
                year = 0;
                month = 1;
                day = 2;
                break;
            case "yyyy/dd/mm":
                year = 0;
                day = 1;
                month = 2;
                break;
        }
        String keys[] = new String[io.dates.keySet().size()];
        io.dates.keySet().toArray(keys);
        for (String key : keys) {
            String[] dateList = new String[io.dates.get(key).size()];
            io.dates.get(key).toArray(dateList);
            for (int i = 0; i < dateList.length; i++) {
                String holder[] = dateList[i].split(io.dateSeparator);
                String outpt = toJulian(Integer.parseInt(holder[month]), Integer.parseInt(holder[day]), Integer.parseInt(holder[year]));
                if(outpt.isEmpty()) {
                    return io; 
                }
                io.dates.get(key).set(i, outpt);
            }
        }
        return io;
    }
    
    /**
     * Finds the days in any given month for any input year.
     * @param month Ranges from 1 to 12.
     * @param year Four digit number preferably.
     */
    private static int daysInMonth(int month, int year) {
        if(month == 2) {
            if(((float)year/4) == Math.floor((float)year/4)) {
                if(((float)year/100) != Math.floor((float)year/100)) {
                    return(29);
                } else if(((float)year/400) == Math.floor((float)year/400)) {
                    return(29);
                } else {
                    return(28);
                }
            } else {
                return(28);
            }
        } else if((month == 4) || (month == 6) || (month == 9) || (month == 11)) {
            return(30);
        } else {
            return(31);
        }
    } 
    
    /**
     * Loads an input file.
     * This method loads an input file that is described by an InformationObject. 
     * It is called from autoLoad and it is also available for direct calling 
     * from other points in the program. 
     * @warning Since this method is being passed an InformationObject it 
     * assumes the data is valid and does not perform any significant validation.
     * @param io The InformationObject that describes the input file.
     * @return ImportationObject describing the data set.
     * @bug This will fail if passed an objects with, for example, multiple
     * spaces between columns instead of just one. This should probably be 
     * fixed at the point this method is called.
     */
    public static ImportationObject load(ImportationObject io) {
        try {
            try (BufferedReader inputStream = new BufferedReader(new FileReader(io.source))) {
                String line = inputStream.readLine();
                while (line != null) {
                    if(!((line.isEmpty()) || (line.startsWith("#")))) {
                        String holder[] = line.split(io.columnSeparator);
                        String content = "";
                        String name = holder[1];
                        int offset = 2;
                        if(!io.species) {
                            name = "weather";
                            offset = 1;
                        }
                        for(int i = offset; i < holder.length; i++) {
                            content += "\t" + holder[i];
                        }
                        if(!io.data.containsKey(name)) {
                            io.data.put(name, new ArrayList<String>());
                            io.dates.put(name, new ArrayList<String>());
                        }
                        io.data.get(name).add(content);
                        io.dates.get(name).add(holder[0]);
                    }
                    line = inputStream.readLine();
                }
            }
            return io;
        } catch(IOException e) {
            ErrorManager.error("The input file could not be loaded.","Importer.load has encountered an error.", e);
        }
        return null;
    }

    /**
     * Creates a database.
     * This method is used to create a database from an InformationObject. It 
     * creates both the [x].about.txt file which describes the database and the 
     * different data files themselves.
     * @param io The InformationObject which describes the database.
     */
    public static void save(ImportationObject io) {
        try {
            io.destination += "/" + io.datasetName;
            File dest = new File(io.destination);
            if (!dest.exists()) {
                dest.mkdirs();
            }
            String keys[] = new String[io.dates.keySet().size()];
            io.dates.keySet().toArray(keys);
            XmlManager.writeDatabaseFile(io.destination + "/" + io.datasetName + ".about.xml", io);
            for (String key : keys) {
                String[] dataList = new String[io.data.get(key).size()];
                io.data.get(key).toArray(dataList);
                String[] datesList = new String[io.dates.get(key).size()];
                io.dates.get(key).toArray(datesList);
                try (final BufferedWriter writer = new BufferedWriter(new FileWriter(io.destination + "/" + io.datasetName + "." + key + ".txt"))) {
                    for(int j = 0; j < datesList.length; j++) {
                        writer.write(datesList[j] + dataList[j] + "\r\n");
                    }
                }
            }
        } catch(IOException e) {
            ErrorManager.error("Could not save the imported file as a database.", "Importer.save has encountered an error.", e);
        }
    }

    /**
     * Converts a calendar date to the equivalent Julian date.
     * @warning To resolve potential ambiguities with two-digit years, it 
     * assumes all values lower than 50 must be 20xx and all values 50 and above 
     * are 19xx. This will result in incorrect calculations if these assumptions 
     * are wrong. To avoid incorrect results, it is recommended that all input 
     * uses either Julian dates or a date format with a four-digit year.
     */
    private static String toJulian(int month, int day, int year) {
        if(year < 50) {
            year += 2000;
        } else if(year < 100) {
            year += 1900;
        }
        int count = 0;
        for(int x = 1; x < month; x++) {
            count += daysInMonth(x, year);
        }
        count += day;
        return String.valueOf(count);
    }
}