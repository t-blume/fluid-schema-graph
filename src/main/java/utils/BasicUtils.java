package utils;

import java.io.*;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.*;


/**
 * Class which contan
 */
public class BasicUtils {
    /**
     * Sorts a map either ascending or descending by its values.
     *
     * @param map       The map to be sorted.
     * @param ascending Boolean if the map is sorted ascending or descending.
     * @return The sorted map.
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean ascending) {
        List<Map.Entry<K, V>> list = new LinkedList<>(map.entrySet());
        Collections.sort(list, Comparator.comparing(o -> (o.getValue())));
        if (!ascending)
            Collections.reverse(list);
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, V> entry : list)
            result.put(entry.getKey(), entry.getValue());

        return result;
    }

    /**
     * Simple way to read a file into a string. Basically a scanner is created
     * with File(path) and then the token size is set to the whole file (\\Z
     * represents the end of the file).
     *
     * @param path to the file to be read.
     * @return The file as a string.
     * @throws FileNotFoundException
     */
    public static String readFile(String path) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(path));
        String fileString = scanner.useDelimiter("\\Z").next();
        scanner.close();
        return fileString;
    }


    public static void writeObjectToFile(Object o, File file) {
        FileOutputStream fileOut;
        try {
            fileOut = new FileOutputStream(file);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(o);
            out.close();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] objectToBytes(Object o) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = null;
        try {
            out = new ObjectOutputStream(bos);
            out.writeObject(o);
            out.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            return null;
        } finally {
            try {
                bos.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }

    public static Object bytesToObject(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            return in.readObject();
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (IOException ex) {
                // ignore close exception
            }
        }
    }


    public static String formatBigDecimal(Number bd) {
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(3);
        return df.format(bd);
    }


    ////DIRTY LITTLE HELPER
    public static long getReallyUsedMemory() {
        long before = getGcCount();
        System.gc();
        while (getGcCount() == before) ;
        return getCurrentlyUsedMemory();
    }

    private static long getGcCount() {
        long sum = 0;
        for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = b.getCollectionCount();
            if (count != -1) {
                sum += count;
            }
        }
        return sum;
    }

    private static long getCurrentlyUsedMemory() {
        return
                ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() +
                        ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
    }

    private static long getCurrentlyMaxMemory() {
        return
                ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getMax() +
                        ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getMax();
    }


    public static File createFile(String filepath) {
        File targetFile = new File(filepath);
        File parent = targetFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IllegalStateException("Couldn't create dir: " + parent);
        }
        return targetFile;
    }

    public static boolean contains(String[] array, String contains) {
        if (array == null)
            return false;

        for (String s : array)
            if ((s == null && contains == null) || s.equals(contains))
                return true;

        return false;
    }


    public static long printProgress(int i, int max, int interval, long start){
        if (i % interval == 0) {
            long stop = System.currentTimeMillis();
            System.out.format("Progress: %08d / %08d RQ/s: %.2f\n", i, max,
                    ((double)interval/((double) ((stop - start)/1000))));
            return stop;
        }else
            return -1;
    }
}
