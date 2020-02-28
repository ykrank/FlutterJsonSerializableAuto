package com.ykrank.flutter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * by shenmingliang1
 * 2019.01.28 11:00.
 */

class Main {
    public static void main(String[] args) throws IOException {
        String fileContent = readToString("food.dart");

        String rgex = "class(.*?)(extends(.*?))?(implements(.*?))?\\{";
        List<String> list = new ArrayList<>();
        List<Integer> start = new ArrayList<>();
        Pattern pattern = Pattern.compile(rgex);// 匹配的模式
        Matcher m = pattern.matcher(fileContent);

        int offset = 1;
        while (m.find()) {
            list.add(m.group(1));
            start.add(m.start());
        }
        if (list.size() > 0) {
            int chooseOffset = start.get(0);
            int index = 1;
            int chooseIndex = 0;
            while (chooseOffset < offset && index < list.size()) {
                chooseIndex++;
                chooseOffset = start.get(index);
                index++;
            }
            System.out.println(list.get(chooseIndex));
            System.out.println(start.get(chooseIndex));
        }

        System.out.println(list);
        System.out.println(start);
    }

    public static String readToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }
}
