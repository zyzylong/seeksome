package com.zyzylong.seeksome.item.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 拍品分类数据对象。
 */
@Data
@Builder
@TableName("category")
public class CategoryDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String code;

    private String name;

    private Integer sort;

    @TableField("is_enabled")
    private Boolean enabled;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
