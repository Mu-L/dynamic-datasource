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

import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.filter.logging.CommonsLogFilter;
import com.alibaba.druid.filter.logging.Log4j2Filter;
import com.alibaba.druid.filter.logging.Log4jFilter;
import com.alibaba.druid.filter.logging.Slf4jLogFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import com.baomidou.dynamic.datasource.creator.DataSourceCreator;
import com.baomidou.dynamic.datasource.creator.DataSourceProperty;
import com.baomidou.dynamic.datasource.enums.DdConstants;
import com.baomidou.dynamic.datasource.exception.ErrorCreateDataSourceException;
import com.baomidou.dynamic.datasource.toolkit.ConfigMergeCreator;
import com.baomidou.dynamic.datasource.toolkit.DsStrUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Druid数据源创建器
 *
 * @author TaoYu
 * @since 2020/1/21
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class DruidDataSourceCreator implements DataSourceCreator {

    private DruidConfig gConfig;

    private static final ConfigMergeCreator<DruidConfig, DruidConfig> MERGE_CREATOR = new ConfigMergeCreator<>("Druid", DruidConfig.class, DruidConfig.class);

    @Override
    public DataSource createDataSource(DataSourceProperty dataSourceProperty) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUsername(dataSourceProperty.getUsername());
        dataSource.setPassword(dataSourceProperty.getPassword());
        dataSource.setUrl(dataSourceProperty.getUrl());
        dataSource.setName(dataSourceProperty.getPoolName());
        String driverClassName = dataSourceProperty.getDriverClassName();
        if (DsStrUtils.hasText(driverClassName)) {
            dataSource.setDriverClassName(driverClassName);
        }
        DruidConfig config = MERGE_CREATOR.create(gConfig, dataSourceProperty.getDruid());
        DruidConfigUtil.configDataSource(dataSource, config);
        List<Filter> proxyFilters = this.initFilters(config, DruidConfigUtil.configFilter(config));
        dataSource.setProxyFilters(proxyFilters);

        //连接参数单独设置
        dataSource.setConnectProperties(config.getConnectionProperties());

        if (Boolean.FALSE.equals(dataSourceProperty.getLazy())) {
            try {
                dataSource.init();
            } catch (SQLException e) {
                throw new ErrorCreateDataSourceException("druid create error", e);
            }
        }
        return dataSource;
    }

    private List<Filter> initFilters(DruidConfig config, String filters) {
        List<Filter> proxyFilters = new ArrayList<>(2);
        if (DsStrUtils.hasText(filters)) {
            String[] filterItems = filters.split(",");
            for (String filter : filterItems) {
                switch (filter) {
                    case "stat":
                        proxyFilters.add(DruidStatConfigUtil.toStatFilter(config.getStat(), gConfig.getStat()));
                        break;
                    case "wall":
                        WallConfig wallConfig = DruidWallConfigUtil.toWallConfig(config.getWall(), gConfig.getWall());
                        WallFilter wallFilter = new WallFilter();
                        wallFilter.setConfig(wallConfig);
                        proxyFilters.add(wallFilter);
                        break;
                    case "slf4j":
                        proxyFilters.add(DruidLogConfigUtil.instantiateFilter(Slf4jLogFilter.class, config.getSlf4j(), gConfig.getSlf4j()));
                        break;
                    case "commons-log":
                        proxyFilters.add(DruidLogConfigUtil.instantiateFilter(CommonsLogFilter.class, config.getCommonsLog(), gConfig.getCommonsLog()));
                        break;
                    case "log4j":
                        proxyFilters.add(DruidLogConfigUtil.instantiateFilter(Log4jFilter.class, config.getLog4j(), gConfig.getLog4j()));
                        break;
                    case "log4j2":
                        proxyFilters.add(DruidLogConfigUtil.instantiateFilter(Log4j2Filter.class, config.getLog4j2(), gConfig.getLog4j2()));
                        break;
                    default:
                        //ignore
                }
            }
        }
//        if (this.applicationContext != null) {
//            for (String filterId : gConfig.getProxyFilters()) {
//                proxyFilters.add(this.applicationContext.getBean(filterId, Filter.class));
//            }
//        }
        return proxyFilters;
    }

    @Override
    public boolean support(DataSourceProperty dataSourceProperty) {
        Class<? extends DataSource> type = dataSourceProperty.getType();
        return type == null || DdConstants.DRUID_DATASOURCE.equals(type.getName());
    }
}