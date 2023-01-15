package os.component.compare.compare;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import os.component.compare.model.Document;
import os.component.compare.model.Passenger;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 对象字段变更视图
 */
@Getter
@Setter
@ToString
public class CompareVO {
    /**
     * 比较类型
     */
    private Serializable classType;
    /**
     * 比较基准字段内容
     */
    private Serializable compareKey;
    /**
     * 记录哪些字段发生了变更
     */
    private Map<String, String> changeFieldMap = new HashMap<>();

    /**
     * 转换后的视图、以字符串方式展示
     */
    private String convertedView;

    /**
     * 视图转换、直接展示
     */
    public void convertStrView() {
        Collection<String> values = changeFieldMap.values();
        if (values.size() > 0) {
            StringBuilder sb = new StringBuilder();
            if (Passenger.class.getCanonicalName().equals(classType)) {
                for (String fieldNote : values) {
                    sb.append(fieldNote).append("\n");
                }
            } else if (Document.class.getCanonicalName().equals(classType)) {
                sb.append("Doc ").append(compareKey).append("(");
                for (String fieldNote : values) {
                    sb.append(fieldNote);
                    sb.append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                sb.append(")");
            }
            this.convertedView = sb.toString();
        }
    }
}
