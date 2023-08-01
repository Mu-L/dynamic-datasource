/*
 * Copyright © 2018 organization baomidou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baomidou.dynamic.datasource.toolkit;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * 用于合并配置并转换成目标配置的工具类
 *
 * @param <C> 自己的配置类
 * @param <T> 目标的配置类
 * @author TaoYu
 */
@Slf4j
public class ConfigMergeCreator<C, T> {

    private final String configName;

    private final Class<C> configClazz;

    private final Class<T> targetClazz;

    private PropertyDescriptor[] descriptors;

    public ConfigMergeCreator(String configName, Class<C> configClazz, Class<T> targetClazz) {
        this.configName = configName;
        this.configClazz = configClazz;
        this.targetClazz = targetClazz;
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(configClazz, Object.class);
            descriptors = beanInfo.getPropertyDescriptors();
        } catch (Exception ignore) {

        }
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    public T create(C global, C item) {
        if (configClazz.equals(targetClazz) && global == null) {
            return (T) item;
        }
        T result = targetClazz.getDeclaredConstructor().newInstance();
        for (PropertyDescriptor pd : descriptors) {
            Class<?> propertyType = pd.getPropertyType();
            if (Properties.class == propertyType) {
                mergeProperties(global, item, result, pd);
            } else if (Collection.class.isAssignableFrom(propertyType)) {
                mergeCollection(global, item, result, pd);
            } else if (Map.class.isAssignableFrom(propertyType)) {
                mergeMap(global, item, result, pd);
            } else {
                mergeBasic(global, item, result, pd);
            }
        }
        return result;
    }

    @SneakyThrows
    private void mergeMap(C global, C item, T result, PropertyDescriptor pd) {
        String name = pd.getName();
        Method readMethod = pd.getReadMethod();
        Map itemValue = (Map) readMethod.invoke(item);
        Map globalValue = global == null ? null : (Map) readMethod.invoke(global);
        Map mergeResult = new HashMap();

        if (globalValue != null) {
            mergeResult.putAll(globalValue);
        }
        if (itemValue != null) {
            mergeResult.putAll(itemValue);
        }
        if (!mergeResult.isEmpty()) {
            setField(result, name, mergeResult);
        }
    }

    @SneakyThrows
    private void mergeCollection(C global, C item, T result, PropertyDescriptor pd) {
        String name = pd.getName();
        Method readMethod = pd.getReadMethod();
        Class<?> propertyType = pd.getPropertyType();
        Collection<?> itemValue = (Collection<?>) readMethod.invoke(item);
        Collection<?> globalValue = global == null ? null : (Collection<?>) readMethod.invoke(global);
        Collection mergeResult;
        if (propertyType.isAssignableFrom(List.class)) {
            mergeResult = new ArrayList<>();
        } else if (propertyType.isAssignableFrom(Set.class)) {
            mergeResult = new HashSet<>();
        } else return;
        if (globalValue != null) {
            mergeResult.addAll(globalValue);
        }
        if (itemValue != null) {
            mergeResult.addAll(itemValue);
        }
        if (!mergeResult.isEmpty()) {
            setField(result, name, mergeResult);
        }
    }

    @SneakyThrows
    private void mergeProperties(C global, C item, T result, PropertyDescriptor pd) {
        String name = pd.getName();
        Method readMethod = pd.getReadMethod();
        Properties itemValue = (Properties) readMethod.invoke(item);
        Properties globalValue = global == null ? null : (Properties) readMethod.invoke(global);
        Properties mergeResult = new Properties();
        if (globalValue != null) {
            mergeResult.putAll(globalValue);
        }
        if (itemValue != null) {
            mergeResult.putAll(itemValue);
        }
        if (!mergeResult.isEmpty()) {
            setField(result, name, mergeResult);
        }
    }

    @SneakyThrows
    private void mergeBasic(C global, C item, T result, PropertyDescriptor pd) {
        String name = pd.getName();
        Method readMethod = pd.getReadMethod();
        Object value = readMethod.invoke(item);
        if (value == null && global != null) {
            value = readMethod.invoke(global);
        }
        if (value != null) {
            setField(result, name, value);
        }
    }

    private void setField(T result, String name, Object value) {
        try {
            PropertyDescriptor propertyDescriptor = new PropertyDescriptor(name, targetClazz);
            Method writeMethod = propertyDescriptor.getWriteMethod();
            writeMethod.invoke(result, value);
        } catch (IntrospectionException | ReflectiveOperationException e) {
            Field field = null;
            try {
                field = targetClazz.getDeclaredField(name);
                field.setAccessible(true);
                field.set(result, value);
            } catch (ReflectiveOperationException e1) {
                log.warn("dynamic-datasource set {} [{}] failed,please check your config or update {}  to the latest version", configName, name, configName);
            } finally {
                if (field != null) {
                    field.setAccessible(false);
                }
            }
        } catch (Exception ee) {
            log.warn("dynamic-datasource set {} [{}] failed,please check your config", configName, name, ee);
        }
    }

}