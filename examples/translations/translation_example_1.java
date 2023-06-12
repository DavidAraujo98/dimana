package examples.translations;

import java.util.Scanner;

public class translation_example_1 {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        int _n;
        String _name;
        double _g;

        /* Suffixes */
        String __NMEC__ = " nmec";
        String __NAME__ = " string";
        String __GRADE__ = " grade";

        System.out.print("NMEC: ");
        _n = sc.nextInt();

        sc.nextLine(); // consume newline

        System.out.print("Name: ");
        _name = sc.nextLine();

        System.out.print("Grade: ");
        _g = sc.nextDouble();

        System.out.printf("%10s %30s %10s\n", "NMEC:", "Name:", "Grade:");
        
        StringBuffer temp_1 = new StringBuffer(_n+"");
        temp_1.setLength(10);
        StringBuffer temp_2 = new StringBuffer(_name);
        temp_2.setLength(30);
        StringBuffer temp_3 = new StringBuffer(_g + "");
        temp_3.setLength(10);

        System.out.printf(
            "%10s %s %30s %s %10s %s\n",
                temp_1, __NMEC__, temp_2, __NAME__, temp_3, __GRADE__
            );

        sc.close();
    }
}
