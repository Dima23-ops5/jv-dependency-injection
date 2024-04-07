package mate.academy.lib;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import mate.academy.service.FileReaderService;
import mate.academy.service.ProductParser;
import mate.academy.service.ProductService;
import mate.academy.service.impl.FileReaderServiceImpl;
import mate.academy.service.impl.ProductParserImpl;
import mate.academy.service.impl.ProductServiceImpl;

public class Injector {
    private static final Injector injector = new Injector();
    private final Map<Class<?>, Object> instances = new HashMap<>();
    private static final Map<Class<?>, Class<?>> MAP_IPLEMENTATYON = Map.of(
            FileReaderService.class, FileReaderServiceImpl.class,
            ProductParser.class, ProductParserImpl.class,
            ProductService.class, ProductServiceImpl.class);

    public static Injector getInjector() {
        return injector;
    }

    public Object getInstance(Class<?> interfaceClazz) {
        Object classImplementationInstanse = null;
        Class<?> clazz = findImplementation(interfaceClazz);
        if (instances.containsKey(clazz)) {
            return instances.get(clazz);
        }

        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            classImplementationInstanse = constructor.newInstance();
            instances.put(clazz, classImplementationInstanse);
            Field[] declaredFields = clazz.getDeclaredFields();
            for (Field field : declaredFields) {
                if (field.isAnnotationPresent(Inject.class)) {
                    Object fieldInstans = getInstance(field.getType());
                    field.setAccessible(true);
                    field.set(classImplementationInstanse, fieldInstans);
                }
            }
        } catch (NoSuchMethodException | InstantiationException
                 | IllegalAccessException
                 | InvocationTargetException e) {
            throw new RuntimeException("Can not create new instance :" + e);
        }
        return classImplementationInstanse;
    }

    public Class<?> findImplementation(Class<?> interfaceClazz) {
        Class<?> implementationClass = MAP_IPLEMENTATYON.get(interfaceClazz);
        if (interfaceClazz.isInterface()) {
            if (implementationClass != null
                    && implementationClass.isAnnotationPresent(Component.class)) {
                return implementationClass;
            } else {
                throw new RuntimeException("Class doesn't annoted Component!");
            }
        } else {
            if (interfaceClazz.isAnnotationPresent(Component.class)) {
                return interfaceClazz;
            } else {
                throw new RuntimeException("Class doesn't annoted Component!");
            }
        }
    }
}
