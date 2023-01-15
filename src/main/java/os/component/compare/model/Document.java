package os.component.compare.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import os.component.compare.compare.Compare;
import os.component.compare.compare.CompareKey;

import java.time.LocalDate;

/**
 * 证件
 */
@Getter
@Setter
@ToString
public class Document {
    @Compare("ID")
    private Long id;
    @Compare("Doc Type")
    @CompareKey
    private String docType;
    @Compare("Doc No")
    private String docNo;
    private LocalDate expireDate;
    private String issueOrg;
    @Compare("Issue Country")
    private String issueCountry;
}
