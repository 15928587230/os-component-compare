package os.component.compare;

import org.junit.Test;
import os.component.compare.compare.CompareDto;
import os.component.compare.compare.CompareUtils;
import os.component.compare.compare.CompareVO;
import os.component.compare.model.Document;
import os.component.compare.model.Passenger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class CompareTest {

    @Test
    public void testCompare() throws Exception {
        Passenger passenger1 = new Passenger();
        passenger1.setId(1000L);
        passenger1.setName("张三");
        Passenger passenger2 = new Passenger();
        passenger2.setId(1000L);
        passenger2.setName("李四");
        passenger2.setDob(new Date());
        List<CompareDto> compareDtoList = CompareUtils.compareBean(1, passenger1, passenger2);
        System.out.println(compareDtoList);

        List<Object> documents = new ArrayList<>();
        Document document1 = new Document();
        document1.setDocType("P");
        document1.setIssueCountry("CHN");
        Document document2 = new Document();
        document2.setDocType("T");
        document2.setIssueCountry("DHL");
        documents.add(document1);
        documents.add(document2);

        List<Object> documents1 = new ArrayList<>();
        Document document3 = new Document();
        document3.setDocType("P");
        document3.setIssueCountry("KKP");
        Document document4 = new Document();
        document4.setDocType("V");
        document4.setIssueCountry("LSS");
        documents1.add(document3);
        documents1.add(document4);
        List<CompareDto> compareDtoList1 = CompareUtils.compareBeanList(2, documents, documents1);
        System.out.println(compareDtoList1);

        compareDtoList.addAll(compareDtoList1);
        List<CompareVO> changedFiledMap = CompareUtils.getChangedFiledMap(Arrays.asList(Passenger.class, Document.class), compareDtoList);
        System.out.println(changedFiledMap);
    }
}
