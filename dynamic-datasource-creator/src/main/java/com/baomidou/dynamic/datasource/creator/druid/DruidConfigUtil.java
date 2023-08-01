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
package com.baomidou.dynamic.datasource.creator.druid;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.dynamic.datasource.toolkit.DsConfigUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Properties;

/**
 * Druid配置工具类
 *
 * @author TaoYu
 * @since 4.0.0
 */
@Slf4j
public final class DruidConfigUtil {

    private static final String FILTERS = "druid.filters";

    private static final String CONFIG_STR = "config";
    private static final String STAT_STR = "stat";

    private static final Map<String, PropertyDescriptor> DATASOURCE_DESCRIPTOR_MAP = DsConfigUtil.getPropertyDescriptorMap(DruidDataSource.class);
    private static final Map<String, PropertyDescriptor> CONFIG_DESCRIPTOR_MAP = DsConfigUtil.getPropertyDescriptorMap(DruidConfig.class);


    /**
     * 根据全局配置和本地配置结合转换为Properties
     *
     * @param c 当前配置
     * @return Druid配置
     */
    public static String configFilter(DruidConfig c) {
        //filters单独处理，默认了stat
        String filters = c.getFilters();
        if (filters == null) {
            filters = STAT_STR;
        }
        String publicKey = c.getPublicKey();
        boolean configFilterExist = publicKey != null && !publicKey.isEmpty();
        if (configFilterExist && !filters.contains(CONFIG_STR)) {
            filters += "," + CONFIG_STR;
        }

        Properties connectProperties = c.getConnectionProperties();
        if (configFilterExist) {
            connectProperties.setProperty("config.decrypt", Boolean.TRUE.toString());
            connectProperties.setProperty("config.decrypt.key", publicKey);
        }
        return filters;
    }


    /**
     * 设置DruidDataSource的值
     *
     * @param dataSource DruidDataSource
     * @param c          当前配置
     */
    @SneakyThrows
    public static void configDataSource(DruidDataSource dataSource, DruidConfig c) {
        for (Map.Entry<String, PropertyDescriptor> configEntry : CONFIG_DESCRIPTOR_MAP.entrySet()) {
            String field = configEntry.getKey();
            PropertyDescriptor configDescriptor = configEntry.getValue();
            Method readMethod = configDescriptor.getReadMethod();
            Object value = readMethod.invoke(c);
            if (value != null) {
                PropertyDescriptor descriptor = DATASOURCE_DESCRIPTOR_MAP.get(field);
                if (descriptor == null) {
                    log.warn("druid current not support [" + field + " ]");
                    return;
                }
                Method writeMethod = descriptor.getWriteMethod();
                if (writeMethod == null) {
                    log.warn("druid current could not set  [" + field + " ]");
                    return;
                }
                try {
                    writeMethod.invoke(dataSource, value);
                } catch (Exception e) {
                    log.warn("druid current  set  [" + field + " ] error", e);
                }
            }
        }
    }

}