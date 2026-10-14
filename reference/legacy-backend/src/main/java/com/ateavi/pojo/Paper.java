package com.ateavi.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * @TableName paper
 */
@TableName(value ="paper")
@Data
public class Paper {
    private Long id;

    private String title;

    private String authors;

    private String venue;

    @TableField("abstract")
    private String paperAbstract;

    private Date uploadTime;

    private Integer citations;

    private String impact;

    private String status;

    private String paperUrl;

    private String noteUrl;

    private Date createdAt;

    private Date updatedAt;
}
