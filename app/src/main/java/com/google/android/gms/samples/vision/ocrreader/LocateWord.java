package com.google.android.gms.samples.vision.ocrreader;

import android.graphics.Point;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public final class LocateWord {

    private static HashMap<Point[], String> map= new HashMap<>();
    private static HashMap<String, Point[]> rMap= new HashMap<>();

    private static float rawX;
    private static float rawY;
    private static int [] viewLoc = new int[2];

    public static void store(String word, Point[] bounds){
        map.put(bounds,word);
        rMap.put(word, bounds);
    }

    public static void store(String word, float left, float top, float right, float bottom){
//        left = (int) left;
//        top = (int) top;
//        right = (int) right;
//        bottom = (int) bottom;

        Point topLeft = new Point();
        Point topRight = new Point();
        Point bottomRight = new Point();
        Point bottomLeft = new Point();

        topLeft.x = (int)left;
        topLeft.y = (int)top;
        topRight.x = (int)right;
        topRight.y = (int)top;
        bottomRight.x = (int)right;
        bottomRight.y = (int)bottom;
        bottomLeft.x = (int)left;
        bottomLeft.y = (int)bottom;

        Point [] pts = {topLeft, topRight, bottomRight, bottomLeft};

        map.put(pts, word);
        rMap.put(word, pts);
    }

    public static void clear(){
        map.clear();
    }

    public static void setCoordinates(float x, float y){
        rawX = x;
        rawY = y;
    }

    public static void setViewLocation(int [] arr){
        viewLoc[0] = arr[0];
        viewLoc[1] = arr[1];
    }

    public static Pair<Integer, Integer> getCoordinates(){
        int x = (int) rawX - viewLoc[0];
        int y = (int) rawY - viewLoc[1];
        Pair<Integer, Integer> pr = new Pair<>(x, y);
        return pr;
    }

    public static Pair<Integer, Integer> getRawCoordinates(){
        Pair<Integer, Integer> pr = new Pair<>((int)rawX, (int)rawY);
        return pr;
    }

    public static HashMap<Point[], String> getMap(){
        return map;
    }

    public static ArrayList<String> findWord(){
        ArrayList<String> list = new ArrayList<>();
        for (Point[] key: map.keySet()){
            if(check(key)){
                list.add(map.get(key));
            }
        }
        return list;
    }

    public static ArrayList<String> getbounds(){
        ArrayList<String> list = new ArrayList<>();
        for (String key : rMap.keySet() ){
            if (key.equals("test")){
                Point [] k = rMap.get(key);
                String ret = k[0] + " " + k[1] + " " + k[2] + " " + k[3];
                list.add(ret);
            }
        }
        return  list;
    }

    private static boolean check(Point[] pts){
        int x1 = pts[0].x;
        int y1 = pts[0].y;
        int x2 = pts[1].x;
        int y2 = pts[1].y;
        int x3 = pts[2].x;
        int y3 = pts[2].y;
        int x4 = pts[3].x;
        int y4 = pts[3].y;
        int x = getCoordinates().first;
        int y = getCoordinates().second;

        float A = area(x1, y1, x2, y2, x3, y3)+
                area(x1, y1, x4, y4, x3, y3);

        /* Calculate area of triangle PAB */
        float A1 = area(x, y, x1, y1, x2, y2);

        /* Calculate area of triangle PBC */
        float A2 = area(x, y, x2, y2, x3, y3);

        /* Calculate area of triangle PCD */
        float A3 = area(x, y, x3, y3, x4, y4);

        /* Calculate area of triangle PAD */
        float A4 = area(x, y, x1, y1, x4, y4);

        /* Check if sum of A1, A2, A3 and A4 is same as A */
        return (A == A1 + A2 + A3 + A4);
    }

    static float area(int x1, int y1, int x2, int y2, int x3, int y3){
        return (float)Math.abs((x1 * (y2 - y3) + x2 * (y3 - y1) + x3 * (y1 - y2)) / 2.0);
    }
}
