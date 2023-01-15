package os.component.compare.compare;

import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;

import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据字段对比工具类
 */
public class CompareUtils {

    /**
     * 获取最新一次操作记录所有的字段变更并分组, 返回对应的变更过的字段
     *
     * @param compareDOList 最新一次操作记录变更的字段列表
     * @param clazzs        涉及到的类列表
     */
    public static final List<CompareVO> getChangedFiledMap(List<Class> clazzs, List<CompareDO> compareDOList) {
        Map<String, String> fieldNoteMap = new HashMap<>();
        for (Class clazz : clazzs) {
            fieldNoteMap.putAll(getFieldNoteMap(clazz));
        }
        return getChangedFiledMap(fieldNoteMap, compareDOList);
    }

    /**
     * 获取一次操作记录所有的字段变更并分组、进行字段名转换
     *
     * @param compareDOList 一次操作记录的所有字段变更记录
     * @param fieldNoteMap  多个类, 变更的字段名不能相同
     */
    private static final List<CompareVO> getChangedFiledMap(Map<String, String> fieldNoteMap,
                                                            List<CompareDO> compareDOList) {
        List<CompareVO> compareVOList = new ArrayList<>();
        if (compareDOList == null || compareDOList.isEmpty()) {
            return compareVOList;
        }

        Map<String, List<CompareDO>> classTypeCompare = compareDOList.
                stream().collect(Collectors.groupingBy(CompareDO::getClassType));
        classTypeCompare.forEach((classType, compareList) -> {
            Map<String, List<CompareDO>> compareKeyCompare = compareList.
                    stream().collect(Collectors.groupingBy(CompareDO::getCompareKey));
            compareKeyCompare.forEach((compareKey, compares) -> {
                CompareVO compareVO = new CompareVO();
                compareVO.setClassType(classType);
                compareVO.setCompareKey(compareKey);
                compareVO.setChangeFieldMap(new HashMap<>());
                for (CompareDO compareDO : compares) {
                    compareVO.getChangeFieldMap().put(fieldNoteMap.get(compareDO.getFieldNote()), compareDO.getFieldNote());
                }
                compareVO.convertStrView();
                compareVOList.add(compareVO);
            });
        });
        return compareVOList;
    }

    /**
     * 多个Bean比较、返回结果没有主键ID
     *
     * @param foreignId 对比结果关联的外键ID、比如操作记录ID
     */
    public static final List<CompareDO> compareBeanList(Serializable foreignId,
                                                        List<Object> sources, List<Object> targets) throws Exception {
        // sources为空、则认为targets全部在变更中新增
        Assert.notNull(sources, "sources can not be null.");
        Assert.notNull(targets, "targets can not be null.");
        Assert.notEmpty(targets, "targets can not be empty.");
        List<CompareDO> compareDOList = new ArrayList<>();
        for (Object target : targets) {
            String targetCompareKey = getCompareFieldValue(target);
            String targetClassName = target.getClass().getCanonicalName();
            Map<String, String> targetMap = getCompareMap(target);
            boolean find = false;
            for (Object source : sources) {
                String sourceCompareKey = getCompareFieldValue(source);
                String sourceClassName = source.getClass().getCanonicalName();
                // 同类同compareKey对比
                if (!"".equals(sourceCompareKey) && sourceCompareKey.equals(targetCompareKey) &&
                        sourceClassName.equals(targetClassName)) {
                    Map<String, String> sourceMap = getCompareMap(source);
                    compareDOList.addAll(compareMap(sourceMap, targetMap, foreignId, targetCompareKey, targetClassName));
                    find = true;
                    break;
                }
            }
            // 如果没找到、则表示变更中新增、所有属性全部标记为变更
            if (!find) {
                compareDOList.addAll(compareMap(new HashMap<>(), targetMap, foreignId, targetCompareKey, targetClassName));
            }
        }
        return compareDOList;
    }

    /**
     * 单个Bean比较、返回结果没有主键ID
     *
     * @param foreignId 对比结果关联的外键ID、比如操作记录ID
     */
    public static final List<CompareDO> compareBean(Serializable foreignId,
                                                    Object source, Object target) throws Exception {
        Assert.notNull(source, "source can not be null.");
        Assert.notNull(target, "target can not be null.");
        List<CompareDO> compareDOList = new ArrayList<>();
        String sourceCompareKey = getCompareFieldValue(source);
        String targetCompareKey = getCompareFieldValue(target);
        String sourceClassName = source.getClass().getCanonicalName();
        String targetClassName = target.getClass().getCanonicalName();
        if ("".equals(sourceCompareKey) || !sourceCompareKey.equals(targetCompareKey) ||
                !sourceClassName.equals(targetClassName)) {
            return compareDOList;
        }
        Map<String, String> sourceMap = getCompareMap(source);
        Map<String, String> targetMap = getCompareMap(target);
        return compareMap(sourceMap, targetMap, foreignId, targetCompareKey, targetClassName);
    }

    /**
     * Map 字段数据对比
     */
    private static final List<CompareDO> compareMap(Map<String, String> sourceMap, Map<String, String> targetMap,
                                                    Serializable foreignId, String compareKey, String classType) {
        List<CompareDO> compareDOList = new ArrayList<>();
        for (Map.Entry<String, String> entry : targetMap.entrySet()) {
            String fieldNote = entry.getKey();
            String newValue = entry.getValue();
            if (!"".equals(newValue) && !newValue.equals(sourceMap.get(fieldNote))) {
                CompareDO compareDO = new CompareDO();
                compareDO.setForeignId(foreignId);
                compareDO.setCompareKey(compareKey);
                compareDO.setClassType(classType);
                compareDO.setFieldNote(fieldNote);
                compareDO.setNewValue(newValue);
                compareDO.setOldValue(sourceMap.getOrDefault(fieldNote, ""));
                compareDO.setCreateTime(LocalDateTime.now());
                compareDOList.add(compareDO);
            }
        }
        return compareDOList;
    }

    /**
     * 获取Compare注解的字段的 fieldNote-> fieldValue Map
     */
    private static final Map<String, String> getCompareMap(Object obj) throws Exception {
        Assert.notNull(obj, "Object can not be null.");
        Map<String, String> fieldValueMap = new HashMap<>();
        Field[] declaredFields = obj.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            Compare annotation = field.getAnnotation(Compare.class);
            if (annotation != null) {
                PropertyDescriptor propertyDescriptor = BeanUtils.
                        getPropertyDescriptor(obj.getClass(), field.getName());
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null) {
                    fieldValueMap.put(annotation.value(), getStrValue(readMethod.invoke(obj)));
                }
            }
        }
        return fieldValueMap;
    }

    /**
     * 获取CompareKey的值、没有则抛出异常
     */
    private static final String getCompareFieldValue(Object obj) throws Exception {
        Assert.notNull(obj, "Object can not be null.");
        Field[] declaredFields = obj.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            CompareKey annotation = field.getAnnotation(CompareKey.class);
            if (annotation != null) {
                PropertyDescriptor propertyDescriptor = BeanUtils.
                        getPropertyDescriptor(obj.getClass(), field.getName());
                Method readMethod = propertyDescriptor.getReadMethod();
                if (readMethod != null) {
                    return getStrValue(readMethod.invoke(obj));
                }
            }
        }
        throw new IllegalArgumentException("Compare Key can not be null");
    }

    /**
     * compare的fieldNote和fieldName的Map
     */
    private static final Map<String, String> getFieldNoteMap(Class clazz) {
        Map<String, String> fieldNoteMap = new HashMap<>();
        Field[] declaredFields = clazz.getDeclaredFields();
        for (Field field : declaredFields) {
            Compare annotation = field.getAnnotation(Compare.class);
            if (annotation != null) {
                fieldNoteMap.put(annotation.value(), field.getName());
            }
        }
        return fieldNoteMap;
    }

    /**
     * 获取Object的字符串值
     */
    private static final String getStrValue(Object obj) {
        if (obj == null) return "";
        if (String.class.isAssignableFrom(obj.getClass())) {
            return (String) obj;
        }
        if (LocalDate.class.isAssignableFrom(obj.getClass())) {
            return ((LocalDate) obj).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        if (LocalDateTime.class.isAssignableFrom(obj.getClass())) {
            return ((LocalDateTime) obj).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (Date.class.isAssignableFrom(obj.getClass())) {
            return new SimpleDateFormat("yyyy-MM-dd").format(((Date) obj));
        }
        if (Number.class.isAssignableFrom(obj.getClass())) {
            return String.valueOf(obj);
        }
        return "";
    }
}
