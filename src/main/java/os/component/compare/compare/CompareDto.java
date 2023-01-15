package os.component.compare.compare;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Bean比较后得到的结果
 */
@Getter
@Setter
@ToString
public class CompareDto {
    /**
     * 主键
     */
    private Serializable id;
    /**
     * 该次数据变更关联的外部ID主键
     */
    private Serializable foreignId;
    /**
     * 该次比较、多个类型分类
     */
    private String classType;
    /**
     * 相同类型、指定compareKey相同的进行比较
     */
    private String compareKey;
    /**
     * 字段别名
     */
    private String fieldNote;
    /**
     * 旧值
     */
    private String oldValue;
    /**
     * 新值
     */
    private String newValue;
    /**
     * 对比时间
     */
    private LocalDateTime createTime;
}
