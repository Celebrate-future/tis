/**
 * Copyright (c) 2020 QingLang, Inc. <baisui@qlangtech.com>
 * <p>
 * This program is free software: you can use, redistribute, and/or modify
 * it under the terms of the GNU Affero General Public License, version 3
 * or later ("AGPL"), as published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.qlangtech.tis.manage.biz.dal.pojo;

import org.apache.commons.lang.StringUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 百岁（baisui@qlangtech.com）
 * @date 2020/04/13
 */
public class Application implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer appId;
    private Integer appType;

    private String projectName;

    public boolean hasAppendSearch4Prefix = false;

    private String recept;

    private String manager;

    private Date createTime;

    private Date updateTime;

    private String isDeleted;

    private Boolean isAutoDeploy;

    /**
     * prop:dpt_id
     */
    private Integer dptId;

    /**
     * prop:dpt_name
     */
    private String dptName;


    private String fullBuildCronTime;


    public Integer getAppId() {
        return this.appId;
    }

    public Integer getAppType() {
        return this.appType;
    }

    public AppType parseAppType() {
        return AppType.parse(this.appType);
    }

    public void setAppType(Integer appType) {
        this.appType = appType;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName == null ? null : projectName.trim();
    }

    public String getRecept() {
        return recept;
    }

    public void setRecept(String recept) {
        this.recept = recept == null ? null : recept.trim();
    }

    public String getManager() {
        return manager;
    }

    public void setManager(String manager) {
        this.manager = manager == null ? null : manager.trim();
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted == null ? null : isDeleted.trim();
    }

    public Boolean getIsAutoDeploy() {
        return isAutoDeploy;
    }

    public void setIsAutoDeploy(Boolean isAutoDeploy) {
        this.isAutoDeploy = isAutoDeploy;
    }

    /**
     * get:dpt_id
     */
    public Integer getDptId() {
        return dptId;
    }

    /**
     * set:dpt_id
     */
    public void setDptId(Integer dptId) {
        this.dptId = dptId;
    }

    /**
     * get:dpt_name
     */
    public String getDptName() {
        return dptName;
    }

    /**
     * set:dpt_name
     */
    public void setDptName(String dptName) {
        this.dptName = dptName == null ? null : dptName.trim();
    }

    public Integer getWorkFlowId() {
        return 0;
    }

    public String getFullBuildCronTime() {
        return this.fullBuildCronTime;
    }

    public void setFullBuildCronTime(String fullBuildCronTime) {
        this.fullBuildCronTime = StringUtils.trimToNull(fullBuildCronTime);
    }
}
