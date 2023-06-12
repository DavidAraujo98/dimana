package examples.translations;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class translation_example_2 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        int _N = 4;
        
        List<Integer> _nmecs = new ArrayList<Integer>();
        List<String> _names = new ArrayList<String>();
        List<Double> _grades = new ArrayList<Double>();
        int _i;

        for (_i = 1; _i < _N; _i++) {
            System.out.print("NMEC: ");
            int temp_1 = sc.nextInt();
            _nmecs.add(temp_1);

            sc.nextLine(); // consume newline

            System.out.print("Name: ");
            String temp_2 = sc.nextLine();
            _names.add(temp_2);

            System.out.print("Grade: ");
            Double temp_3 = sc.nextDouble();
            _grades.add(temp_3);

            sc.nextLine(); // consume newline
        }

        StringBuffer temp_4 = new StringBuffer("NMEC");
        temp_4.setLength(10);
        StringBuffer temp_5 = new StringBuffer("Name");
        temp_5.setLength(30);
        StringBuffer temp_6 = new StringBuffer("Grade");
        temp_6.setLength(10);

        System.out.printf("%10s %30s %10s\n", temp_4, temp_5, temp_6);

        for (_i = 1; _i < _nmecs.size(); _i++) {
            StringBuffer temp_7 = new StringBuffer(_nmecs.get(_i-1) + " nmec");
            temp_7.setLength(10);
            StringBuffer temp_8 = new StringBuffer(_names.get(_i-1) + " string");
            temp_8.setLength(30);
            StringBuffer temp_9 = new StringBuffer(_grades.get(_i-1) + " grade");
            temp_9.setLength(10);



            System.out.printf("%10s %30s %10s\n", temp_7, temp_8, temp_9);
        }

        sc.close();
    }
}
