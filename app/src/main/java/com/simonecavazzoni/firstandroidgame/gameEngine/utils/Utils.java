package com.simonecavazzoni.firstandroidgame.gameEngine.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Utils {

    private static List<Integer> list = new ArrayList<>();

    public static List<Integer> extractNumbers(int n) {
        list.clear();

        if (n == 0) {
            return list;
        }

        list.add(0);
        list.add(1);
        list.add(2);
        list.add(3);

        Collections.shuffle(list);
        return list.subList(0, n);
    }
}
