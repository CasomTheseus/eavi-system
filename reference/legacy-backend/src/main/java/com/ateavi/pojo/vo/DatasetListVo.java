package com.ateavi.pojo.vo;

import lombok.Data;

/**
 * ClassName:DatasetListVo
 * Package:
 * Description:
 *
 * @Author MoHan
 * @Create 2025/10/20 23:06
 * @Version 1.0
 */
@Data
public class DatasetListVo {

    private Integer page = 1;
    private Integer pageSize = 20;
    private String search;
    private Integer taskId;
    private String status;
    private String sortBy;
    private String sortOrder;
}
