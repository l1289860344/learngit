package zy;

import edu.princeton.cs.algs4.Point2D;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LTest {
    private int x;
    private  int y;

    public LTest(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LTest lTest = (LTest) o;
        return x == lTest.x &&
                y == lTest.y;
    }
    @Override
    public int hashCode() {

        return Objects.hash(x, y);
    }

    public static void main(String[] args) {
        Point2D p3 = new Point2D(1, 2);
        LTest ltest=new LTest(1,2);
        Map<Object,String> map=new HashMap<Object, String>();
        map.put(1,"123");
        map.put(ltest,"456");
        map.put(p3,"789");
        ltest=new LTest(1,2);
       // p3 = new Point2D(1, 2);
        System.out.println(map.get(1));
        System.out.println(map.get(ltest));
        System.out.println(map.get(p3));

    }
}
