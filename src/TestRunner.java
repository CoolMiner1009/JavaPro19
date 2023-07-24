import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface Test {
    int priority() default 5;
}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface BeforeSuite {}

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@interface AfterSuite {}


public class TestRunner {
    public static void start(Class<?> testClass) {
        Method beforeSuiteMethod = null;
        Method afterSuiteMethod = null;
        List<Method> testMethods = new ArrayList<>();

        // Збираємо методи тестів, BeforeSuite та AfterSuite
        Method[] methods = testClass.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isAnnotationPresent(BeforeSuite.class)) {
                if (beforeSuiteMethod == null) {
                    beforeSuiteMethod = method;
                } else {
                    throw new RuntimeException("BeforeSuite method already exists in the test class");
                }
            } else if (method.isAnnotationPresent(AfterSuite.class)) {
                if (afterSuiteMethod == null) {
                    afterSuiteMethod = method;
                } else {
                    throw new RuntimeException("AfterSuite method already exists in the test class");
                }
            } else if (method.isAnnotationPresent(Test.class)) {
                testMethods.add(method);
            }
        }

        // Виконуємо метод BeforeSuite, якщо він є
        if (beforeSuiteMethod != null) {
            executeMethod(beforeSuiteMethod);
        }

        // Сортуємо методи тестів за пріоритетом
        testMethods.sort(Comparator.comparingInt(method -> method.getAnnotation(Test.class).priority()));

        // Виконуємо методи тестів
        for (Method testMethod : testMethods) {
            executeMethod(testMethod);
        }

        // Виконуємо метод AfterSuite, якщо він є
        if (afterSuiteMethod != null) {
            executeMethod(afterSuiteMethod);
        }
    }

    private static void executeMethod(Method method) {
        try {
            Object testClassInstance = method.getDeclaringClass().getDeclaredConstructor().newInstance();
            method.invoke(testClassInstance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

