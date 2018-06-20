package ru.spbau.dkaznacheev.benchmark;

import java.util.LinkedList;
import java.util.List;

public class Util {
    public static List<Integer> sort(List<Integer> list) {
        int[] a = list.stream().mapToInt(e -> e).toArray();

        List<Integer> result = new LinkedList<>();

        for (int i = 0; i < a.length - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < a.length; j++) {
                if (a[j] < a[minIndex])
                    minIndex = j;
            }
            int t = a[i];
            a[i] = a[minIndex];
            a[minIndex] = t;
            result.add(a[i]);
        }
        return result;
    }
}
