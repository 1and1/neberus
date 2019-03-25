package net.oneandone.neberus.test;

import java.io.File;
import static org.assertj.core.api.Assertions.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.BeforeClass;
import org.junit.Test;

public class DtoWithPublicFieldsTest {

    private static Document document;

    @BeforeClass
    public static void readHtml() throws Exception {
        document = Jsoup.parse(new File("../neberus-test-javax.ws.rs/target/apidocs/DtoWithPublicFields.html"), "utf-8");
    }

    @Test
    public void test_containsJs() {
        assertThat(document).isNotNull();
        assertThat(document.head().children()).isNotNull();
    }

}
