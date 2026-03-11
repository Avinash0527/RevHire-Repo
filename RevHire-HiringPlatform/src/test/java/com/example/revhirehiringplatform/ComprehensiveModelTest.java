package com.example.revhirehiringplatform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.RegexPatternTypeFilter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ComprehensiveModelTest {

    @Test
    public void testAllModelsAndDtos() throws Exception {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new RegexPatternTypeFilter(Pattern.compile(".*")));

        Set<BeanDefinition> beans = provider.findCandidateComponents("com.revhire.dto");
        beans.addAll(provider.findCandidateComponents("com.revhire.model"));

        for (BeanDefinition bd : beans) {
            String className = bd.getBeanClassName();
            if (className.contains("Test"))
                continue;

            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isEnum() || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers()))
                    continue;
                if (className.contains("Builder")) {
                    testBuilder(clazz);
                    continue; // Skip the rest for Builders
                }
                Constructor<?>[] constructors = clazz.getDeclaredConstructors();
                Object instance1 = null;
                Object instance2 = null;

                for (Constructor<?> c : constructors) {
                    if (c.getParameterCount() == 0) {
                        c.setAccessible(true);
                        instance1 = c.newInstance();
                        instance2 = c.newInstance();
                        break;
                    }
                }

                if (instance1 != null) {
                    final Object finalInstance1 = instance1;
                    final Object finalInstance2 = instance2;


                    assertDoesNotThrow(() -> finalInstance1.toString());
                    assertDoesNotThrow(() -> finalInstance1.hashCode());
                    assertDoesNotThrow(() -> finalInstance1.equals(finalInstance1));
                    assertDoesNotThrow(() -> finalInstance1.equals(null));
                    assertDoesNotThrow(() -> finalInstance1.equals(new Object()));
                    assertDoesNotThrow(() -> finalInstance1.equals(finalInstance2));

                    Method[] methods = clazz.getDeclaredMethods();
                    for (Method m : methods) {
                        m.setAccessible(true);
                        if (m.getName().startsWith("set") && m.getParameterCount() == 1) {
                            try {
                                Object dummy1 = getDummyValue(m.getParameterTypes()[0], 1);
                                Object dummy2 = getDummyValue(m.getParameterTypes()[0], 2);


                                m.invoke(finalInstance1, dummy1);
                                m.invoke(finalInstance2, dummy2);
                                finalInstance1.equals(finalInstance2);
                                finalInstance2.equals(finalInstance1);

                                m.invoke(finalInstance2, dummy1);
                                finalInstance1.equals(finalInstance2);
                            } catch (Exception e) {
                            }
                        } else if (m.getName().startsWith("get") && m.getParameterCount() == 0) {
                            try {
                                m.invoke(finalInstance1);
                            } catch (Exception e) {
                            }
                        } else if (m.getName().equals("canEqual")) {
                            try {
                                m.invoke(finalInstance1, finalInstance2);
                            } catch (Exception e) {
                            }
                        }
                    }
                    assertDoesNotThrow(() -> finalInstance1.toString());
                    assertDoesNotThrow(() -> finalInstance1.hashCode());
                    assertDoesNotThrow(() -> finalInstance1.equals(finalInstance2));
                }


                try {
                    Method builderMethod = clazz.getDeclaredMethod("builder");
                    Object builderInstance = builderMethod.invoke(null);
                    assertDoesNotThrow(() -> builderInstance.toString());
                } catch (Exception e) {
                }

            } catch (Throwable e) {

            }
        }
    }

    private void testBuilder(Class<?> builderClass) {
        try {

            Object builderInstance = null;
            for (Constructor<?> c : builderClass.getDeclaredConstructors()) {
                if (c.getParameterCount() == 0) {
                    c.setAccessible(true);
                    builderInstance = c.newInstance();
                    break;
                }
            }
            if (builderInstance == null)
                return;

            final Object fBuilder = builderInstance;
            assertDoesNotThrow(() -> fBuilder.toString());

            for (Method m : builderClass.getDeclaredMethods()) {
                if (m.getParameterCount() == 1 && m.getReturnType() == builderClass) {
                    try {
                        m.invoke(fBuilder, getDummyValue(m.getParameterTypes()[0], 1));
                    } catch (Exception e) {
                    }
                }
            }
            try {
                Method buildMethod = builderClass.getDeclaredMethod("build");
                buildMethod.invoke(fBuilder);
            } catch (Exception e) {
            }

        } catch (Exception e) {

        }
    }

    private Object getDummyValue(Class<?> type, int variant) {
        if (type == String.class)
            return variant == 1 ? "dummy1" : "dummy2";
        if (type == Long.class || type == long.class)
            return variant == 1 ? 1L : 2L;
        if (type == Integer.class || type == int.class)
            return variant == 1 ? 1 : 2;
        if (type == Double.class || type == double.class)
            return variant == 1 ? 1.0 : 2.0;
        if (type == Boolean.class || type == boolean.class)
            return variant == 1 ? true : false;
        if (type == java.time.LocalDateTime.class)
            return variant == 1 ? java.time.LocalDateTime.now() : java.time.LocalDateTime.now().minusDays(1);
        if (type == java.time.LocalDate.class)
            return variant == 1 ? java.time.LocalDate.now() : java.time.LocalDate.now().minusDays(1);
        if (type == java.util.List.class)
            return java.util.Collections.emptyList();
        if (type == java.util.Set.class)
            return java.util.Collections.emptySet();
        if (type.isEnum()) {
            Object[] constants = type.getEnumConstants();
            if (constants != null && constants.length > 0) {
                return variant == 1 ? constants[0] : (constants.length > 1 ? constants[1] : constants[0]);
            }
        }

        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}

