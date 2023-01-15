package os.component.compare.model;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import os.component.compare.compare.Compare;
import os.component.compare.compare.CompareKey;

import java.util.Date;
import java.util.List;

/**
 * 乘客
 */
@Getter
@Setter
@ToString
public class Passenger {
    @Compare("ID")
    @CompareKey
    private Long id;
    @Compare("Name")
    private String name;
    @Compare("Nationally")
    private String nationally;
    @Compare("Dob")
    private Date dob;
    @Compare("Age")
    private Integer age;
    private List<Document> documents;
}
