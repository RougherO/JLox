/*
 * This is a helper code to help us generate the 
 * Syntax Tree java file and is not a part of 
 * the actual interpreter.
 */
package com.interpreter.tools;

import com.interpreter.lox.ExitCode;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAST {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(ExitCode.INCORRECT_CMD_USAGE.exitCode);
        }
        String outputDir = args[0];
        /*
         * Defining the Grammar for our JLox
         */
        defineAST(outputDir, "Expr", Arrays.asList(
                "Binary    : Expr left, Token operator, Expr right",
                "Grouping  : Expr expression",
                "Literal   : Object value",
                "Unary     : Token operator, Expr right"));
    }

    private static void defineAST(
            String outputDir, String baseName,
            List<String> types) throws IOException {
        String path = outputDir + "/" + baseName + ".java";
        PrintWriter writer = new PrintWriter(path);

        writer.println("package com.interpreter.lox;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("abstract class " + baseName + " {");

        defineVisitor(writer, baseName, types);

        // AST classes
        for (String type : types) {
            String[] list = type.split(":");
            String typeName = list[0].trim();
            String fields = list[1].trim();
            /*
             * This function is reponsible for writing the
             * entire class definition
             */
            defineType(writer, baseName, typeName, fields);
        }

        writer.println("    abstract <T> T accept(Visitor<T> visitor);");

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(
            PrintWriter writer, String baseName,
            List<String> types) {
        writer.println("    interface Visitor<T> {");

        // creating each class type visit function
        for (String type : types) {
            String[] list = type.split(":");
            String className = list[0].trim();
            writer.println("        T visit" + className + baseName + "("
                    + className + " " + baseName.toLowerCase() + ");");
            writer.println();
        }
        writer.println("    }");
        writer.println();
    }

    private static void defineType(
            PrintWriter writer, String baseName,
            String typeName, String fieldList) {
        writer.println("    static class " + typeName + " extends " + baseName + " {");

        // Constructor
        writer.println("        " + typeName + "(" + fieldList + ") {");

        // Store paramters in fields
        String[] fields = fieldList.split(",");
        for (String field : fields) {
            String[] list = field.trim().split(" ");
            String name = list[1];
            writer.println("            this." + name + " = " + name + ";");
        }
        writer.println("        }");

        // Implementing visitor accept function for each type
        writer.println();
        writer.println("        @Override");
        writer.println("        <T> T accept(Visitor<T> visitor) {");
        writer.println("            return visitor.visit" + typeName + baseName + "(this);");
        writer.println("        }");

        // Fields
        writer.println();
        for (String field : fields) {
            writer.println("        final " + field.trim() + ";");
        }

        writer.println("    }");
        writer.println();
    }
}
