package info.kgeorgiy.ja.merkulov.implementor;

import info.kgeorgiy.java.advanced.implementor.Impler;
import info.kgeorgiy.java.advanced.implementor.ImplerException;
import info.kgeorgiy.java.advanced.implementor.JarImpler;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Class implementing {@link Impler} and {@link JarImpler}.
 * Creates new class implementing given interface.
 *
 * @author Gh0stfc
 * */
public class Implementor implements Impler, JarImpler {

    /**
     * Line-separator for generated <code>.java</code> files.
     * */
    public static final String SEPARATOR = System.lineSeparator();

    /**
     * Space for generated <code>.java</code> files.
     * */
    public static final String WHITESPACE = " ";

    /**
     * Checking for existing of path, where generated <code>.java</code>
     * file will be placed
     *
     * @param token type token to create valid path of new class
     * @param root root directory
     * @throws ImplerException if there will not be directory
     * to store new <code>.java</code> file
     * @return {@link String} that is path where new <code>.java</code> file will be placed
     * */
    private String filePath(Class<?> token, Path root) throws ImplerException {
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
        return path;
    }

    /**
     * Implementing upper part of file, such package, class name and its parents
     * @param token token for creating valid class-name
     * @return upper part of file in {@link String} value
     * */
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

    /**
     * Getting default value of entity
     * @param method needed for getting returning-type of this method
     * @return default value of entity in {@link String} value
     * */
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

    /**
     * Parsing {@link Parameter[]} parameters to {@link String}
     * @param parameters parameters we need parse
     * @return parameters in {@link String} value
     * */
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


    /**
     * Getting exceptions to specified method
     * @param method {@link Method} whose exceptions are parsing
     * @return exceptions in {@link String} value
     * */
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

    /**
     * Implementing all method, including top, exceptions and body
     * @param method specified method, which must be implemented
     * @return whole method in {@link String} value
     * */
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

        // Getting exceptions to method
        String exceptions = getExceptions(method);
        if (exceptions.length() != 0) {
            result.append("throws").append(WHITESPACE).append(exceptions);
        }

        // Getting default returning value
        result.append("{").append(SEPARATOR).append("return").append(WHITESPACE);
        result.append(getDefaultValue(method)).append(";").append(SEPARATOR).append("}");
        return result.toString();
    }


    /**
     * Implementing all methods, declared in specified interface
     * @param token token, should be implemented
     * @return whole implemented methods in {@link String} value
     * */
    private String methodsImplementing(Class<?> token) {
        StringBuilder res = new StringBuilder();
        Method[] methods = token.getMethods();
        for (Method method : methods) {
            res.append(getMethod(method)).append(SEPARATOR);
        }
        return res.toString();
    }

    /**
     * Produces code implementing class or interface specified by provided {@code token}.
     * @param token type token to create implementation for.
     * @param root root directory.
     * @throws ImplerException when implementation cannot be generated.
     */
    @Override
    public void implement(Class<?> token, Path root) throws ImplerException {
        if (!token.isInterface() || Modifier.isPrivate(token.getModifiers()) || token.isPrimitive() ||
                token.isArray() || Modifier.isFinal(token.getModifiers()) || token == Enum.class) {
            throw new ImplerException("Wrong token");
        }

//        File newClass = filePath(token, root);
        String path = filePath(token, root);
        StringBuilder sb = new StringBuilder();

//        try (BufferedWriter wr = new BufferedWriter(new FileWriter(newClass))) {
        try (BufferedWriter wr = Files.newBufferedWriter(Paths.get(path))) {
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

    /**
     * Calling {@link Implementor#implement(Class, Path)} or {@link Implementor#implementJar(Class, Path)}
     * depends on number of arguments
     * @param args name of interface will be implemented
     * */
    public static void main(String[] args) {
        if (args == null) {
            System.err.println("No arguments");
            return;
        }
        if (args.length != 2 && args.length != 3) {
            System.err.println("More/less arguments, than 2/3");
            return;
        }
        if (args[0] == null || args[1] == null) {
            System.err.println("First/Second argument is null");
            return;
        }
        if (args.length == 3 && args[2] == null) {
            System.err.println("Third argument is null");
            return;
        }
        try {
            if (args.length == 2)
                new Implementor().implement(Class.forName(args[0]), Paths.get(args[1]));
            else
                new Implementor().implementJar(Class.forName(args[1]), Paths.get(args[2]));
        } catch (ImplerException e) {
            System.err.println("ImplerException " + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.err.println("ClassNotFound " + e.getMessage());
        }

    }


    /**
     * Produces <var>.jar</var> file implementing interface specified by provided <var>token</var>.
     * @param token type token to create implementation for.
     * @param jarFile target <var>.jar</var> file.
     * @throws ImplerException when implementation cannot be generated.
     */
    @Override
    public void implementJar(Class<?> token, Path jarFile) throws ImplerException {
        implement(token, Paths.get("."));
        String pathOfImplementingFile = Paths.get(token.getPackageName().replace(".", File.separator))
                .resolve(token.getSimpleName() + "Impl").toString();
        try {
            String[] args = {pathOfImplementingFile + ".java",
                    "-cp",
                    // taken from kgeorgiy's code
                    File.pathSeparator + getClassPath(token),
                    "-encoding",
                    "UTF-8"
            };
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            int exitCode = compiler.run(null, null, null, args);
            if (exitCode != 0) {
                throw new ImplerException("Can't compile file");
            }
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        try (ZipOutputStream outputStream = new JarOutputStream(Files.newOutputStream(jarFile))) {
            String pathOfClassFile = pathOfImplementingFile.replace(File.separator, "/") + ".class";
            outputStream.putNextEntry(new ZipEntry(pathOfClassFile));
            Files.copy(Paths.get(pathOfClassFile), outputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static Path getClassPath(Class<?> token) throws URISyntaxException {
        return Path.of(token.getProtectionDomain().getCodeSource().getLocation().toURI());
    }
}
