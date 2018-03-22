package ru.grachoffs.helpers;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Slf4j
public class RandomHelper {
    private static Random random = new Random();

    public static void writeRandomValues(List<Long> values, String fileName) {
        try (FileOutputStream fout = new FileOutputStream(fileName)) {
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(values);
        } catch (IOException e) {
            log.error("Error writing to file", e);
        }
    }

    public static List<Long> readRandomValues(String fileName) {
        try (FileInputStream fin = new FileInputStream(fileName)) {
            ObjectInputStream ois = new ObjectInputStream(fin);
            return (List<Long>) ois.readObject();
        } catch (IOException e) {
            log.error("Error reading from file", e);
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            log.error("Class not found in file", e);
            throw new RuntimeException(e);
        }
    }

    public static List<Long> fillRandomValues(int valuesCount, int range, int probability, long initialValue) {
        List<Long> values = new ArrayList<Long>(valuesCount);
        Long value = initialValue;
        Boolean isGrowingTrend = random.nextBoolean();
        for (int counter = 0; counter <valuesCount; counter++) {
            values.add(value);
            value += randomValue(range) * (isGrowingTrend || value<range ? 1 : -1);
            if (changeTrend(range, probability)) {
                isGrowingTrend = !isGrowingTrend;
            }
        }
        return values;
    }

    public static List<Long> fillValuesBy1(int valuesCount) {
        List<Long> values = new ArrayList<Long>(valuesCount);
        for (int counter = 0; counter <valuesCount; counter++) {
            values.add(1L);
        }
        return values;
    }

    private static boolean changeTrend(int range, int probability) {
        boolean val = random.nextInt(range) < probability;
        if (val) {
//            log.info("changing trend...");
        }
        return val;
    }

    private static int randomValue(int range) {
        return random.nextInt(range)-(range/10);
    }

}
