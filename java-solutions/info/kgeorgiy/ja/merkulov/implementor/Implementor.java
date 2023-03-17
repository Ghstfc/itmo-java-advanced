package info.kgeorgiy.ja.merkulov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Implementor implements Impler {

    public static final String SEPARATOR = System.lineSeparator();

    public static final String WHITESPACE = " ";

    private File filePath(Class<?> token, Path root) throws ImplerException {
        String name = token.getSimpleName() + "Impl";
        String packageName = token.getPackageName();
        String path = root.resolve(packageName.replace(".", File.separator))
                .resolve(name + ".java").toString();

        File classFile = new File(path);
        if (!classFile.exists()) {
            File outputParent = classFile.getParentFile();
            if (outputParent != null && !outputParent.exists() && !outputParent.mkdirs()) {
                throw new ImplerException("Failed to create path to output file");
            }
        }
        return classFile;
    }

    private String headerImplementing(Class<?> token) {
        String name = token.getSimpleName() + "Impl";
        String pack = token.getPackageName();
        StringBuilder result = new StringBuilder();

        // Package checking
        if (!pack.equals("")) {
            result.append("package").append(WHITESPACE).append(pack).append(";").append(SEPARATOR);
        }
        // Header implementing
        result.append("public class").append(WHITESPACE).append(name).append(WHITESPACE)
                .append("implements").append(WHITESPACE).append(token.getCanonicalName())
                .append(WHITESPACE).append("{").append(SEPARATOR);
        return result.toString();
    }

    private String getDefaultValue(Method method) {
        Class<?> retType = method.getReturnType();
        if (!retType.isPrimitive()) {
            return "null";
        } else if (retType.equals(void.class)) {
            return "";
        } else if (retType.equals(boolean.class)) {
            return "false";
        }
        return "0";
    }

    private String getParameters(Parameter[] parameters) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < parameters.length; i++) {
            res.append(parameters[i].getType().getCanonicalName()).append(WHITESPACE).append(parameters[i].getName());
            if (i != parameters.length - 1) {
                res.append(",").append(WHITESPACE);
            }
        }
        return res.toString();
    }

    private String getExceptions(Method method) {
        StringBuilder sb = new StringBuilder();
        Class<?>[] exceptions = method.getExceptionTypes();
        if (exceptions.length == 0)
            return sb.toString();
        for (int i = 0; i < exceptions.length; i++) {
            sb.append(exceptions[i].getCanonicalName());
            if (i != exceptions.length - 1) {
                sb.append(",").append(WHITESPACE);
            }
        }
        return sb.toString();
    }

    private String getMethod(Method method) {
        StringBuilder result = new StringBuilder();
        String retType = method.getReturnType().getCanonicalName();
        String name = method.getName();
        Parameter[] parameters = method.getParameters();

        String stringedParameters = getParameters(parameters);
        // Getting header of method with parameters
        result.append("public").append(WHITESPACE).append(retType)
                .append(WHITESPACE).append(name).append("(");
        result.append(stringedParameters).append(")");

        // Getting exceptions of method
        String exceptions = getExceptions(method);
        if (exceptions.length() != 0) {
            result.append("throws").append(WHITESPACE).append(exceptions);
        }

        // Getting default returning value
        result.append("{").append(SEPARATOR).append("return").append(WHITESPACE);
        result.append(getDefaultValue(method)).append(";").append(SEPARATOR).append("}");
        return result.toString();
    }

    private String methodsImplementing(Class<?> token) {
        StringBuilder res = new StringBuilder();
        Method[] methods = token.getMethods();
        for (Method method : methods) {
            res.append(getMethod(method)).append(SEPARATOR);
        }
        return res.toString();
    }

    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (!token.isInterface() || Modifier.isPrivate(token.getModifiers()) || token.isPrimitive() ||
                token.isArray() || Modifier.isFinal(token.getModifiers()) || token == Enum.class) {
            throw new ImplerException("Wrong token");
        }

        File newClass = filePath(token, root);

        StringBuilder sb = new StringBuilder();
        try (BufferedWriter wr = new BufferedWriter(new FileWriter(newClass))) {
            // Getting Header
            sb.append(headerImplementing(token));
            // Getting methods
            sb.append(methodsImplementing(token));
            sb.append("}").append(SEPARATOR);
            wr.write(sb.toString());
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage() + " Can't write to file");
        }
    }

    public static void main(String[] args) {
        if (args == null) {
            System.err.println("No arguments");
            return;
        }
        if (args.length != 2) {
            System.err.println("More/less arguments, than 2");
            return;
        }
        if (args[0] == null) {
            System.err.println("First argument is null");
            return;
        }
        if (args[1] == null) {
            System.err.println("Second argument is null");
            return;
        }
        try {
            new Implementor().implement(Class.forName(args[0]), Paths.get(args[1]));
        } catch (ImplerException e) {
            System.err.println("ImplerException" + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("ClassNotFound" + e.getMessage());
        }

    }
}
