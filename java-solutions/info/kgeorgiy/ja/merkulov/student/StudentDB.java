package info.kgeorgiy.ja.merkulov.student;

import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class StudentDB implements StudentQuery {

    private <B> List<B> func(Function<? super Student, B> foo, List<Student> students) {
        return students.stream()
                .map(foo)
                .collect(Collectors.toList());
    }


    @Override
    public List<String> getFirstNames(List<Student> students) {
        return func(Student::getFirstName, students);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return func(Student::getLastName, students);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return func(Student::getGroup, students);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return func(s -> s.getFirstName() + " " + s.getLastName(), students);
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return students.stream()
                .sorted(Comparator.comparing(Student::getFirstName))
                .map(Student::getFirstName)
                .collect(Collectors.toSet());
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream()
                .max(Student::compareTo)
                .map(Student::getFirstName)
                .orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return students.stream()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }


    private final Comparator<Student> comparator = Comparator.comparing(Student::getLastName)
            .thenComparing(Student::getFirstName)
            .reversed()
            .thenComparing(Student::compareTo);

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return students.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    private List<Student> finder(Predicate<? super Student> foo, Collection<Student> students) {
        return students.stream()
                .filter(foo)
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return finder(student -> name.equals(student.getFirstName()), students);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return finder(student -> name.equals(student.getLastName()), students);

    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return finder(student -> group.equals(student.getGroup()), students);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return students.stream()
                .filter(student -> group.equals(student.getGroup()))
                .collect(Collectors.toMap(Student::getLastName,
                        Student::getFirstName,
                        (s1, s2) -> s1.compareTo(s2) < 0 ? s1 : s2));
    }

}
