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

import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * Druid参数配置
 *
 * @author TaoYu
 * @since 1.2.0
 */
@Getter
@Setter
public class DruidConfig {

    private Integer initialSize;
    private Integer minIdle;
    private Integer maxActive;
    private Integer maxWait;
    private Integer maxWaitThreadCount;
    private Integer notFullTimeoutRetryCount;
    private Integer maxPoolPreparedStatementPerConnectionSize;

    private Long minEvictableIdleTimeMillis;
    private Long maxEvictableIdleTimeMillis;
    private Long timeBetweenEvictionRunsMillis;
    private Long timeBetweenLogStatsMillis;
    private Long phyTimeoutMillis;
    private Long phyMaxUseCount;
    private Long keepAliveBetweenTimeMillis;

    private Boolean keepAlive;
    private Boolean asyncInit;
    private Boolean failFast;
    private Boolean testWhileIdle;
    private Boolean testOnBorrow;
    private Boolean initVariants;
    private Boolean useUnfairLock;
    private Boolean resetStatEnable;
    private Boolean initGlobalVariants;
    private Boolean useGlobalDataSourceStat;
    private Boolean killWhenSocketReadTimeout;
    private Boolean clearFiltersEnable;
    private Boolean poolPreparedStatements;

    private String filters;
    private String validationQuery;
    private String initConnectionSqls;

    private Properties connectionProperties;

    //上面是connectionProperties里支持的属性

    private Map<String, Object> extra = new HashMap<>();


    private Integer statSqlMaxSize;
    private String defaultCatalog;
    private Boolean defaultAutoCommit;
    private Boolean defaultReadOnly;
    private Integer defaultTransactionIsolation;
    private Boolean testOnReturn;
    private Integer validationQueryTimeout;


    private Boolean sharePreparedStatements;
    private Integer connectionErrorRetryAttempts;
    private Boolean breakAfterAcquireFailure;
    private Boolean removeAbandoned;
    private Integer removeAbandonedTimeoutMillis;
    private Boolean logAbandoned;
    private Integer queryTimeout;
    private Integer transactionQueryTimeout;
    private String publicKey;
    private Integer connectTimeout;
    private Integer socketTimeout;
    private Long timeBetweenConnectErrorMillis;

    private Map<String, Object> wall = new HashMap<>();
    private Map<String, Object> slf4j = new HashMap<>();
    private Map<String, Object> log4j = new HashMap<>();
    private Map<String, Object> log4j2 = new HashMap<>();
    private Map<String, Object> commonsLog = new HashMap<>();
    private Map<String, Object> stat = new HashMap<>();

    private List<String> proxyFilters = new ArrayList<>();
}