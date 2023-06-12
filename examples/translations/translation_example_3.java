package examples.translations;

import java.util.Scanner;

public class translation_example_3 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        double _l;
        double _t;
        double _v;

        // Suffixes
        String _VELOCITY = "v";
        String _LENGTH = "meter";
        String _TIME = "second";

        _l = 10.0; // Semantic error: l=10 is not allowed
        _t = 2.0;
        _v = _l / _t; // Semantic error: t/l is not allowed
        System.out.println("Velocity: " + _v + "m/s");

        System.out.print("Distance: ");
        _l = sc.nextDouble();
        System.out.print("Time: ");
        _t = sc.nextDouble();
        _v = _l / _t;
        System.out.println("Velocity: " + _v + "m/s");

        System.out.printf("%10s %30s %10s\n", "Length:", "Time:", "Velocity:");


        System.out.printf(
            "%10s %s %30s %s %10s %s\n", 
                _l, _LENGTH, _t, _TIME, _v, _VELOCITY
            );

        sc.close();
    }
}
