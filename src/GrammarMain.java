import java.io.FileNotFoundException;
import java.io.IOException;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.io.*;

import java.util.Scanner;

import org.stringtemplate.v4.*;

public class GrammarMain {
    public static void main(String[] args) {

        if (args.length == 0 || args[0].equals("-h") || args[0].equals("--help")) {
            printHelp();
            System.exit(0);
        }

        String inputFileName = null;
        String outputFileName = "Output.java";

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-o") && i + 1 < args.length) {
                outputFileName = args[i + 1];
            } else {
                inputFileName = args[i];
            }
        }

        if (inputFileName == null) {
            System.err.println("Input file not provided.");
            printHelp();
            System.exit(1);
        }

        // Determine the imports folder and relative location
        File inputFile = null;
        try {
            inputFile = new File(inputFileName);

        } catch (Exception e) {
            System.err.println("Input file not found.");
            printHelp();
            System.exit(1);
        }

        String inputDirectory = inputFile.getParent(); // gives, i.e. -> ../examples
        if (inputDirectory == null)
            inputDirectory = "";

        try {

            // create a CharStream that reads from standard input:
            CharStream input = CharStreams.fromFileName(inputFileName);
            // create a lexer that feeds off of input CharStream:
            GrammarLexer lexer = new GrammarLexer(input);
            // create a buffer of tokens pulled from the lexer:
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            // create a parser that feeds off the tokens buffer:
            GrammarParser parser = new GrammarParser(tokens);
            // begin parsing at program rule:
            ParseTree mainTree = parser.program();
            HashMap < String, ParseTree > importedTrees = new HashMap < String, ParseTree > ();
            if (parser.getNumberOfSyntaxErrors() == 0) {

                PreCompiler preVisitor = new PreCompiler();
                List < String > mainTreeImports = preVisitor.visit(mainTree);

                getImports(mainTreeImports, 0, importedTrees, inputDirectory);

                //System.out.println("Imported trees: " + importedTrees.toString());
                System.out.println("List of imported files: " + mainTreeImports.toString());

                SemanticAnalysis semanticVisitor = new SemanticAnalysis(importedTrees, inputFileName, inputDirectory);
                semanticVisitor.visit(mainTree);
                System.out.println("File is Semantically Correct");

                
                Compiler compileVisitor = new Compiler(importedTrees, inputFileName);
                ST code = compileVisitor.visit(mainTree);
                String outputClass = outputFileName.endsWith(".java") ? outputFileName.substring(0, outputFileName.length() - 5) : outputFileName;
                code.add("name", outputClass);
                
                PrintWriter pw = new PrintWriter(new File(outputFileName));
                pw.print(code.render());
                pw.close();
                code.add("name", outputClass);
            }
        }   catch (NullPointerException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (RecognitionException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (RuntimeException e) {
            System.err.println(e.toString());
            // Debug
            //e.printStackTrace();
            //System.exit(1);
        }
    }

    public static void getImports(List < String > importedFiles, int importedFilesIndex,
        HashMap < String, ParseTree > importedTrees, String inputDirectory) throws FileNotFoundException {
        // Pre-condition
        if (importedFiles.size() == 0 || importedFilesIndex >= importedFiles.size()){
            return;
        }

        String importString = importedFiles.get(importedFilesIndex);
        if (importedTrees.containsKey(importString)) {
            getImports(importedFiles, importedFilesIndex+1, importedTrees, inputDirectory);
            return;
        }
        try {
            String combinedFilePath;
            if(inputDirectory != ""){
                File combinedFile = new File(inputDirectory, importString);
                combinedFilePath = combinedFile.getAbsolutePath();
            }else
                combinedFilePath = importString;

            CharStream importedFile = CharStreams.fromFileName(combinedFilePath);

            GrammarLexer importedLexer = new GrammarLexer(importedFile);
            CommonTokenStream importedTokens = new CommonTokenStream(importedLexer);
            GrammarParser importedParser = new GrammarParser(importedTokens);
            ParseTree importedTree = importedParser.program();
            PreCompiler importedVisitor = new PreCompiler();
            List < String > importedImportList = importedVisitor.visit(importedTree);

            for (String tempImport: importedImportList) {
                if (!importedTrees.containsKey(tempImport)) {
                    importedFiles.add(tempImport);
                }
            }

            importedTrees.put(importString, importedTree);

            // Recursive Call
            getImports(importedFiles, importedFilesIndex+1, importedTrees, inputDirectory);

            return;
        } catch (IOException e) {
            throw new FileNotFoundException("Import file " + importString + " error: " + e.toString());
        }
    }

    public static void printHelp() {
        System.out.println("Usage: java GrammarMain [options] input_file");
        System.out.println("Options:");
        System.out.println("  -o <output_file>      Specify the output file (default: Output.java)");
        System.out.println("  -h, --help            Print help information");
    }

}